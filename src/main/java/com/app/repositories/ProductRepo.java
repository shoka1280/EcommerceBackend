package com.app.repositories;
// ProductRepo.java


import com.app.entites.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepo extends JpaRepository<Product, Long> {
    Page<Product> findByCategory_CategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByProductNameContaining(String keyword, Pageable pageable);
    List<Product> findByBrand(String brand);
    List<Product> findByType(String type);
}