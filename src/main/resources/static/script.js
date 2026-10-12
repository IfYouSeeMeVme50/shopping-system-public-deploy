'use strict';

// 文件地址兼容：新上传的文件存数据库，URL 形如 /images/12.jpg；旧数据为裸文件名走 /uploads/
function fileUrl(p) {
    return p && p.startsWith('/') ? p : '/uploads/' + p;
}

// ===== 全局状态 =====
let currentUser = null;
let allProducts = [];
let filteredProducts = [];
let buyerCategories = [];
let activePrimaryCategoryId = null;
let activeSecondaryCategoryId = null;
let currentSearch = '';
let shoppingCart = [];
let selectedCartItems = new Set();
let cartSelectionDirty = false;
let favorites = [];
let orderFilter = 'all';
let orderHistoryCache = [];

// ===== 页面初始化 =====
document.addEventListener('DOMContentLoaded', () => {
    bindForms();
    restoreLogin();
    loadCategoriesForBuyer();
    loadProducts();
    loadCart();
    loadFavorites();
});

function bindForms() {
    const customerForm = document.getElementById('customer-form');
    if (customerForm) {
        customerForm.addEventListener('submit', submitPurchaseIntent);
    }

    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
}

// ===== 登录状态处理 =====
function restoreLogin() {
    const token = localStorage.getItem('userToken');
    const userInfo = localStorage.getItem('userInfo');
    if (token && userInfo) {
        try {
            currentUser = JSON.parse(userInfo);
            updateUserInterface();
        } catch (e) {
            console.warn('解析本地用户信息失败，清除缓存');
            localStorage.removeItem('userToken');
            localStorage.removeItem('userInfo');
        }
    }
}

function updateUserInterface() {
    const userInfoDiv = document.getElementById('user-info');
    const loginRegisterDiv = document.getElementById('login-register');
    const usernameDisplay = document.getElementById('username-display');
    const cartIcon = document.getElementById('cart-icon');

    if (currentUser) {
        if (usernameDisplay) usernameDisplay.textContent = currentUser.username || '';
        if (userInfoDiv) userInfoDiv.style.display = 'inline';
        if (loginRegisterDiv) loginRegisterDiv.style.display = 'none';
        if (cartIcon) cartIcon.style.display = 'inline';
    } else {
        if (userInfoDiv) userInfoDiv.style.display = 'none';
        if (loginRegisterDiv) loginRegisterDiv.style.display = 'inline';
        if (cartIcon) cartIcon.style.display = 'none';
    }
}

// ===== 商品与分类 =====
async function loadProducts() {
    try {
        const response = await fetch('/api/products');
        if (response.ok) {
            allProducts = await response.json();
        } else {
            allProducts = [];
        }
    } catch (error) {
        console.error('加载商品失败:', error);
        allProducts = [];
    }
    filterProducts();
}

async function loadCategoriesForBuyer() {
    try {
        const response = await fetch('/api/categories');
        if (response.ok) {
            buyerCategories = await response.json();
        } else {
            buyerCategories = [];
        }
    } catch (error) {
        console.error('加载分类失败:', error);
        buyerCategories = [];
    }
    renderCategoryFilters();
}

function renderCategoryFilters() {
    const filtersSection = document.getElementById('buyer-category-filters');
    const primaryContainer = document.getElementById('primary-category-filters');
    if (!filtersSection || !primaryContainer) return;

    primaryContainer.innerHTML = '';
    if (!buyerCategories.length) {
        filtersSection.style.display = 'none';
        const secondaryGroup = document.getElementById('secondary-filter-group');
        if (secondaryGroup) secondaryGroup.style.display = 'none';
        return;
    }

    filtersSection.style.display = 'block';
    const allButton = createFilterButton('全部', !activePrimaryCategoryId && !activeSecondaryCategoryId);
    allButton.addEventListener('click', () => {
        activePrimaryCategoryId = null;
        activeSecondaryCategoryId = null;
        renderCategoryFilters();
        filterProducts();
    });
    primaryContainer.appendChild(allButton);

    buyerCategories.forEach(category => {
        const button = createFilterButton(category.name, activePrimaryCategoryId === category.id);
        button.addEventListener('click', () => {
            activePrimaryCategoryId = category.id;
            activeSecondaryCategoryId = null;
            renderCategoryFilters();
            filterProducts();
        });
        primaryContainer.appendChild(button);
    });

    renderSecondaryFilters();
}

