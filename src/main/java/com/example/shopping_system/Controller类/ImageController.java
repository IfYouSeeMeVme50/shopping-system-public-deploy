package com.example.shopping_system.Controller类;

import com.example.shopping_system.实体类.StoredImage;
import com.example.shopping_system.Repository接口.StoredImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 从数据库读取上传的图片/媒体文件
 * URL 格式：/images/{id} 或 /images/{id}.{扩展名}（扩展名仅用于前端类型判断）
 */
@RestController
public class ImageController {

    @Autowired
    private StoredImageRepository storedImageRepository;

    @GetMapping("/images/{fileKey}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileKey) {
        // fileKey 形如 "12.jpg" 或 "12"，取前导数字作为 id
        String idPart = fileKey.split("\\.")[0];
        Long id;
        try {
            id = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            return ResponseEntity.notFound().build();
        }

        return storedImageRepository.findById(id)
                .map(this::toResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<byte[]> toResponse(StoredImage image) {
        byte[] data = image.getData();
        if (data == null || data.length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(image.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                // id 定长内容，可长期缓存；跨容器休眠/重启后依然有效（数据在 TiDB）
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(data.length))
                .body(data);
    }
}
