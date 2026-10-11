package com.example.shopping_system.DTO类;

import com.example.shopping_system.实体类.Product;
import com.example.shopping_system.实体类.ProductStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private Integer frozenStock;
    private ProductStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime soldAt;
    
    private String image;
    private List<String> images;
    private CategoryDTO category;
    private SellerDTO seller;

    public ProductDTO() {}

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.frozenStock = product.getFrozenStock();
        this.status = product.getStatus();
        this.createdAt = product.getCreatedAt();
        this.soldAt = product.getSoldAt();
        this.image = product.getImage();
        this.images = product.getImages();
        
        // 安全地处理分类
        if (product.getCategory() != null) {
            try {
                this.category = new CategoryDTO(product.getCategory());
            } catch (Exception e) {
                // 如果分类加载失败，设置为null
                this.category = null;
            }
        }
        
        // 安全地处理卖家
        if (product.getSeller() != null) {
            try {
                this.seller = new SellerDTO(product.getSeller());
            } catch (Exception e) {
                // 如果卖家加载失败，设置为null
                this.seller = null;
            }
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getFrozenStock() { return frozenStock; }
    public void setFrozenStock(Integer frozenStock) { this.frozenStock = frozenStock; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getSoldAt() { return soldAt; }
    public void setSoldAt(LocalDateTime soldAt) { this.soldAt = soldAt; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public CategoryDTO getCategory() { return category; }
    public void setCategory(CategoryDTO category) { this.category = category; }

    public SellerDTO getSeller() { return seller; }
    public void setSeller(SellerDTO seller) { this.seller = seller; }
}
