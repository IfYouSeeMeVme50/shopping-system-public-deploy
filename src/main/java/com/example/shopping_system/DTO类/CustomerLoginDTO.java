package com.example.shopping_system.DTO类;

import javax.validation.constraints.NotBlank;

public class CustomerLoginDTO {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    // 构造函数
    public CustomerLoginDTO() {}
    
    public CustomerLoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getter和Setter方法
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
