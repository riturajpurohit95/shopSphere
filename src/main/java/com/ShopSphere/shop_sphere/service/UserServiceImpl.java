package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.repository.UserDao;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public int createUser(User user) {
        validateForCreate(user);

        if (userExistsByEmail(user.getEmail())) {
            throw new RuntimeException("User with email already exists: " + user.getEmail());
        }

        int id = userDao.save(user);
        if (id <= 0) {
            throw new RuntimeException("Failed to create user with email: " + user.getEmail());
        }
        return id;
    }

    @Override
    public User getUserById(int userId) {
        if (userId <= 0) throw new IllegalArgumentException("userId must be positive");
        User user = userDao.findById(userId);
        if (user == null) throw new ResourceNotFoundException("No user found for userId: " + userId);
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        validateEmail(email);
        User user = userDao.findByEmail(email);
        if (user == null) throw new ResourceNotFoundException("No user found for email: " + email);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public int updateUser(User user) {
        validateForUpdate(user);

        User existing = userDao.findById(user.getUserId());
        if (existing == null) throw new ResourceNotFoundException("Cannot update; user not found for userId: " + user.getUserId());

        // Role and email cannot be updated
        user.setRole(existing.getRole());
        user.setEmail(existing.getEmail());

        int rows = userDao.update(user);
        if (rows <= 0) throw new RuntimeException("Update failed for userId: " + user.getUserId());
        return rows;
    }

    @Override
    public void deleteUser(int userId) {
        if (userId <= 0) throw new IllegalArgumentException("userId must be positive");
        int rows = userDao.delete(userId);
        if (rows <= 0) throw new ResourceNotFoundException("Delete failed; user not found for userId: " + userId);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        if (!StringUtils.hasText(email)) return false;
        return userDao.findByEmail(email) != null;
    }
    
    @Override
    public int getLocationIdOfUser(int userId) {
        return userDao.getLocationIdOfUser(userId);
    }

    @Override
    public Map<String, Object> getUserWithLocation(int userId) {
        return userDao.getUserWithLocation(userId);
    }

    // ---------------- Validation ----------------

    private void validateForCreate(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (!StringUtils.hasText(user.getName())) throw new IllegalArgumentException("name cannot be empty");
        validateEmail(user.getEmail());
        if (!StringUtils.hasText(user.getPassword())) throw new IllegalArgumentException("password cannot be empty");
        validateRole(user.getRole());
        // Phone and locationId are optional
    }

    private void validateForUpdate(User user) {
        if (user == null) throw new IllegalArgumentException("User cannot be null");
        if (user.getUserId() <= 0) throw new IllegalArgumentException("userId must be positive");
        if (!StringUtils.hasText(user.getName())) throw new IllegalArgumentException("name cannot be empty");
        if (!StringUtils.hasText(user.getPassword())) throw new IllegalArgumentException("password cannot be empty");
        // Email and role are immutable
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) throw new IllegalArgumentException("email cannot be empty");
        if (!EMAIL_REGEX.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email format: " + email);
    }

    private void validateRole(String role) {
        if (!StringUtils.hasText(role)) throw new IllegalArgumentException("role cannot be empty");
        String normalized = role.trim();
        if (!("Admin".equalsIgnoreCase(normalized) || "Seller".equalsIgnoreCase(normalized) || "Buyer".equalsIgnoreCase(normalized))) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Allowed: Admin, Seller, Buyer");
        }
    }
}
