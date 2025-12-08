package com.ShopSphere.shop_sphere.model;

import java.sql.Timestamp;

public class Review {
	
	public Review(int reviewId, int userId, int productId, int rating, String reviewText, String status,
			Timestamp createdAt) {
		this.reviewId = reviewId;
		this.userId = userId;
		this.productId = productId;
		this.rating = rating;
		this.reviewText = reviewText;
		this.status = status;
		this.createdAt = createdAt;
	}

	private int reviewId;
	private int userId;
	private int productId;
	private int rating;
	private String reviewText;
	private String status;
	private Timestamp createdAt;
	
	public Review() {}

	public int getReviewId() {
		return reviewId;
	}

	public int getUserId() {
		return userId;
	}

	public int getProductId() {
		return productId;
	}

	public int getRating() {
		return rating;
	}

	public String getReviewText() {
		return reviewText;
	}

	public String getStatus() {
		return status;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public void setRating(int rating) {
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
		return "Review [reviewId=" + reviewId + ", userId=" + userId + ", productId=" + productId + ", rating=" + rating
				+ ", reviewText=" + reviewText + ", status=" + status + ", createdAt=" + createdAt + "]";
	}

}
