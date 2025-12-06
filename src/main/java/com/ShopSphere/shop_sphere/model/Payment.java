package com.ShopSphere.shop_sphere.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
	
	
	

	public Payment(String gatewayRef, String upiVpa, String responsePayload) {
		super();
		this.gatewayRef = gatewayRef;
		this.upiVpa = upiVpa;
		this.responsePayload = responsePayload;
	}

	public Payment(int paymentId, int orderId, int userId, BigDecimal amount, String currency, String paymentMethod,
			LocalDateTime createdAt, String status) {
		super();
		this.paymentId = paymentId;
		this.orderId = orderId;
		this.userId = userId;
		this.amount = amount;
		this.currency = currency;
		this.paymentMethod = paymentMethod;
		this.createdAt = createdAt;
		this.status = status;
	}

	public Payment(int orderId, int userId, BigDecimal amount, String currency, String paymentMethod,
			LocalDateTime createdAt, String status) {
		super();
		this.orderId = orderId;
		this.userId = userId;
		this.amount = amount;
		this.currency = currency;
		this.paymentMethod = paymentMethod;
		this.createdAt = createdAt;
		this.status = status;
	}

	private int paymentId;
	private int orderId;
	private int userId;
	private BigDecimal amount;
	private String currency;
	private String paymentMethod;
	private LocalDateTime createdAt;
	private String status;
	private String gatewayRef;
	private String upiVpa;
	private String responsePayload;
	
	public Payment() {}

	public int getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}

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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGatewayRef() {
		return gatewayRef;
	}

	public void setGatewayRef(String gatewayRef) {
		this.gatewayRef = gatewayRef;
	}

	public String getUpiVpa() {
		return upiVpa;
	}

	public void setUpiVpa(String upiVpa) {
		this.upiVpa = upiVpa;
	}

	public String getResponsePayload() {
		return responsePayload;
	}

	public void setResponsePayload(String responsePayload) {
		this.responsePayload = responsePayload;
	}

	

	

}
