package com.ShopSphere.shop_sphere.repository;

import java.util.List;

import com.ShopSphere.shop_sphere.model.WishlistItem;

public interface WishlistItemDao {
	
	int addItem(WishlistItem wishlistItem);
	List<WishlistItem> findByWishlistId(int wishlistId);
	int deleteItem(int wishlistItemId);
	int getWishlistIdByItem(int wishlistItemId);
	int getWishlistOwnerId(int wishlistId);
}
