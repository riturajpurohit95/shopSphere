package com.ShopSphere.shop_sphere.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.ShopSphere.shop_sphere.model.OrderItem;

public interface OrderItemDao {
	
	int save(OrderItem item);
	Optional<OrderItem> findById(int orderItemsId);
	List<OrderItem> findByOrderId(int orderId);
	List<OrderItem> findByProductId(int productId);
	int updateQuantityANDTotalPrice(int orderItemsId,int quantity,BigDecimal totalPrice);
	int deleteById(int orderItemsId);
    
}
