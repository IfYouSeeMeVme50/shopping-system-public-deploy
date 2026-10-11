package com.example.shopping_system.实体类;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class IntentStatusConverter implements AttributeConverter<IntentStatus, String> {

    @Override
    public String convertToDatabaseColumn(IntentStatus status) {
        if (status == null) {
            return IntentStatus.PENDING.name();
        }
        return status.name();
    }

    @Override
    public IntentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return IntentStatus.PENDING;
        }

        // 尝试根据枚举名称匹配（数据库存储英文）
        try {
            return IntentStatus.valueOf(dbData);
        } catch (Exception e) {
            // 如果不匹配，返回默认值
            return IntentStatus.PENDING;
        }
    }
}