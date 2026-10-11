package com.example.shopping_system.Service类;

import com.example.shopping_system.实体类.Cart;
import com.example.shopping_system.实体类.Customer;
import com.example.shopping_system.实体类.Product;
import com.example.shopping_system.Repository接口.CartRepository;
import com.example.shopping_system.Repository接口.CustomerRepository;
import com.example.shopping_system.Repository接口.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * 添加商品到购物车
     */
    public Cart addToCart(String phone, Long productId, int quantity) {
        // 根据手机号获取或创建客户
        Customer customer = customerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setPhone(phone);
                    newCustomer.setUsername(phone);
                    newCustomer.setPassword(phone);
                    newCustomer.setAddress("默认地址"); // 设置默认地址以通过验证
                    return customerRepository.save(newCustomer);
                });
        
        // 获取商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 检查是否已在购物车中
        Optional<Cart> existingCartItem = cartRepository.findByCustomerAndProduct(customer, product);
        
        if (existingCartItem.isPresent()) {
            // 已存在则更新数量
            Cart cart = existingCartItem.get();
            cart.setQuantity(cart.getQuantity() + quantity);
            return cartRepository.save(cart);
        } else {
            // 不存在则创建新条目
            Cart cart = new Cart();
            cart.setCustomer(customer);
            cart.setProduct(product);
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        }
    }
    
    /**
     * 从购物车移除商品
     */
    public void removeFromCart(String phone, Long productId) {
        // 根据手机号获取客户
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("客户不存在"));
        
        // 获取商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 删除购物车条目
        cartRepository.deleteByCustomerAndProduct(customer, product);
    }
    
    /**
     * 更新购物车商品数量
     */
    public Cart updateCartQuantity(String phone, Long productId, int quantity) {
        // 根据手机号获取客户
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("客户不存在"));
        
        // 获取商品
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 查找购物车条目
        Cart cart = cartRepository.findByCustomerAndProduct(customer, product)
                .orElseThrow(() -> new RuntimeException("商品不在购物车中"));
        
        // 更新数量，如果数量为0则删除
        if (quantity <= 0) {
            cartRepository.delete(cart);
            return null;
        } else {
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        }
    }
    
    /**
     * 获取客户的购物车列表
     */
    public List<Cart> getCartList(String phone) {
        // 根据手机号获取或创建客户
        Customer customer = customerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setPhone(phone);
                    newCustomer.setUsername(phone);
                    newCustomer.setPassword(phone);
                    newCustomer.setAddress("默认地址"); // 设置默认地址以通过验证
                    return customerRepository.save(newCustomer);
                });
        
        return cartRepository.findByCustomer(customer);
    }
    
    /**
     * 清空客户的购物车
     */
    public void clearCart(String phone) {
        // 根据手机号获取客户
        Customer customer = customerRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("客户不存在"));
        
        // 清空购物车
        cartRepository.deleteByCustomer(customer);
    }
    
    /**
     * 获取客户购物车商品总数
     */
    public Long getCartItemCount(String phone) {
        // 根据手机号获取或创建客户
        Customer customer = customerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setPhone(phone);
                    newCustomer.setUsername(phone);
                    newCustomer.setPassword(phone);
                    newCustomer.setAddress("默认地址"); // 设置默认地址以通过验证
                    return customerRepository.save(newCustomer);
                });
        
        return cartRepository.countByCustomer(customer);
    }
}