function renderSecondaryFilters() {
    const secondaryGroup = document.getElementById('secondary-filter-group');
    const secondaryContainer = document.getElementById('secondary-category-filters');
    if (!secondaryGroup || !secondaryContainer) return;

    secondaryContainer.innerHTML = '';
    const selectedPrimary = buyerCategories.find(category => category.id === activePrimaryCategoryId);
    if (!selectedPrimary || !(selectedPrimary.children && selectedPrimary.children.length)) {
        secondaryGroup.style.display = 'none';
        activeSecondaryCategoryId = null;
        return;
    }

    secondaryGroup.style.display = 'block';
    const allButton = createFilterButton('全部', !activeSecondaryCategoryId, 'secondary');
    allButton.addEventListener('click', () => {
        activeSecondaryCategoryId = null;
        renderSecondaryFilters();
        filterProducts();
    });
    secondaryContainer.appendChild(allButton);

    selectedPrimary.children.forEach(child => {
        const button = createFilterButton(child.name, activeSecondaryCategoryId === child.id, 'secondary');
        button.addEventListener('click', () => {
            activeSecondaryCategoryId = child.id;
            renderSecondaryFilters();
            filterProducts();
        });
        secondaryContainer.appendChild(button);
    });
}

function createFilterButton(label, isActive, type = 'primary') {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `filter-button${type === 'secondary' ? ' secondary' : ''}`;
    button.textContent = label;
    if (isActive) button.classList.add('active');
    return button;
}

function findSecondaryCategory(categoryId) {
    for (const primary of buyerCategories) {
        const match = (primary.children || []).find(child => child.id === categoryId);
        if (match) {
            return { parent: primary, child: match };
        }
    }
    return null;
}

function filterProducts() {
    filteredProducts = allProducts.filter(product => {
        const keyword = currentSearch ? currentSearch.toLowerCase() : '';
        const productName = (product.name || '').toLowerCase();
        const productDescription = (product.description || '').toLowerCase();

        if (keyword && !productName.includes(keyword) && !productDescription.includes(keyword)) {
            return false;
        }

        if (product.status !== 'ACTIVE') {
            return false;
        }

        if (activeSecondaryCategoryId) {
            return product.category && product.category.id === activeSecondaryCategoryId;
        }

        if (activePrimaryCategoryId) {
            if (!product.category) return false;
            if (product.category.parent && product.category.parent.id === activePrimaryCategoryId) return true;
            return product.category.id === activePrimaryCategoryId;
        }

        return true;
    });

    updateProductsTitle();
    displayProducts();
}

function updateProductsTitle() {
    const title = document.getElementById('products-title');
    if (!title) return;

    if (activeSecondaryCategoryId) {
        const match = findSecondaryCategory(activeSecondaryCategoryId);
        if (match) {
            title.textContent = `当前分类: ${match.parent.name} / ${match.child.name}`;
            return;
        }
    }

    if (activePrimaryCategoryId) {
        const primary = buyerCategories.find(category => category.id === activePrimaryCategoryId);
        if (primary) {
            title.textContent = `当前分类: ${primary.name}`;
            return;
        }
    }

    title.textContent = '全部商品';
}

// ===== 商品展示 =====
function displayProducts() {
    const productsGrid = document.getElementById('products-grid');
    if (!productsGrid) return;

    if (!filteredProducts.length) {
        productsGrid.innerHTML = `
            <div class="no-product" style="grid-column: 1 / -1;">
                <h3>暂无商品</h3>
                <p>没有找到符合条件的商品</p>
            </div>
        `;
        return;
    }

    productsGrid.innerHTML = filteredProducts.map(buildProductCard).join('');
}

function buildProductCard(product) {
    const hasImage = product.images && product.images.length > 0;
    const imageBlock = hasImage
        ? `<img src="${fileUrl(product.images[0])}" alt="${product.name}" onclick="showProductDetails(${product.id})">
           <div class="view-details-overlay" onclick="showProductDetails(${product.id})"><span>查看详情</span></div>`
        : '<div style="display: flex; align-items: center; justify-content: center; height: 100%; background: #f8f9fa; color: #6c757d;">暂无图片</div>';

    const categoryText = product.category
        ? (product.category.parent ? `${product.category.parent.name}/${product.category.name}` : product.category.name)
        : '未分类';

    const stockText = product.stock === 0 ? '已售罄' : `库存: ${product.stock}`;

    return `
        <div class="product-card">
            <div class="product-image">${imageBlock}</div>
            <h3 class="product-name">${product.name}</h3>
            <p class="product-category">${categoryText}</p>
            <p class="product-description">${product.description || '暂无描述'}</p>
            <div class="product-price">¥${parseFloat(product.price || 0).toFixed(2)}</div>
            <div class="product-stock ${product.stock <= 5 ? 'low' : ''} ${product.stock === 0 ? 'out' : ''}">
                ${stockText}
            </div>
            <div class="product-buttons">
                <button class="btn btn-info" onclick="showProductDetails(${product.id})">查看详情</button>
                <button class="btn btn-warning" onclick="addToCart(${product.id}, 1)" ${product.stock === 0 ? 'disabled' : ''}>
                    加入购物车
                </button>
                <button class="btn btn-success" onclick="addToFavorites(${product.id})">
                    ${favorites.some(fav => fav.id === product.id) ? '已收藏' : '收藏'}
                </button>
                <button class="btn btn-primary" onclick="showPurchaseForm(${product.id})" ${product.stock === 0 ? 'disabled' : ''}>
                    ${product.stock === 0 ? '已售罄' : '立即购买'}
                </button>
            </div>
        </div>
    `;
}

