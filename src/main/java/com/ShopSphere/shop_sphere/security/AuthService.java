package com.ShopSphere.shop_sphere.security;

import com.ShopSphere.shop_sphere.exception.BadRequestException;
import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.repository.UserDao;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
 
@Service
public class AuthService {
 
    private final UserDao userDao;
    private final JwtTokenUtil jwtTokenUtil;
 
    public AuthService(UserDao userDao, JwtTokenUtil jwtTokenUtil) {
        this.userDao = userDao;
        this.jwtTokenUtil = jwtTokenUtil;
    }
 
    // ---------- SIGNUP ----------
    public void signup(SignupRequest req) {
        if (userDao.findByEmail(req.getEmail()) != null) {
            throw new BadRequestException("Email already exists");
        }
 
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPhone(req.getPhone());
        u.setRole(req.getRole().toUpperCase()); // force uppercase
        u.setLocationId(req.getLocationId());
 
        // secure password
        String hashed = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt());
        u.setPassword(hashed);
 
        userDao.save(u);
    }
 
    // ---------- LOGIN ----------
    public String login(String email, String password) {
        User user = userDao.findByEmail(email);
 
        if (user == null) {
            throw new BadRequestException("Invalid email");
        }
 
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }
 
        return jwtTokenUtil.generateToken(
                user.getUserId(),
                user.getRole(),   // ADMIN/SELLER/BUYER
                user.getEmail()
        );
    }
}
 
