package com.example.shopping_system.DTO类;

import com.example.shopping_system.实体类.Seller;

public class SellerDTO {
    private Long id;
    private String username;

    public SellerDTO() {}

    public SellerDTO(Seller seller) {
        this.id = seller.getId();
        this.username = seller.getUsername();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
