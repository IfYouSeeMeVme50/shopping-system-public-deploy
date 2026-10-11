package com.example.shopping_system.Service类;

import com.example.shopping_system.实体类.Customer;
import com.example.shopping_system.实体类.Favorite;
import com.example.shopping_system.实体类.Product;
import com.example.shopping_system.Repository接口.FavoriteRepository;
import com.example.shopping_system.Repository接口.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerService customerService;

    /**
     * 添加商品到收藏夹
     */
    public Favorite addToFavorite(String phone, Long productId) {
        // 根据手机号获取或创建客户
        Customer customer = customerService.findByPhone(phone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setPhone(phone);
                    // 设置默认用户名、密码和地址
                    newCustomer.setUsername(phone);
                    newCustomer.setPassword(phone);
                    newCustomer.setAddress("默认地址");
                    return customerService.register(newCustomer);
                });

        // 获取商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 检查是否已收藏
        if (favoriteRepository.existsByCustomerAndProduct(customer, product)) {
            throw new RuntimeException("商品已在收藏夹中");
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setCustomer(customer);
        favorite.setProduct(product);

        return favoriteRepository.save(favorite);
    }

    /**
     * 从收藏夹移除商品
     */
    public void removeFromFavorite(String phone, Long productId) {
        // 根据手机号获取或创建客户
        Customer customer = customerService.findByPhone(phone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setPhone(phone);
                    // 设置默认用户名、密码和地址
                    newCustomer.setUsername(phone);
                    newCustomer.setPassword(phone);
                    newCustomer.setAddress("默认地址");
                    return customerService.register(newCustomer);
                });

        // 获取商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 获取收藏记录
        Favorite favorite = favoriteRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new RuntimeException("商品不在收藏夹中"));

        // 删除收藏记录
        favoriteRepository.delete(favorite);
    }

    /**
     * 获取客户的收藏列表
     */
    public List<Favorite> getFavoriteList(String phone) {
        // 根据手机号获取或创建客户
        Customer customer = customerService.findByPhone(phone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setPhone(phone);
                    // 设置默认用户名、密码和地址
                    newCustomer.setUsername(phone);
                    newCustomer.setPassword(phone);
                    newCustomer.setAddress("默认地址");
                    return customerService.register(newCustomer);
                });

        return favoriteRepository.findByCustomer(customer);
    }

    /**
     * 检查商品是否在收藏夹中
     */
    public boolean isFavorite(String phone, Long productId) {
        // 根据手机号获取或创建客户
        Customer customer = customerService.findByPhone(phone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setPhone(phone);
                    // 设置默认用户名、密码和地址
                    newCustomer.setUsername(phone);
                    newCustomer.setPassword(phone);
                    newCustomer.setAddress("默认地址");
                    return customerService.register(newCustomer);
                });

        // 获取商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        return favoriteRepository.existsByCustomerAndProduct(customer, product);
    }
}