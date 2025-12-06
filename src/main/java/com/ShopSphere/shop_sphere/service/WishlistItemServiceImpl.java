package com.ShopSphere.shop_sphere.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ShopSphere.shop_sphere.exception.BadRequestException;
import com.ShopSphere.shop_sphere.exception.DuplicateResourceException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Wishlist;
import com.ShopSphere.shop_sphere.model.WishlistItem;
import com.ShopSphere.shop_sphere.repository.WishlistDao;
import com.ShopSphere.shop_sphere.repository.WishlistItemDao;

@Service
public class WishlistItemServiceImpl implements WishlistItemService {

    private final WishlistItemDao wishlistItemDao;
    private final WishlistDao wishlistDao;

    public WishlistItemServiceImpl(WishlistItemDao wishlistItemDao, WishlistDao wishlistDao) {
        this.wishlistItemDao = wishlistItemDao;
        this.wishlistDao = wishlistDao;
    }

    @Override
    public int addItemToWishlist(WishlistItem wishlistItem) {
        // Validate input
        if (wishlistItem == null) {
            throw new BadRequestException("WishlistItem cannot be null");
        }
        if (wishlistItem.getWishlistId() <= 0) {
            throw new BadRequestException("Invalid wishlistId: " + wishlistItem.getWishlistId());
        }
        if (wishlistItem.getProductId() <= 0) {
            throw new BadRequestException("Invalid productId: " + wishlistItem.getProductId());
        }

        // Validate wishlist existence
        Wishlist wishlist = wishlistDao.findById(wishlistItem.getWishlistId());
        if (wishlist == null) {
            throw new ResourceNotFoundException(
                "Wishlist not found for wishlistId: " + wishlistItem.getWishlistId()
            );
        }

        // Add item (or get existing ID if already present)
        int id = wishlistItemDao.addItem(wishlistItem);

        if (id <= 0) {
            throw new BadRequestException(
                "Product already exists in wishlist: productId=" + wishlistItem.getProductId()
            );
        }

        return id;
    }

    @Override
    public List<WishlistItem> getItemsByWishlistId(int wishlistId) {
        if (wishlistId <= 0) {
            throw new BadRequestException("Invalid wishlistId: " + wishlistId);
        }

        List<WishlistItem> items = wishlistItemDao.findByWishlistId(wishlistId);
        if (items == null || items.isEmpty()) {
            throw new ResourceNotFoundException("No wishlist items found for wishlistId: " + wishlistId);
        }

        return items;
    }

    @Override
    public int deleteItem(int wishlistItemId) {
        if (wishlistItemId <= 0) {
            throw new BadRequestException("Invalid wishlistItemId: " + wishlistItemId);
        }

        int rows = wishlistItemDao.deleteItem(wishlistItemId);
        if (rows <= 0) {
            throw new ResourceNotFoundException(
                "No wishlist item found with id: " + wishlistItemId
            );
        }

        return rows;
    }

    @Override
    public int getWishlistIdByItem(int wishlistItemId) {
        return wishlistItemDao.getWishlistIdByItem(wishlistItemId);
    }

    @Override
    public int getWishlistOwnerId(int wishlistId) {
        return wishlistItemDao.getWishlistOwnerId(wishlistId);
    }
}
