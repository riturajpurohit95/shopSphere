package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ShopSphere.shop_sphere.model.Order;

public interface OrderService {
	Order createOrder(Order order);
	Order getOrderById(int orderId);
	List<Order> getOrdersByUserId(int userId);
	int updateOrderStatus(int orderId, String orderStatus);
	int updatePaymentStatus(int OrderId, String status);
	Order cancelOrder(int orderId);
	void deleteOrder(int orderId);
	int placeOrder(int buyerId, int productId);
	
	int expireOldPendingOrders();
    
	List<Map<String, Object>> getOrdersWithItems(int userId);
	List<Order> getAllOrders();
	List<Order> getOrdersBySellerId(int userId);
}
