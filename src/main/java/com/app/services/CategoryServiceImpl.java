package com.app.services;

import com.app.entites.Category;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CategoryCreateDTO;
import com.app.payloads.CategoryDTO;
import com.app.payloads.CategoryResponse;
import com.app.repositories.CategoryRepo;
import com.app.services.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private ModelMapper modelMapper;

   @Override
public CategoryDTO createCategory(CategoryCreateDTO categoryCreateDTO) {
    Category category = modelMapper.map(categoryCreateDTO, Category.class);
    Category saved = categoryRepo.save(category);
    return modelMapper.map(saved, CategoryDTO.class);
}

@Override
public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
    Category existing = categoryRepo.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "ID", categoryId));
    existing.setCategoryName(categoryDTO.getCategoryName());
    Category updated = categoryRepo.save(existing);
    return modelMapper.map(updated, CategoryDTO.class);
}

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "ID", categoryId));
        categoryRepo.delete(category);
        return "Category deleted successfully";
    }

    @Override
    public CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        Page<Category> page = categoryRepo.findAll(pageable);
        List<CategoryDTO> content = page.getContent().stream()
                .map(cat -> modelMapper.map(cat, CategoryDTO.class)).collect(Collectors.toList());

        CategoryResponse response = new CategoryResponse();
        response.setContent(content);
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLastPage(page.isLast());

        return response;
    }
}
