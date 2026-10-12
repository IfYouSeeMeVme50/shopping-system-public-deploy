package com.example.shopping_system.实体类;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 数据库存储的商品图片/媒体文件（BLOB）
 * 解决 Render 容器磁盘临时存储导致运行时上传文件丢失的问题
 */
@Entity
@Table(name = "stored_images")
public class StoredImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String originalName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Lob
    @Column(name = "data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] data;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public StoredImage() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
