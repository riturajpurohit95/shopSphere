package com.ShopSphere.shop_sphere.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import com.ShopSphere.shop_sphere.model.Order;

public class SellerOrderRowMapper implements RowMapper<Order> {

    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setShippingAddress(rs.getString("shipping_address"));
        o.setOrderStatus(rs.getString("orderStatus"));
        o.setPaymentMethod(rs.getString("payment_method"));

        Timestamp placed = rs.getTimestamp("placed_at");
        if (placed != null) {
            o.setPlacedAt(placed.toLocalDateTime());
        }

        return o;
    }
}
