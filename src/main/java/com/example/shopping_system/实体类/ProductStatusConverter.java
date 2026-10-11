package com.example.shopping_system.实体类;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ProductStatusConverter implements AttributeConverter<ProductStatus, String> {

    @Override
    public String convertToDatabaseColumn(ProductStatus status) {
        if (status == null) {
            return ProductStatus.ACTIVE.name();
        }
        return status.name();
    }

    @Override
    public ProductStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return ProductStatus.ACTIVE;
        }

        // 首先尝试根据枚举名称匹配（数据库存储英文）
        try {
            return ProductStatus.valueOf(dbData);
        } catch (Exception e) {
            // 如果英文名称匹配失败，尝试根据中文名称匹配（兼容旧数据）
            try {
                return ProductStatus.fromChineseName(dbData);
            } catch (IllegalArgumentException ex) {
                // 如果都不匹配，返回默认值
                return ProductStatus.ACTIVE;
            }
        }
    }
}
