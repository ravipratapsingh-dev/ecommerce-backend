package ecommerce_backend.service;

import ecommerce_backend.dto.OrderRequest;
import ecommerce_backend.dto.OrderResponse;
import ecommerce_backend.dto.OrderItemResponse;
import ecommerce_backend.entity.*;
import ecommerce_backend.exception.BadRequestException;
import ecommerce_backend.exception.ResourceNotFoundException;
import ecommerce_backend.exception.UnauthorizedException;
import ecommerce_backend.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class); // ✅ LOGGER

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    // ===============================
    // 🔥 PLACE ORDER
    // ===============================
    public String placeOrder(String email, OrderRequest request) {

        log.info("Placing order for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.error("Cart not found for user: {}", email);
                    return new ResourceNotFoundException("Cart not found");
                });

        if (cart.getItems().isEmpty()) {
            log.warn("Cart is empty for user: {}", email);
            throw new BadRequestException("Cart is empty");
        }

        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        log.info("Calculated total amount {} for user {}", total, email);

        Order order = new Order();
        order.setUser(user);

        // Copy items
        List<CartItem> orderItems = cart.getItems().stream().map(item -> {
            CartItem newItem = new CartItem();
            newItem.setProduct(item.getProduct());
            newItem.setQuantity(item.getQuantity());
            return newItem;
        }).collect(Collectors.toList());

        order.setItems(orderItems);
        order.setTotalAmount(total);
        order.setStatus("PLACED");
        order.setAddress(request.getAddress());
        order.setPaymentMethod(request.getPaymentMethod());

        orderRepository.save(order);

        log.info("Order saved successfully for user: {}", email);

        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Cart cleared after order for user: {}", email);

        return "Order placed successfully";
    }

    // ===============================
    // 📦 GET MY ORDERS
    // ===============================
    public List<Order> getMyOrders(String email) {

        log.info("Fetching orders for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found while fetching orders: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        List<Order> orders = orderRepository.findByUser(user);

        log.info("Fetched {} orders for user: {}", orders.size(), email);

        return orders;
    }

    // ===============================
    // 🔍 GET ORDER BY ID
    // ===============================
    public Order getOrderById(String email, Long orderId) {

        log.info("Fetching order {} for user {}", orderId, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found: {}", orderId);
                    return new ResourceNotFoundException("Order not found");
                });

        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized access attempt for order {} by user {}", orderId, email);
            throw new UnauthorizedException("Unauthorized access");
        }

        log.info("Order {} fetched successfully for user {}", orderId, email);

        return order;
    }

    // ===============================
    // 🔄 UPDATE ORDER STATUS
    // ===============================
    public Order updateOrderStatus(Long orderId, String status) {

        log.info("Updating order status: orderId={}, newStatus={}", orderId, status);

        // ✅ FIXED: id -> orderId
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String currentStatus = order.getStatus();

        // ✅ 1. CANCEL LOGIC
        if (status.equals("CANCELLED")) {

            if (!currentStatus.equals("PLACED")) {
                throw new RuntimeException("Only PLACED orders can be cancelled");
            }
        }

        // ✅ 2. OPTIONAL (GOOD PRACTICE)
        // prevent invalid manual status jump
        if (currentStatus.equals("CANCELLED")) {
            throw new RuntimeException("Cancelled order cannot be updated");
        }

        // ✅ 3. UPDATE STATUS
        order.setStatus(status);

        Order updated = orderRepository.save(order);

        log.info("Order {} status updated to {}", orderId, status);

        return updated;
    }


    // ===============================
    // 🚀 DTO CONVERTER
    // ===============================
    public OrderResponse convertToResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setAddress(order.getAddress());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setOrderDate(order.getOrderDate());

        List<OrderItemResponse> items = order.getItems().stream().map(item -> {
            OrderItemResponse i = new OrderItemResponse();
            i.setProductName(item.getProduct().getName());
            i.setPrice(item.getProduct().getPrice());
            i.setQuantity(item.getQuantity());
            return i;
        }).collect(Collectors.toList());

        response.setItems(items);

        return response;
    }
}
