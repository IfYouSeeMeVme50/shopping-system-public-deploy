// 卖家后台JavaScript
// 文件地址兼容：新上传的文件存数据库，URL 形如 /images/12.jpg；旧数据为裸文件名走 /uploads/
function fileUrl(p) {
    return p && p.startsWith('/') ? p : '/uploads/' + p;
}

let currentSeller = null;
let quillEditor = null;
let categoryTree = [];

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 绑定表单提交事件
    document.getElementById('login-form').addEventListener('submit', login);
    document.getElementById('product-form').addEventListener('submit', publishProduct);
    document.getElementById('change-password-form').addEventListener('submit', changePassword);
    document.getElementById('primary-category-form').addEventListener('submit', handlePrimaryCategorySubmit);
    document.getElementById('secondary-category-form').addEventListener('submit', handleSecondaryCategorySubmit);
    
    // 绑定图片选择事件，用于预览
    document.getElementById('product-images').addEventListener('change', handleImageSelection);
    document.getElementById('primary-category').addEventListener('change', handlePrimaryCategoryChange);
    document.getElementById('category-tree').addEventListener('click', handleCategoryTreeClick);
    
    // 初始化富文本编辑器
    initRichTextEditor();
    
    // 加载商品类别数据
    loadCategories();
});

// 初始化富文本编辑器
function initRichTextEditor() {
    // 加载Quill.js库
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/quill@1.3.7/dist/quill.min.js';
    script.onload = function() {
        // 初始化Quill编辑器
        quillEditor = new Quill('#editor-container', {
            theme: 'snow',
            modules: {
                toolbar: [
                    [{ 'header': [1, 2, 3, false] }],
                    ['bold', 'italic', 'underline', 'strike'],
                    [{ 'color': [] }, { 'background': [] }],
                    [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                    [{ 'align': [] }],
                    ['link', 'image'],
                    ['clean']
                ]
            },
            placeholder: '请输入商品详细描述...',
            formats: [
                'header', 'bold', 'italic', 'underline', 'strike',
                'color', 'background', 'list', 'bullet', 'align',
                'link', 'image'
            ]
        });
        
        // 监听编辑器内容变化，同步到隐藏的textarea
        quillEditor.on('text-change', function() {
            const descriptionTextarea = document.getElementById('product-description');
            descriptionTextarea.value = quillEditor.root.innerHTML;
        });
    };
    document.head.appendChild(script);
}

// 加载商品类别数据
async function loadCategories() {
    try {
        const response = await fetch('/api/seller/categories');
        if (response.ok) {
            const data = await response.json();
            categoryTree = Array.isArray(data) ? data : [];
        } else {
            categoryTree = getFallbackCategories();
        }
    } catch (error) {
        console.error('加载类别失败:', error);
        categoryTree = getFallbackCategories();
    }
    refreshCategoryUI();
}

function refreshCategoryUI() {
    populatePrimaryCategories(categoryTree);
    populateSecondaryParentOptions();
    renderCategoryTree();
}

function getCategoryOptionValue(category) {
    if (!category) {
        return '';
    }
    if (category.id !== undefined && category.id !== null && category.id !== '') {
        return String(category.id);
    }
    return category.name || '';
}

function findCategoryByValue(value) {
    if (!value) {
        return null;
    }
    return categoryTree.find(category => {
        if (category.id !== undefined && category.id !== null && category.id !== '') {
            return String(category.id) === value;
        }
        return category.name === value;
    }) || null;
}

function getFallbackCategories() {
    console.warn('使用示例分类数据');
    return [
        {
            id: null,
            name: '食品',
            description: '示例大类',
            level: 1,
            children: [
                { id: null, name: '零食', description: '示例小类', level: 2 },
                { id: null, name: '生鲜', description: '示例小类', level: 2 }
            ]
        },
        {
            id: null,
            name: '服装',
            description: '示例大类',
            level: 1,
            children: [
                { id: null, name: '男装', description: '示例小类', level: 2 },
                { id: null, name: '女装', description: '示例小类', level: 2 }
            ]
        }
    ];
}

function populatePrimaryCategories(categories) {
    const select = document.getElementById('primary-category');
    select.innerHTML = '';
    
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = '请选择一级类别';
    defaultOption.disabled = true;
    defaultOption.selected = true;
    select.appendChild(defaultOption);
    
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = getCategoryOptionValue(category);
        option.textContent = category.name;
        select.appendChild(option);
    });
    
    const firstCategory = categories.find(cat => getCategoryOptionValue(cat));
    if (firstCategory) {
        select.value = getCategoryOptionValue(firstCategory);
        populateSecondaryCategories(firstCategory.children || []);
    } else {
        populateSecondaryCategories([]);
    }
}

function handlePrimaryCategoryChange(event) {
    const selectedPrimary = event.target.value;
    const category = findCategoryByValue(selectedPrimary);
    populateSecondaryCategories(category ? (category.children || []) : []);
}

function populateSecondaryCategories(secondaryCategories) {
    const select = document.getElementById('secondary-category');
    select.innerHTML = '';
    
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = '请选择二级类别';
    defaultOption.disabled = true;
    defaultOption.selected = true;
    select.appendChild(defaultOption);
    
    secondaryCategories.forEach(category => {
        const option = document.createElement('option');
        option.value = getCategoryOptionValue(category);
        option.textContent = category.name;
        select.appendChild(option);
    });
}

