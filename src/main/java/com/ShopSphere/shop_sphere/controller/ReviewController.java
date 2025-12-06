package com.ShopSphere.shop_sphere.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.ReviewDto;
import com.ShopSphere.shop_sphere.model.Review;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.ReviewService;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private Review dtoToEntity(ReviewDto dto) {
        Review r = new Review();
//        r.setReviewId(dto.getReviewId());
        r.setUserId(dto.getUserId());
        r.setProductId(dto.getProductId());
        r.setRating(dto.getRating());
        r.setReviewText(dto.getReviewText());
        r.setStatus("VISIBLE"); // Safe default
        return r;
    }

    private ReviewDto entityToDto(Review r) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(r.getReviewId());
        dto.setUserId(r.getUserId());
        dto.setProductId(r.getProductId());
        dto.setRating(r.getRating());
        dto.setReviewText(r.getReviewText());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    private void validateAdmin(HttpServletRequest request) {
        if (!SecurityUtil.isAdmin(request)) {
            throw new SecurityException("Unauthorized: Admin access required");
        }
    }

    private void validateUser(HttpServletRequest request, int userId) {
        int loggedUserId = SecurityUtil.getLoggedInUserId(request);
        if (!SecurityUtil.isAdmin(request) && loggedUserId != userId) {
            throw new SecurityException("Unauthorized: Cannot access another user's data");
        }
    }

    @AllowedRoles({"BUYER"})
    @PostMapping
    public ResponseEntity<?> saveReview(@RequestBody ReviewDto dto, HttpServletRequest request) {
        validateUser(request, dto.getUserId());

        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5)
            throw new IllegalArgumentException("Rating must be between 1 and 5");

        if (dto.getReviewText() == null || dto.getReviewText().isBlank())
            throw new IllegalArgumentException("Review text cannot be empty");

        int reviewId = reviewService.saveReview(dtoToEntity(dto));

        return ResponseEntity.ok(Map.of("reviewId", reviewId));
    }


    @AllowedRoles({"BUYER", "ADMIN", "SELLER"})
    @GetMapping("/product/{productId}")
    public List<ReviewDto> getReviewsByProduct(@PathVariable int productId) {
        return reviewService.getReviewsByProduct(productId)
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @AllowedRoles({"BUYER", "ADMIN", "SELLER"})
    @GetMapping("/user/{userId}")
    public List<ReviewDto> getReviewsByUser(@PathVariable int userId, HttpServletRequest request) {
        validateUser(request, userId);
        return reviewService.getReviewsByUser(userId)
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @AllowedRoles({"ADMIN"})
    @PatchMapping("/{reviewId}/status/{status}")
    public String updateStatus(@PathVariable int reviewId, @PathVariable String status, HttpServletRequest request) {
        validateAdmin(request);
        int rows = reviewService.updateReviewStatus(reviewId, status);
        return rows > 0 ? "Review status updated" : "No changes";
    }

    @AllowedRoles({"BUYER", "ADMIN", "SELLER"})
    @GetMapping("/productReview/{productId}")
    public ResponseEntity<?> getReviewsForProduct(@PathVariable int productId) {
        List<Map<String, Object>> reviews = reviewService.getReviewsByProductId(productId);

        if (reviews.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No reviews found for productId: " + productId);
        }

        return ResponseEntity.ok(reviews);
    }
}
