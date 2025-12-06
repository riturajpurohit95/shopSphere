package com.ShopSphere.shop_sphere.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class Order {
	
	




	

	public Order(int orderId, int userId, BigDecimal totalAmount, String shippingAddress, String orderStatus,
			LocalDateTime placedAt, String paymentMethod, String productName, Integer productQuantity,
			BigDecimal unitPrice, String razorpayOrderId) {
		super();
		this.orderId = orderId;
		this.userId = userId;
		this.totalAmount = totalAmount;
		this.shippingAddress = shippingAddress;
		this.orderStatus = orderStatus;
		this.placedAt = placedAt;
		this.paymentMethod = paymentMethod;
		this.productName = productName;
		this.productQuantity = productQuantity;
		this.unitPrice = unitPrice;
		this.razorpayOrderId = razorpayOrderId;
	}

	private int orderId;
	private int userId;
	private BigDecimal totalAmount;
	private String shippingAddress;
	private String orderStatus;
	private LocalDateTime placedAt;
	private String paymentMethod;
    private String productName;
    private Integer productQuantity;
    private BigDecimal unitPrice;
	private String razorpayOrderId;
	
	public Order() {}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
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

	// in Order.java

	public String getRazorpayOrderId() {
	    return razorpayOrderId;
	}

	public void setRazorpayOrderId(String razorpayOrderId) {
	    this.razorpayOrderId = razorpayOrderId;
	}

	public Object getProductName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(Integer productQuantity) {
		this.productQuantity = productQuantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}


	
}
