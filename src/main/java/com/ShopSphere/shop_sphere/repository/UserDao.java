package com.ShopSphere.shop_sphere.repository;

import com.ShopSphere.shop_sphere.model.User;

import java.util.List;
import java.util.Map;

public interface UserDao {

    // CRUD Operations
    int save(User user);

    User findById(int userId);

    User findByEmail(String email);

    List<User> findAll();

    int update(User user);

    int delete(int userId);
    int getLocationIdOfUser(int userId);

    // Custom / Utility Methods
    Map<String, Object> getUserWithLocation(int userId);
}
