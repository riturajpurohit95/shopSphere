package com.ShopSphere.shop_sphere.exception;

public class OrderAlreadyProcessedException extends RuntimeException{

	public OrderAlreadyProcessedException(int orderId, String status) {
		super("Order "+ orderId+ "cannot be modified because it is already" + status);
	}
}
