package com.ShopSphere.shop_sphere.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.WishlistItemDto;
import com.ShopSphere.shop_sphere.exception.BadRequestException;
import com.ShopSphere.shop_sphere.model.WishlistItem;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.WishlistItemService;

import jakarta.servlet.http.HttpServletRequest;


@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/wishlist-items")
public class WishlistItemController {

    private final WishlistItemService wishlistItemService;

    public WishlistItemController(WishlistItemService wishlistItemService) {
        this.wishlistItemService = wishlistItemService;
    }

    private WishlistItem dtoToEntity(WishlistItemDto dto) {
        WishlistItem wi = new WishlistItem();
        wi.setWishlistId(dto.getWishlistId());
        wi.setProductId(dto.getProductId());
        return wi;
    }

    private WishlistItemDto entityToDto(WishlistItem wi) {
        WishlistItemDto dto = new WishlistItemDto();
        dto.setWishlistItemsId(wi.getWishlistItemsId());
        dto.setWishlistId(wi.getWishlistId());
        dto.setProductId(wi.getProductId());
        return dto;
    }

    // ---------------- Security Helper ----------------
    private void validateUserOrAdmin(HttpServletRequest request, int wishlistId) {
        int loggedUserId = SecurityUtil.getLoggedInUserId(request);
        int ownerId = wishlistItemService.getWishlistOwnerId(wishlistId);
        if (!SecurityUtil.isAdmin(request) && loggedUserId != ownerId) {
            throw new SecurityException("Unauthorized: Cannot access another user's wishlist items");
        }
    }

    // ---------------- API Endpoints ----------------

//    @AllowedRoles({"BUYER", "ADMIN"})
//    @PostMapping
//    public ResponseEntity<String> addItem(@RequestBody WishlistItemDto dto, HttpServletRequest request) {
//        validateUserOrAdmin(request, dto.getWishlistId());
//
//        try {
//            int id = wishlistItemService.addItemToWishlist(dtoToEntity(dto));
//            return ResponseEntity.ok("Product added to wishlist successfully. Item ID: " + id);
//        } catch (BadRequestException ex) {
//            // This usually means the product is already in the wishlist
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
//        }
//    }
    
    @AllowedRoles({"BUYER", "ADMIN"})
    @PostMapping
    public ResponseEntity<Map<String, Object>> addItem(@RequestBody WishlistItemDto dto, HttpServletRequest request) {
        validateUserOrAdmin(request, dto.getWishlistId());

        try {
            int id = wishlistItemService.addItemToWishlist(dtoToEntity(dto));
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Product added to wishlist successfully",
                "id", id
            ));
        } catch (BadRequestException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "message", ex.getMessage()
            ));
        }
    }


    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/wishlist/{wishlistId}")
    public ResponseEntity<List<WishlistItemDto>> getItemsByWishlist(@PathVariable int wishlistId, HttpServletRequest request) {
        validateUserOrAdmin(request, wishlistId);
        List<WishlistItemDto> items = wishlistItemService.getItemsByWishlistId(wishlistId)
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @AllowedRoles({"BUYER", "ADMIN"})
    @DeleteMapping("/{wishlistItemId}")
    public ResponseEntity<String> deleteItem(@PathVariable int wishlistItemId, HttpServletRequest request) {
        int wishlistId = wishlistItemService.getWishlistIdByItem(wishlistItemId);
        validateUserOrAdmin(request, wishlistId);

        wishlistItemService.deleteItem(wishlistItemId);
        return ResponseEntity.ok("Wishlist item deleted successfully");
    }
}
