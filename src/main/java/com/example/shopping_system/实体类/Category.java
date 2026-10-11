package com.example.shopping_system.实体类;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;

@Entity
@Table(name = "categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 200)
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @Column(name = "level")
    private Integer level = 1; // 1: 一级分类, 2: 二级分类
    
    // 构造函数
    public Category() {}
    
    public Category(String name, String description, Integer level) {
        this.name = name;
        this.description = description;
        this.level = level;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
}
