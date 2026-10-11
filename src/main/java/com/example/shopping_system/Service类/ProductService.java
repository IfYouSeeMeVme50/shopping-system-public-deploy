package com.example.shopping_system.Service类;

import com.example.shopping_system.DTO类.CategoryNodeDTO;
import com.example.shopping_system.DTO类.CategoryDTO;
import com.example.shopping_system.DTO类.ProductDTO;
import com.example.shopping_system.DTO类.SellerDTO;
import com.example.shopping_system.实体类.Category;
import com.example.shopping_system.实体类.Product;
import com.example.shopping_system.实体类.ProductStatus;
import com.example.shopping_system.实体类.Seller;
import com.example.shopping_system.Repository接口.CategoryRepository;
import com.example.shopping_system.Repository接口.ProductRepository;
import com.example.shopping_system.Repository接口.SellerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    @Value("${file.upload-dir:uploads/}")
    private String uploadDir;

    public Optional<Product> getActiveProduct() {
        return productRepository.findFirstByStatus(ProductStatus.ACTIVE);
    }

    public Product createProduct(Product product, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "" + (int)(Math.random() * 1000);
            Path filePath = resolveUploadPath(fileName);
            Files.write(filePath, imageFile.getBytes());
            product.setImage(fileName);
        }

        product.setStatus(ProductStatus.ACTIVE);
        Product saved = productRepository.save(product);
        // 清除缓存，保证数据一致性
        redisCacheService.clearAllProductCache();
        log.info("商品创建成功，已清除Redis缓存: id={}", saved.getId());
        return saved;
    }

    public Product createProductWithMultipleImages(Product product, List<MultipartFile> imageFiles) throws IOException {
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "" + (int)(Math.random() * 1000);
                    Path filePath = resolveUploadPath(fileName);
                    Files.write(filePath, imageFile.getBytes());
                    product.getImages().add(fileName);
                }
            }
        }

        product.setStatus(ProductStatus.ACTIVE);
        Product saved = productRepository.save(product);
        redisCacheService.clearAllProductCache();
        log.info("商品创建成功（多图），已清除Redis缓存: id={}", saved.getId());
        return saved;
    }

    public Product addImagesToProduct(Long productId, List<MultipartFile> imageFiles) throws IOException {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (MultipartFile imageFile : imageFiles) {
                    if (imageFile != null && !imageFile.isEmpty()) {
                        String fileName = System.currentTimeMillis() + "" + (int)(Math.random() * 1000);
                        Path filePath = resolveUploadPath(fileName);
                        Files.write(filePath, imageFile.getBytes());
                        product.getImages().add(fileName);
                    }
                }
            }

            Product saved = productRepository.save(product);
            redisCacheService.clearProductCache(productId);
            log.info("商品图片已更新，已清除Redis缓存: id={}", productId);
            return saved;
        }
        return null;
    }

    /**
     * 获取所有商品（带Redis缓存）
     */
    public List<Product> getAllProducts() {
        // 1. 尝试从Redis缓存获取
        List<Product> cachedProducts = redisCacheService.getCachedProductList();
        if (cachedProducts != null && !cachedProducts.isEmpty()) {
            log.info("从Redis缓存获取商品列表，共 {} 条", cachedProducts.size());
            return cachedProducts;
        }

        // 2. 缓存未命中，从数据库查询
        log.info("Redis缓存未命中，从数据库查询商品列表");
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();

        // 3. 初始化懒加载属性，确保缓存完整数据
        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                if (product.getCategory() != null) {
                    org.hibernate.Hibernate.initialize(product.getCategory());
                    if (product.getCategory().getParent() != null) {
                        org.hibernate.Hibernate.initialize(product.getCategory().getParent());
                    }
                }
                if (product.getSeller() != null) {
                    org.hibernate.Hibernate.initialize(product.getSeller());
                }
            }
        }

        // 4. 回填缓存（预热热点数据）
        if (products != null && !products.isEmpty()) {
            redisCacheService.cacheProductList(products);
            log.info("商品列表已写入Redis缓存，共 {} 条", products.size());
        }

        return products;
    }

    public List<ProductDTO> getAllProductDTOs() {
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();
        List<ProductDTO> productDTOs = new ArrayList<>();
        
        for (Product product : products) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setDescription(product.getDescription());
            productDTO.setPrice(product.getPrice());
            productDTO.setStock(product.getStock());
            productDTO.setFrozenStock(product.getFrozenStock());
            productDTO.setStatus(product.getStatus());
            productDTO.setCreatedAt(product.getCreatedAt());
            productDTO.setSoldAt(product.getSoldAt());
            productDTO.setImage(product.getImage());
            productDTO.setImages(product.getImages());
            
            try {
                if (product.getCategory() != null) {
                    org.hibernate.Hibernate.initialize(product.getCategory());
                    CategoryDTO categoryDTO = new CategoryDTO(product.getCategory());
                    productDTO.setCategory(categoryDTO);
                }
            } catch (Exception e) {
                productDTO.setCategory(null);
            }
            
            try {
                if (product.getSeller() != null) {
                    org.hibernate.Hibernate.initialize(product.getSeller());
                    SellerDTO sellerDTO = new SellerDTO(product.getSeller());
                    productDTO.setSeller(sellerDTO);
                }
            } catch (Exception e) {
                productDTO.setSeller(null);
            }
            
            productDTOs.add(productDTO);
        }
        
        return productDTOs;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingOrDescriptionContaining(keyword, keyword, pageable);
    }

    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    /**
     * 根据ID获取商品（带Redis缓存，缓存不可用时自动降级直查数据库）
     */
    public Optional<Product> getProductById(Long id) {
        // 1. 尝试从Redis缓存获取
        Product cachedProduct = redisCacheService.getCachedProduct(id);
        if (cachedProduct != null) {
            // 缓存的Product可能丢失seller关联，必须从数据库校准后才能用于购买链路
            if (reattachSellerFromDb(cachedProduct)) {
                log.info("从Redis缓存获取商品: id={}", id);
                return Optional.of(cachedProduct);
            }
            log.info("缓存数据不完整（缺少卖家），回退数据库查询: id={}", id);
        }

        // 2. 缓存未命中，从数据库查询
        log.info("Redis缓存未命中，从数据库查询商品: id={}", id);
        Optional<Product> productOpt = productRepository.findById(id);

        // 3. 初始化懒加载关联后再回填缓存，避免缓存数据丢失seller/category
        productOpt.ifPresent(product -> {
            initLazyFields(product);
            redisCacheService.cacheProduct(product);
        });

        return productOpt;
    }

    /**
     * 缓存命中的商品需要从数据库重新加载seller，保证购买链路拿到完整卖家信息
     */
    private boolean reattachSellerFromDb(Product product) {
        try {
            Seller cached = product.getSeller();
            if (cached == null || cached.getId() == null) {
                // 缓存里丢失了seller，无法采信，必须回退数据库
                return false;
            }
            Seller fresh = sellerRepository.findById(cached.getId()).orElse(null);
            if (fresh == null) {
                return false;
            }
            product.setSeller(fresh);
            return true;
        } catch (Exception e) {
            log.warn("重新加载卖家信息失败，回退数据库查询: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 初始化懒加载关联（category/seller），在写入缓存或返回前调用
     */
    private void initLazyFields(Product product) {
        try {
            if (product.getCategory() != null) {
                org.hibernate.Hibernate.initialize(product.getCategory());
                if (product.getCategory().getParent() != null) {
                    org.hibernate.Hibernate.initialize(product.getCategory().getParent());
                }
            }
            if (product.getSeller() != null) {
                org.hibernate.Hibernate.initialize(product.getSeller());
            }
        } catch (Exception e) {
            log.warn("初始化商品关联数据失败: {}", e.getMessage());
        }
    }

    public Product updateProductStatus(Long productId, ProductStatus status) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStatus(status);
            if (status == ProductStatus.SOLD) {
                product.setSoldAt(java.time.LocalDateTime.now());
            }
            Product updated = productRepository.save(product);
            // 清除缓存
            redisCacheService.clearProductCache(productId);
            log.info("商品状态更新，已清除Redis缓存: id={}, status={}", productId, status);
            return updated;
        }
        return null;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getLevel1Categories() {
        return categoryRepository.findByLevel(1);
    }

    public List<Category> getLevel2Categories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category createPrimaryCategory(String name, String description) {
        categoryRepository.findByNameAndLevelAndParentIsNull(name, 1)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("同名的一级分类已存在");
                });
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setLevel(1);
        return categoryRepository.save(category);
    }

    public Category createSecondaryCategory(Long parentId, String name, String description) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("父级分类不存在"));
        if (parent.getLevel() != 1) {
            throw new IllegalArgumentException("只能在一级分类下创建二级分类");
        }
        categoryRepository.findByNameAndParentId(name, parentId)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("该一级分类下已存在同名二级分类");
                });
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setLevel(2);
        category.setParent(parent);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        if (productRepository.existsByCategoryId(categoryId)) {
            throw new IllegalStateException("分类下仍有关联商品，无法删除");
        }

        if (category.getLevel() == 1) {
            List<Category> children = categoryRepository.findByParentId(category.getId());
            for (Category child : children) {
                if (productRepository.existsByCategoryId(child.getId())) {
                    throw new IllegalStateException("该一级分类下的部分二级分类仍有关联商品，无法删除");
                }
            }
            if (!children.isEmpty()) {
                categoryRepository.deleteAll(children);
            }
        }

        categoryRepository.delete(category);
    }

    public List<CategoryNodeDTO> getCategoryTree() {
        List<Category> level1Categories = getLevel1Categories();
        List<CategoryNodeDTO> tree = new ArrayList<>();
        for (Category level1 : level1Categories) {
            List<CategoryNodeDTO> children = getLevel2Categories(level1.getId())
                    .stream()
                    .map(CategoryNodeDTO::new)
                    .collect(Collectors.toList());
            CategoryNodeDTO node = new CategoryNodeDTO(level1);
            node.setChildren(children);
            tree.add(node);
        }
        return tree;
    }

    public void updateStock(Long productId, Integer newStock) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(newStock);

            if (newStock == 0 && product.getStatus() == ProductStatus.ACTIVE) {
                product.setStatus(ProductStatus.SOLD);
            }

            productRepository.save(product);
            redisCacheService.clearProductCache(productId);
            log.info("库存已更新，已清除Redis缓存: id={}", productId);
        }
    }

    public void checkAndAutoDisableZeroStockProducts() {
        List<Product> activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE);
        for (Product product : activeProducts) {
            if (product.getStock() == 0) {
                product.setStatus(ProductStatus.SOLD);
                productRepository.save(product);
                redisCacheService.clearProductCache(product.getId());
            }
        }
    }

    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.ACTIVE, pageable);
    }

    public Page<Product> getSoldProducts(Pageable pageable) {
        return productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.SOLD, pageable);
    }

    public Page<Product> getFrozenProducts(Pageable pageable) {
        return productRepository.findByStatusOrderByCreatedAtDesc(ProductStatus.FROZEN, pageable);
    }

    public Page<Product> getProductsByCategoryLevel(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
    }

    public Page<Product> getAllProductsWithStatus(ProductStatus status, Pageable pageable) {
        if (status != null) {
            return productRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        return productRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private Path resolveUploadPath(String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        return uploadPath.resolve(fileName);
    }
}

