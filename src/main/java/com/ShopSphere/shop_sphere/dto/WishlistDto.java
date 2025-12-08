
package com.ShopSphere.shop_sphere.dto;

import jakarta.validation.constraints.NotNull;

public class WishlistDto {

    public WishlistDto(
            Integer wishlistId,
            @NotNull(message = "User ID is required") Integer userId
    ) {
        this.wishlistId = wishlistId;
        this.userId = userId;
    }

    public WishlistDto() {}

    private Integer wishlistId;

    @NotNull(message = "User ID is required")
    private Integer userId;

    // Getters
    public Integer getWishlistId() {
        return wishlistId;
    }

    public Integer getUserId() {
        return userId;
    }

    // Setters
    public void setWishlistId(Integer wishlistId) {
        this.wishlistId = wishlistId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "WishlistDto [wishlistId=" + wishlistId + ", userId=" + userId + "]";
    }
}
