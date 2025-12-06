package com.ShopSphere.shop_sphere.model;

import java.math.BigDecimal;

public class OrderItem {
	
	
	public OrderItem(int orderId, int productId, String productName, int quantity, BigDecimal unitPrice,
			BigDecimal totalItemPrice) {
		super();
		this.orderId = orderId;
		this.productId = productId;
		this.productName = productName;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalItemPrice = totalItemPrice;
	}

	private int orderItemsId;
	private int orderId;
	private int productId;
	private String productName;
	private int quantity;
	private BigDecimal unitPrice;
	private BigDecimal totalItemPrice;
	
	public OrderItem() {}

	public int getOrderItemsId() {
		return orderItemsId;
	}

	public void setOrderItemsId(int orderItemsId) {
		this.orderItemsId = orderItemsId;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public BigDecimal getTotalItemPrice() {
		return totalItemPrice;
	}

	public void setTotalItemPrice(BigDecimal totalItemPrice) {
		this.totalItemPrice = totalItemPrice;
	}

	@Override
	public String toString() {
		return "OrderItem [orderItemsId=" + orderItemsId + ", orderId=" + orderId + ", productId=" + productId
				+ ", productName=" + productName + ", quantity=" + quantity + ", unitPrice=" + unitPrice
				+ ", totalItemPrice=" + totalItemPrice + "]";
	}

	

}
