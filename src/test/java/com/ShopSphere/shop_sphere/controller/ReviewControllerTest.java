package com.ShopSphere.shop_sphere.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ShopSphere.shop_sphere.dto.ReviewDto;
import com.ShopSphere.shop_sphere.model.Review;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.ReviewService;

public class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Review sampleReview;
    private ReviewDto sampleDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();

        sampleReview = new Review();
        sampleReview.setReviewId(1);
        sampleReview.setUserId(10);
        sampleReview.setProductId(20);
        sampleReview.setRating(4);
        sampleReview.setReviewText("Nice product");
        sampleReview.setStatus("VISIBLE");

        sampleDto = new ReviewDto();
        sampleDto.setUserId(10);
        sampleDto.setProductId(20);
        sampleDto.setRating(4);
        sampleDto.setReviewText("Nice product");
        // status left null -> controller sets VISIBLE
    }

    @Test
    void testSaveReview_Success() throws Exception {
        // Security: logged-in user equals dto.userId, not admin
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(reviewService.saveReview(any(Review.class))).thenReturn(42);

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reviewId").value(42));

            verify(reviewService, times(1)).saveReview(any(Review.class));
        }
    }

    @Test
    void testGetReviewsByProduct_ReturnsList() throws Exception {
        when(reviewService.getReviewsByProduct(20)).thenReturn(Arrays.asList(sampleReview));

        mockMvc.perform(get("/api/reviews/product/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].reviewId").value(1))
                .andExpect(jsonPath("$[0].productId").value(20));

        verify(reviewService, times(1)).getReviewsByProduct(20);
    }

    @Test
    void testGetReviewsByUser_Success_WithValidation() throws Exception {
        // Security: logged-in user equals requested user
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(reviewService.getReviewsByUser(10)).thenReturn(Arrays.asList(sampleReview));

            mockMvc.perform(get("/api/reviews/user/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1))
                    .andExpect(jsonPath("$[0].userId").value(10));

            verify(reviewService, times(1)).getReviewsByUser(10);
        }
    }

    @Test
    void testUpdateStatus_Admin_Succeeds() throws Exception {
        // Security: admin
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);

            when(reviewService.updateReviewStatus(5, "HIDDEN")).thenReturn(1);

            mockMvc.perform(patch("/api/reviews/5/status/HIDDEN"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Review status updated"));

            verify(reviewService, times(1)).updateReviewStatus(5, "HIDDEN");
        }
    }

    @Test
    void testGetReviewsForProduct_NotFound() throws Exception {
        when(reviewService.getReviewsByProductId(100)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/reviews/productReview/100"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No reviews found for productId: 100")));

        verify(reviewService, times(1)).getReviewsByProductId(100);
    }

    @Test
    void testGetReviewsForProduct_Found() throws Exception {
        Map<String, Object> row = Map.of("review_id", 1, "rating", 5, "review_text", "Great", "reviewer_name", "Alice");
        when(reviewService.getReviewsByProductId(20)).thenReturn(Arrays.asList(row));

        mockMvc.perform(get("/api/reviews/productReview/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].reviewer_name").value("Alice"));

        verify(reviewService, times(1)).getReviewsByProductId(20);
    }

    // Parameterized example: simple sanity for allowed product ids
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {10, 20, 30})
    void parameterizedProductIds(int productId) throws Exception {
        when(reviewService.getReviewsByProduct(productId)).thenReturn(List.of());
        mockMvc.perform(get("/api/reviews/product/" + productId))
               .andExpect(status().isOk());
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled");
    }
}