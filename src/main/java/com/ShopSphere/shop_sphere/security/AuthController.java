package com.ShopSphere.shop_sphere.security;

 
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.Map;
 
@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
 
    private final AuthService authService;
 
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
 
    // -------- SIGNUP --------
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        authService.signup(req);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }
 
    // -------- LOGIN --------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String token = authService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
 
