
package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;

import com.ShopSphere.shop_sphere.model.Review;

public interface ReviewService {

    /**
     * Persists a review and returns its generated ID (from DAO).
     */
    int saveReview(Review review);

    /**
     * Retrieves all reviews for a given product.
     */
    List<Review> getReviewsByProduct(int productId);

    /**
     * Retrieves all reviews created by a given user.
     */
    List<Review> getReviewsByUser(int userId);

    /**
     * Updates the status enum (VISIBLE/HIDDEN/REPORTED) of a review by ID.
     * Returns the number of rows affected.
     */
    int updateReviewStatus(int reviewId, String status);
    
    List<Map<String, Object>> getReviewsByProductId(int productId);

}
