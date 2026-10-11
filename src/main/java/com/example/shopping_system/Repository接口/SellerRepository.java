package com.example.shopping_system.Repository接口;

import com.example.shopping_system.实体类.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    
    Optional<Seller> findByUsername(String username);
    
    boolean existsByUsername(String username);
}
