package com.ShopSphere.shop_sphere.repository;

import java.math.BigDecimal;
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
	Order cancelOrder(int orderId);
	int deleteById(int orderId);
	
	void updateTotalAmount(int orderId, BigDecimal totalAmount);

	
	List<Order> findByStatusAndPlacedAtBefore(String status, LocalDateTime cutOffTime);
	
	int updateRazorpayOrderId(int orderId, String razorpayOrderId);
	List<Map<String, Object>> getOrdersWithItems(int userId);
	List<Order> findAll();
	List<Order> findBySeller(int userId);
	
	List<Order> findOrdersWithPaymentByUserId(int userId);
	Order getOrderById(int orderId);
    
}