function showProductDetails(productId) {
    const product = allProducts.find(p => p.id === productId);
    if (!product) return;

    const categoryText = product.category
        ? (product.category.parent ? `${product.category.parent.name}/${product.category.name}` : product.category.name)
        : '未分类';

    const imageGallery = product.images && product.images.length
        ? product.images.map(img => `
                <div class="gallery-item">
                    <img src="${fileUrl(img)}" alt="${product.name}">
                </div>
            `).join('')
        : '<p>暂无图片</p>';

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
                        ${imageGallery}
                    </div>
                </div>
            </div>

            <div class="product-info">
                <p><strong>类别：</strong>${categoryText}</p>
                <p><strong>描述：</strong>${product.description || '暂无描述'}</p>
                <p><strong>价格：</strong>¥${parseFloat(product.price || 0).toFixed(2)}</p>
                <p><strong>库存：</strong>${product.stock ?? 0}</p>
                <p><strong>状态：</strong>${product.stock === 0 ? '已售罄' : '在售'}</p>
                <p><strong>发布时间：</strong>${product.createdAt ? new Date(product.createdAt).toLocaleString() : '未知'}</p>
            </div>

            <div class="product-actions">
                <button class="btn btn-primary" onclick="showPurchaseForm(${product.id})" ${product.stock === 0 ? 'disabled' : ''}>
                    ${product.stock === 0 ? '已售罄' : '立即购买'}
                </button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
}

// ===== 搜索与排序 =====
function searchProducts() {
    const input = document.getElementById('search-input');
    currentSearch = input ? input.value.trim() : '';
    filterProducts();
}

function handleSearchKeypress(event) {
    if (event.key === 'Enter') {
        searchProducts();
    }
}

function sortProducts() {
    const sortSelect = document.getElementById('sort-select');
    if (!sortSelect) return;
    const sortValue = sortSelect.value;

    switch (sortValue) {
        case 'price_asc':
            filteredProducts.sort((a, b) => a.price - b.price);
            break;
        case 'price_desc':
            filteredProducts.sort((a, b) => b.price - a.price);
            break;
        case 'name':
            filteredProducts.sort((a, b) => a.name.localeCompare(b.name));
            break;
        default:
            break;
    }

    displayProducts();
}

// ===== 模态框控制 =====
function showLoginForm() {
    const modal = document.getElementById('login-modal');
    if (modal) modal.style.display = 'flex';
}

function showRegisterForm() {
    const modal = document.getElementById('register-modal');
    if (modal) modal.style.display = 'flex';
}

function showUserMenu() {
    const modal = document.getElementById('user-menu');
    if (modal) modal.style.display = 'flex';
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.style.display = 'none';
}

