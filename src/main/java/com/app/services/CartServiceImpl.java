package com.app.services;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Product;
import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CartDTO;
import com.app.payloads.CartItemDTO;
import com.app.payloads.ProductDTO;
import com.app.repositories.CartItemRepo;
import com.app.repositories.CartRepo;
import com.app.repositories.ProductRepo;
import com.app.repositories.UserRepo;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CartItemRepo cartItemRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    // --- USER METHODS ---

    @Override
    public CartDTO addProductToUserCart(String email, Long productId, Integer quantity) {
        Cart cart = getCartByUserEmail(email);
        return addProductToCart(cart, productId, quantity);
    }

    @Override
    public CartDTO updateProductQuantityInUserCart(String email, Long productId, Integer quantity) {
        Cart cart = getCartByUserEmail(email);
        return updateProductQuantityInCart(cart, productId, quantity);
    }

    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepo.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        return deleteProductFromCart(cart, productId);
    }
    @Override
public String deleteProductFromUserCart(String email, Long productId) {
    Cart cart = getCartByUserEmail(email);
    return deleteProductFromCart(cart, productId);
}

    @Override
    public CartDTO getUserCart(String email) {
        Cart cart = getCartByUserEmail(email);
        return toCartDTO(cart);
    }

    // --- ADMIN METHODS ---

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepo.findAll();
        if (carts.isEmpty()) throw new APIException("No cart exists");
        return carts.stream().map(this::toCartDTO).collect(Collectors.toList());
    }

    @Override
    public CartDTO getCartById(Long cartId) {
        Cart cart = cartRepo.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        return toCartDTO(cart);
    }

    // --- INTERNAL HELPERS ---

    private Cart getCartByUserEmail(String email) {
        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Cart cart = user.getCart();
        if (cart == null) throw new APIException("Cart not found for user");
        return cart;
    }

    private CartDTO addProductToCart(Cart cart, Long productId, Integer quantity) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        if (product.getQuantity() == 0) throw new APIException(product.getProductName() + " is not available");
        if (product.getQuantity() < quantity) throw new APIException("Please order quantity less than or equal to available stock: " + product.getQuantity());
        CartItem existingItem = cartItemRepo.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if (existingItem != null) throw new APIException("Product already exists in cart");

        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        product.setQuantity(product.getQuantity() - quantity);
        cartItemRepo.save(newCartItem);

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        return toCartDTO(cart);
    }

    private CartDTO updateProductQuantityInCart(Cart cart, Long productId, Integer quantity) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if (cartItem == null) throw new APIException("Product not available in cart");

        int available = product.getQuantity() + cartItem.getQuantity();
        if (quantity > available) throw new APIException("Only " + available + " units available.");

        product.setQuantity(available - quantity);
        double oldTotal = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setQuantity(quantity);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());

        cart.setTotalPrice(oldTotal + (cartItem.getProductPrice() * quantity));
        cartItemRepo.save(cartItem);

        return toCartDTO(cart);
    }

    private String deleteProductFromCart(Cart cart, Long productId) {
        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if (cartItem == null) throw new ResourceNotFoundException("Product", "productId", productId);

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        Product product = cartItem.getProduct();
        product.setQuantity(product.getQuantity() + cartItem.getQuantity());
        cartItemRepo.deleteCartItemByProductIdAndCartId(cart.getCartId(), productId);

        return "Product " + product.getProductName() + " removed from cart.";
    }
    private CartItemDTO toCartItemDTO(CartItem item) {
    CartItemDTO dto = new CartItemDTO();
    dto.setCartItemId(item.getCartItemId());
    dto.setParentCartId(item.getCart().getCartId());
    dto.setQuantity(item.getQuantity());
    dto.setDiscount(item.getDiscount());
    dto.setProductSpecialPrice(item.getProductPrice());
    if (item.getProduct() != null) {
        ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
        dto.setProduct(productDTO);
    }
    return dto;
}

  private CartDTO toCartDTO(Cart cart) {
    CartDTO cartDTO = new CartDTO();
    cartDTO.setCartId(cart.getCartId());
    cartDTO.setTotalPrice(cart.getTotalPrice());

    // Map cart items manually
    List<CartItemDTO> cartItemDTOs = cart.getCartItems().stream()
        .map(this::toCartItemDTO)
        .collect(Collectors.toList());
    cartDTO.setCartItems(cartItemDTOs);

    // Map products manually
    List<ProductDTO> productDTOs = cart.getCartItems().stream()
        .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class))
        .collect(Collectors.toList());
    cartDTO.setProducts(productDTOs);

    return cartDTO;
}
}