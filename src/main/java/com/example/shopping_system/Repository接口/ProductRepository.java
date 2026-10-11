package com.example.shopping_system.Repository接口;

import com.example.shopping_system.实体类.Product;
import com.example.shopping_system.实体类.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findFirstByStatusOrderByCreatedAtDesc(ProductStatus status);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findFirstByStatus(ProductStatus status);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    List<Product> findByStatus(ProductStatus status);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByStatusAndCategoryIdOrderByCreatedAtDesc(ProductStatus status, Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByStatusAndNameContainingOrStatusAndDescriptionContaining(
            ProductStatus status1,
            String name,
            ProductStatus status2,
            String description,
            Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findById(Long id);

    boolean existsByCategoryId(Long categoryId);
}
