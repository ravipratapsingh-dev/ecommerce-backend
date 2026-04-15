package ecommerce_backend.dto;

import ecommerce_backend.controller.PaymentController;
import jakarta.validation.constraints.NotBlank;

public class OrderRequest {
    @NotBlank(message = "Address is Required")
    private String address;

    @NotBlank(message = "Payment method is Required")
    private String paymentMethod;

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}