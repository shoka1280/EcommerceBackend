// CategoryService.java
package com.app.services;

import com.app.entites.Category;
import com.app.payloads.CategoryCreateDTO;
import com.app.payloads.CategoryDTO;
import com.app.payloads.CategoryResponse;

import jakarta.validation.Valid;

public interface CategoryService {
   CategoryDTO createCategory(CategoryCreateDTO categoryCreateDTO);

CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);
    String deleteCategory(Long categoryId);
    CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}