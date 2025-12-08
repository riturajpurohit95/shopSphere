package com.ShopSphere.shop_sphere.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.OrderItem;

public interface OrderItemsService {

	OrderItem createOrderItem(OrderItem item);
	OrderItem getOrderItemById(int orderItemsId);
	List<OrderItem> getOrderItemsByOrderId(int orderId);
	List<OrderItem> getOrderItemsByProductId(int productId);
	OrderItem updateItemQuantity(int orderItemsId, int quantity);
	void deleteOrderItem(int orderItemsId);
}
