package com.ShopSphere.shop_sphere.model;

public class CartItem {
	
	public CartItem(int cartItemsId, int cartId, int productId, int quantity) {
		this.cartItemsId = cartItemsId;
		this.cartId = cartId;
		this.productId = productId;
		this.quantity = quantity;
	}

	private int cartItemsId;
	private int cartId;
	private int productId;
	private int quantity;
	
	public CartItem() {}

	public int getCartItemsId() {
		return cartItemsId;
	}

	public int getCartId() {
		return cartId;
	}

	public int getProductId() {
		return productId;
	}
	public int getQuantity() {
		return quantity;
	}


	public void setCartItemsId(int cartItemsId) {
		this.cartItemsId = cartItemsId;
	}

	public void setCartId(int cartId) {
		this.cartId = cartId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}
	

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "CartItem [cartItemsId=" + cartItemsId + ", cartId=" + cartId + ", productId=" + productId + "]";
	}

}
