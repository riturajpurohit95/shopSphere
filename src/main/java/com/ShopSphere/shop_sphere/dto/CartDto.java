package com.ShopSphere.shop_sphere.dto;

import jakarta.validation.constraints.NotNull;

public class CartDto {
	
	public CartDto(Integer cartId, @NotNull(message = "User ID is required") Integer userId, Double totalAmount
			/*boolean empty*/) {
		super();
		this.cartId = cartId;
		this.userId = userId;
		this.totalAmount = totalAmount;
		//this.empty = empty;
	}
	
	public CartDto(){}
	
	private Integer cartId;
	@NotNull(message = "User ID is required")
	private Integer userId;
//	private List<CartItemDto> items;
	private Double totalAmount;
	
	//private boolean empty;
	public Integer getCartId() {
		return cartId;
	}
	public Integer getUserId() {
		return userId;
	}
//	public List<CartItemDto> getItems() {
//		return items;
//	}
	public Double getTotalAmount() {
		return totalAmount;
	}
	/*public boolean isEmpty() {
		return empty;
	}*/
	public void setCartId(Integer cartId) {
		this.cartId = cartId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
//	public void setItems(List<CartItemDto> items) {
//		this.items = items;
//	}
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	/*public void setEmpty(boolean empty) {
		this.empty = empty;
	}*/
	
	
}
