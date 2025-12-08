package com.ShopSphere.shop_sphere.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ShopSphere.shop_sphere.model.Category;

public class CategoryRowMapper implements RowMapper<Category>{
	
	@Override
	public Category mapRow(ResultSet rs, int rowNum)throws SQLException{
		Category c = new Category();
		c.setCategoryId(rs.getInt("category_id"));
		c.setCategoryName(rs.getString("category_name"));	
		return c;
	}

}
