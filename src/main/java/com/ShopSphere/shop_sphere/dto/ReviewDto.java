
package com.ShopSphere.shop_sphere.dto;

import java.sql.Timestamp;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewDto {

    public ReviewDto(
            Integer reviewId,
            @NotNull(message = "User ID is required") Integer userId,
            @NotNull(message = "Product ID is required") Integer productId,
            @NotNull(message = "Rating is required") @Min(value = 1, message = "Rating must be at least 1")
            @Max(value = 5, message = "Rating must be at most 5") Integer rating,
            @NotNull(message = "Review text is required") @Size(min = 1, message = "Review text cannot be empty")
            String reviewText,
            @NotNull(message = "Status is required") String status,
            Timestamp createdAt
    ) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.status = status;
        this.createdAt = createdAt;
    }

    private Integer reviewId;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotNull(message = "Review text is required")
    @Size(min = 1, message = "Review text cannot be empty")
    private String reviewText;

    @NotNull(message = "Status is required")
    private String status; // "VISIBLE" | "HIDDEN" | "REPORTED"

    private Timestamp createdAt;

    public ReviewDto() {}

    public Integer getReviewId() {
        return this.reviewId;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public Integer getProductId() {
        return this.productId;
    }

    public Integer getRating() {
        return this.rating;
    }

    public String getReviewText() {
        return this.reviewText;
    }

    public String getStatus() {
        return this.status;
    }

    public Timestamp getCreatedAt() {
        return this.createdAt;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ReviewDto [reviewId=" + reviewId
                + ", userId=" + userId
                + ", productId=" + productId
                + ", rating=" + rating
                + ", reviewText=" + reviewText
                + ", status=" + status
                + ", createdAt=" + createdAt + "]";
    }
}
