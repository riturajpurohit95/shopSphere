package com.ShopSphere.shop_sphere.repository;

import com.ShopSphere.shop_sphere.model.Wishlist;
import com.ShopSphere.shop_sphere.util.WishlistRowMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class WishlistDaoImpl implements WishlistDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public WishlistDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // SQL Queries
    private static final String INSERT_SQL = "INSERT INTO wishlists (user_id) VALUES (?)";
    private static final String SELECT_BY_USER_ID_SQL = "SELECT wishlist_id, user_id FROM wishlists WHERE user_id = ?";
    private static final String SELECT_BY_ID_SQL = "SELECT wishlist_id, user_id FROM wishlists WHERE wishlist_id = ?";
    private static final String SELECT_ALL_SQL = "SELECT wishlist_id, user_id FROM wishlists ORDER BY wishlist_id";
    private static final String DELETE_SQL = "DELETE FROM wishlists WHERE wishlist_id = ?";
    private static final String EXISTS_BY_USER_SQL = "SELECT COUNT(*) FROM wishlists WHERE user_id = ?";
    private static final String COUNT_ITEMS_SQL = "SELECT COUNT(*) FROM wishlist_items WHERE wishlist_id = ?";
    private static final String SELECT_ITEMS_BY_USER =
            "SELECT wi.wishlist_items_id, p.product_id, p.product_name, p.product_price " +
            "FROM wishlist_items wi " +
            "JOIN products p ON wi.product_id = p.product_id " +
            "JOIN wishlists w ON wi.wishlist_id = w.wishlist_id " +
            "WHERE w.user_id = ?";

    @Override
    public int createWishlist(int userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) return keyHolder.getKey().intValue();
        } catch (DuplicateKeyException e) {
            Wishlist existing = findByUserId(userId);
            if (existing != null) return existing.getWishlistId();
        }
        Wishlist fallback = findByUserId(userId);
        return fallback != null ? fallback.getWishlistId() : 0;
    }

    @Override
    public Wishlist findByUserId(int userId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_BY_USER_ID_SQL, new WishlistRowMapper(), userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Wishlist findById(int wishlistId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_BY_ID_SQL, new WishlistRowMapper(), wishlistId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Wishlist> getAllWishlists() {
        return jdbcTemplate.query(SELECT_ALL_SQL, new WishlistRowMapper());
    }

    @Override
    public int deleteWishlist(int wishlistId) {
        return jdbcTemplate.update(DELETE_SQL, wishlistId);
    }

    @Override
    public boolean wishlistExistsForUser(int userId) {
        Integer count = jdbcTemplate.queryForObject(EXISTS_BY_USER_SQL, Integer.class, userId);
        return count != null && count > 0;
    }

    @Override
    public boolean isWishlistEmpty(int wishlistId) {
        Integer count = jdbcTemplate.queryForObject(COUNT_ITEMS_SQL, Integer.class, wishlistId);
        return count == null || count == 0;
    }

    @Override
    public List<Map<String, Object>> getWishlistItems(int userId) {
        return jdbcTemplate.queryForList(SELECT_ITEMS_BY_USER, userId);
    }
}
