package com.example.shopping_system.DTO类;

import com.example.shopping_system.实体类.Category;

public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Integer level;
    private CategoryDTO parent;

    public CategoryDTO() {}

    public CategoryDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.level = category.getLevel();
        
        // 安全地处理父分类
        if (category.getParent() != null) {
            try {
                this.parent = new CategoryDTO(category.getParent());
            } catch (Exception e) {
                // 如果父分类加载失败，设置为null
                this.parent = null;
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

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public CategoryDTO getParent() { return parent; }
    public void setParent(CategoryDTO parent) { this.parent = parent; }
}
