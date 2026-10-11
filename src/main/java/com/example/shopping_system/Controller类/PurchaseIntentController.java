package com.example.shopping_system.Controller类;

import com.example.shopping_system.DTO类.PurchaseIntentDTO;
import com.example.shopping_system.实体类.PurchaseIntent;
import com.example.shopping_system.实体类.Product;
import com.example.shopping_system.实体类.IntentStatus;
import com.example.shopping_system.Service类.PurchaseIntentService;
import com.example.shopping_system.Service类.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PurchaseIntentController {
    
    @Autowired
    private PurchaseIntentService purchaseIntentService;
    
    @Autowired
    private ProductService productService;
    
    // 状态更新请求DTO类
    public static class StatusUpdateRequest {
        private String newStatus;
        
        public String getNewStatus() {
            return newStatus;
        }
        
        public void setNewStatus(String newStatus) {
            this.newStatus = newStatus;
        }
    }
    
    @PostMapping("/purchase-intents")
    public ResponseEntity<?> createPurchaseIntent(@Valid @RequestBody PurchaseIntentDTO purchaseIntentDTO) {
        try {
            Optional<Product> product = productService.getProductById(purchaseIntentDTO.getProductId());
            if (product.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "商品不存在"));
            }
            
            Product targetProduct = product.get();
            if (targetProduct.getStatus() != com.example.shopping_system.实体类.ProductStatus.ACTIVE) {
                return ResponseEntity.badRequest().body(Map.of("error", "商品不可购买"));
            }
            
            if (purchaseIntentDTO.getQuantity() > targetProduct.getStock()) {
                return ResponseEntity.badRequest().body(Map.of("error", "库存不足"));
            }
            
            PurchaseIntent purchaseIntent = new PurchaseIntent();
            purchaseIntent.setProduct(targetProduct);
            purchaseIntent.setCustomerName(purchaseIntentDTO.getCustomerName());
            purchaseIntent.setCustomerPhone(purchaseIntentDTO.getCustomerPhone());
            purchaseIntent.setCustomerAddress(purchaseIntentDTO.getCustomerAddress());
            purchaseIntent.setQuantity(purchaseIntentDTO.getQuantity());
            
            PurchaseIntent savedIntent = purchaseIntentService.createPurchaseIntent(purchaseIntent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "购买意向提交成功，请等待卖家联系");
            response.put("intentId", savedIntent.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "提交失败：" + e.getMessage()));
        }
    }
    
    // 根据客户手机号获取购买意向列表
    @GetMapping("/customer/{phone}/purchase-intents")
    public ResponseEntity<?> getPurchaseIntentsByCustomerPhone(@PathVariable String phone) {
        try {
            List<PurchaseIntent> purchaseIntents = purchaseIntentService.getPurchaseIntentsByCustomerPhone(phone);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("purchaseIntents", purchaseIntents);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 根据客户手机号分页获取购买意向列表
    @GetMapping("/customer/{phone}/purchase-intents/page")
    public ResponseEntity<?> getPurchaseIntentsByCustomerPhonePage(
            @PathVariable String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<PurchaseIntent> purchaseIntents = purchaseIntentService.getPurchaseIntentsByCustomerPhone(phone);
            
            // 简单分页处理
            int start = Math.min((int) (page * size), purchaseIntents.size());
            int end = Math.min(start + size, purchaseIntents.size());
            List<PurchaseIntent> pagedIntents = purchaseIntents.subList(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("purchaseIntents", pagedIntents);
            response.put("totalPages", (int) Math.ceil((double) purchaseIntents.size() / size));
            response.put("totalElements", purchaseIntents.size());
            response.put("currentPage", page);
            response.put("pageSize", size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 取消订单（买家操作）
    @PostMapping("/purchase-intents/{id}/cancel")
    public ResponseEntity<?> cancelPurchaseIntent(@PathVariable Long id) {
        try {
            // 调用服务层将订单状态更新为CANCELLED，传入isBuyerOperation=true表示这是买家操作
            PurchaseIntent updatedIntent = purchaseIntentService.updateIntentStatus(id, IntentStatus.CANCELLED, true);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "订单已成功取消");
            response.put("intent", updatedIntent);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 处理错误
            return ResponseEntity.badRequest().body(Map.of("error", "取消订单失败: " + e.getMessage()));
        }
    }
    
    // 更新购买意向状态（卖家操作）
    @PostMapping("/purchase-intents/{id}/status")
    public ResponseEntity<?> updatePurchaseIntentStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest statusUpdateRequest) {
        try {
            // 验证请求中的状态值
            if (statusUpdateRequest.getNewStatus() == null || statusUpdateRequest.getNewStatus().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "新状态不能为空"));
            }
            
            // 转换为枚举类型
            IntentStatus newStatus;
            try {
                // 通过枚举名称转换
                newStatus = IntentStatus.valueOf(statusUpdateRequest.getNewStatus());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的状态值: " + statusUpdateRequest.getNewStatus()));
            }
            
            // 调用服务层更新状态
            PurchaseIntent updatedIntent = purchaseIntentService.updateIntentStatus(id, newStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "状态更新成功");
            response.put("intent", updatedIntent);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // 状态转换不合法
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // 其他错误
            return ResponseEntity.badRequest().body(Map.of("error", "状态更新失败: " + e.getMessage()));
        }
    }
}
