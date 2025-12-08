package com.ShopSphere.shop_sphere.security;

public class LoginRequest {
    private String email;
    private String password;
 
    // Getters + Setters
    
    public String getEmail() {
    	return email;
    }
    
    public String getPassword() {
    	return password;
    }
    
    public void setEmail(String email) {
    	this.email = email;
    }
    
    public void setPassword(String password) {
    	this.password = password;
    }
    
    @Override
    public String toString() {
    	return "LoginRequest [email=" + email +", password=****]";
    }
}
 
