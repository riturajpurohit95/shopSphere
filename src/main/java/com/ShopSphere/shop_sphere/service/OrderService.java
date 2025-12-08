package com.ShopSphere.shop_sphere.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ShopSphere.shop_sphere.dto.OrderRequest;
import com.ShopSphere.shop_sphere.model.Order;

public interface OrderService {
	Order createOrder(OrderRequest dto);
	Order getOrderById(int orderId);
	List<Order> getOrdersByUserId(int userId);
	int updateOrderStatus(int orderId, String orderStatus);
	int updatePaymentStatus(int OrderId, String status);
	Order cancelOrder(int orderId);
	void deleteOrder(int orderId);
	int placeOrder(int buyerId, int productId);
	
	void updateTotalAmount(int orderId, BigDecimal totalAmount);

	
	int expireOldPendingOrders();
    
	List<Map<String, Object>> getOrdersWithItems(int userId);
	List<Order> getAllOrders();
	List<Order> getOrdersBySellerId(int userId);
	
	 List<Order> getOrdersWithPaymentByUser(int userId);
}


