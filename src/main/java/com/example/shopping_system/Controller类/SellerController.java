package com.example.shopping_system.Controller类;

import com.example.shopping_system.DTO类.*;
import com.example.shopping_system.实体类.Category;
import com.example.shopping_system.实体类.Customer;
import com.example.shopping_system.实体类.Product;
import com.example.shopping_system.实体类.PurchaseIntent;
import com.example.shopping_system.实体类.IntentStatus;
import com.example.shopping_system.实体类.Seller;
import com.example.shopping_system.Service类.CustomerService;
import com.example.shopping_system.Service类.ProductService;
import com.example.shopping_system.Service类.PurchaseIntentService;
import com.example.shopping_system.Service类.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@CrossOrigin(origins = "*")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PurchaseIntentService purchaseIntentService;


    @Autowired
    private CustomerService customerService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody SellerLoginDTO loginDTO) {
        Optional<Seller> seller = sellerService.login(loginDTO.getUsername(), loginDTO.getPassword());
        if (seller.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "登录成功");
            response.put("seller", new SellerDTO(seller.get()));
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(Map.of("error", "用户名或密码错误"));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        try {
            return ResponseEntity.ok(productService.getCategoryTree());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "加载分类数据失败: " + e.getMessage()));
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryManageRequest request) {
        try {
            if (request.getLevel() == 1) {
                productService.createPrimaryCategory(request.getName(), request.getDescription());
            } else if (request.getLevel() == 2) {
                if (request.getParentId() == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "请选择对应的一级分类"));
                }
                productService.createSecondaryCategory(request.getParentId(), request.getName(), request.getDescription());
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "仅支持一、二级分类"));
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "分类创建成功");
            response.put("categories", productService.getCategoryTree());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        try {
            productService.deleteCategory(categoryId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "分类删除成功");
            response.put("categories", productService.getCategoryTree());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/product")
    public ResponseEntity<?> publishProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Double price,
            @RequestParam Integer stock,
            @RequestParam(required = false) String primaryCategory,
            @RequestParam(required = false) String secondaryCategory,
            @RequestParam(required = false) List<MultipartFile> images) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);
            product.setSeller(sellerService.getDefaultSeller());

            if (primaryCategory != null && secondaryCategory != null) {
                Category category = resolveCategory(primaryCategory, secondaryCategory);
                product.setCategory(category);
            }

            Product savedProduct = productService.createProductWithMultipleImages(product, images);
            return ResponseEntity.ok(Map.of("message", "商品发布成功", "product", savedProduct));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "图片上传失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "发布失败: " + e.getMessage()));
        }
    }

    @GetMapping("/purchase-intents")
    public ResponseEntity<?> getSellerPurchaseIntents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // 根据状态筛选购买意向
            List<PurchaseIntent> purchaseIntents;
            if (status != null && !status.trim().isEmpty()) {
                // 将状态转换为枚举类型（转换为大写以兼容前端传递的小写值）
                IntentStatus filterStatus;
                try {
                    filterStatus = IntentStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "无效的状态值: " + status));
                }
                // 这里需要根据实际的Service实现调整，假设Service提供按状态筛选的方法
                purchaseIntents = purchaseIntentService.getAllPurchaseIntents()
                        .stream()
                        .filter(intent -> intent.getStatus() == filterStatus)
                        .collect(Collectors.toList());
            } else {
                purchaseIntents = purchaseIntentService.getAllPurchaseIntents();
            }
            
            // 简单分页处理
            int start = Math.min((int) (page * size), purchaseIntents.size());
            int end = Math.min(start + size, purchaseIntents.size());
            List<PurchaseIntent> pagedIntents;
            if (start < end) {
                pagedIntents = purchaseIntents.subList(start, end);
            } else {
                pagedIntents = Collections.emptyList();
            }
            
            // 转换为响应格式
            List<Map<String, Object>> responseIntents = pagedIntents.stream().map(intent -> {
                Map<String, Object> intentMap = new HashMap<>();
                intentMap.put("id", intent.getId());
                intentMap.put("product_name", intent.getProduct().getName());
                intentMap.put("price", intent.getProduct().getPrice());
                intentMap.put("customer_name", intent.getCustomerName());
                intentMap.put("customer_phone", intent.getCustomerPhone());
                intentMap.put("customer_address", intent.getCustomerAddress());
                intentMap.put("quantity", intent.getQuantity());
                intentMap.put("status", intent.getStatus().name().toLowerCase());
                intentMap.put("created_at", intent.getCreatedAt());
                intentMap.put("updated_at", intent.getUpdatedAt());
                return intentMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("purchaseIntents", responseIntents);
            response.put("totalPages", (int) Math.ceil((double) purchaseIntents.size() / size));
            response.put("totalElements", purchaseIntents.size());
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "获取购买意向失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/complete-transaction")
    public ResponseEntity<?> completeTransaction(@RequestBody CompleteTransactionDTO completeDTO) {
        try {
            PurchaseIntent updatedIntent = purchaseIntentService.completeTransaction(
                    completeDTO.getIntentId(), completeDTO.getSuccess());
            if (updatedIntent != null) {
                String message = Boolean.TRUE.equals(completeDTO.getSuccess()) ?
                        "交易完成，商品已标记为已售出" : "交易取消，商品已恢复在售状态";
                return ResponseEntity.ok(Map.of("message", message));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败，购买意向不存在"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "操作失败: " + e.getMessage()));
        }
    }

    // 更新购买意向状态（卖家操作）
    @PutMapping("/purchase-intents/{id}/status")
    public ResponseEntity<?> updatePurchaseIntentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdateRequest) {
        try {
            // 获取新状态
            String newStatus = statusUpdateRequest.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "新状态不能为空"));
            }
            
            // 转换为枚举类型
            IntentStatus intentStatus;
            try {
                // 通过枚举名称转换
                intentStatus = IntentStatus.valueOf(newStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的状态值: " + newStatus));
            }
            
            // 调用服务层更新状态
            PurchaseIntent updatedIntent = purchaseIntentService.updateIntentStatus(id, intentStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "状态更新成功");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // 状态转换不合法
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // 其他错误
            return ResponseEntity.badRequest().body(Map.of("error", "状态更新失败: " + e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getProductsHistory() {
        try {
            return ResponseEntity.ok(productService.getAllProducts());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "加载历史商品失败"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDTO passwordDTO) {
        try {
            boolean success = sellerService.changePassword(
                    passwordDTO.getUsername(),
                    passwordDTO.getOldPassword(),
                    passwordDTO.getNewPassword());
            if (success) {
                return ResponseEntity.ok(Map.of("message", "密码修改成功"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "旧密码错误或用户不存在"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "修改失败: " + e.getMessage()));
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            List<Map<String, Object>> responseCustomers = customers.stream().map(customer -> {
                Map<String, Object> customerMap = new HashMap<>();
                customerMap.put("id", customer.getId());
                customerMap.put("username", customer.getUsername());
                customerMap.put("phone", customer.getPhone());
                customerMap.put("address", customer.getAddress());
                customerMap.put("createdAt", customer.getCreatedAt());
                return customerMap;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(responseCustomers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "加载买家信息失败: " + e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerPhone}/orders")
    public ResponseEntity<?> getCustomerOrders(@PathVariable String customerPhone) {
        try {
            List<PurchaseIntent> purchaseIntents = purchaseIntentService.getPurchaseIntentsByCustomerPhone(customerPhone);
            
            // 转换为响应格式，与买家端数据结构一致
            List<Map<String, Object>> responseOrders = purchaseIntents.stream().map(intent -> {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", intent.getId());
                orderMap.put("status", intent.getStatus().name().toLowerCase());
                orderMap.put("createdAt", intent.getCreatedAt());
                orderMap.put("updatedAt", intent.getUpdatedAt());
                
                // 计算总金额
                Product product = intent.getProduct();
                double totalAmount = 0;
                if (product != null) {
                    totalAmount = product.getPrice() * intent.getQuantity();
                    orderMap.put("productName", product.getName());
                    orderMap.put("productPrice", product.getPrice());
                    orderMap.put("quantity", intent.getQuantity());
                    
                    // 添加完整的商品对象，与买家端一致
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("id", product.getId());
                    productMap.put("name", product.getName());
                    productMap.put("price", product.getPrice());
                    productMap.put("images", product.getImages());
                    productMap.put("description", product.getDescription());
                    productMap.put("stock", product.getStock());
                    productMap.put("status", product.getStatus().name());
                    orderMap.put("product", productMap);
                }
                
                orderMap.put("totalAmount", totalAmount);
                
                return orderMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(responseOrders);
        } catch (Exception e) {
            // 记录错误日志
            e.printStackTrace();
            // 返回明确的错误信息，确保前端能正确解析
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "获取买家订单失败: " + e.getMessage()));
        }
    }
    
    // 卖家端取消订单接口
    @PostMapping("/purchase-intents/{id}/cancel")
    public ResponseEntity<?> cancelOrderAsSeller(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            // 获取取消原因
            String reason = request.get("reason");
            
            // 调用服务层更新状态为CANCELLED
            PurchaseIntent updatedIntent = purchaseIntentService.updateIntentStatus(id, IntentStatus.CANCELLED);
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单已成功取消");
            response.put("reason", reason);
            response.put("updatedIntent", updatedIntent);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 状态转换不合法
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // 其他错误
            return ResponseEntity.badRequest().body(Map.of("error", "取消订单失败: " + e.getMessage()));
        }
    }



    private Category resolveCategory(String primaryCategoryValue, String secondaryCategoryValue) {
        Long secondaryId = parseCategoryId(secondaryCategoryValue);
        if (secondaryId != null) {
            return productService.getCategoryById(secondaryId)
                    .orElseThrow(() -> new IllegalArgumentException("所选二级分类不存在，请刷新后重试"));
        }
        if (secondaryCategoryValue == null || secondaryCategoryValue.trim().isEmpty()) {
            throw new IllegalArgumentException("请选择二级分类");
        }
        Category primary = resolvePrimaryCategory(parseCategoryId(primaryCategoryValue), primaryCategoryValue);
        return productService.getLevel2Categories(primary.getId()).stream()
                .filter(cat -> cat.getName().equals(secondaryCategoryValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("所选二级分类不存在，请先在分类管理中创建"));
    }

    private Category resolvePrimaryCategory(Long categoryId, String categoryName) {
        if (categoryId != null) {
            return productService.getCategoryById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("所选一级分类不存在，请刷新后重试"));
        }
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("请选择一级分类");
        }
        return productService.getLevel1Categories().stream()
                .filter(cat -> cat.getName().equals(categoryName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("所选一级分类不存在，请先在分类管理中创建"));
    }

    private Long parseCategoryId(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
