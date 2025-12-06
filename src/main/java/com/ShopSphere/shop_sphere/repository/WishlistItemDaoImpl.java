package com.ShopSphere.shop_sphere.repository;

import com.ShopSphere.shop_sphere.model.WishlistItem;
import com.ShopSphere.shop_sphere.util.WishlistItemRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class WishlistItemDaoImpl implements WishlistItemDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public WishlistItemDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------- SQL --------
    private static final String INSERT_SQL =
        "INSERT INTO wishlist_items (wishlist_id, product_id) " +
        "VALUES (?, ?) " +
        "ON DUPLICATE KEY UPDATE wishlist_items_id = LAST_INSERT_ID(wishlist_items_id)";

    private static final String SELECT_BY_WISHLIST_SQL =
        "SELECT wishlist_items_id, wishlist_id, product_id " +
        "FROM wishlist_items WHERE wishlist_id = ? ORDER BY wishlist_items_id";

    private static final String DELETE_SQL =
        "DELETE FROM wishlist_items WHERE wishlist_items_id = ?";

    private static final String SELECT_WISHLIST_ID_SQL =
        "SELECT wishlist_id FROM wishlist_items WHERE wishlist_items_id = ?";

    private static final String SELECT_OWNER_SQL =
        "SELECT user_id FROM wishlists WHERE wishlist_id = ?";

    // -------- DAO Methods --------

    @Override
    public int addItem(WishlistItem wishlistItem) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, wishlistItem.getWishlistId());
            ps.setInt(2, wishlistItem.getProductId());
            return ps;
        }, keyHolder);

        int id = keyHolder.getKey() != null ? keyHolder.getKey().intValue() : 0;
        wishlistItem.setWishlistItemsId(id);
        return id;
    }

    @Override
    public List<WishlistItem> findByWishlistId(int wishlistId) {
        return jdbcTemplate.query(SELECT_BY_WISHLIST_SQL, new WishlistItemRowMapper(), wishlistId);
    }

    @Override
    public int deleteItem(int wishlistItemId) {
        return jdbcTemplate.update(DELETE_SQL, wishlistItemId);
    }

    @Override
    public int getWishlistIdByItem(int wishlistItemId) {
        return jdbcTemplate.queryForObject(SELECT_WISHLIST_ID_SQL, Integer.class, wishlistItemId);
    }

    @Override
    public int getWishlistOwnerId(int wishlistId) {
        return jdbcTemplate.queryForObject(SELECT_OWNER_SQL, Integer.class, wishlistId);
    }
}
