
package com.ShopSphere.shop_sphere.service;

import java.util.List;

import com.ShopSphere.shop_sphere.model.WishlistItem;

public interface WishlistItemService {

    /**
     * Adds an item to a wishlist and returns the generated wishlist_items_id.
     */
    int addItemToWishlist(WishlistItem wishlistItem);

    /**
     * Returns all items for a given wishlist ID.
     */
    List<WishlistItem> getItemsByWishlistId(int wishlistId);

    /**
     * Deletes a wishlist item by its primary key.
     * Returns the number of rows affected.
     */
    int deleteItem(int wishlistItemId);
    
    int getWishlistIdByItem(int wishlistItemId);
    
    int getWishlistOwnerId(int wishlistId);
}
