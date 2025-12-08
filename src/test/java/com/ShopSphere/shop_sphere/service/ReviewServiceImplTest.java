package com.ShopSphere.shop_sphere.service;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Review;
import com.ShopSphere.shop_sphere.repository.ReviewDao;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewDao reviewDao;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private Review review2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        review = new Review(); // using setters to avoid depending on constructor signature
        review.setUserId(10);
        review.setProductId(20);
        review.setRating(4);
        review.setReviewText("Nice product");
        review.setStatus(null); // expect default "VISIBLE" set in validate

        review2 = new Review();
        review2.setUserId(11);
        review2.setProductId(21);
        review2.setRating(5);
        review2.setReviewText("Great!");
        review2.setStatus("HIDDEN");
    }

    @Test
    void testSaveReview_Success() {
        when(reviewDao.hasUserPurchasedProduct(10, 20)).thenReturn(true);
        when(reviewDao.save(any(Review.class))).thenReturn(42);

        int id = reviewService.saveReview(review);

        assertEquals(42, id);
        verify(reviewDao, times(1)).hasUserPurchasedProduct(10, 20);
        verify(reviewDao, times(1)).save(any(Review.class));
    }

    @Test
    void testSaveReview_DaoFailure_ThrowsRuntime() {
        when(reviewDao.hasUserPurchasedProduct(10, 20)).thenReturn(true);
        when(reviewDao.save(any(Review.class))).thenReturn(0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reviewService.saveReview(review));
        assertTrue(ex.getMessage().contains("Failed to save review"));
        verify(reviewDao, times(1)).save(any(Review.class));
    }

    @Test
    void testSaveReview_NullReview_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.saveReview(null));
    }

    @Test
    void testSaveReview_UserNotPurchased_ThrowsIllegalArgument() {
        when(reviewDao.hasUserPurchasedProduct(10, 20)).thenReturn(false);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> reviewService.saveReview(review));
        assertTrue(ex.getMessage().contains("You can only review products"));
        verify(reviewDao, times(1)).hasUserPurchasedProduct(10, 20);
        verify(reviewDao, never()).save(any());
    }

    @Test
    void testSaveReview_DefaultStatusSetBeforeSave() {
        // capture the Review passed to DAO to check that status was normalized to VISIBLE
        when(reviewDao.hasUserPurchasedProduct(10, 20)).thenReturn(true);
        when(reviewDao.save(any(Review.class))).thenReturn(100);

        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        int id = reviewService.saveReview(review);

        assertEquals(100, id);
        verify(reviewDao).save(captor.capture());
        Review savedArg = captor.getValue();
        assertNotNull(savedArg.getStatus());
        assertEquals("VISIBLE", savedArg.getStatus());
    }

    @Test
    void testSaveReview_InvalidRating_ThrowsIllegalArgument() {
        review.setRating(0); // invalid rating
        when(reviewDao.hasUserPurchasedProduct(10, 20)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> reviewService.saveReview(review));
        verify(reviewDao, never()).save(any());
    }

    @Test
    void testGetReviewsByProduct_Valid() {
        when(reviewDao.findByProduct(20)).thenReturn(Arrays.asList(review, review2));
        List<Review> reviews = reviewService.getReviewsByProduct(20);
        assertEquals(2, reviews.size());
        verify(reviewDao, times(1)).findByProduct(20);
    }

    @Test
    void testGetReviewsByProduct_InvalidProductId() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.getReviewsByProduct(0));
        verify(reviewDao, never()).findByProduct(anyInt());
    }

    @Test
    void testGetReviewsByUser_Valid() {
        when(reviewDao.findByUser(10)).thenReturn(Arrays.asList(review));
        List<Review> reviews = reviewService.getReviewsByUser(10);
        assertEquals(1, reviews.size());
        verify(reviewDao, times(1)).findByUser(10);
    }

    @Test
    void testGetReviewsByUser_InvalidUserId() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.getReviewsByUser(-5));
        verify(reviewDao, never()).findByUser(anyInt());
    }

    @Test
    void testUpdateReviewStatus_Success() {
        when(reviewDao.updateStatus(5, "HIDDEN")).thenReturn(1);
        int rows = reviewService.updateReviewStatus(5, "HIDDEN");
        assertEquals(1, rows);
        verify(reviewDao, times(1)).updateStatus(5, "HIDDEN");
    }

    @Test
    void testUpdateReviewStatus_InvalidId() {
        assertThrows(IllegalArgumentException.class, () -> reviewService.updateReviewStatus(0, "VISIBLE"));
        verify(reviewDao, never()).updateStatus(anyInt(), anyString());
    }

    @Test
    void testUpdateReviewStatus_NotFound_ThrowsResourceNotFound() {
        when(reviewDao.updateStatus(99, "VISIBLE")).thenReturn(0);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> reviewService.updateReviewStatus(99, "VISIBLE"));
        assertTrue(ex.getMessage().contains("No review found to update"));
        verify(reviewDao, times(1)).updateStatus(99, "VISIBLE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"VISIBLE", "hidden", " RePorTed "})
    void testValidateStatus_AcceptsValidStatuses(String status) {
        // We test via updateReviewStatus (which calls validateStatus) using a positive id and stubbing dao
        when(reviewDao.updateStatus(1, status.trim().toUpperCase())).thenReturn(1);
        // Because updateStatus is called with normalized status in service, we check it returns 1
        // Note: Service normalizes status input; here we simply ensure no exception is thrown
        when(reviewDao.updateStatus(1, "VISIBLE")).thenReturn(1);
        // Call using a valid reviewId and one of the provided statuses. We won't assert dao argument normalization here strictly,
        // but ensure that invalid values will be rejected by other tests.
        assertDoesNotThrow(() -> reviewService.updateReviewStatus(1, status));
    }
}