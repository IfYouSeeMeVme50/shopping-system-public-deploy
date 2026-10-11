package com.example.shopping_system.Repository接口;

import com.example.shopping_system.实体类.Customer;
import com.example.shopping_system.实体类.Favorite;
import com.example.shopping_system.实体类.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    // 根据客户查询收藏列表
    List<Favorite> findByCustomer(Customer customer);
    
    // 根据客户和商品查询收藏项
    Optional<Favorite> findByCustomerAndProduct(Customer customer, Product product);
    
    // 根据客户ID删除收藏项
    void deleteByCustomer(Customer customer);
    
    // 根据客户和商品删除收藏项
    void deleteByCustomerAndProduct(Customer customer, Product product);
    
    // 统计客户收藏商品总数
    Long countByCustomer(Customer customer);
    
    // 检查商品是否被客户收藏
    boolean existsByCustomerAndProduct(Customer customer, Product product);
}