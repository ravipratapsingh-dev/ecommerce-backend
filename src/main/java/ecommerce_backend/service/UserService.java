package ecommerce_backend.service;

import ecommerce_backend.dto.LoginRequest;
import ecommerce_backend.dto.RegisterRequest;
import ecommerce_backend.entity.User;
import ecommerce_backend.exception.BadRequestException;
import ecommerce_backend.exception.ResourceNotFoundException;
import ecommerce_backend.exception.UnauthorizedException;
import ecommerce_backend.repository.UserRepository;
import ecommerce_backend.security.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class); // ✅ LOGGER

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(JwtUtil jwtUtil, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTER
    public String registerUser(RegisterRequest request) {

        log.info("Register request received for email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed - Email already exists: {}", request.getEmail());
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        log.info("User registered successfully: {}", request.getEmail());

        return "User registered successfully";
    }

    // LOGIN
    public String loginUser(LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Login failed - User not found: {}", request.getEmail());
                    return new ResourceNotFoundException("User not found");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - Invalid password for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        log.info("Login successful for email: {}", request.getEmail());

        return token;
    }
}
