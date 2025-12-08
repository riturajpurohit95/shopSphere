package com.ShopSphere.shop_sphere.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ShopSphere.shop_sphere.model.Location;

public class LocationRowMapper implements RowMapper<Location>{
	
	@Override
	public Location mapRow(ResultSet rs, int rowNum)throws SQLException{
		Location c = new Location();
		c.setLocationId(rs.getInt("location_id"));
		c.setCity(rs.getString("city"));
		c.setHubValue(rs.getInt("hub_value"));	
		return c;
	}

}
