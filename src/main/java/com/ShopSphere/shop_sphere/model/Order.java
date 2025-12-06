package com.ShopSphere.shop_sphere.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private int orderId;
    private int userId;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String orderStatus;
    private LocalDateTime placedAt;
    private String paymentMethod;
    private String razorpayOrderId;
    
    private List<OrderItem> items;  // + getter & setter
    
    private String paymentStatus;


    public Order() {}

    public Order(int orderId, int userId, BigDecimal totalAmount,
                 String shippingAddress, String orderStatus,
                 LocalDateTime placedAt, String paymentMethod,
                 String razorpayOrderId, List<OrderItem> items,
                 String paymentStatus) {

        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.orderStatus = orderStatus;
        this.placedAt = placedAt;
        this.paymentMethod = paymentMethod;
        this.razorpayOrderId = razorpayOrderId;
        this.items= items;
        this.paymentStatus = paymentStatus;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

    // NEW GETTER/SETTER for paymentStatus
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
