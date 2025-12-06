/*package com.ShopSphere.shop_sphere.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.UserDto;
import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -------- Mappers --------
    private User dtoToEntity(UserDto dto) {
        User u = new User();
        u.setUserId(dto.getUserId() != null ? dto.getUserId() : 0);
        u.setName(dto.getName());
        u.setEmail(dto.getEmail());
        u.setPhone(dto.getPhone());
        u.setRole(dto.getRole());
        u.setLocationId(dto.getLocationId());
//        u.setPassword(dto.getPassword());
        return u;
    }

    private UserDto entityToDto(User u) {
        UserDto dto = new UserDto();
        dto.setUserId(u.getUserId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone());
        dto.setRole(u.getRole());
        dto.setLocationId(u.getLocationId());
        return dto;
    }

    // -------- Security Helpers --------
    private void validateAdmin(HttpServletRequest request) {
        if (!SecurityUtil.isAdmin(request)) {
            throw new SecurityException("Unauthorized: Admin access required");
        }
    }

    private void validateUserOrAdmin(HttpServletRequest request, int userId) {
        int loggedUserId = SecurityUtil.getLoggedInUserId(request);
        if (!SecurityUtil.isAdmin(request) && loggedUserId != userId) {
            throw new SecurityException("Unauthorized: Cannot access or modify another user's data");
        }
    }

    // -------- Endpoints --------

    @AllowedRoles({"ADMIN"})
    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable int userId, HttpServletRequest request) {
        validateUserOrAdmin(request, userId);
        User u = userService.getUserById(userId);
        return entityToDto(u);
    }

    @AllowedRoles({"ADMIN"})
    @GetMapping("/email/{email}")
    public UserDto getUserByEmail(@PathVariable String email) {
        User u = userService.getUserByEmail(email);
        return entityToDto(u);
    }

    @AllowedRoles({"ADMIN"})
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream().map(this::entityToDto).collect(Collectors.toList());
    }

    @AllowedRoles({"BUYER", "SELLER", "ADMIN"})
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId, @Valid @RequestBody UserDto dto,
                                        HttpServletRequest request) {
        validateUserOrAdmin(request, userId);

        User u = dtoToEntity(dto);
        u.setUserId(userId);

        // Prevent email and role updates
        User existing = userService.getUserById(userId);
        u.setEmail(existing.getEmail());
        u.setRole(existing.getRole());

        int rows = userService.updateUser(u);
        return ResponseEntity.ok(rows > 0 ? "User updated successfully" : "No changes");
    }

    @AllowedRoles({"ADMIN"})
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable int userId) {
        userService.deleteUser(userId);
        return "User deleted successfully";
    }

    @AllowedRoles({"ADMIN"})
    @GetMapping("/exists/{email}")
    public boolean userExists(@PathVariable String email) {
        return userService.userExistsByEmail(email);
    }

    @AllowedRoles({"BUYER", "SELLER", "ADMIN"})
    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable int userId, HttpServletRequest request) {
        validateUserOrAdmin(request, userId);
        try {
            Map<String, Object> userProfile = userService.getUserWithLocation(userId);
            return ResponseEntity.ok(userProfile);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + userId);
        }
    }
}
*/
package com.ShopSphere.shop_sphere.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.UserDto;
import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -------- Mappers --------
    private User dtoToEntity(UserDto dto) {
        User u = new User();
        u.setUserId(dto.getUserId() != null ? dto.getUserId() : 0);
        u.setName(dto.getName());
        u.setEmail(dto.getEmail());
        u.setPhone(dto.getPhone());
        u.setRole(dto.getRole());
        u.setLocationId(dto.getLocationId());
        // Password is intentionally NOT set here for updates
        // u.setPassword(dto.getPassword());
        return u;
    }

    private UserDto entityToDto(User u) {
        UserDto dto = new UserDto();
        dto.setUserId(u.getUserId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getPhone());
        dto.setRole(u.getRole());
        dto.setLocationId(u.getLocationId());
        return dto;
    }

    // -------- Security Helpers --------
    private void validateAdmin(HttpServletRequest request) {
        if (!SecurityUtil.isAdmin(request)) {
            throw new SecurityException("Unauthorized: Admin access required");
        }
    }

    private void validateUserOrAdmin(HttpServletRequest request, int userId) {
        int loggedUserId = SecurityUtil.getLoggedInUserId(request);
        if (!SecurityUtil.isAdmin(request) && loggedUserId != userId) {
            throw new SecurityException("Unauthorized: Cannot access or modify another user's data");
        }
    }

    // -------- Endpoints --------

    @AllowedRoles({"ADMIN", "SELLER", "ADMIN"})
    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable int userId, HttpServletRequest request) {
        validateUserOrAdmin(request, userId);
        User u = userService.getUserById(userId);
        return entityToDto(u);
    }

    @AllowedRoles({"ADMIN"})
    @GetMapping("/email/{email}")
    public UserDto getUserByEmail(@PathVariable String email) {
        User u = userService.getUserByEmail(email);
        return entityToDto(u);
    }

    @AllowedRoles({"ADMIN"})
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream().map(this::entityToDto).collect(Collectors.toList());
    }

    @AllowedRoles({"BUYER", "SELLER", "ADMIN"})
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable int userId,
                                        @Valid @RequestBody UserDto dto,
                                        HttpServletRequest request) {
        validateUserOrAdmin(request, userId);

        User u = dtoToEntity(dto);
        u.setUserId(userId);

        // Prevent email and role updates, and KEEP EXISTING PASSWORD
        User existing = userService.getUserById(userId);
        u.setEmail(existing.getEmail());
        u.setRole(existing.getRole());
        u.setPassword(existing.getPassword());   // <-- keep old password

        int rows = userService.updateUser(u);
        return ResponseEntity.ok(rows > 0 ? "User updated successfully" : "No changes");
    }

    @AllowedRoles({"ADMIN"})
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable int userId) {
        userService.deleteUser(userId);
        return "User deleted successfully";
    }

    @AllowedRoles({"ADMIN"})
    @GetMapping("/exists/{email}")
    public boolean userExists(@PathVariable String email) {
        return userService.userExistsByEmail(email);
    }

    @AllowedRoles({"BUYER", "SELLER", "ADMIN"})
    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable int userId, HttpServletRequest request) {
        validateUserOrAdmin(request, userId);
        try {
            Map<String, Object> userProfile = userService.getUserWithLocation(userId);
            return ResponseEntity.ok(userProfile);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + userId);
        }
    }
}
