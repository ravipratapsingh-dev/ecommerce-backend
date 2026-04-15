package ecommerce_backend.service;

import ecommerce_backend.entity.Order;
import ecommerce_backend.exception.BadRequestException;
import ecommerce_backend.exception.ResourceNotFoundException;
import ecommerce_backend.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class); // ✅ LOGGER

    @Autowired
    private OrderRepository orderRepository;

    public String makePayment(Long orderId) {

        log.info("Payment initiated for orderId: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found for payment: {}", orderId);
                    return new ResourceNotFoundException("Order not found");
                });

        log.info("Order status before payment: {} for orderId {}", order.getStatus(), orderId);

        // already paid
        if ("PAID".equals(order.getStatus())) {
            log.warn("Payment attempt on already paid order: {}", orderId);
            throw new BadRequestException("Order already paid");
        }

        //  wrong state
        if (!"PLACED".equals(order.getStatus())) {
            log.warn("Invalid payment attempt. Order not in PLACED state: orderId={}, status={}",
                    orderId, order.getStatus());
            throw new BadRequestException("Order not in PLACED state");
        }

        // simulate payment success
        order.setStatus("PAID");
        orderRepository.save(order);

        log.info("Payment successful for orderId: {}", orderId);

        return "Payment successful for Order ID: " + orderId;
    }
}
