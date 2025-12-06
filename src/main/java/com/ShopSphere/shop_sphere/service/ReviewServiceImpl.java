
package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Review;
import com.ShopSphere.shop_sphere.repository.ReviewDao;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;

    public ReviewServiceImpl(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    @Override
    public int saveReview(Review review) {
        validateReviewForCreate(review);
        // DAO saves and returns generated PK (review_id)
        int id = reviewDao.save(review);
        if (id <= 0) {
            // Defensive: if DAO returns non-positive on failure
            throw new RuntimeException("Failed to save review for productId=" + review.getProductId()
                    + " and userId=" + review.getUserId());
        }
        return id;
    }

    @Override
    public List<Review> getReviewsByProduct(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("productId must be positive");
        }
        List<Review> reviews = reviewDao.findByProduct(productId);
        // It’s okay to return empty list; throw only if your API requires strict presence
        return reviews;
    }

    @Override
    public List<Review> getReviewsByUser(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
        List<Review> reviews = reviewDao.findByUser(userId);
        return reviews;
    }

    @Override
    public int updateReviewStatus(int reviewId, String status) {
        if (reviewId <= 0) {
            throw new IllegalArgumentException("reviewId must be positive");
        }
        validateStatus(status);
        int rows = reviewDao.updateStatus(reviewId, status);
        if (rows <= 0) {
            // When update returns 0, either ID not found or status unchanged
            throw new ResourceNotFoundException("No review found to update for reviewId: " + reviewId);
        }
        return rows;
    }

    // ---------- Helpers / validations ----------

    private void validateReviewForCreate(Review review) {

        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }

        if (review.getUserId() <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }

        if (review.getProductId() <= 0) {
            throw new IllegalArgumentException("productId must be positive");
        }

        // ⭐ NEW IMPORTANT RULE ⭐
        if (!reviewDao.hasUserPurchasedProduct(review.getUserId(), review.getProductId())) {
            throw new IllegalArgumentException(
                "You can only review products that you have purchased and received (SHIPPED or DELIVERED)."
            );
        }


        if (!StringUtils.hasText(review.getReviewText())) {
            throw new IllegalArgumentException("reviewText cannot be empty");
        }

        validateRating(review.getRating());

        // Normalize and verify status
        String status = review.getStatus();
        if (!StringUtils.hasText(status)) {
            review.setStatus("VISIBLE");
        } else {
            validateStatus(status);
        }
    }


    private void validateRating(int rating) {
        // Typical 1..5 rating scale; adjust as needed
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
    }

    private void validateStatus(String status) {
        // Align with enum('VISIBLE','HIDDEN','REPORTED')
        String normalized = status.trim().toUpperCase();
        if (!("VISIBLE".equals(normalized) || "HIDDEN".equals(normalized) || "REPORTED".equals(normalized))) {
            throw new IllegalArgumentException("Invalid status: " + status
                    + ". Allowed values: VISIBLE, HIDDEN, REPORTED");
        }
    }
    
    @Override
    public List<Map<String, Object>> getReviewsByProductId(int productId) {
        return reviewDao.getReviewsByProductId(productId);
    }
}
