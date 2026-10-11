package com.example.shopping_system.Service类;

import com.example.shopping_system.实体类.PurchaseIntent;
import com.example.shopping_system.实体类.Product;
import com.example.shopping_system.实体类.IntentStatus;
import com.example.shopping_system.实体类.ProductStatus;
import com.example.shopping_system.Repository接口.PurchaseIntentRepository;
import com.example.shopping_system.Repository接口.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PurchaseIntentService {
    
    @Autowired
    private PurchaseIntentRepository purchaseIntentRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    public PurchaseIntent createPurchaseIntent(PurchaseIntent purchaseIntent) {
        // 按数量冻结库存
        Product product = purchaseIntent.getProduct();
        Integer quantity = purchaseIntent.getQuantity();
        
        // 检查库存是否足够
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足，无法创建购买意向");
        }
        
        // 减少库存，增加冻结库存
        product.setStock(product.getStock() - quantity);
        product.setFrozenStock(product.getFrozenStock() + quantity);
        
        // 如果库存为0，则冻结商品；否则保持活跃状态
        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.FROZEN);
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        productRepository.save(product);
        return purchaseIntentRepository.save(purchaseIntent);
    }
    
    public List<PurchaseIntent> getAllPurchaseIntents() {
        return purchaseIntentRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Optional<PurchaseIntent> getPurchaseIntentById(Long id) {
        return purchaseIntentRepository.findById(id);
    }
    
    // 根据客户手机号获取购买意向列表
    public List<PurchaseIntent> getPurchaseIntentsByCustomerPhone(String customerPhone) {
        return purchaseIntentRepository.findByCustomerPhoneOrderByCreatedAtDesc(customerPhone);
    }
    
    // 更新购买意向状态的方法，支持完整的状态流转
    public PurchaseIntent updateIntentStatus(Long intentId, IntentStatus newStatus, boolean isBuyerOperation) {
        Optional<PurchaseIntent> intentOpt = purchaseIntentRepository.findById(intentId);
        if (intentOpt.isPresent()) {
            PurchaseIntent intent = intentOpt.get();
            
            // 验证状态转换是否合法
            if (!isValidStatusTransition(intent.getStatus(), newStatus, isBuyerOperation)) {
                throw new RuntimeException("不允许的状态转换：从" + intent.getStatus() + "到" + newStatus);
            }
            
            Product product = intent.getProduct();
            Integer quantity = intent.getQuantity();
            
            // 更新状态和更新时间
            intent.setStatus(newStatus);
            intent.setUpdatedAt(java.time.LocalDateTime.now());
            
            // 根据新状态处理库存和商品状态
            switch (newStatus) {
                case STOCKED_UP:
                    // 备货完成：保持冻结库存不变
                    // 可以在这里添加备货相关的业务逻辑
                    break;
                case SHIPPED:
                    // 已发货：保持冻结库存不变
                    // 可以在这里添加发货相关的业务逻辑
                    break;
                case COMPLETED:
                    // 交易完成：减少冻结库存
                    product.setFrozenStock(product.getFrozenStock() - quantity);
                    
                    // 只有在库存为0时才将商品状态设为已售，否则保持活跃状态
                    if (product.getStock() == 0 && product.getFrozenStock() == 0) {
                        product.setStatus(ProductStatus.SOLD);
                        product.setSoldAt(java.time.LocalDateTime.now());
                    } else {
                        product.setStatus(ProductStatus.ACTIVE);
                    }
                    break;
                case CANCELLED:
                    // 交易取消：恢复库存，减少冻结库存
                    product.setStock(product.getStock() + quantity);
                    product.setFrozenStock(product.getFrozenStock() - quantity);
                    product.setStatus(ProductStatus.ACTIVE);
                    break;
                case PENDING:
                default:
                    // 待处理状态不需要特殊处理
                    break;
            }
            
            purchaseIntentRepository.save(intent);
            productRepository.save(product);
            return intent;
        }
        throw new RuntimeException("未找到ID为" + intentId + "的购买意向");
    }
    
    // 兼容旧的completeTransaction方法
    public PurchaseIntent completeTransaction(Long intentId, boolean success) {
        // 默认以卖家操作处理
        if (success) {
            return updateIntentStatus(intentId, IntentStatus.COMPLETED, false);
        } else {
            return updateIntentStatus(intentId, IntentStatus.CANCELLED, false);
        }
    }
    
    // 卖家专用的状态更新方法（保持向后兼容）
    public PurchaseIntent updateIntentStatus(Long intentId, IntentStatus newStatus) {
        return updateIntentStatus(intentId, newStatus, false);
    }
    
    // 验证状态转换是否合法
    private boolean isValidStatusTransition(IntentStatus currentStatus, IntentStatus newStatus, boolean isBuyerOperation) {
        // 基本规则：COMPLETED和CANCELLED状态不允许再转换
        if (currentStatus == IntentStatus.COMPLETED || currentStatus == IntentStatus.CANCELLED) {
            return false;
        }
        
        // 买家操作规则：只能在PENDING和STOCKED_UP状态下取消订单
        if (isBuyerOperation && newStatus == IntentStatus.CANCELLED) {
            return currentStatus == IntentStatus.PENDING || currentStatus == IntentStatus.STOCKED_UP;
        }
        
        // 卖家操作规则和非取消操作的通用规则
        switch (currentStatus) {
            case PENDING:
                return newStatus == IntentStatus.STOCKED_UP || 
                       newStatus == IntentStatus.CANCELLED;
            case STOCKED_UP:
                return newStatus == IntentStatus.SHIPPED || 
                       newStatus == IntentStatus.CANCELLED;
            case SHIPPED:
                return newStatus == IntentStatus.COMPLETED || 
                       (!isBuyerOperation && newStatus == IntentStatus.CANCELLED); // 卖家可以在SHIPPED状态下取消订单，买家不行
            default:
                return false;
        }
    }
}
