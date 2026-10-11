package com.example.shopping_system.Repository接口;

import com.example.shopping_system.实体类.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByLevel(Integer level);

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    Optional<Category> findByNameAndLevelAndParentIsNull(String name, Integer level);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.parent.id = :parentId")
    Optional<Category> findByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);

    boolean existsByParentId(Long parentId);
}
