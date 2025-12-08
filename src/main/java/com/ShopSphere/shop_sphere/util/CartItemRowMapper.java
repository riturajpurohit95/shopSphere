package com.ShopSphere.shop_sphere.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ShopSphere.shop_sphere.model.CartItem;

public class CartItemRowMapper implements RowMapper<CartItem>{
	
	@Override
	public CartItem mapRow(ResultSet rs, int rowNum)throws SQLException{
		CartItem c = new CartItem();
		c.setCartItemsId(rs.getInt("cart_items_id"));
		c.setCartId(rs.getInt("cart_id"));
		c.setProductId(rs.getInt("product_id"));
		c.setQuantity(rs.getInt("quantity"));	
		
		return c;
	}

}
