package com.ShopSphere.shop_sphere.repository;

import com.ShopSphere.shop_sphere.model.Review;
import com.ShopSphere.shop_sphere.util.ReviewRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class ReviewDaoImpl implements ReviewDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
            "INSERT INTO reviews (user_id, product_id, rating, review_text, status) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_BY_PRODUCT_SQL =
            "SELECT review_id, user_id, product_id, rating, review_text, status, created_at " +
            "FROM reviews WHERE product_id = ?";

    private static final String SELECT_BY_USER_SQL =
            "SELECT review_id, user_id, product_id, rating, review_text, status, created_at " +
            "FROM reviews WHERE user_id = ?";

    private static final String UPDATE_STATUS_SQL =
            "UPDATE reviews SET status = ? WHERE review_id = ?";

    @Override
    public int save(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, review.getUserId());
            ps.setInt(2, review.getProductId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getReviewText());
            ps.setString(5, review.getStatus());
            return ps;
        }, keyHolder);

        return (keyHolder.getKey() != null) ? keyHolder.getKey().intValue()  : 0;
    }

    @Override
    public List<Review> findByProduct(int productId) {
        return jdbcTemplate.query(SELECT_BY_PRODUCT_SQL, new ReviewRowMapper(), productId);
    }

    @Override
    public List<Review> findByUser(int userId) {
        return jdbcTemplate.query(SELECT_BY_USER_SQL, new ReviewRowMapper(), userId);
    }

    @Override
    public int updateStatus(int reviewId, String status) {
        return jdbcTemplate.update(UPDATE_STATUS_SQL, status, reviewId);
    }
    
    @Override
    public boolean hasUserPurchasedProduct(int userId, int productId) {
        String sql = """
            SELECT COUNT(*) 
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.order_id
        WHERE o.user_id = ?
          AND oi.product_id = ?
          AND o.orderStatus IN ('SHIPPED', 'DELIVERED')
        """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productId);
        return count != null && count > 0;
    }

    
    @Override
    public List<Map<String, Object>> getReviewsByProductId(int productId) {
        String sql = "SELECT r.review_id, r.rating, r.review_text, u.name AS reviewer_name " +
                     "FROM reviews r " +
                     "INNER JOIN user u ON r.user_id = u.user_id " +
                     "WHERE r.product_id = ?";

        return jdbcTemplate.queryForList(sql, productId);
    }
}
