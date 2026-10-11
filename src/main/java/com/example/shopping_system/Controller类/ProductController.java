package com.example.shopping_system.Controller类;

import com.example.shopping_system.DTO类.CategoryNodeDTO;
import com.example.shopping_system.DTO类.ProductDTO;
import com.example.shopping_system.实体类.Category;
import com.example.shopping_system.实体类.Product;
import com.example.shopping_system.Service类.ProductService;
import com.example.shopping_system.Service类.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private SellerService sellerService;

    @GetMapping("/product")
    public ResponseEntity<?> getActiveProduct() {
        Optional<Product> product = productService.getActiveProduct();
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("id", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        // 使用带Redis缓存的方法
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/page")
    public ResponseEntity<Page<Product>> getProductsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/products/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
    }

    @GetMapping("/products/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryNodeDTO>> getAllCategories() {
        return ResponseEntity.ok(productService.getCategoryTree());
    }

    @GetMapping("/categories/level1")
    public ResponseEntity<List<Category>> getLevel1Categories() {
        return ResponseEntity.ok(productService.getLevel1Categories());
    }

    @GetMapping("/categories/level2/{parentId}")
    public ResponseEntity<List<Category>> getLevel2Categories(@PathVariable Long parentId) {
        return ResponseEntity.ok(productService.getLevel2Categories(parentId));
    }

    @PostMapping("/products/multiple-images")
    public ResponseEntity<?> createProductWithMultipleImages(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);
            product.setSeller(sellerService.getDefaultSeller());

            if (categoryId != null) {
                Category category = new Category();
                category.setId(categoryId);
                product.setCategory(category);
            }

            Product savedProduct = productService.createProductWithMultipleImages(product, imageFiles);
            return ResponseEntity.ok(savedProduct);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/products/{productId}/images")
    public ResponseEntity<?> addImagesToProduct(
            @PathVariable Long productId,
            @RequestParam("imageFiles") List<MultipartFile> imageFiles) {
        try {
            Product updatedProduct = productService.addImagesToProduct(productId, imageFiles);
            if (updatedProduct != null) {
                return ResponseEntity.ok(updatedProduct);
            }
            Map<String, String> response = new HashMap<>();
            response.put("error", "商品不存在");
            return ResponseEntity.status(404).body(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category savedCategory = productService.createCategory(category);
        return ResponseEntity.ok(savedCategory);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        Optional<Product> product = productService.getProductById(productId);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        }
        Map<String, String> response = new HashMap<>();
        response.put("error", "商品不存在");
        return ResponseEntity.status(404).body(response);
    }
}
