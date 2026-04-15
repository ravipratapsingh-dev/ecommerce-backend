package ecommerce_backend.controller;

import ecommerce_backend.dto.ApiResponse;
import ecommerce_backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // ✅ ADD PRODUCT TO CART
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addToCart(@RequestParam Long productId,
                                                         @RequestParam int quantity) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();

        cartService.addToCart(email, productId, quantity);

        return ResponseEntity.ok(new ApiResponse<>("Product added to cart", null));
    }

    // ✅ VIEW CART
    @GetMapping
    public ApiResponse<Object> getCart(Authentication authentication) {

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        Object cart = cartService.getCart(email);

        return new ApiResponse<>("Cart fetched", cart);
    }

    // ✅ REMOVE ITEM
    @DeleteMapping("/remove/{itemId}")
    public ApiResponse<String> removeItem(@PathVariable Long itemId) {

        String message = cartService.removeItem(itemId);

        return new ApiResponse<>(message, null);
    }

    // ✅ CLEAR CART
    @DeleteMapping("/clear")
    public ApiResponse<String> clearCart(Authentication authentication) {

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        String message = cartService.clearCart(email);

        return new ApiResponse<>(message, null);
    }
}