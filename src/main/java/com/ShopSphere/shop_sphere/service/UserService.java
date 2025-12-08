
package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;

import com.ShopSphere.shop_sphere.model.User;

public interface UserService {

    /**
     * Creates a user and returns the generated user_id.
     */
    int createUser(User user);

    /**
     * Retrieves a user by primary key.
     */
    User getUserById(int userId);

    /**
     * Retrieves a user by email (unique or natural key).
     */
    User getUserByEmail(String email);

    /**
     * Returns all users.
     */
    List<User> getAllUsers();

    /**
     * Updates a user record. Returns the number of rows affected.
     */
    int updateUser(User user);

    /**
     * Deletes a user by ID. Throws if nothing was deleted.
     */
    void deleteUser(int userId);

    /**
     * Convenience check to see if an email is already registered.
     */
    boolean userExistsByEmail(String email);
    
    int getLocationIdOfUser(int userId);

    
    Map<String, Object> getUserWithLocation(int userId);
}