// 处理图片选择，显示预览
function handleImageSelection(event) {
    const files = event.target.files;
    const previewContainer = document.getElementById('image-preview');
    const previewImages = document.getElementById('preview-images');
    
    // 清空预览容器
    previewImages.innerHTML = '';
    
    if (files.length > 0) {
        previewContainer.style.display = 'block';
        
        // 为每个文件创建预览
        Array.from(files).forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = function(e) {
                const previewItem = document.createElement('div');
                previewItem.className = 'preview-item';
                
                const img = document.createElement('img');
                img.src = e.target.result;
                img.alt = `预览图 ${index + 1}`;
                
                const caption = document.createElement('p');
                caption.textContent = index === 0 ? '主图' : `图片 ${index + 1}`;
                
                previewItem.appendChild(img);
                previewItem.appendChild(caption);
                previewImages.appendChild(previewItem);
            };
            reader.readAsDataURL(file);
        });
    } else {
        previewContainer.style.display = 'none';
    }
}

// 处理多媒体文件选择，显示预览
function handleMediaSelection(event) {
    const files = event.target.files;
    const previewContainer = document.getElementById('media-preview') || document.createElement('div');
    const previewMedia = document.getElementById('preview-media') || document.createElement('div');
    
    // 确保预览容器存在
    if (!document.getElementById('media-preview')) {
        previewContainer.id = 'media-preview';
        previewMedia.id = 'preview-media';
        previewContainer.appendChild(previewMedia);
        event.target.parentNode.appendChild(previewContainer);
    }
    
    // 清空预览容器
    previewMedia.innerHTML = '';
    
    if (files.length > 0) {
        previewContainer.style.display = 'block';
        
        // 为每个文件创建预览
        Array.from(files).forEach((file, index) => {
            const fileType = file.type.split('/')[0];
            let previewElement;
            
            // 根据文件类型创建不同的预览元素
            if (fileType === 'video') {
                previewElement = document.createElement('video');
                previewElement.controls = true;
            } else if (fileType === 'audio') {
                previewElement = document.createElement('audio');
                previewElement.controls = true;
            } else {
                // 默认为图片
                previewElement = document.createElement('img');
            }
            
            const previewItem = document.createElement('div');
            previewItem.className = 'preview-item';
            
            const reader = new FileReader();
            reader.onload = function(e) {
                previewElement.src = e.target.result;
                previewElement.alt = `媒体文件 ${index + 1}`;
                previewElement.style.maxWidth = '300px';
                previewElement.style.margin = '10px';
                
                const caption = document.createElement('p');
                caption.textContent = `${file.name} (${fileType})`;
                
                previewItem.appendChild(previewElement);
                previewItem.appendChild(caption);
                previewMedia.appendChild(previewItem);
            };
            reader.readAsDataURL(file);
        });
    } else {
        previewContainer.style.display = 'none';
    }
}

// 卖家登录
async function login(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const loginData = {
        username: formData.get('username'),
        password: formData.get('password')
    };
    
    try {
        const response = await fetch('/api/seller/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });
        
        const result = await response.json();
        
        if (response.ok) {
            currentSeller = result.seller;
            showSellerMessage('登录成功', 'success');
            document.getElementById('login-section').style.display = 'none';
            document.getElementById('dashboard').style.display = 'block';
            loadCurrentProductInfo();
            loadPurchaseIntents();
            loadProductsHistory();
        } else {
            showSellerMessage(result.error, 'error');
        }
    } catch (error) {
        console.error('登录失败:', error);
        showSellerMessage('登录失败，请重试', 'error');
    }
}

// 切换标签页
function switchTab(tabName) {
    // 隐藏所有标签内容
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(tab => tab.classList.remove('active'));
    
    // 移除所有标签的激活状态
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => tab.classList.remove('active'));
    
    // 激活选中的标签和内容
    document.getElementById(tabName).classList.add('active');
    event.target.classList.add('active');
    
    // 根据标签页加载数据
    if (tabName === 'purchase-intents') {
        loadPurchaseIntents();
    } else if (tabName === 'history') {
        loadProductsHistory();
    } else if (tabName === 'product-management') {
        loadCurrentProductInfo();
    } else if (tabName === 'customer-info') {
        loadCustomers();
    }
}

