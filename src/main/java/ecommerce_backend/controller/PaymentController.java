package ecommerce_backend.controller;

import ecommerce_backend.dto.ApiResponse;
import ecommerce_backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pay/{orderId}")
    public ApiResponse<String> pay(@PathVariable Long orderId) {
        String message = paymentService.makePayment(orderId);
        return new ApiResponse<>("Payment successful", message);
    }
}