// ProductService.java
package com.app.services;

import com.app.payloads.ProductCreateDTO;
import com.app.payloads.ProductDTO;
import com.app.payloads.ProductResponse;
import com.app.entites.Product;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ProductService {
   ProductDTO addProduct(Long categoryId, ProductCreateDTO productCreateDTO);

ProductDTO updateProduct(Long productId, ProductDTO productDTO);
    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;
    String deleteProduct(Long productId);
    ProductResponse getAllProducts(int pageNumber, int pageSize, String sortBy, String sortOrder);
    ProductResponse searchByCategory(Long categoryId, int pageNumber, int pageSize, String sortBy, String sortOrder);
    ProductResponse searchProductByKeyword(String keyword, int pageNumber, int pageSize, String sortBy, String sortOrder);
    List<ProductDTO> getProductsByBrand(String brand);
    List<ProductDTO> getProductsByType(String type);
}