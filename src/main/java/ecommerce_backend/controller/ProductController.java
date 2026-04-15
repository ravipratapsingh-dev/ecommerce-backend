package ecommerce_backend.controller;

import ecommerce_backend.dto.ApiResponse;
import ecommerce_backend.entity.Product;
import ecommerce_backend.service.ProductService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // ADD PRODUCT
    @PostMapping
    public ApiResponse<Product> addProduct(@RequestBody Product product) {

        Product saved = service.addProduct(product);

        return new ApiResponse<>("Product added successfully", saved);
    }

    // GET ALL PRODUCTS
    @GetMapping
    public ApiResponse<List<Product>> getAllProducts() {

        List<Product> products = service.getAllProducts();

        return new ApiResponse<>("Products fetched", products);
    }

    // DELETE PRODUCT
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteProduct(@PathVariable Long id) {

        service.deleteProduct(id);

        return new ApiResponse<>("Product deleted successfully", null);
    }
}
