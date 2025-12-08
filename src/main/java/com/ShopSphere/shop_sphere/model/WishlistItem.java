package com.ShopSphere.shop_sphere.model;

public class WishlistItem {
	
	public WishlistItem(int wishlistItemsId, int wishlistId, int productId) {
		this.wishlistItemsId = wishlistItemsId;
		this.wishlistId = wishlistId;
		this.productId = productId;
	}

	private int wishlistItemsId;
	private int wishlistId;
	private int productId;
	
	public WishlistItem() {}

	public int getWishlistItemsId() {
		return wishlistItemsId;
	}

	public int getWishlistId() {
		return wishlistId;
	}

	public int getProductId() {
		return productId;
	}

	public void setWishlistItemsId(int wishlistItemsId) {
		this.wishlistItemsId = wishlistItemsId;
	}

	public void setWishlistId(int wishlistId) {
		this.wishlistId = wishlistId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	@Override
	public String toString() {
		return "WishlistItem [wishlistItemsId=" + wishlistItemsId + ", wishlistId=" + wishlistId + ", productId="
				+ productId + "]";
	}
	
	

}
