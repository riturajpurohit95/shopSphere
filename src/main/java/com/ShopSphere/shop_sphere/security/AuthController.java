package com.ShopSphere.shop_sphere.security;

 
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.repository.UserDao;

import java.util.HashMap;
import java.util.Map;
 
@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
 
    private final AuthService authService;
    private final UserDao userDao;
 
    public AuthController(AuthService authService, UserDao userDao) {
        this.authService = authService;
        this.userDao = userDao;
    }
 
    // -------- SIGNUP --------
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        authService.signup(req);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }
 
    // -------- LOGIN --------
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
//        String token = authService.login(req.getEmail(), req.getPassword());
//        return ResponseEntity.ok(Map.of("token", token));
//    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String token = authService.login(req.getEmail(), req.getPassword());
        User user = userDao.findByEmail(req.getEmail());  // get user details

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getUserId());
        userMap.put("name", user.getName() != null ? user.getName() : "");
        userMap.put("email", user.getEmail());
        userMap.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userMap.put("role", user.getRole());
        userMap.put("locationId", user.getLocationId() != null ? user.getLocationId() : "");

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userMap);

        return ResponseEntity.ok(response);
    }


}
 
