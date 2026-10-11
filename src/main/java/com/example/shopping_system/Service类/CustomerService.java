package com.example.shopping_system.Service类;

import com.example.shopping_system.实体类.Customer;
import com.example.shopping_system.Repository接口.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    /**
     * 买家注册
     */
    public Customer register(Customer customer) {
        // 检查用户名是否已存在
        if (customerRepository.existsByUsername(customer.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 保存买家信息
        return customerRepository.save(customer);
    }
    
    /**
     * 买家登录
     */
    public Customer login(String username, String password) {
        Optional<Customer> customer = customerRepository.findByUsernameAndPassword(username, password);
        if (customer.isPresent()) {
            return customer.get();
        } else {
            throw new RuntimeException("用户名或密码错误");
        }
    }
    
    /**
     * 根据ID查找买家
     */
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }
    
    /**
     * 根据用户名查找买家
     */
    public Optional<Customer> findByUsername(String username) {
        return customerRepository.findByUsername(username);
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return customerRepository.existsByUsername(username);
    }
    
    /**
     * 更新买家信息
     */
    public Customer updateCustomer(Customer customer) {
        if (!customerRepository.existsById(customer.getId())) {
            throw new RuntimeException("买家不存在");
        }
        return customerRepository.save(customer);
    }
    
    /**
     * 获取所有买家信息
     */
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    /**
     * 根据手机号查找买家
     */
    public Optional<Customer> findByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }
}
