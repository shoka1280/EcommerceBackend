package com.app.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.payloads.CartDTO;
import com.app.services.CartService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class CartController {

    @Autowired
    private CartService cartService;

    // --- USER ENDPOINTS (no cartId, no email in path) ---

    @PostMapping("/public/cart/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(
            @PathVariable Long productId,
            @PathVariable Integer quantity,
            Principal principal) {
        CartDTO cartDTO = cartService.addProductToUserCart(principal.getName(), productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @PutMapping("/public/cart/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> updateCartProduct(
            @PathVariable Long productId,
            @PathVariable Integer quantity,
            Principal principal) {
        CartDTO cartDTO = cartService.updateProductQuantityInUserCart(principal.getName(), productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/public/cart/products/{productId}")
    public ResponseEntity<String> deleteProductFromCart(
            @PathVariable Long productId,
            Principal principal) {
        String status = cartService.deleteProductFromUserCart(principal.getName(), productId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @GetMapping("/public/cart")
    public ResponseEntity<CartDTO> getUserCart(Principal principal) {
        CartDTO cartDTO = cartService.getUserCart(principal.getName());
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    // --- ADMIN ENDPOINTS (with cartId) ---

    @GetMapping("/admin/carts")
    public ResponseEntity<List<CartDTO>> getCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOs, HttpStatus.OK);
    }

    @GetMapping("/admin/carts/{cartId}")
    public ResponseEntity<CartDTO> getCartById(@PathVariable Long cartId) {
        CartDTO cartDTO = cartService.getCartById(cartId);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }
}