// 加载当前商品信息
// 显示商品详情
function showProductDetails(productId) {
    // 获取商品数据
    fetch(`/api/products/${productId}`)
        .then(response => {
            if (!response.ok) {
                // 如果没有专门的单个商品API，从全部商品中查找
                return fetch('/api/products')
                    .then(res => res.json())
                    .then(products => {
                        const product = products.find(p => p.id === productId);
                        if (!product) throw new Error('商品未找到');
                        return product;
                    });
            }
            return response.json();
        })
        .then(product => {
            // 创建详情弹窗
            const modal = document.createElement('div');
            modal.className = 'product-details-modal';
            modal.innerHTML = `
                <div class="product-details-content">
                    <span class="close-modal" onclick="this.parentElement.parentElement.remove()">&times;</span>
                    <h2>${product.name}</h2>
                    
                    <div class="media-gallery">
                        <div class="image-gallery">
                            <h3>图片预览</h3>
                            <div class="gallery-grid">
                                ${product.images && product.images.length > 0 ? 
                                    product.images.map(img => `
                                        <div class="gallery-item">
                                            <img src="${fileUrl(img)}" alt="${product.name}">
                                        </div>
                                    `).join('') : 
                                    '<p>暂无图片</p>'
                                }
                            </div>
                        </div>
                        
                        ${product.media && product.media.length > 0 ? `
                            <div class="media-gallery">
                                <h3>富媒体文件</h3>
                                ${product.media.map(file => {
                                    const fileExtension = file.split('.').pop().toLowerCase();
                                    const isVideo = ['mp4', 'webm', 'ogg'].includes(fileExtension);
                                    const isAudio = ['mp3', 'wav', 'ogg'].includes(fileExtension);
                                    
                                    if (isVideo) {
                                        return `
                                            <div class="media-item">
                                                <video controls style="max-width: 100%;">
                                                    <source src="${fileUrl(file)}" type="video/${fileExtension === 'webm' ? 'webm' : fileExtension === 'ogg' ? 'ogg' : 'mp4'}">
                                                    您的浏览器不支持视频播放。
                                                </video>
                                                <p>${file}</p>
                                            </div>
                                        `;
                                    } else if (isAudio) {
                                        return `
                                            <div class="media-item">
                                                <audio controls>
                                                    <source src="${fileUrl(file)}" type="audio/${fileExtension === 'ogg' ? 'ogg' : 'mpeg'}">
                                                    您的浏览器不支持音频播放。
                                                </audio>
                                                <p>${file}</p>
                                            </div>
                                        `;
                                    }
                                    return '';
                                }).join('')}
                            </div>
                        ` : ''}
                    </div>
                    
                    <div class="product-info">
                        <p><strong>描述：</strong>${product.description || '暂无描述'}</p>
                        <p><strong>价格：</strong>¥${parseFloat(product.price).toFixed(2)}</p>
                        <p><strong>库存：</strong>${product.stock || 0}</p>
                        <p><strong>类别：</strong>${product.category ? 
                            (product.category.parent ? 
                                `${product.category.parent.name}/${product.category.name}` : 
                                product.category.name) : '未分类'}</p>
                        <p><strong>状态：</strong>${{
                            'active': '在售',
                            'sold_out': '已售罄',
                            'frozen': '已冻结',
                            'sold': '已售出'
                        }[product.status] || product.status}</p>
                        <p><strong>发布时间：</strong>${new Date(product.created_at).toLocaleString()}</p>
                    </div>
                </div>
            `;
            
            document.body.appendChild(modal);
        })
        .catch(error => {
            console.error('获取商品详情失败:', error);
            showSellerMessage('获取商品详情失败', 'error');
        });
}

async function loadCurrentProductInfo() {
    try {
        const response = await fetch('/api/products');
        const products = await response.json();
        
        const currentProductInfo = document.getElementById('current-product-info');
        
        // 过滤出活跃和售罄的商品（注意状态值的大小写）
        const activeProducts = products.filter(p => p.status === 'ACTIVE' || p.status === 'SOLD_OUT');
        
        if (activeProducts.length > 0) {
            let html = `
                <h3>当前在售商品</h3>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>商品名称</th>
                            <th>价格</th>
                            <th>库存</th>
                            <th>冻结库存</th>
                            <th>状态</th>
                            <th>发布时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
            `;
            
            activeProducts.forEach(product => {
                const statusText = {
                    'active': '在售',
                    'sold_out': '已售罄',
                    'frozen': '已冻结',
                    'sold': '已售出'
                }[product.status] || product.status;
                
                const statusClass = {
                    'active': 'status-active',
                    'sold_out': 'status-sold-out',
                    'frozen': 'status-frozen',
                    'sold': 'status-sold'
                }[product.status] || '';
                
                html += `
                    <tr>
                        <td>${product.name}</td>
                        <td>¥${parseFloat(product.price).toFixed(2)}</td>
                        <td>${product.stock || 0}</td>
                        <td>${product.frozenStock || 0}</td>
                        <td><span class="${statusClass}">${statusText}</span></td>
                        <td>${new Date(product.created_at).toLocaleString()}</td>
                        <td>
                            <button class="btn btn-info btn-sm" onclick="showProductDetails(${product.id})">查看详情</button>
                            <button class="btn btn-warning btn-sm" onclick="openEditProductModal(${product.id})">编辑</button>
                            ${(product.status || '').toUpperCase() === 'ACTIVE' ?
                                `<button class="btn btn-secondary btn-sm" onclick="toggleProductStatus(${product.id}, 'FROZEN')">下架</button>` : ''}
                            <button class="btn btn-danger btn-sm" onclick="deleteProduct(${product.id})">删除</button>
                        </td>
                    </tr>
                `;
            });
            
            html += '</tbody></table>';
            currentProductInfo.innerHTML = html;
        } else {
            currentProductInfo.innerHTML = `
                <div class="no-product">
                    <h3>暂无商品在售</h3>
                    <p>请发布新商品开始销售</p>
                </div>
            `;
        }
    } catch (error) {
        console.error('加载商品信息失败:', error);
        currentProductInfo.innerHTML = '<p>加载商品信息失败</p>';
    }
}

// 发布商品
async function publishProduct(event) {
    event.preventDefault();
    
    // 确保富文本编辑器内容同步到隐藏的textarea
    if (quillEditor) {
        const descriptionTextarea = document.getElementById('product-description');
        descriptionTextarea.value = quillEditor.root.innerHTML;
    }
    
    // 获取表单数据
    const name = document.getElementById('product-name').value;
    const description = document.getElementById('product-description').value;
    const price = document.getElementById('product-price').value;
    const stock = document.getElementById('product-stock').value;
    const primarySelect = document.getElementById('primary-category');
    const secondarySelect = document.getElementById('secondary-category');
    const primaryCategory = primarySelect.value;
    const secondaryCategory = secondarySelect.value;
    
    // 表单验证
    if (!name || !description || !price || !stock || !primaryCategory || !secondaryCategory) {
        showSellerMessage('请填写所有必填字段', 'error');
        return;
    }
    
    if (isNaN(price) || isNaN(stock)) {
        showSellerMessage('价格和库存必须是数字', 'error');
        return;
    }
    
    // 创建FormData对象，直接从表单获取所有数据
    const formData = new FormData(event.target);
    
    // 手动添加分类信息到FormData，确保分类信息正确传递
    formData.set('primaryCategory', primaryCategory);
    formData.set('secondaryCategory', secondaryCategory);
    
    try {
        const response = await fetch('/api/seller/product', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showSellerMessage(result.message, 'success');
            event.target.reset();
            // 清空富文本编辑器
            if (quillEditor) {
                quillEditor.setText('');
            }
            loadCurrentProductInfo();
        } else {
            showSellerMessage(result.error, 'error');
        }
    } catch (error) {
        console.error('发布商品失败:', error);
        showSellerMessage('发布失败，请重试', 'error');
    }
}

