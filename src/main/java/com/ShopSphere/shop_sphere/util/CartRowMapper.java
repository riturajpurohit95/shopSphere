package com.ShopSphere.shop_sphere.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ShopSphere.shop_sphere.model.Cart;

public class CartRowMapper implements RowMapper<Cart>{
	
	@Override
	public Cart mapRow(ResultSet rs, int rowNum)throws SQLException{
		Cart c = new Cart();
		c.setCartId(rs.getInt("cart_id"));
		c.setUserId(rs.getInt("user_id"));	
		return c;
	}

}