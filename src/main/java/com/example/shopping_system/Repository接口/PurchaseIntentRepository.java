package com.example.shopping_system.Repository接口;

import com.example.shopping_system.实体类.PurchaseIntent;
import com.example.shopping_system.实体类.IntentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseIntentRepository extends JpaRepository<PurchaseIntent, Long> {
    
    List<PurchaseIntent> findAllByOrderByCreatedAtDesc();
    
    List<PurchaseIntent> findByStatusOrderByCreatedAtDesc(IntentStatus status);
    
    @Query("SELECT pi FROM PurchaseIntent pi WHERE pi.product.id = ?1 AND pi.status = 'PENDING'")
    List<PurchaseIntent> findPendingIntentsByProductId(Long productId);
    
    // 根据客户手机号查找购买意向，按创建时间倒序排列
    List<PurchaseIntent> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);
}
