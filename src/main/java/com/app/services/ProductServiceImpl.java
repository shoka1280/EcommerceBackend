// ProductServiceImpl.java
package com.app.services;

import com.app.entites.Product;
import com.app.exceptions.ResourceNotFoundException;
import com.app.entites.Category;
import com.app.payloads.ProductCreateDTO;
import com.app.payloads.ProductDTO;
import com.app.payloads.ProductResponse;
import com.app.repositories.ProductRepo;
import com.app.repositories.CategoryRepo;
import com.app.services.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductCreateDTO productCreateDTO) {
        Product product = modelMapper.map(productCreateDTO, Product.class);
        Category category = categoryRepo.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "ID", categoryId));
        product.setCategory(category);
        Product saved = productRepo.save(product);
        return toProductDTO(saved);
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "ID", productId));
        // update fields from productDTO
        product.setProductName(productDTO.getProductName());
        product.setDescription(productDTO.getDescription());
        // ...other fields...
        Product updated = productRepo.save(product);
        return toProductDTO(updated);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product existing = productRepo.findById(productId).orElseThrow();
        existing.setImage(image.getOriginalFilename());
        return toProductDTO(productRepo.save(existing));
    }

    @Override
    public String deleteProduct(Long productId) {
        productRepo.deleteById(productId);
        return "Product deleted successfully";
    }

    @Override
    public ProductResponse getAllProducts(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        Page<Product> page = productRepo.findAll(pageable);
        List<ProductDTO> content = page.getContent().stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
        return new ProductResponse(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        Page<Product> page = productRepo.findByCategory_CategoryId(categoryId, pageable);
        List<ProductDTO> content = page.getContent().stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
        return new ProductResponse(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        Page<Product> page = productRepo.findByProductNameContaining(keyword, pageable);
        List<ProductDTO> content = page.getContent().stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
        return new ProductResponse(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Override
    public List<ProductDTO> getProductsByBrand(String brand) {
        return productRepo.findByBrand(brand).stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getProductsByType(String type) {
        return productRepo.findByType(type).stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
    }
    private ProductDTO toProductDTO(Product product) {
    return modelMapper.map(product, ProductDTO.class);
}

    // Helper method to map Product to ProductDTO with reviews and averageRating
   
}


