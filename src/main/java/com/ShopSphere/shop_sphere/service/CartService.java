package com.ShopSphere.shop_sphere.service;

import java.util.List;

import com.ShopSphere.shop_sphere.dto.CartItemDto;
import com.ShopSphere.shop_sphere.model.Cart;

public interface CartService {
	
	Cart createCart(int userId);
	Cart getCartByUserId(int userId);
	Cart getCartById(int cartId);
	List<Cart> getAllCarts();
	void deleteCart(int cartId);
	boolean cartExistsForUser(int userId);
	boolean isCarEmpty(int cartId);
	
	List<CartItemDto> getCartItemsByUserId(int userId);
   
}
