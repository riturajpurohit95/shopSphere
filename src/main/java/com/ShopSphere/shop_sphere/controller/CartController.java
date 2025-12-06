package com.ShopSphere.shop_sphere.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.CartDto;
import com.ShopSphere.shop_sphere.dto.CartItemDto;
import com.ShopSphere.shop_sphere.model.Cart;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.CartService;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ---------------- Helper Mapping Methods ----------------

    private CartDto entityToDto(Cart cart) {
        CartDto cartDto = new CartDto();
        cartDto.setCartId(cart.getCartId());
        cartDto.setUserId(cart.getUserId());
        return cartDto;
    }

    // ---------------------- Security Helper ----------------

    private void validateUserCart(int userId, HttpServletRequest request) {
        int loggedUserId = SecurityUtil.getLoggedInUserId(request);
        if (!SecurityUtil.isAdmin(request) && loggedUserId != userId) {
            throw new SecurityException("Unauthorized: Cannot access another user's cart");
        }
    }

    private void validateCartById(int cartId, HttpServletRequest request) {
        Cart cart = cartService.getCartById(cartId);
        int loggedUserId = SecurityUtil.getLoggedInUserId(request);
        if (!SecurityUtil.isAdmin(request) && loggedUserId != cart.getUserId()) {
            throw new SecurityException("Unauthorized: Cannot access another user's cart");
        }
    }

    // ---------------------- API Endpoints ------------------------

    @AllowedRoles({"BUYER","ADMIN"})
    @PostMapping("/{userId}")
    public ResponseEntity<CartDto> createCart(@PathVariable int userId, HttpServletRequest request) {
        validateUserCart(userId, request);
        Cart cart = cartService.createCart(userId);
        return ResponseEntity.ok(entityToDto(cart));
    }

    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDto> getCartByUserId(@PathVariable int userId, HttpServletRequest request) {
        validateUserCart(userId, request);
        Cart cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(entityToDto(cart));
    }

    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCartById(@PathVariable int cartId, HttpServletRequest request) {
        validateCartById(cartId, request);
        Cart cart = cartService.getCartById(cartId);
        return ResponseEntity.ok(entityToDto(cart));
    }

    @AllowedRoles({"ADMIN"})
    @GetMapping
    public ResponseEntity<List<CartDto>> getAllCarts() {
        List<CartDto> carts = cartService.getAllCarts()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(carts);
    }

    @AllowedRoles({"BUYER", "ADMIN"})
    @DeleteMapping("/{cartId}")
    public ResponseEntity<String> deleteCart(@PathVariable int cartId, HttpServletRequest request) {
        validateCartById(cartId, request);
        cartService.deleteCart(cartId);
        return ResponseEntity.ok("Cart deleted successfully");
    }

    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/exists/{userId}")
    public ResponseEntity<Boolean> cartExistsForUser(@PathVariable int userId, HttpServletRequest request) {
        validateUserCart(userId, request);
        return ResponseEntity.ok(cartService.cartExistsForUser(userId));
    }

    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/userCart/{userId}")
    public ResponseEntity<List<CartItemDto>> getCartItems(@PathVariable int userId, HttpServletRequest request) {
        validateUserCart(userId, request);
        return ResponseEntity.ok(cartService.getCartItemsByUserId(userId));
    }
}
