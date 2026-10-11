package com.example.shopping_system.DTO类;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CategoryManageRequest {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "分类级别不能为空")
    private Integer level;

    private Long parentId;

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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
