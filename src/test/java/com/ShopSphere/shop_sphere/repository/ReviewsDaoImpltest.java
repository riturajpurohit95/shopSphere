package com.ShopSphere.shop_sphere.repository;

//import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.params.ParameterizedTest;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.ShopSphere.shop_sphere.model.Review;
import com.ShopSphere.shop_sphere.util.ReviewRowMapper;

public class ReviewsDaoImpltest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReviewDaoImpl reviewDao;

    private Review review;
    private Review review2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        review = new Review();
        review.setUserId(10);
        review.setProductId(20);
        review.setRating(4);
        review.setReviewText("Nice");

        review2 = new Review();
        review2.setUserId(11);
        review2.setProductId(20);
        review2.setRating(5);
        review2.setReviewText("Great");
    }

    @Test
    void testSave_ShouldReturnGeneratedKey() {
        // simulate jdbcTemplate.update(PreparedStatementCreator, KeyHolder) filling the keyHolder
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            // Simulate generated key
            Map<String, Object> keyMap = new HashMap<>();
            keyMap.put("GENERATED_KEY", 123);
            // depending on KeyHolder implementation, adding to keyList is acceptable
            kh.getKeyList().add(keyMap);
            return 1;
        }).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class));

        int id = reviewDao.save(review);
        assertTrue(id > 0);
    }

    @Test
    void testFindByProduct_Found() {
        when(jdbcTemplate.query(anyString(), any(ReviewRowMapper.class), eq(20)))
            .thenReturn(Arrays.asList(review, review2));

        List<Review> list = reviewDao.findByProduct(20);
        assertEquals(2, list.size());
        assertEquals(20, list.get(0).getProductId());
    }

    @Test
    void testFindByProduct_Empty() {
        when(jdbcTemplate.query(anyString(), any(ReviewRowMapper.class), eq(99)))
            .thenReturn(Collections.emptyList());

        List<Review> list = reviewDao.findByProduct(99);
        assertTrue(list.isEmpty());
    }

    @Test
    void testFindByUser_Found() {
        when(jdbcTemplate.query(anyString(), any(ReviewRowMapper.class), eq(10)))
            .thenReturn(Arrays.asList(review));

        List<Review> list = reviewDao.findByUser(10);
        assertEquals(1, list.size());
        assertEquals(10, list.get(0).getUserId());
    }

    @Test
    void testUpdateStatus() {
        when(jdbcTemplate.update(anyString(), anyString(), anyInt())).thenReturn(1);
        int rows = reviewDao.updateStatus(5, "HIDDEN");
        assertEquals(1, rows);
    }

    @Test
    void testHasUserPurchasedProduct_ReturnsTrue() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(10), eq(20))).thenReturn(2);
        boolean has = reviewDao.hasUserPurchasedProduct(10, 20);
        assertTrue(has);
    }

    @Test
    void testHasUserPurchasedProduct_ReturnsFalse() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(10), eq(99))).thenReturn(0);
        boolean has = reviewDao.hasUserPurchasedProduct(10, 99);
        assertFalse(has);
    }

    @Test
    void testGetReviewsByProductId() {
        Map<String, Object> row = new HashMap<>();
        row.put("review_id", 1);
        row.put("rating", 5);
        row.put("review_text", "Nice");
        row.put("reviewer_name", "Alice");
        when(jdbcTemplate.queryForList(anyString(), eq(20))).thenReturn(Arrays.asList(row));

        List<Map<String, Object>> result = reviewDao.getReviewsByProductId(20);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).get("reviewer_name"));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = { "Good", "Bad", "Neutral" })
    void testParameterizedReviewText(String txt) {
        Review r = new Review();
        r.setReviewText(txt);
        assertNotNull(r.getReviewText());
        assertTrue(r.getReviewText().length() > 0);
    }

    @Disabled("Example of disabled test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled");
    }
}