package com.ShopSphere.shop_sphere.dto;

import java.util.List;

public class OrderRequest {
    private int userId;
    private String shippingAddress;
    private String paymentMethod;
    private List<OrderItemDto> items;
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getShippingAddress() {
		return shippingAddress;
	}
	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public List<OrderItemDto> getItems() {
		return items;
	}
	public void setItems(List<OrderItemDto> items) {
		this.items = items;
	}


}

