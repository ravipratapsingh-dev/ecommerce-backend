package ecommerce_backend.controller;

import ecommerce_backend.dto.*;
import ecommerce_backend.entity.Order;
import ecommerce_backend.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //  PLACE ORDER
    @PostMapping("/place")
    public ApiResponse<String> placeOrder(@Valid @RequestBody OrderRequest request,
                                          Authentication authentication) {

        String email = authentication.getName();

        String message = orderService.placeOrder(email, request);

        return new ApiResponse<>(message, null);
    }

    // GET MY ORDERS
    @GetMapping("/my")
    public ApiResponse<List<OrderResponse>> getMyOrders(Authentication authentication) {

        String email = authentication.getName();

        List<OrderResponse> orders = orderService.getMyOrders(email)
                .stream()
                .map(orderService::convertToResponse)
                .toList();

        return new ApiResponse<>("Orders fetched", orders);
    }

    //  GET ORDER BY ID
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id,
                                                   Authentication authentication) {

        String email = authentication.getName();

        Order order = orderService.getOrderById(email, id);

        OrderResponse response = orderService.convertToResponse(order);

        return new ApiResponse<>("Order fetched", response);
    }

    //  UPDATE ORDER STATUS
    @PutMapping("/{id}/status")
    public ApiResponse<Order> updateOrderStatus(@PathVariable Long id,
                                                @RequestBody OrderStatusRequest request) {

        Order updated = orderService.updateOrderStatus(id, request.getStatus());

        return new ApiResponse<>("Order status updated", updated);
    }
}
