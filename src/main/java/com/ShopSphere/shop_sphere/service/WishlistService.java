
package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;

import com.ShopSphere.shop_sphere.model.Wishlist;

public interface WishlistService {

    Wishlist createWishlist(int userId);
    Wishlist getWishlistByUserId(int userId);
    Wishlist getWishlistById(int wishlistId);
    List<Wishlist> getAllWishlists();
    void deleteWishlist(int wishlistId);
    boolean wishlistExistsForUser(int userId);
    boolean isWishlistEmpty(int wishlistId);
    
    List<Map<String, Object>> getWishlistItems(int userId);
}
