package com.example.shopping_system.Controller类;

import com.example.shopping_system.DTO类.CustomerLoginDTO;
import com.example.shopping_system.DTO类.CustomerRegisterDTO;
import com.example.shopping_system.实体类.Customer;
import com.example.shopping_system.Service类.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@Validated
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    /**
     * 买家注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CustomerRegisterDTO registerDTO) {
        try {
            // 创建买家实体
            Customer customer = new Customer();
            customer.setUsername(registerDTO.getUsername());
            customer.setPassword(registerDTO.getPassword()); // 注意：实际项目中应该加密密码
            customer.setPhone(registerDTO.getPhone());
            customer.setAddress(registerDTO.getAddress());
            customer.setRememberMe(false);
            
            // 注册买家
            Customer savedCustomer = customerService.register(customer);
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("customerId", savedCustomer.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            // 返回错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            // 返回系统错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "系统错误，请稍后重试");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 买家登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody CustomerLoginDTO loginDTO) {
        try {
            // 验证登录
            Customer customer = customerService.login(loginDTO.getUsername(), loginDTO.getPassword());
            
            // 返回成功响应（模拟token）
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", generateMockToken(customer));
            
            // 返回用户信息（不包含密码）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", customer.getId());
            userInfo.put("username", customer.getUsername());
            userInfo.put("phone", customer.getPhone());
            userInfo.put("address", customer.getAddress());
            response.put("user", userInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            // 返回错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            // 返回系统错误响应
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "系统错误，请稍后重试");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    
    
    /**
     * 生成模拟token（实际项目中应该使用JWT等安全机制）
     */
    private String generateMockToken(Customer customer) {
        return "mock_token_" + customer.getId() + "_" + System.currentTimeMillis();
    }
}
