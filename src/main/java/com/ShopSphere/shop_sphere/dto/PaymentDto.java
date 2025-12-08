package com.ShopSphere.shop_sphere.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentDto {

    public PaymentDto(Integer payment_id,
			@NotNull(message = "Order Id is required") @Min(value = 1, message = "orderId must be >=1") Integer orderId,
			@NotNull(message = "UserId is required") @Min(value = 1, message = "userId must be >=1") Integer userId,
			@NotNull(message = "Amount is required") @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be >=0.01") BigDecimal amount,
			@NotBlank(message = "Currency is required") String currency,
			@NotBlank(message = "Payment Method is required") String paymentMethod, LocalDateTime createdAt,
			@NotBlank(message = "status is required") String status, String gatewayRef, String upiVpa,
			String responsePayLoad) {
		super();
		this.payment_id = payment_id;
		this.orderId = orderId;
		this.userId = userId;
		this.amount = amount;
		this.currency = currency;
		this.paymentMethod = paymentMethod;
		this.createdAt = createdAt;
		this.status = status;
		this.gatewayRef = gatewayRef;
		this.upiVpa = upiVpa;
		this.responsePayLoad = responsePayLoad;
	}





	private Integer payment_id;
	
	
	@NotNull(message  = "Order Id is required")
	@Min(value = 1,message = "orderId must be >=1")
	private Integer orderId;
	
	@NotNull(message  = "UserId is required")
	@Min(value = 1,message = "userId must be >=1")
	private Integer userId;
	
	@NotNull(message  = "Amount is required")
	@DecimalMin(value = "0.01",inclusive = true, message = "Amount must be >=0.01")
	private BigDecimal amount;
	
	@NotBlank(message  = "Currency is required")
	private String currency ;
	
	@NotBlank(message  = "Payment Method is required")
	private String paymentMethod ;
	
	private LocalDateTime createdAt;
	
	@NotBlank(message  = "status is required")
	private String status ;
	
	private String gatewayRef;
	private String upiVpa;
	private String responsePayLoad;
	
	
	
	
	
	public PaymentDto() {}





	public Integer getPayment_id() {
		return payment_id;
	}





	public void setPayment_id(Integer payment_id) {
		this.payment_id = payment_id;
	}





	public Integer getOrderId() {
		return orderId;
	}





	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}





	public Integer getUserId() {
		return userId;
	}





	public void setUserId(Integer userId) {
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





	public String getResponsePayLoad() {
		return responsePayLoad;
	}





	public void setResponsePayLoad(String responsePayLoad) {
		this.responsePayLoad = responsePayLoad;
	}

}
