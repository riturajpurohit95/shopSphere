package com.ShopSphere.shop_sphere.repository;

import java.util.List;
import java.util.Optional;

import com.ShopSphere.shop_sphere.model.CartItem;

public interface CartItemDao {

    CartItem addItem(CartItem cartItem);
    Optional<CartItem> findByCartId(int cartId);
    Optional<CartItem> findByProductAndCart(int cartId, int productId);
    List<CartItem> findAllByCartId(int cartId);
    boolean existsInCart(int cartId, int productId);
    int updateItemQuantity(int cartItemId, int quantity);
    int deleteItem(int cartItemId);
    int deleteItemByProductId(int cartId, int productId);
    double calculateTotalAmount(int cartId);
    Optional<CartItem> findById(int cartItemId);
}
