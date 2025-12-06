
package com.ShopSphere.shop_sphere.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 60, message = "Name must be between 2 and 60 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String password;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Pattern(regexp = "Admin|Seller|Buyer", message = "Role must be Admin, Seller, or Buyer")
    private String role = "Buyer"; // default if omitted

    private Integer locationId;

    // getters/setters + toString...
}
