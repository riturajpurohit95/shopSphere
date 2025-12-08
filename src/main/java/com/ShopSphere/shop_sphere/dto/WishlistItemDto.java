
package com.ShopSphere.shop_sphere.dto;

import jakarta.validation.constraints.NotNull;

public class WishlistItemDto {

    public WishlistItemDto(
            Integer wishlistItemsId,
            @NotNull(message = "Wishlist ID is required") Integer wishlistId,
            @NotNull(message = "Product ID is required") Integer productId
    ) {
        this.wishlistItemsId = wishlistItemsId;
        this.wishlistId = wishlistId;
        this.productId = productId;
    }

    public WishlistItemDto() {}

    private Integer wishlistItemsId;

    @NotNull(message = "Wishlist ID is required")
    private Integer wishlistId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    // Getters
    public Integer getWishlistItemsId() {
        return wishlistItemsId;
    }

    public Integer getWishlistId() {
        return wishlistId;
    }

    public Integer getProductId() {
        return productId;
    }

    // Setters
    public void setWishlistItemsId(Integer wishlistItemsId) {
        this.wishlistItemsId = wishlistItemsId;
    }

    public void setWishlistId(Integer wishlistId) {
        this.wishlistId = wishlistId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "WishlistItemDto [wishlistItemsId=" + wishlistItemsId
                + ", wishlistId=" + wishlistId
                + ", productId=" + productId + "]";
    }
}