// 加载购买意向
async function loadPurchaseIntents() {
    try {
        const response = await fetch('/api/seller/purchase-intents');
        const data = await response.json();
        // 正确提取purchaseIntents数组，处理API返回的数据结构
        const intents = data.purchaseIntents || [];
        
        const intentsList = document.getElementById('intents-list');
        
        if (intents.length > 0) {
            let html = `
                <div class="table-scroll">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>商品名称</th>
                            <th>价格</th>
                            <th>购买数量</th>
                            <th>客户姓名</th>
                            <th>电话</th>
                            <th>地址</th>
                            <th>提交时间</th>
                            <th>更新时间</th>
                            <th>状态</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
            `;
            
            intents.forEach(intent => {
                const statusText = getStatusText(intent.status);
                
                const statusClass = {
                    'pending': 'status-pending',
                    'stocked_up': 'status-stocked-up',
                    'shipped': 'status-shipped',
                    'completed': 'status-completed',
                    'cancelled': 'status-cancelled'
                }[intent.status] || '';
                
                // 根据状态显示不同的操作按钮
                let actions = '';
                
                // 状态推进按钮（注意：API返回的状态值是小写的）
                if (intent.status === 'pending') {
                    actions += `<button class="btn btn-primary btn-sm" onclick="advanceOrderStatus(${intent.id}, 'STOCKED_UP')">备货完成</button> `;
                } else if (intent.status === 'stocked_up') {
                    actions += `<button class="btn btn-info btn-sm" onclick="advanceOrderStatus(${intent.id}, 'SHIPPED')">开始发货</button> `;
                } else if (intent.status === 'shipped') {
                    actions += `<button class="btn btn-success btn-sm" onclick="advanceOrderStatus(${intent.id}, 'COMPLETED')">交易完成</button> `;
                }
                
                // 取消按钮（交易完成前都可以取消）
                if (intent.status !== 'completed' && intent.status !== 'cancelled') {
                    actions += `<button class="btn btn-danger btn-sm" onclick="cancelOrderAsSeller(${intent.id}, '${intent.product_name}')">取消订单</button>`;
                }
                
                // 如果没有操作，显示"无操作"
                if (!actions) {
                    actions = '无操作';
                }
                
                html += `
                    <tr>
                        <td>${intent.product_name}</td>
                        <td>¥${parseFloat(intent.price).toFixed(2)}</td>
                        <td>${intent.quantity || 1}</td>
                        <td>${intent.customer_name}</td>
                        <td>${intent.customer_phone}</td>
                        <td>${intent.customer_address}</td>
                        <td>${new Date(intent.created_at).toLocaleString()}</td>
                        <td>${intent.updated_at ? new Date(intent.updated_at).toLocaleString() : '-'}</td>
                        <td><span class="${statusClass}">${statusText}</span></td>
                        <td>${actions}</td>
                    </tr>
                `;
            });
            
            html += '</tbody></table></div>';
            intentsList.innerHTML = html;
        } else {
            intentsList.innerHTML = '<p>暂无购买意向</p>';
        }
    } catch (error) {
        console.error('加载购买意向失败:', error);
        document.getElementById('intents-list').innerHTML = '<p>加载失败</p>';
    }
}

// 完成交易
async function completeTransaction(intentId, success) {
    if (!confirm(success ? '确认交易成功？商品将被标记为已售出。' : '确认交易失败？商品将恢复在售状态。')) {
        return;
    }
    
    try {
        const response = await fetch('/api/seller/complete-transaction', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
        body: JSON.stringify({ intentId: intentId, success: success })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showSellerMessage(result.message, 'success');
            loadPurchaseIntents();
            loadCurrentProductInfo();
        } else {
            showSellerMessage(result.error, 'error');
        }
    } catch (error) {
        console.error('完成交易失败:', error);
        showSellerMessage('操作失败，请重试', 'error');
    }
}
//加载
// 加载历史商品
async function loadProductsHistory() {
    try {
        const response = await fetch('/api/seller/products');
        const products = await response.json();
        
        const productsHistory = document.getElementById('products-history');
        
        if (products.length > 0) {
            let html = `
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>商品名称</th>
                            <th>价格</th>
                            <th>状态</th>
                            <th>发布时间</th>
                            <th>售出时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
            `;
            
            products.forEach(product => {
                const statusText = {
                    'active': '在售',
                    'frozen': '已冻结',
                    'sold': '已售出'
                }[product.status] || product.status;
                
                const statusClass = {
                    'active': 'status-active',
                    'frozen': 'status-frozen',
                    'sold': 'status-sold'
                }[product.status] || '';
                
                html += `
                    <tr>
                        <td>${product.name}</td>
                        <td>¥${parseFloat(product.price).toFixed(2)}</td>
                        <td><span class="${statusClass}">${statusText}</span></td>
                        <td>${new Date(product.created_at).toLocaleString()}</td>
                        <td>${product.sold_at ? new Date(product.sold_at).toLocaleString() : '-'}</td>
                        <td>
                            <button class="btn btn-info btn-sm" onclick="showProductDetails(${product.id})">查看详情</button>
                            <button class="btn btn-warning btn-sm" onclick="openEditProductModal(${product.id})">编辑</button>
                            ${(product.status || '').toUpperCase() === 'ACTIVE' ?
                                `<button class="btn btn-secondary btn-sm" onclick="toggleProductStatus(${product.id}, 'FROZEN')">下架</button>` : ''}
                            ${(product.status || '').toUpperCase() === 'FROZEN' ?
                                `<button class="btn btn-success btn-sm" onclick="toggleProductStatus(${product.id}, 'ACTIVE')">上架</button>` : ''}
                            <button class="btn btn-danger btn-sm" onclick="deleteProduct(${product.id})">删除</button>
                        </td>
                    </tr>
                `;
            });
            
            html += '</tbody></table>';
            productsHistory.innerHTML = html;
        } else {
            productsHistory.innerHTML = '<p>暂无历史商品记录</p>';
        }
    } catch (error) {
        console.error('加载历史商品失败:', error);
        document.getElementById('products-history').innerHTML = '<p>加载失败</p>';
    }
}

