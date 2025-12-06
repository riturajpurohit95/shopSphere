package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Optional;

import com.ShopSphere.shop_sphere.model.CartItem;

public interface CartItemService {

    CartItem addItem(CartItem cartItem);
    Optional<CartItem> findItemByCartId(int cartId);
    List<CartItem> getItemsByCartId(int cartId);
    CartItem updateItemQuantity(int cartItemId, int quantity);
    void deleteItem(int cartItemId);
    void deleteItemByProductId(int cartId, int productId);
    double calculateTotalAmount(int cartId);
    boolean existsInCart(int cartId, int productId);
	CartItem getItemById(int cartItemId);
}
