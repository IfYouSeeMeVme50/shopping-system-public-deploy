package com.example.shopping_system.Repository接口;

import com.example.shopping_system.实体类.StoredImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoredImageRepository extends JpaRepository<StoredImage, Long> {
}