// 修改密码
async function changePassword(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const passwordData = {
        username: currentSeller.username,
        oldPassword: formData.get('oldPassword'),
        newPassword: formData.get('newPassword')
    };
    
    const confirmPassword = formData.get('confirmPassword');
    
    if (passwordData.newPassword !== confirmPassword) {
        showSellerMessage('新密码和确认密码不一致', 'error');
        return;
    }
    
    try {
        const response = await fetch('/api/seller/change-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(passwordData)
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showSellerMessage(result.message, 'success');
            event.target.reset();
        } else {
            showSellerMessage(result.error, 'error');
        }
    } catch (error) {
        console.error('修改密码失败:', error);
        showSellerMessage('修改失败，请重试', 'error');
    }
}

// 返回主页
function goToHomePage() {
    // 跳转到主页，使用相对路径确保正确导航
    window.location.href = '/index.html';
}

// 退出登录
function logout() {
    currentSeller = null;
    document.getElementById('dashboard').style.display = 'none';
    document.getElementById('login-section').style.display = 'block';
    document.getElementById('login-form').reset();
}

// 显示卖家消息
function showSellerMessage(message, type) {
    const messageDiv = document.getElementById('seller-message');
    messageDiv.textContent = message;
    messageDiv.className = `message ${type}`;
    messageDiv.style.display = 'block';
    
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}

// 加载买家信息列表
async function loadCustomers() {
  try {
    const response = await fetch('/api/seller/customers');
    let customers = [];
    if (response.ok) {
      customers = await response.json();
    }
    
    const customersList = document.getElementById('customers-list');
    
    if (customers.length === 0) {
      customersList.innerHTML = `
        <div class="no-customers">
          <h4>暂无买家信息</h4>
          <p>目前没有买家信息记录</p>
        </div>
      `;
    } else {
      // 创建表格显示买家信息，添加查看订单按钮
      customersList.innerHTML = `
        <table class="customers-table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>手机号</th>
              <th>默认地址</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            ${customers.map(customer => `
              <tr>
                <td>${customer.username}</td>
                <td>${customer.phone}</td>
                <td>${customer.address}</td>
                <td>
                  <button class="btn-view-orders" data-phone="${customer.phone}" onclick="showCustomerOrderHistory('${customer.phone}')">
                    查看订单
                  </button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    }
  } catch (error) {
    console.error('获取买家信息失败:', error);
    showSellerMessage('获取买家信息失败', 'error');
  }
}

// 显示买家历史订单
async function showCustomerOrderHistory(customerPhone) {
  try {
    // 检查customerPhone是否有效
    if (!customerPhone || customerPhone === 'undefined' || customerPhone === 'null') {
      showSellerMessage('买家手机号无效', 'error');
      return;
    }
    
    const response = await fetch(`/api/seller/customer/${customerPhone}/orders`);
    
    let orders = [];
    if (response.ok) {
      orders = await response.json();
    } else {
      // 如果API返回错误，解析错误信息
      const errorResult = await response.json();
      showSellerMessage(errorResult.error || '暂无订单记录', 'info');
      orders = [];
    }
    
    // 创建订单历史弹窗
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.id = 'order-history-modal';
    
    if (orders.length === 0) {
      modal.innerHTML = `
        <div class="modal-content">
          <div class="modal-header">
            <h3>买家订单历史</h3>
            <span class="close-modal" onclick="closeModal('order-history-modal')">&times;</span>
          </div>
          <div class="modal-body">
            <div class="no-orders">
              <h4>暂无订单记录</h4>
              <p>该买家还没有任何订单</p>
            </div>
          </div>
        </div>
      `;
    } else {
      modal.innerHTML = `
        <div class="modal-content">
          <div class="modal-header">
            <h3>买家订单历史</h3>
            <span class="close-modal" onclick="closeModal('order-history-modal')">&times;</span>
          </div>
          <div class="modal-body">
            ${orders.map(order => `
              <div class="order-item">
                <div class="order-header">
                  <strong>订单编号: ${order.id}</strong>
                  <span class="status-${order.status}">${getStatusText(order.status)}</span>
                </div>
                <div>下单时间: ${new Date(order.createdAt).toLocaleString()}</div>
                <div>总金额: ¥${order.totalAmount.toFixed(2)}</div>
                <div class="order-products">
                  <div class="order-product">
                    <span>${order.productName} × ${order.quantity}</span>
                    <span>¥${order.productPrice.toFixed(2)}</span>
                  </div>
                </div>
              </div>
            `).join('')}
          </div>
        </div>
      `;
    }
    
    // 添加到页面并显示
    document.body.appendChild(modal);
    modal.style.display = 'flex';
  } catch (error) {
    console.error('获取订单历史失败:', error);
    showSellerMessage('获取订单历史失败', 'error');
  }
}

