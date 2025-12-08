package com.ShopSphere.shop_sphere.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ShopSphere.shop_sphere.model.WishlistItem;

public class WishlistItemRowMapper implements RowMapper<WishlistItem> {

    @Override
    public WishlistItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        WishlistItem item = new WishlistItem();
        item.setWishlistItemsId(rs.getInt("wishlist_items_id"));
        item.setWishlistId(rs.getInt("wishlist_id"));
        item.setProductId(rs.getInt("product_id"));
        return item;
    }
}