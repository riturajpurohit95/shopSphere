package com.ShopSphere.shop_sphere.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.CartItemDto;
import com.ShopSphere.shop_sphere.exception.ForbiddenException;
import com.ShopSphere.shop_sphere.exception.OutOfStockException;
import com.ShopSphere.shop_sphere.model.CartItem;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.model.Cart;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.CartItemService;
import com.ShopSphere.shop_sphere.service.CartService;
import com.ShopSphere.shop_sphere.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    private final CartItemService cartItemService;
    private final CartService cartService;
    private final ProductService productService;

    public CartItemController(CartItemService cartItemService, CartService cartService, ProductService productService) {
        this.cartItemService = cartItemService;
        this.cartService = cartService;
        this.productService = productService;
    }

    // ---------------- DTO Mapper ----------------
    private CartItem dtoToEntity(CartItemDto dto) {
        CartItem item = new CartItem();
        item.setCartId(dto.getCartId());
        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        return item;
    }

    private CartItemDto entityToDto(CartItem item) {
        Product product = productService.getProductById(item.getProductId());

        String name = null;
        Double price = null;
        String image = null;

        if (product != null) {
            name = product.getProductName();
            if (product.getProductPrice() != null) {
                price = product.getProductPrice().doubleValue(); // convert BigDecimal -> Double
            }
            image = product.getImageUrl();
        }

        return new CartItemDto(
                item.getCartItemsId(),
                item.getCartId(),
                item.getProductId(),
                item.getQuantity(),
                name,
                price,
                image
        );
    }


    // ---------------- Security Helper ----------------
    private void validateCartOwner(int cartId, HttpServletRequest request) {
        Cart cart = cartService.getCartById(cartId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for id: " + cartId);
        }
        int loggedUserId = SecurityUtil.getLoggedInUserId(request);
        if (!SecurityUtil.isAdmin(request) && loggedUserId != cart.getUserId()) {
            throw new ForbiddenException("You are not allowed to access this resource");
        }
    }

    // ---------------- APIs ----------------

    /**
     * Add item to cart.
     */
    @AllowedRoles({"BUYER"})
    @PostMapping
    public CartItemDto addItem(@RequestBody @Valid CartItemDto dto, HttpServletRequest request) {

        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productService.getProductById(dto.getProductId());
        if (product.getProductQuantity() < dto.getQuantity()) {
            throw new OutOfStockException("Product is out of stock or requested quantity not available");
        }

        validateCartOwner(dto.getCartId(), request);

        CartItem saved = cartItemService.addItem(dtoToEntity(dto));
        return entityToDto(saved);
    }

    /**
     * Get all items in a cart.
     */
    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/cart/{cartId}")
    public List<CartItemDto> getItemsByCartId(@PathVariable int cartId, HttpServletRequest request) {
        validateCartOwner(cartId, request);
        return cartItemService.getItemsByCartId(cartId)
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a single cart item by its id.
     */
    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/{cartItemId}")
    public CartItemDto getItem(@PathVariable int cartItemId, HttpServletRequest request) {
        CartItem item = cartItemService.getItemById(cartItemId);
        if (item == null) {
            throw new IllegalArgumentException("Cart item not found with id: " + cartItemId);
        }
        validateCartOwner(item.getCartId(), request);
        return entityToDto(item);
    }

    /**
     * Update quantity of an existing cart item.
     */
    @AllowedRoles({"BUYER", "ADMIN"})
    @PutMapping("/{cartItemId}/quantity/{quantity}")
    public CartItemDto updateItemQuantity(@PathVariable int cartItemId,
                                          @PathVariable int quantity,
                                          HttpServletRequest request) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        CartItem item = cartItemService.getItemById(cartItemId);
        if (item == null) {
            throw new IllegalArgumentException("Cart item not found with id: " + cartItemId);
        }

        validateCartOwner(item.getCartId(), request);

        Product product = productService.getProductById(item.getProductId());
        if (quantity > product.getProductQuantity()) {
            throw new OutOfStockException("Requested quantity exceeds available stock");
        }

        CartItem updated = cartItemService.updateItemQuantity(cartItemId, quantity);
        return entityToDto(updated);
    }

    /**
     * Delete a cart item.
     */
    @AllowedRoles({"BUYER", "ADMIN"})
    @DeleteMapping("/{cartItemId}")
    public String deleteItem(@PathVariable int cartItemId, HttpServletRequest request) {
        CartItem item = cartItemService.getItemById(cartItemId);
        if (item == null) {
            throw new IllegalArgumentException("Cart item not found with id: " + cartItemId);
        }
        validateCartOwner(item.getCartId(), request);
        cartItemService.deleteItem(cartItemId);
        return "Cart item deleted successfully";
    }

    /**
     * Calculate total amount for a cart.
     */
    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/total/{cartId}")
    public double calculateTotalAmount(@PathVariable int cartId, HttpServletRequest request) {
        validateCartOwner(cartId, request);
        return cartItemService.calculateTotalAmount(cartId);
    }

    /**
     * Check if a product exists in a cart.
     */
    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/exists/{cartId}/{productId}")
    public boolean existsInCart(@PathVariable int cartId,
                                @PathVariable int productId,
                                HttpServletRequest request) {
        validateCartOwner(cartId, request);
        return cartItemService.existsInCart(cartId, productId);
    }
}
