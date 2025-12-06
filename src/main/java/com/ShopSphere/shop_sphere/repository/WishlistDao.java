
package com.ShopSphere.shop_sphere.repository;

import java.util.List;
import java.util.Map;

import com.ShopSphere.shop_sphere.model.Wishlist;

public interface WishlistDao {

    int createWishlist(int userId);

    Wishlist findById(int wishlistId);

    Wishlist findByUserId(int userId);

    List<Wishlist> getAllWishlists();

    int deleteWishlist(int wishlistId);

    boolean wishlistExistsForUser(int userId);

    boolean isWishlistEmpty(int wishlistId);
    
    List<Map<String, Object>> getWishlistItems(int userId);
}
