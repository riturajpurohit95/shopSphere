package com.ShopSphere.shop_sphere.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.RowMapper;

import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.Payment;

public class PaymentRowMapper implements RowMapper<Payment> {
	@Override
	public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Payment p = new Payment();
		p.setPaymentId(rs.getInt("payment_id"));
		p.setOrderId(rs.getInt("order_id"));
		p.setUserId(rs.getInt("user_id"));
		p.setAmount(rs.getBigDecimal("amount"));
		p.setCurrency(rs.getString("currency"));
		p.setPaymentMethod(rs.getString("payment_method"));
		p.setStatus(rs.getString("status"));
		Timestamp created = rs.getTimestamp("created_at");
            if(created != null) {
			LocalDateTime createdAt = created.toLocalDateTime();
			p.setCreatedAt(createdAt);
            }
		
		return p;
	}
	

}
