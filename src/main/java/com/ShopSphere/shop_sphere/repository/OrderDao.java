package com.ShopSphere.shop_sphere.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.ShopSphere.shop_sphere.model.Order;
import java.util.Optional;

public interface OrderDao {
	
	int save(Order order);
	Optional<Order> findById(int orderId);
	List<Order> findByUserId(int userId);
	
	int updateOrderStatus(int orderId, String orderStatus);
	//int updatePaymentStatus(int orderId, String status);
	int cancelOrder(int orderId);
	int deleteById(int orderId);
	
	List<Order> findByStatusAndPlacedAtBefore(String status, LocalDateTime cutOffTime);
	
	int updateRazorpayOrderId(int orderId, String razorpayOrderId);
	List<Map<String, Object>> getOrdersWithItems(int userId);
	List<Order> findAll();
	List<Order> findBySeller(int userId);
    
}
