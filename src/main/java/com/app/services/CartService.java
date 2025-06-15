package com.app.services;

import java.util.List;
import com.app.payloads.CartDTO;

public interface CartService {
    // User endpoints (use email from Principal)
    CartDTO addProductToUserCart(String email, Long productId, Integer quantity);
    CartDTO updateProductQuantityInUserCart(String email, Long productId, Integer quantity);
    String deleteProductFromUserCart(String email, Long productId);
    CartDTO getUserCart(String email);


	
    // Admin endpoints (use cartId)
    List<CartDTO> getAllCarts();
    CartDTO getCartById(Long cartId);

    // Admin/internal use
    String deleteProductFromCart(Long cartId, Long productId);
}