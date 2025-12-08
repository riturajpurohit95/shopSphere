package com.ShopSphere.shop_sphere.model;

public class Cart {
	
	public Cart(int cartId, int userId) {
		super();
		this.cartId = cartId;
		this.userId = userId;
	}

	private int cartId;
	private int userId;
	
	public Cart() {}

	public int getCartId() {
		return cartId;
	}

	public int getUserId() {
		return userId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "Cart [cartId=" + cartId + ", userId=" + userId + "]";
	}

}
