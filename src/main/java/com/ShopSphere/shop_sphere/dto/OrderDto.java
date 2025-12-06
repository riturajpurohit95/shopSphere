package com.ShopSphere.shop_sphere.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderDto {

    public OrderDto() {}

    public OrderDto(
            Integer order_id,
            @NotNull(message = "UserId is required")
            @Min(value = 1, message = "userId must be >= 1")
            Integer userId,

            @NotNull(message = "Total Amount is required")
            @DecimalMin(value = "0.0", message = "Total Amount must be >= 0.0")
            @Digits(integer = 10, fraction = 2, message = "Total Amount must have max 2 decimal places")
            BigDecimal totalAmount,

            @NotBlank(message = "Shipping Address is required")
            String shippingAddress,

            @NotBlank(message = "Status is required")
            String orderStatus,

            LocalDateTime placedAt,
            String paymentMethod,
            String razorpayOrderId
            ,String paymentStatus
    ) {
        this.order_id = order_id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.orderStatus = orderStatus;
        this.placedAt = placedAt;
        this.paymentMethod = paymentMethod;
        this.razorpayOrderId = razorpayOrderId;
        this.paymentStatus = paymentStatus;
    }

    private Integer order_id;

    @NotNull(message = "UserId is required")
    @Min(value = 1, message = "userId must be >= 1")
    private Integer userId;

    @NotNull(message = "Total Amount is required")
    @DecimalMin(value = "0.0", message = "Total Amount must be >= 0.0")
    @Digits(integer = 10, fraction = 2, message = "Total Amount must have max 2 decimal places")
    private BigDecimal totalAmount;

    @NotBlank(message = "Shipping Address is required")
    private String shippingAddress;

    @NotBlank(message = "Status is required")
    private String orderStatus;

    private LocalDateTime placedAt;

    private String paymentMethod;

    // ✅ Added Razorpay Order ID field here
    private String razorpayOrderId;
    
    private String paymentStatus;


    // ----------- Getters & Setters ------------ //

    public Integer getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
}