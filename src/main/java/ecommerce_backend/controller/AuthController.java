package ecommerce_backend.controller;

import ecommerce_backend.dto.LoginRequest;
import ecommerce_backend.dto.RegisterRequest;
import ecommerce_backend.dto.ApiResponse;
import ecommerce_backend.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // REGISTER
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody RegisterRequest request) {

        String message = userService.registerUser(request);

        return new ApiResponse<>(message, null);
    }

    // LOGIN
    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginRequest request) {

        String token = userService.loginUser(request);

        return new ApiResponse<>("Login successful", token);
    }
}
