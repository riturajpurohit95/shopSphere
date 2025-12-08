package com.ShopSphere.shop_sphere.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ShopSphere.shop_sphere.model.Wishlist;

public class WishlistRowMapper implements RowMapper<Wishlist> {

    @Override
    public Wishlist mapRow(ResultSet rs, int rowNum) throws SQLException {
        Wishlist w = new Wishlist();
        w.setWishlistId(rs.getInt("wishlist_id"));
        w.setUserId(rs.getInt("user_id"));
        return w;
    }
}