// 获取订单状态文本
function getOrderStatusText(status) {
  switch (status) {
    case 'COMPLETED':
      return '已完成';
    case 'PENDING':
      return '待处理';
    case 'CANCELLED':
      return '已取消';
    default:
      return status;
  }
}

// 获取购买意向状态文本
function getStatusText(status) {
  // 处理小写状态值（API返回的是小写）
  const upperStatus = status.toUpperCase();
  switch (upperStatus) {
    case 'PENDING':
      return '客户下单';
    case 'STOCKED_UP':
      return '备货完成';
    case 'SHIPPED':
      return '开始发货';
    case 'COMPLETED':
      return '交易完成';
    case 'CANCELLED':
      return '已取消';
    default:
      return status;
  }
}

// 关闭弹窗
function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.style.display = 'none';
    document.body.removeChild(modal);
  }
}


function populateSecondaryParentOptions() {
    const select = document.getElementById('secondary-parent');
    if (!select) return;
    select.innerHTML = '';
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = categoryTree.length ? '请选择所属一级分类' : '请先创建一级分类';
    defaultOption.disabled = true;
    defaultOption.selected = true;
    select.appendChild(defaultOption);
    categoryTree.forEach(category => {
        if (!category.id) return;
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        select.appendChild(option);
    });
    select.disabled = categoryTree.length === 0;
}

function renderCategoryTree() {
    const container = document.getElementById('category-tree');
    if (!container) return;
    container.innerHTML = '';
    if (!categoryTree.length) {
        const emptyState = document.createElement('p');
        emptyState.className = 'empty-state';
        emptyState.textContent = '当前暂无分类，请先创建一级分类。';
        container.appendChild(emptyState);
        return;
    }
    categoryTree.forEach(category => {
        const node = document.createElement('div');
        node.className = 'category-node';
        const header = document.createElement('div');
        header.className = 'category-node-header';
        header.innerHTML = `
            <div>
                <p class="category-title">${category.name}</p>
                ${category.description ? '<p class="category-desc">' + category.description + '</p>' : ''}
            </div>
            <button type="button" class="btn btn-danger btn-sm delete-category-btn" data-id="${category.id || ''}" data-name="${category.name}">删除</button>
        `;
        node.appendChild(header);
        const deleteBtn = header.querySelector('.delete-category-btn');
        if (!category.id) {
            deleteBtn.disabled = true;
            deleteBtn.title = '示例分类不可删除';
        }
        const children = category.children || [];
        if (children.length) {
            const childList = document.createElement('div');
            childList.className = 'category-children';
            children.forEach(child => {
                const childRow = document.createElement('div');
                childRow.className = 'category-child';
                childRow.innerHTML = `
                    <span>${child.name}${child.description ? ' - ' + child.description : ''}</span>
                    <button type="button" class="btn btn-secondary btn-sm delete-category-btn" data-id="${child.id || ''}" data-name="${child.name}">删除</button>
                `;
                const childDeleteBtn = childRow.querySelector('.delete-category-btn');
                if (!child.id) {
                    childDeleteBtn.disabled = true;
                    childDeleteBtn.title = '示例分类不可删除';
                }
                childList.appendChild(childRow);
            });
            node.appendChild(childList);
        } else {
            const emptyChild = document.createElement('p');
            emptyChild.className = 'empty-subcategory';
            emptyChild.textContent = '暂无二级分类';
            node.appendChild(emptyChild);
        }
        container.appendChild(node);
    });
}

async function handlePrimaryCategorySubmit(event) {
    event.preventDefault();
    const nameInput = document.getElementById('new-primary-name');
    const descInput = document.getElementById('new-primary-description');
    const name = nameInput.value.trim();
    const description = descInput.value.trim();
    if (!name) {
        showSellerMessage('请输入一级分类名称', 'error');
        return;
    }
    await saveCategory({
        name,
        description,
        level: 1
    }, '一级分类创建成功');
    event.target.reset();
}

async function handleSecondaryCategorySubmit(event) {
    event.preventDefault();
    const parentSelect = document.getElementById('secondary-parent');
    const nameInput = document.getElementById('new-secondary-name');
    const descInput = document.getElementById('new-secondary-description');
    const parentId = parentSelect.value;
    const name = nameInput.value.trim();
    const description = descInput.value.trim();
    if (!parentId) {
        showSellerMessage('请选择所属的一级分类', 'error');
        return;
    }
    if (!name) {
        showSellerMessage('请输入二级分类名称', 'error');
        return;
    }
    await saveCategory({
        name,
        description,
        level: 2,
        parentId: Number(parentId)
    }, '二级分类创建成功');
    event.target.reset();
    parentSelect.selectedIndex = 0;
}

async function saveCategory(payload, successMessage) {
    try {
        const response = await fetch('/api/seller/categories', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });
        const result = await response.json();
        if (response.ok) {
            categoryTree = result.categories || categoryTree;
            refreshCategoryUI();
            showSellerMessage(result.message || successMessage, 'success');
        } else {
            showSellerMessage(result.error || '保存分类失败', 'error');
        }
    } catch (error) {
        console.error('保存分类失败:', error);
        showSellerMessage('保存分类失败，请稍后重试', 'error');
    }
}

