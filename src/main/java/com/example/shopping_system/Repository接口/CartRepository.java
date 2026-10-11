package com.example.shopping_system.Repository接口;

import com.example.shopping_system.实体类.Cart;
import com.example.shopping_system.实体类.Customer;
import com.example.shopping_system.实体类.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    // 根据客户查询购物车列表
    List<Cart> findByCustomer(Customer customer);
    
    // 根据客户和商品查询购物车项
    Optional<Cart> findByCustomerAndProduct(Customer customer, Product product);
    
    // 根据客户ID删除购物车项
    void deleteByCustomer(Customer customer);
    
    // 根据客户和商品删除购物车项
    void deleteByCustomerAndProduct(Customer customer, Product product);
    
    // 统计客户购物车商品总数
    Long countByCustomer(Customer customer);
}