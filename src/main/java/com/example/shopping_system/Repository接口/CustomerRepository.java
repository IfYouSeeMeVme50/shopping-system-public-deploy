package com.example.shopping_system.Repository接口;

import com.example.shopping_system.实体类.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // 根据用户名查找买家
    Optional<Customer> findByUsername(String username);
    
    // 检查用户名是否存在
    boolean existsByUsername(String username);
    
    // 根据用户名和密码查找买家（用于登录验证）
    Optional<Customer> findByUsernameAndPassword(String username, String password);
    
    // 根据手机号查找买家
    Optional<Customer> findByPhone(String phone);
}
