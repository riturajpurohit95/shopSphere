package com.ShopSphere.shop_sphere.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ShopSphere.shop_sphere.model.User;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setPhone(rs.getString("phone")); 
        // nullable
        String role = rs.getString("role");
        u.setRole(role != null ? role.toUpperCase():"BUYER");      // ENUM('Admin','Seller','Buyer')

        // location_id may be NULL: read as int and check wasNull
        int locId = rs.getInt("location_id");
        u.setLocationId(rs.wasNull() ? null : locId);

        return u;
    }
}