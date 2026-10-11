package com.example.shopping_system.DTO类;

import com.example.shopping_system.实体类.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryNodeDTO {

    private Long id;
    private String name;
    private String description;
    private Integer level;
    private List<CategoryNodeDTO> children = new ArrayList<>();

    public CategoryNodeDTO() {
    }

    public CategoryNodeDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.level = category.getLevel();
    }

    public CategoryNodeDTO(Category category, List<CategoryNodeDTO> children) {
        this(category);
        if (children != null) {
            this.children = children;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<CategoryNodeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryNodeDTO> children) {
        this.children = children;
    }
}
