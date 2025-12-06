package com.ShopSphere.shop_sphere.model;

public class Wishlist {
	
	public Wishlist(int wishlistId, int userId) {
		this.wishlistId = wishlistId;
		this.userId = userId;
	}

	private int wishlistId;
	private int userId;
	
	public Wishlist() {}

	public int getWishlistId() {
		return wishlistId;
	}

	public int getUserId() {
		return userId;
	}

	public void setWishlistId(int wishlistId) {
		this.wishlistId = wishlistId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "Wishlist [wishlistId=" + wishlistId + ", userId=" + userId + "]";
	}

}