// ===== 认证 =====
async function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = {
        username: formData.get('username'),
        password: formData.get('password')
    };

    try {
        const response = await fetch('/api/customer/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await response.json();
        if (response.ok && result.success) {
            currentUser = result.user;
            localStorage.setItem('userToken', result.token);
            localStorage.setItem('userInfo', JSON.stringify(result.user));
            updateUserInterface();
            closeModal('login-modal');
            showMessage('登录成功', 'success');
        } else {
            showMessage((result && result.error) || '登录失败，请检查用户名和密码', 'error');
        }
    } catch (error) {
        console.error('登录失败:', error);
        showMessage('网络错误，请稍后再试', 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = {
        username: formData.get('username'),
        password: formData.get('password'),
        phone: formData.get('phone'),
        address: formData.get('address')
    };

    try {
        const response = await fetch('/api/customer/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await response.json();
        if (response.ok && result.success) {
            closeModal('register-modal');
            showMessage('注册成功，请登录', 'success');
            showLoginForm();
        } else {
            showMessage((result && result.error) || '注册失败，请重试', 'error');
        }
    } catch (error) {
        console.error('注册失败:', error);
        showMessage('网络错误，请稍后再试', 'error');
    }
}

function logout() {
    currentUser = null;
    localStorage.removeItem('userToken');
    localStorage.removeItem('userInfo');
    updateUserInterface();
    closeModal('user-menu');
    showMessage('已退出登录', 'info');
}

// ===== 订单历史 =====
async function showOrderHistory() {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        return;
    }

    try {
        // 使用客户手机号从购买意向表获取数据
        const response = await fetch(`/api/customer/${currentUser.phone}/purchase-intents/page?page=0&size=20`);
        let purchaseIntents = [];
        if (response.ok) {
            const result = await response.json();
            if (result.success) {
                purchaseIntents = result.purchaseIntents || [];
            } else {
                showMessage(result.message || '获取订单失败', 'error');
                return;
            }
        }

        const orderList = document.getElementById('order-history-list');
        if (!orderList) return;

        if (!purchaseIntents.length) {
            orderList.innerHTML = `
                <div class="no-orders">
                    <h4>暂无订单记录</h4>
                    <p>您还没有任何订单</p>
                </div>
            `;
        } else {
            orderList.innerHTML = purchaseIntents.map(intent => {
                const totalAmount = intent.product ? intent.product.price * intent.quantity : 0;
                // 取消权限限制：仅订单处于"客户下单"、"商家确认"、"备货完成"状态时，可发起取消操作
                // 取消禁止场景：订单进入"开始发货"状态后，"取消订单"按钮不可点击
                const upperStatus = intent.status ? intent.status.toUpperCase() : '';
                const canCancel = upperStatus === 'PENDING' || upperStatus === 'STOCKED_UP'; // PENDING=客户下单，STOCKED_UP=备货完成
                // 注意：需求中的"商家确认"状态可能对应PENDING状态或需要后端确认，这里暂时按PENDING处理
                const statusText = getStatusText(intent.status);
                
                return `
                <div class="order-item" data-intent-id="${intent.id}">
                    <div class="order-header">
                        <strong>订单号 ${intent.id}</strong>
                        <span class="status-${(intent.status || '').toLowerCase()}">${statusText}</span>
                    </div>
                    <div class="order-product-info">
                        <div class="product-image">
                            ${intent.product && intent.product.images && intent.product.images.length > 0
                                ? `<img src="${fileUrl(intent.product.images[0])}" alt="${intent.product.name}">`
                                : '<div class="no-image">暂无图片</div>'}
                        </div>
                        <div class="product-details">
                            <h4>${intent.product ? intent.product.name : '商品已下架'}</h4>
                            <p>数量: ${intent.quantity}</p>
                            <p>单价: ¥${intent.product ? Number(intent.product.price || 0).toFixed(2) : '0.00'}</p>
                        </div>
                    </div>
                    <div class="order-summary">
                        <div>下单时间: ${intent.createdAt ? new Date(intent.createdAt).toLocaleString() : '-'}</div>
                        <div>更新时间: ${intent.updatedAt ? new Date(intent.updatedAt).toLocaleString() : '-'}</div>
                        <div class="total-amount">总金额: ¥${Number(totalAmount).toFixed(2)}</div>
                    </div>
                    <div class="order-actions">
                        ${canCancel ? `
                            <button class="btn btn-danger btn-sm" onclick="cancelOrder(${intent.id}, '${intent.product ? intent.product.name : '商品'}')">
                                取消订单
                            </button>
                        ` : ''}
                    </div>
                </div>
            `;
            }).join('');
        }

        closeModal('user-menu');
        const modal = document.getElementById('order-history-modal');
        if (modal) modal.style.display = 'flex';
    } catch (error) {
        console.error('获取订单历史失败:', error);
        showMessage('获取订单历史失败', 'error');
    }
}

// 获取购买意向状态文本
function getStatusText(status) {
    console.log('getStatusText called with status:', status);
    // 处理大小写状态值（确保兼容性）
    const upperStatus = status ? status.toUpperCase() : '';
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

// 取消订单函数
async function cancelOrder(orderId, productName) {
    // 二次确认弹窗：提示"是否确认取消该订单？取消后将终止交易流程"
    if (!confirm(`是否确认取消该订单？取消后将终止交易流程\n\n订单号: ${orderId}\n商品: ${productName}`)) {
        return;
    }

    try {
        const response = await fetch(`/api/purchase-intents/${orderId}/cancel`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (response.ok && result.success) {
            showMessage('订单取消成功，状态已更新为"买家主动取消"', 'success');
            // 刷新订单历史
            showOrderHistory();
        } else {
            showMessage(result.message || '取消订单失败', 'error');
        }
    } catch (error) {
        console.error('取消订单失败:', error);
        showMessage('取消订单失败，请稍后再试', 'error');
    }
}

// ===== 购买意向 =====
function showPurchaseForm(productId) {
    const product = allProducts.find(p => p.id === productId);
    if (!product) return;

    const quantityInput = document.getElementById('purchase-quantity');
    if (quantityInput) {
        quantityInput.max = product.stock;
        quantityInput.value = 1;
        quantityInput.disabled = false;
    }

    if (currentUser) {
        const name = document.getElementById('customer_name');
        const phone = document.getElementById('customer_phone');
        const address = document.getElementById('customer_address');
        if (name) name.value = currentUser.username || '';
        if (phone) phone.value = currentUser.phone || '';
        if (address) address.value = currentUser.address || '';
    } else {
        const form = document.getElementById('customer-form');
        if (form) form.reset();
    }

    const form = document.getElementById('customer-form');
    if (form) {
        form.dataset.productId = productId;
        form.dataset.cartPurchase = 'false';
        delete form.dataset.cartItems;
    }

    const modal = document.getElementById('purchase-form');
    if (modal) modal.style.display = 'flex';
}

async function submitPurchaseIntent(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const isCartPurchase = event.target.dataset.cartPurchase === 'true';

    const purchaseData = {
        customerName: formData.get('customer_name') || (currentUser ? currentUser.username : ''),
        customerPhone: formData.get('customer_phone') || (currentUser ? currentUser.phone : ''),
        customerAddress: formData.get('customer_address') || (currentUser ? currentUser.address : '')
    };

    if (!purchaseData.customerName || !purchaseData.customerPhone || !purchaseData.customerAddress) {
        showMessage('请填写完整的客户信息', 'error');
        return;
    }

    if (isCartPurchase) {
        let cartItems = [];
        try {
            cartItems = JSON.parse(event.target.dataset.cartItems || '[]');
        } catch (error) {
            console.error('解析购物车购买数据失败:', error);
        }

        if (!cartItems.length) {
            showMessage('请选择要购买的商品', 'error');
            return;
        }

        await submitCartPurchase(cartItems, purchaseData);
        closeModal('purchase-form');
        event.target.reset();
        const quantityInput = document.getElementById('purchase-quantity');
        if (quantityInput) quantityInput.disabled = false;
        return;
    }

    const productId = Number(event.target.dataset.productId);
    const product = allProducts.find(p => p.id === productId);
    if (!product) {
        showMessage('商品不存在', 'error');
        return;
    }

    const quantity = Number(formData.get('quantity') || 1);
    if (quantity > product.stock) {
        showMessage('库存不足', 'error');
        return;
    }

    try {
        const response = await fetch('/api/purchase-intents', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                productId,
                customerName: purchaseData.customerName,
                customerPhone: purchaseData.customerPhone,
                customerAddress: purchaseData.customerAddress,
                quantity
            })
        });
        const result = await response.json();
        if (response.ok) {
            showMessage(result.message || '提交成功', 'success');
            closeModal('purchase-form');
            event.target.reset();
            loadProducts();
        } else {
            showMessage(result.error || '提交失败，请重试', 'error');
        }
    } catch (error) {
        console.error('提交购买意向失败:', error);
        showMessage('提交失败，请稍后再试', 'error');
    }
}

// ===== 通用消息 =====
function showMessage(message, type) {
    const messageDiv = document.getElementById('message');
    if (!messageDiv) return;
    messageDiv.textContent = message;
    messageDiv.className = `message ${type}`;
    messageDiv.style.display = 'block';

    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}

// ===== 购物车功能 =====
async function loadCart() {
    if (!currentUser) {
        shoppingCart = [];
        return;
    }
    
    try {
        const response = await fetch(`/api/cart?customerPhone=${currentUser.phone}`);
        if (response.ok) {
            const cartItems = await response.json();
            // 将后端数据转换为前端所需格式
            shoppingCart = cartItems.map(item => ({
                productId: item.product.id,
                quantity: item.quantity,
                product: {
                    id: item.product.id,
                    name: item.product.name,
                    price: item.product.price,
                    images: item.product.images,
                    stock: item.product.stock
                }
            }));
        } else {
            console.error('获取购物车数据失败');
            shoppingCart = [];
        }
    } catch (error) {
        console.error('获取购物车数据错误:', error);
        shoppingCart = [];
    }
    updateCartBadge();
}

async function saveCart() {
    // 由于购物车数据现在直接保存到后端，此函数保持为空以维持兼容性
    return;
}

function reconcileSelectedCartItems() {
    const availableIds = new Set(shoppingCart.map(item => item.productId));
    for (const id of Array.from(selectedCartItems)) {
        if (!availableIds.has(id)) {
            selectedCartItems.delete(id);
        }
    }
    if (shoppingCart.length > 0 && selectedCartItems.size === 0 && !cartSelectionDirty) {
        shoppingCart.forEach(item => selectedCartItems.add(item.productId));
    }
}

function updateCartSummary() {
    const cartTotal = document.getElementById('cart-total-amount');
    const selectedCount = document.getElementById('cart-selected-count');
    const selectAllCheckbox = document.getElementById('cart-select-all');
    if (!cartTotal) return;

    const selectedItems = shoppingCart.filter(item => selectedCartItems.has(item.productId));
    const total = selectedItems.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
    cartTotal.textContent = total.toFixed(2);

    if (selectedCount) {
        selectedCount.textContent = selectedItems.length;
    }

    if (selectAllCheckbox) {
        const allSelected = selectedItems.length > 0 && selectedItems.length === shoppingCart.length;
        selectAllCheckbox.checked = allSelected;
        selectAllCheckbox.indeterminate = selectedItems.length > 0 && selectedItems.length < shoppingCart.length;
    }
}

function updateCartBadge() {
    const cartBadge = document.getElementById('cart-badge');
    if (cartBadge) {
        const totalItems = shoppingCart.reduce((sum, item) => sum + item.quantity, 0);
        cartBadge.textContent = totalItems;
        cartBadge.style.display = totalItems > 0 ? 'inline' : 'none';
    }
}

async function addToCart(productId, quantity = 1) {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        return;
    }
    
    try {
        const response = await fetch(`/api/cart?customerPhone=${currentUser.phone}&productId=${productId}&quantity=${quantity}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            await loadCart(); // 重新加载购物车数据
            showMessage('已添加到购物车', 'success');
            
            // 显示收藏提示弹窗
            showFavoritePrompt(productId);
        } else {
            const errorMessage = await response.text();
            showMessage(errorMessage || '添加购物车失败', 'error');
        }
    } catch (error) {
        console.error('添加购物车错误:', error);
        showMessage('网络错误，请稍后再试', 'error');
    }
}

async function removeFromCart(productId) {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        return;
    }
    
    await updateCartQuantity(productId, 0);
}

async function updateCartQuantity(productId, quantity) {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        return;
    }
    
    try {
        const response = await fetch(`/api/cart?customerPhone=${currentUser.phone}&productId=${productId}&quantity=${quantity}`, {
            method: 'PUT'
        });
        
        if (response.ok) {
            await loadCart(); // 重新加载购物车数据
            showCart();
        } else {
            const errorMessage = await response.text();
            showMessage(errorMessage || '更新购物车数量失败', 'error');
        }
    } catch (error) {
        console.error('更新购物车数量错误:', error);
        showMessage('网络错误，请稍后再试', 'error');
    }
}

async function showCart() {
    await loadCart();
    const cartList = document.getElementById('cart-items');
    const cartTotal = document.getElementById('cart-total-amount');
    
    if (!cartList || !cartTotal) return;

    if (shoppingCart.length === 0) {
        selectedCartItems.clear();
        cartSelectionDirty = false;
        cartList.innerHTML = `
            <div class="empty-cart">
                <h4>购物车为空</h4>
                <p>快去添加一些商品吧！</p>
            </div>
        `;
        cartTotal.textContent = '0.00';
        updateCartSummary();
        return;
    }

    reconcileSelectedCartItems();
    cartList.innerHTML = shoppingCart.map(item => {
        const isChecked = selectedCartItems.has(item.productId) ? 'checked' : '';
        return `
            <div class="cart-item">
                <div class="cart-item-select">
                    <input type="checkbox" class="cart-select" value="${item.productId}" ${isChecked}>
                </div>
                <div class="cart-item-image">
                    ${item.product.images && item.product.images.length > 0 
                        ? `<img src="${fileUrl(item.product.images[0])}" alt="${item.product.name}">`
                        : '<div class="no-image">暂无图片</div>'}
                </div>
                <div class="cart-item-details">
                    <h4>${item.product.name}</h4>
                    <p class="cart-item-price">¥${parseFloat(item.product.price).toFixed(2)}</p>
                </div>
                <div class="cart-item-controls">
                    <div class="quantity-controls">
                        <button class="btn btn-sm" onclick="updateCartQuantity(${item.productId}, ${item.quantity - 1})">&minus;</button>
                        <span class="quantity">${item.quantity}</span>
                        <button class="btn btn-sm" onclick="updateCartQuantity(${item.productId}, ${item.quantity + 1})">&plus;</button>
                    </div>
                    <button class="btn btn-danger btn-sm" onclick="removeFromCart(${item.productId})">删除</button>
                </div>
            </div>
        `;
    }).join('');

    const itemCheckboxes = document.querySelectorAll('.cart-select');
    itemCheckboxes.forEach(checkbox => {
        checkbox.onchange = () => {
            const productId = Number(checkbox.value);
            if (checkbox.checked) {
                selectedCartItems.add(productId);
            } else {
                selectedCartItems.delete(productId);
            }
            cartSelectionDirty = true;
            updateCartSummary();
        };
    });

    const selectAllCheckbox = document.getElementById('cart-select-all');
    if (selectAllCheckbox) {
        selectAllCheckbox.onchange = () => {
            if (selectAllCheckbox.checked) {
                shoppingCart.forEach(item => selectedCartItems.add(item.productId));
            } else {
                selectedCartItems.clear();
            }
            itemCheckboxes.forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
            cartSelectionDirty = true;
            updateCartSummary();
        };
    }

    updateCartSummary();
    
    const modal = document.getElementById('cart-modal');
    if (modal) modal.style.display = 'flex';
}

async function clearCart() {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        return;
    }
    
    if (shoppingCart.length === 0) {
        showMessage('购物车已为空', 'info');
        return;
    }

    if (confirm('确定要清空购物车吗？')) {
        for (const item of shoppingCart) {
            await updateCartQuantity(item.productId, 0);
        }
        selectedCartItems.clear();
        cartSelectionDirty = false;
        await loadCart();
        showCart();
        showMessage('购物车已清空', 'success');
    }
}

function setOrderFilter(filter) {
    orderFilter = filter;
    
    // 更新标签页状态
    const tabs = document.querySelectorAll('.tab-btn');
    tabs.forEach(tab => tab.classList.remove('active'));
    
    const activeTab = document.getElementById(`order-tab-${filter}`);
    if (activeTab) activeTab.classList.add('active');
    
    // 这里可以添加订单过滤逻辑
    showMessage(`已切换到${getOrderFilterText(filter)}订单`, 'info');
}

function getOrderFilterText(filter) {
    switch (filter) {
        case 'all': return '全部';
        case 'in_progress': return '正在进行的';
        case 'pending': return '待处理';
        case 'completed': return '已完成';
        case 'cancelled': return '已取消';
        default: return filter;
    }
}

// ===== 初始化购物车和收藏 =====
// DOMContentLoaded 事件监听器已在文件开头定义





async function checkoutFromCart() {
    if (shoppingCart.length === 0) {
        showMessage('购物车为空', 'error');
        return;
    }

    if (!currentUser) {
        showMessage('请先登录', 'error');
        return;
    }

    // 显示收藏提示弹窗
    showFavoritePrompt();
    
    // 关闭购物车弹窗
    closeModal('cart-modal');
}

// ===== 收藏功能 =====
// 从后端加载收藏数据
async function loadFavorites() {
    if (!currentUser) {
        favorites = [];
        return;
    }
    
    try {
        const response = await fetch(`/api/favorites?phone=${currentUser.phone}`);
        if (response.ok) {
            const data = await response.json();
            // 将后端返回的收藏数据转换为前端需要的格式
            favorites = data.map(item => item.product);
        } else {
            console.error('获取收藏列表失败');
            favorites = [];
        }
    } catch (error) {
        console.error('获取收藏列表错误:', error);
        favorites = [];
    }
}

// 保存收藏数据到后端（不再需要保存到localStorage）
async function saveFavorites() {
    // 由于收藏数据现在直接保存到后端，此函数保持为空以维持兼容性
    return;
}

// 添加商品到收藏
async function addToFavorites(productId) {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        return;
    }
    
    try {
        const response = await fetch(`/api/favorites?phone=${currentUser.phone}&productId=${productId}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            const result = await response.json();
            showMessage('已添加到收藏', 'success');
            // 重新加载收藏列表以更新UI
            await loadFavorites();
        } else {
            const errorMessage = await response.text();
            showMessage(errorMessage || '添加收藏失败', 'error');
        }
    } catch (error) {
        console.error('添加收藏错误:', error);
        showMessage('网络错误，请稍后再试', 'error');
    }
}

