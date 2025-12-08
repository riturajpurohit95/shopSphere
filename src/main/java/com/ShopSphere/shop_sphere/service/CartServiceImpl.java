package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ShopSphere.shop_sphere.dto.CartItemDto;
import com.ShopSphere.shop_sphere.exception.CartNotFoundException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.exception.ValidationException;
import com.ShopSphere.shop_sphere.model.Cart;
import com.ShopSphere.shop_sphere.repository.CartDao;

@Service
public class CartServiceImpl implements CartService {

    private final CartDao cartDao;

    public CartServiceImpl(CartDao cartDao) {
        this.cartDao = cartDao;
    }

    // ------------------------ Create Cart ------------------------

    @Override
    public Cart createCart(int userId) {

        validateUserId(userId);

        if (cartDao.cartExistsForUser(userId)) {
            throw new ValidationException("Cart already exists for userId: " + userId);
        }

        int cartId = cartDao.createCart(userId);
        Cart createdCart = cartDao.findById(cartId);

        if (createdCart == null) {
            throw new RuntimeException("Cart creation failed for userId: " + userId);
        }

        return createdCart;
    }

    // ------------------------ Get Cart by User ------------------------

    @Override
    public Cart getCartByUserId(int userId) {

        validateUserId(userId);

        Cart cart = cartDao.findByUserId(userId);

        if (cart == null) {
            throw new ResourceNotFoundException("No cart found for userId: " + userId);
        }

        return cart;
    }

    // ------------------------ Get Cart by CartId ------------------------

    @Override
    public Cart getCartById(int cartId) {

        validateCartId(cartId);

        Cart cart = cartDao.findById(cartId);

        if (cart == null) {
            throw new CartNotFoundException("No cart found for cartId: " + cartId);
        }
        return cart;
    }

    // ------------------------ Get All Carts ------------------------

    @Override
    public List<Cart> getAllCarts() {
        return cartDao.getAllCarts();
    }

    // ------------------------ Delete Cart ------------------------

    @Override
    public void deleteCart(int cartId) {

        validateCartId(cartId);
        Cart existing = getCartById(cartId);

        int rows = cartDao.deleteCart(existing.getCartId());

        if (rows <= 0) {
            throw new ValidationException("Failed to delete cart with cartId: " + cartId);
        }
    }

    // ------------------------ Cart Exists Check ------------------------

    @Override
    public boolean cartExistsForUser(int userId) {

        validateUserId(userId);

        return cartDao.cartExistsForUser(userId);
    }

    // ------------------------ Check if Cart is Empty ------------------------

    @Override
    public boolean isCarEmpty(int cartId) {

        validateCartId(cartId);

        Cart existing = getCartById(cartId);
        return cartDao.isCartEmpty(existing.getCartId());
    }

    // ------------------------ Get Cart Items ------------------------

    @Override
    public List<CartItemDto> getCartItemsByUserId(int userId) {

        validateUserId(userId);

        if (!cartDao.cartExistsForUser(userId)) {
            throw new ResourceNotFoundException("Cart does not exist for userId: " + userId);
        }

        List<Map<String, Object>> rows = cartDao.getCartItems(userId);

        return rows.stream().map(r -> {
            return new CartItemDto(
                ((Number) r.get("cart_items_id")).intValue(),
                ((Number) r.get("cart_id")).intValue(),
                ((Number) r.get("product_id")).intValue(),
                ((Number) r.get("quantity")).intValue(),
                r.get("product_name").toString(),
                ((Number) r.get("product_price")).doubleValue(),
                r.get("image_url") != null ? r.get("image_url").toString() : null
            );
        }).collect(Collectors.toList());
    }

    // ------------------------ Validation Helpers ------------------------

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new ValidationException("Invalid userId: " + userId);
        }

        // TODO: later → check user existence in UserService or UserDao
        // if (!userDao.exists(userId)) throw new ResourceNotFoundException("User not found");
    }

    private void validateCartId(int cartId) {
        if (cartId <= 0) {
            throw new ValidationException("Invalid cartId: " + cartId);
        }
    }
}
