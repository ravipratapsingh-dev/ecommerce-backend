package ecommerce_backend.service;

import ecommerce_backend.entity.*;
import ecommerce_backend.exception.ResourceNotFoundException;
import ecommerce_backend.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // ✅ ADD TO CART
    @Transactional
    public void addToCart(String email, Long productId, int quantity) {

        log.info("Add to cart: user={}, productId={}, qty={}", email, productId, quantity);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>()); // ✅ FIX
                    return cartRepository.save(newCart);
                });

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setCart(cart);

        cart.getItems().add(item);

        cartRepository.save(cart);

        log.info("Product added successfully to cart");
    }

    // ✅ GET CART
    public List<CartItem> getCart(String email) {

        log.info("Fetching cart for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("Creating new cart for user: {}", email);
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>()); // ✅ FIX
                    return cartRepository.save(newCart);
                });

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        log.info("Cart fetched successfully");

        return cart.getItems();
    }

    // ✅ REMOVE ITEM
    public String removeItem(Long itemId) {

        log.info("Removing item: {}", itemId);

        if (!cartItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        cartItemRepository.deleteById(itemId);

        return "Item removed from cart";
    }

    // ✅ CLEAR CART
    @Transactional
    public String clearCart(String email) {

        log.info("Clearing cart for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteAllByCart(cart);

        return "Cart cleared Successfully";
    }
}