// 从收藏中移除
async function removeFromFavorites(productId) {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        return;
    }
    
    try {
        const response = await fetch(`/api/favorites?phone=${currentUser.phone}&productId=${productId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            const result = await response.text();
            showMessage('已从收藏中移除', 'success');
            // 重新加载收藏列表以更新UI
            await loadFavorites();
            // 重新显示收藏列表以反映变化
            showFavorites();
        } else {
            const errorMessage = await response.text();
            showMessage(errorMessage || '移除收藏失败', 'error');
        }
    } catch (error) {
        console.error('移除收藏错误:', error);
        showMessage('网络错误，请稍后再试', 'error');
    }
}

// 显示收藏列表
async function showFavorites() {
    await loadFavorites();
    const favoritesList = document.getElementById('favorites-list');
    const selectAllCheckbox = document.getElementById('select-all-favorites');
    
    if (!favoritesList) return;

    if (favorites.length === 0) {
        favoritesList.innerHTML = `
            <div class="empty-favorites">
                <h4>暂无收藏商品</h4>
                <p>快去收藏一些喜欢的商品吧！</p>
            </div>
        `;
        return;
    }

    favoritesList.innerHTML = favorites.map(fav => {
        const hasImage = fav.images && fav.images.length > 0;
        
        return `
            <div class="favorite-item">
                <div class="favorite-checkbox">
                    <input type="checkbox" class="favorite-select" value="${fav.id}">
                </div>
                <div class="favorite-image">
                    ${hasImage 
                        ? `<img src="${fileUrl(fav.images[0])}" alt="${fav.name}">`
                        : '<div class="no-image">暂无图片</div>'}
                </div>
                <div class="favorite-details">
                    <h4>${fav.name}</h4>
                    <p class="favorite-price">¥${parseFloat(fav.price).toFixed(2)}</p>
                </div>
                <div class="favorite-actions">
                    <button class="btn btn-primary btn-sm" onclick="addToCart(${fav.id}, 1)">
                        <i class="fas fa-shopping-cart"></i> 加入购物车
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="removeFromFavorites(${fav.id})">
                        <i class="fas fa-heart-broken"></i> 取消收藏
                    </button>
                </div>
            </div>
        `;
    }).join('');

    // 绑定全选功能
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.favorite-select');
            checkboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }

    // 显示收藏列表模态框
    const modal = document.getElementById('favorites-modal');
    if (modal) modal.style.display = 'flex';
}

function addSelectedToCart() {
    const selectedCheckboxes = document.querySelectorAll('.favorite-select:checked');
    if (selectedCheckboxes.length === 0) {
        showMessage('请选择要加入购物车的商品', 'error');
        return;
    }

    selectedCheckboxes.forEach(checkbox => {
        const productId = parseInt(checkbox.value);
        addToCart(productId, 1);
    });

    showMessage(`已将 ${selectedCheckboxes.length} 件商品加入购物车`, 'success');
}

async function removeSelectedFavorites() {
    const selectedCheckboxes = document.querySelectorAll('.favorite-select:checked');
    if (selectedCheckboxes.length === 0) {
        showMessage('请选择要取消收藏的商品', 'error');
        return;
    }

    // 转换为数组以便使用async/await
    const selectedProductIds = Array.from(selectedCheckboxes).map(checkbox => parseInt(checkbox.value));
    let removedCount = 0;

    // 逐个取消收藏，并显示进度
    for (const productId of selectedProductIds) {
        try {
            await removeFromFavorites(productId);
            removedCount++;
        } catch (error) {
            console.error(`取消收藏商品 ${productId} 失败:`, error);
        }
    }

    if (removedCount > 0) {
        showMessage(`已取消收藏 ${removedCount} 件商品`, 'success');
    } else {
        showMessage('取消收藏失败', 'error');
    }
}

// ===== 收藏提示弹窗 =====
function showFavoritePrompt(productId = null) {
    // 如果用户已经登录，显示收藏提示
    if (currentUser && productId && !favorites.some(fav => fav.id === productId)) {
        const modal = document.getElementById('favorite-prompt-modal');
        if (modal) {
            modal.style.display = 'flex';
            // 设置产品ID用于收藏
            modal.dataset.productId = productId;
        }
    }
}

function addToFavoritesFromPrompt() {
    const modal = document.getElementById('favorite-prompt-modal');
    if (modal && modal.dataset.productId) {
        const productId = parseInt(modal.dataset.productId);
        addToFavorites(productId);
        closeModal('favorite-prompt-modal');
    }
}

function skipFavoritePrompt() {
    closeModal('favorite-prompt-modal');
}

// ===== 缺失的函数 =====
function selectAllFavorites() {
    const checkboxes = document.querySelectorAll('.favorite-select');
    const allChecked = Array.from(checkboxes).every(checkbox => checkbox.checked);
    
    checkboxes.forEach(checkbox => {
        checkbox.checked = !allChecked;
    });
}

function showPurchaseFromCart() {
    if (shoppingCart.length === 0) {
        showMessage('购物车为空', 'error');
        return;
    }

    if (!currentUser) {
        showMessage('请先登录', 'error');
        return;
    }

    const selectedItems = shoppingCart.filter(item => selectedCartItems.has(item.productId));
    if (selectedItems.length === 0) {
        showMessage('请选择要购买的商品', 'error');
        return;
    }

    const form = document.getElementById('customer-form');
    if (form) {
        form.dataset.cartPurchase = 'true';
        form.dataset.cartItems = JSON.stringify(selectedItems);
        delete form.dataset.productId;
    }

    const quantityInput = document.getElementById('purchase-quantity');
    if (quantityInput) {
        quantityInput.value = 1;
        quantityInput.disabled = true;
    }

    if (currentUser) {
        const name = document.getElementById('customer_name');
        const phone = document.getElementById('customer_phone');
        const address = document.getElementById('customer_address');
        if (name) name.value = currentUser.username || '';
        if (phone) phone.value = currentUser.phone || '';
        if (address) address.value = currentUser.address || '';
    }

    const modal = document.getElementById('purchase-form');
    if (modal) modal.style.display = 'flex';
}

async function submitCartPurchase(selectedItems, customerInfo) {
    let successCount = 0;
    let failureCount = 0;

    for (const item of selectedItems) {
        const payload = {
            productId: item.productId,
            customerName: customerInfo.customerName,
            customerPhone: customerInfo.customerPhone,
            customerAddress: customerInfo.customerAddress,
            quantity: item.quantity
        };

        try {
            const response = await fetch('/api/purchase-intents', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                successCount += 1;
                await updateCartQuantity(item.productId, 0);
            } else {
                failureCount += 1;
            }
        } catch (error) {
            console.error('批量购买提交失败:', error);
            failureCount += 1;
        }
    }

    await loadCart();
    showCart();

    if (successCount > 0 && failureCount === 0) {
        showMessage(`已提交 ${successCount} 件商品的购买意向`, 'success');
        return;
    }

    if (successCount > 0 && failureCount > 0) {
        showMessage(`已提交 ${successCount} 件，失败 ${failureCount} 件，请检查库存`, 'info');
        return;
    }

    showMessage('提交失败，请稍后重试', 'error');
}