function handleCategoryTreeClick(event) {
    const target = event.target.closest('.delete-category-btn');
    if (!target || target.disabled) {
        return;
    }
    const categoryId = target.dataset.id;
    const categoryName = target.dataset.name || '该分类';
    if (!categoryId) {
        showSellerMessage('示例分类无法删除', 'info');
        return;
    }
    if (!confirm(`确定删除分类「${categoryName}」吗？删除后无法恢复。`)) {
        return;
    }
    deleteCategoryById(categoryId);
}

async function deleteCategoryById(categoryId) {
    try {
        const response = await fetch(`/api/seller/categories/${categoryId}`, {
            method: 'DELETE'
        });
        const result = await response.json();
        if (response.ok) {
            categoryTree = result.categories || categoryTree;
            refreshCategoryUI();
            showSellerMessage(result.message || '分类已删除', 'success');
        } else {
            showSellerMessage(result.error || '删除分类失败', 'error');
        }
    } catch (error) {
        console.error('删除分类失败:', error);
        showSellerMessage('删除分类失败，请稍后重试', 'error');
    }
}

// 卖家端订单状态推进
async function advanceOrderStatus(intentId, newStatus) {
    const statusTextMap = {
        'STOCKED_UP': '备货完成',
        'SHIPPED': '开始发货',
        'COMPLETED': '交易完成'
    };
    
    const statusText = statusTextMap[newStatus] || newStatus;
    
    if (!confirm(`确定要将订单状态更新为"${statusText}"吗？`)) {
        return;
    }
    
    try {
        const response = await fetch(`/api/seller/purchase-intents/${intentId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ status: newStatus })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showSellerMessage(result.message || `订单状态已更新为"${statusText}"`, 'success');
            loadPurchaseIntents(); // 重新加载购买意向列表
        } else {
            showSellerMessage(result.error || '状态更新失败', 'error');
        }
    } catch (error) {
        console.error('更新订单状态失败:', error);
        showSellerMessage('更新订单状态失败，请重试', 'error');
    }
}

// 卖家端订单取消
async function cancelOrderAsSeller(intentId, productName) {
    // 获取取消原因
    const reason = prompt(`请输入取消订单 ${intentId} (${productName}) 的原因：\n\n注意：交易完成前都可以取消订单。`, '');
    
    if (reason === null) {
        return; // 用户点击了取消
    }
    
    if (!reason.trim()) {
        showSellerMessage('请输入取消原因', 'error');
        return;
    }
    
    if (!confirm(`确定要取消订单 ${intentId} (${productName}) 吗？\n\n取消原因：${reason}`)) {
        return;
    }
    
    try {
        const response = await fetch(`/api/seller/purchase-intents/${intentId}/cancel`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ reason: reason.trim() })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showSellerMessage(result.message || '订单已取消', 'success');
            loadPurchaseIntents(); // 重新加载购买意向列表
        } else {
            showSellerMessage(result.error || '取消订单失败', 'error');
        }
    } catch (error) {
        console.error('取消订单失败:', error);
        showSellerMessage('取消订单失败，请重试', 'error');
    }
}

// ==================== 商品管理（编辑/图片/上下架/删除） ====================

// 打开编辑商品弹窗
async function openEditProductModal(productId) {
    try {
        const response = await fetch(`/api/products/${productId}`);
        if (!response.ok) {
            showSellerMessage('获取商品信息失败', 'error');
            return;
        }
        const product = await response.json();
        renderEditProductModal(product);
    } catch (error) {
        console.error('获取商品信息失败:', error);
        showSellerMessage('获取商品信息失败', 'error');
    }
}

function renderEditProductModal(product) {
    const old = document.getElementById('edit-product-modal');
    if (old) old.remove();

    const category = product.category || null;
    const currentSecondaryId = category ? String(category.id) : '';
    const currentPrimaryId = category && category.parent ? String(category.parent.id)
        : (category && !category.parent ? String(category.id) : '');

    const primaryOptions = (categoryTree || []).map(c =>
        `<option value="${c.id}" ${String(c.id) === currentPrimaryId ? 'selected' : ''}>${c.name}</option>`
    ).join('');

    const imagesHtml = (product.images && product.images.length > 0)
        ? product.images.map(img => `
            <div class="gallery-item edit-img-item">
                <img src="${fileUrl(img)}" alt="">
                <button type="button" class="btn btn-danger btn-sm img-delete-btn"
                        onclick="deleteProductImage(${product.id}, '${encodeURIComponent(img)}')">删除</button>
            </div>
        `).join('')
        : '<p>暂无图片</p>';

    const modal = document.createElement('div');
    modal.className = 'product-details-modal';
    modal.id = 'edit-product-modal';
    modal.innerHTML = `
        <div class="product-details-content">
            <span class="close-modal" onclick="document.getElementById('edit-product-modal').remove()">&times;</span>
            <h2>编辑商品 #${product.id}</h2>
            <form id="edit-product-form" onsubmit="return saveProductEdit(event, ${product.id})">
                <div class="edit-form-row">
                    <label>名称</label>
                    <input type="text" id="edit-name" value="${(product.name || '').replace(/"/g, '&quot;')}" maxlength="50" required>
                </div>
                <div class="edit-form-row">
                    <label>价格（¥）</label>
                    <input type="number" id="edit-price" value="${product.price}" min="0.01" step="0.01" required>
                </div>
                <div class="edit-form-row">
                    <label>库存</label>
                    <input type="number" id="edit-stock" value="${product.stock}" min="0" step="1" required>
                </div>
                <div class="edit-form-row">
                    <label>描述</label>
                    <textarea id="edit-description" rows="4">${product.description || ''}</textarea>
                </div>
                <div class="edit-form-row">
                    <label>一级分类</label>
                    <select id="edit-primary-category" onchange="refreshEditSecondaryOptions('${currentSecondaryId}')">
                        <option value="">不修改</option>
                        ${primaryOptions}
                    </select>
                </div>
                <div class="edit-form-row">
                    <label>二级分类</label>
                    <select id="edit-secondary-category">
                        <option value="">不修改</option>
                    </select>
                </div>
                <div class="edit-form-row">
                    <label>现有图片（点删除立即生效）</label>
                    <div class="gallery-grid">${imagesHtml}</div>
                </div>
                <div class="edit-form-row">
                    <label>追加新图片</label>
                    <input type="file" id="edit-new-images" multiple accept="image/*,video/*,audio/*">
                </div>
                <div class="edit-form-actions">
                    <button type="submit" class="btn btn-success">保存修改</button>
                    <button type="button" class="btn btn-secondary"
                            onclick="document.getElementById('edit-product-modal').remove()">取消</button>
                </div>
            </form>
        </div>
    `;
    document.body.appendChild(modal);

    // 若当前商品已有分类，预填二级分类选项
    if (currentPrimaryId) {
        refreshEditSecondaryOptions(currentSecondaryId);
    }
}

// 根据一级分类刷新编辑弹窗的二级分类选项
function refreshEditSecondaryOptions(selectedId) {
    const primaryId = document.getElementById('edit-primary-category').value;
    const secondarySelect = document.getElementById('edit-secondary-category');
    secondarySelect.innerHTML = '<option value="">不修改</option>';
    if (!primaryId) return;
    const primary = (categoryTree || []).find(c => String(c.id) === String(primaryId));
    if (!primary || !primary.children) return;
    primary.children.forEach(child => {
        const opt = document.createElement('option');
        opt.value = child.id;
        opt.textContent = child.name;
        if (selectedId && String(child.id) === String(selectedId)) {
            opt.selected = true;
        }
        secondarySelect.appendChild(opt);
    });
}

// 保存商品编辑
async function saveProductEdit(event, productId) {
    event.preventDefault();

    const formData = new FormData();
    formData.append('name', document.getElementById('edit-name').value);
    formData.append('description', document.getElementById('edit-description').value);
    formData.append('price', document.getElementById('edit-price').value);
    formData.append('stock', document.getElementById('edit-stock').value);

    const primaryCategory = document.getElementById('edit-primary-category').value;
    const secondaryCategory = document.getElementById('edit-secondary-category').value;
    if (primaryCategory && secondaryCategory) {
        formData.append('primaryCategory', primaryCategory);
        formData.append('secondaryCategory', secondaryCategory);
    }

    const files = document.getElementById('edit-new-images').files;
    for (const file of files) {
        formData.append('newImages', file);
    }

    try {
        const response = await fetch(`/api/seller/product/${productId}`, {
            method: 'PUT',
            body: formData
        });
        const result = await response.json();
        if (response.ok) {
            showSellerMessage(result.message || '商品已更新', 'success');
            const modal = document.getElementById('edit-product-modal');
            if (modal) modal.remove();
            loadProductsHistory();
            loadCurrentProductInfo();
        } else {
            showSellerMessage(result.error || '更新失败', 'error');
        }
    } catch (error) {
        console.error('更新商品失败:', error);
        showSellerMessage('更新失败，请重试', 'error');
    }
    return false;
}

// 删除商品的单张图片
async function deleteProductImage(productId, encodedUrl) {
    if (!confirm('确定删除这张图片吗？')) return;
    try {
        const response = await fetch(`/api/seller/product/${productId}/image?url=${encodedUrl}`, {
            method: 'DELETE'
        });
        const result = await response.json();
        if (response.ok) {
            showSellerMessage(result.message || '图片已删除', 'success');
            // 用最新数据重渲染弹窗
            renderEditProductModal(result.product);
            loadProductsHistory();
        } else {
            showSellerMessage(result.error || '删除图片失败', 'error');
        }
    } catch (error) {
        console.error('删除图片失败:', error);
        showSellerMessage('删除图片失败，请重试', 'error');
    }
}

// 上架/下架商品
async function toggleProductStatus(productId, newStatus) {
    const actionText = newStatus === 'ACTIVE' ? '上架' : '下架';
    if (!confirm(`确定${actionText}该商品吗？`)) return;
    try {
        const response = await fetch(`/api/seller/product/${productId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });
        const result = await response.json();
        if (response.ok) {
            showSellerMessage(result.message || `商品已${actionText}`, 'success');
            loadProductsHistory();
            loadCurrentProductInfo();
        } else {
            showSellerMessage(result.error || `${actionText}失败`, 'error');
        }
    } catch (error) {
        console.error(`${actionText}失败:`, error);
        showSellerMessage(`${actionText}失败，请重试`, 'error');
    }
}

// 删除商品（有订单记录的商品会被后端拒绝，引导下架）
async function deleteProduct(productId) {
    if (!confirm('确定删除该商品吗？删除后不可恢复。\n（有订单记录的商品无法删除，建议使用下架）')) return;
    try {
        const response = await fetch(`/api/seller/product/${productId}`, {
            method: 'DELETE'
        });
        const result = await response.json();
        if (response.ok) {
            showSellerMessage(result.message || '商品已删除', 'success');
            loadProductsHistory();
            loadCurrentProductInfo();
        } else {
            showSellerMessage(result.error || '删除失败', 'error');
        }
    } catch (error) {
        console.error('删除商品失败:', error);
        showSellerMessage('删除失败，请重试', 'error');
    }
}
