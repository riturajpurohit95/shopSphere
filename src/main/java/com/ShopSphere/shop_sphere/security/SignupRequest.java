package com.ShopSphere.shop_sphere.security;
 
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
 
public class SignupRequest {
 
    @NotBlank(message = "Name is required")
    private String name;
 
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
 
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
 
    private String phone;
 
    @NotBlank(message = "Role is required") // ADMIN, SELLER, BUYER
    private String role;
 
    private Integer locationId;
 
    public SignupRequest() {}
 
    public SignupRequest(String name, String email, String password, String phone, String role, Integer locationId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.locationId = locationId;
    }
 
    // -------- Getters --------
    public String getName() {
        return name;
    }
 
    public String getEmail() {
        return email;
    }
 
    public String getPassword() {
        return password;
    }
 
    public String getPhone() {
        return phone;
    }
 
    public String getRole() {
        return role;
    }
 
    public Integer getLocationId() {
        return locationId;
    }
 
    // -------- Setters --------
    public void setName(String name) {
        this.name = name;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
 
    public void setPhone(String phone) {
        this.phone = phone;
    }
 
    public void setRole(String role) {
        this.role = role;
    }
 
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }
 
    @Override
    public String toString() {
        return "SignupRequest [name=" + name + ", email=" + email + ", password=****, phone=" + phone + ", role=" + role
                + ", locationId=" + locationId + "]";
    }
}
 