package com.example.shopping_system.实体类;

public enum ProductStatus {
    ACTIVE("在售"), FROZEN("冻结"), SOLD("已售");
    
    private final String chineseName;
    
    ProductStatus(String chineseName) {
        this.chineseName = chineseName;
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    // 根据中文名称获取枚举值
    public static ProductStatus fromChineseName(String chineseName) {
        for (ProductStatus status : values()) {
            if (status.chineseName.equals(chineseName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的商品状态: " + chineseName);
    }
}
