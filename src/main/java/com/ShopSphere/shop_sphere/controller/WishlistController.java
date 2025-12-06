package com.ShopSphere.shop_sphere.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.WishlistDto;
import com.ShopSphere.shop_sphere.exception.BadRequestException;
import com.ShopSphere.shop_sphere.model.Wishlist;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.WishlistService;

import jakarta.servlet.http.HttpServletRequest;


@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // ---------------- Helper ----------------
    private WishlistDto entityToDto(Wishlist w) {
        WishlistDto dto = new WishlistDto();
        dto.setWishlistId(w.getWishlistId());
        dto.setUserId(w.getUserId());
        return dto;
    }

    private int getLoggedUserId(HttpServletRequest request) {
        return SecurityUtil.getLoggedInUserId(request);
    }

    private void validateUserAccess(HttpServletRequest request, int ownerUserId) {
        int loggedUserId = getLoggedUserId(request);
        if (!SecurityUtil.isAdmin(request) && loggedUserId != ownerUserId) {
            throw new SecurityException("Unauthorized: Cannot access another user's wishlist");
        }
    }

    // ---------------- CREATE ----------------
    @AllowedRoles({"BUYER", "ADMIN"})
    @PostMapping
    public ResponseEntity<WishlistDto> createWishlist(HttpServletRequest request) {
        int userId = getLoggedUserId(request);
        Wishlist w = wishlistService.createWishlist(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityToDto(w));
    }

    // ---------------- GET USER WISHLIST ----------------
    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/my")
    public ResponseEntity<WishlistDto> getMyWishlist(HttpServletRequest request) {
        int userId = getLoggedUserId(request);
        Wishlist w = wishlistService.getWishlistByUserId(userId);
        return ResponseEntity.ok(entityToDto(w));
    }

    // ---------------- GET BY ID ----------------
    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/{wishlistId}")
    public ResponseEntity<WishlistDto> getWishlistById(@PathVariable int wishlistId, HttpServletRequest request) {
        Wishlist w = wishlistService.getWishlistById(wishlistId);
        validateUserAccess(request, w.getUserId());
        return ResponseEntity.ok(entityToDto(w));
    }

    // ---------------- GET ALL (ADMIN) ----------------
    @AllowedRoles({"ADMIN"})
    @GetMapping
    public List<WishlistDto> getAllWishlists() {
        return wishlistService.getAllWishlists().stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    // ---------------- DELETE ----------------
    @AllowedRoles({"BUYER", "ADMIN"})
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<String> deleteWishlist(@PathVariable int wishlistId, HttpServletRequest request) {
        Wishlist w = wishlistService.getWishlistById(wishlistId);
        validateUserAccess(request, w.getUserId());
        wishlistService.deleteWishlist(wishlistId);
        return ResponseEntity.ok("Wishlist deleted successfully");
    }

    // ---------------- CHECK IF WISHLIST EXISTS ----------------
    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/exists")
    public ResponseEntity<Boolean> wishlistExists(HttpServletRequest request) {
        int userId = getLoggedUserId(request);
        boolean exists = wishlistService.wishlistExistsForUser(userId);
        return ResponseEntity.ok(exists);
    }
    


    // ---------------- CHECK IF WISHLIST IS EMPTY ----------------
    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/is-empty")
    public ResponseEntity<Boolean> isWishlistEmpty(HttpServletRequest request) {
        int userId = getLoggedUserId(request);
        Wishlist w = wishlistService.getWishlistByUserId(userId);
        boolean empty = wishlistService.isWishlistEmpty(w.getWishlistId());
        return ResponseEntity.ok(empty);
    }

    // ---------------- GET WISHLIST ITEMS ----------------
    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/items")
    public ResponseEntity<List<Map<String, Object>>> getWishlistItems(HttpServletRequest request) {
        int userId = getLoggedUserId(request);
        List<Map<String, Object>> items = wishlistService.getWishlistItems(userId);
        return ResponseEntity.ok(items);
    }
}
