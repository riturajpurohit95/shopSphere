package com.ShopSphere.shop_sphere.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ShopSphere.shop_sphere.dto.PaymentDto;
import com.ShopSphere.shop_sphere.exception.OrderAlreadyProcessedException;
import com.ShopSphere.shop_sphere.exception.PaymentMethodNotSupportedException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.repository.OrderDao;
import com.ShopSphere.shop_sphere.repository.OrderItemDao;
import com.ShopSphere.shop_sphere.repository.ProductDao;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final PaymentService paymentService;
    private final DeliveryService deliveryService;
    private final ProductDao productDao;
    private final OrderItemDao orderItemDao;

    public OrderServiceImpl(OrderDao orderDao,
                            PaymentService paymentService,
                            DeliveryService deliveryService,
                            ProductDao productDao,
                            OrderItemDao orderItemDao) {
        this.orderDao = orderDao;
        this.paymentService = paymentService;
        this.deliveryService = deliveryService;
        this.productDao = productDao;
        this.orderItemDao = orderItemDao;
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }

        String method = order.getPaymentMethod();
        if (method == null || !(method.equalsIgnoreCase("COD") || method.equalsIgnoreCase("UPI"))) {
            throw new PaymentMethodNotSupportedException(method);
        }

        // initial lifecycle status
        if (order.getOrderStatus() == null || order.getOrderStatus().isEmpty()) {
            order.setOrderStatus("PENDING");
        }

        if (order.getPlacedAt() == null) {
            order.setPlacedAt(LocalDateTime.now());
        }

        int rows = orderDao.save(order);
        if (rows <= 0) {
            throw new RuntimeException("Create failed for order of userId: " + order.getUserId());
        }

        // create initial payment record
        PaymentDto dto = new PaymentDto();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setAmount(order.getTotalAmount());
        dto.setCurrency("INR");
        dto.setPaymentMethod(method.toUpperCase());

        paymentService.createPayment(dto);

        return orderDao.findById(order.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found after create: " + order.getOrderId()));
    }

    @Override
    public Order getOrderById(int orderId) {
        return orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }
    @Override
	public List<Order> getAllOrders(){
		return orderDao.findAll();
	}

    @Override
    public List<Order> getOrdersByUserId(int userId) {
        return orderDao.findByUserId(userId);
    }
    @Override
    public List<Order> getOrdersBySellerId(int userId) {
        return orderDao.findBySeller(userId);
    }

    /**
     * Central place for updating order status
     * and auto-syncing payment status.
     */
    @Override
    @Transactional
    public int updateOrderStatus(int orderId, String orderStatus) {

        int rows = orderDao.updateOrderStatus(orderId, orderStatus);
        if (rows <= 0) {
            throw new RuntimeException("Update status failed for orderId: " + orderId);
        }

        // auto-sync payment with this new order status
        syncPaymentWithOrder(orderId, orderStatus);

        return rows;
    }
  
    /**
     * Called when payment status is updated from outside (e.g. webhook / admin).
     * Keeps your existing behaviour: payment → order sync.
     */
    @Override
    @Transactional
    public int updatePaymentStatus(int orderId, String status) {

        PaymentDto payment = paymentService.getPaymentByOrderId(orderId);
        if (payment == null) {
            throw new RuntimeException("Payment not found for orderId: " + orderId);
        }

        // update payment table
        paymentService.updatePaymentStatus(payment.getPayment_id(), status);

        Order order = getOrderById(orderId);
        String currentOrderStatus = order.getOrderStatus();
        String nextOrderStatus = currentOrderStatus;

        if ("PAID".equalsIgnoreCase(status)) {
            if ("PENDING".equalsIgnoreCase(currentOrderStatus)) {
                nextOrderStatus = "PROCESSING";
            }
        } else if ("REFUNDED".equalsIgnoreCase(status)) {
            nextOrderStatus = "REFUNDED";
        } else if ("FAILED".equalsIgnoreCase(status)) {
            nextOrderStatus = "PENDING";
        } else if ("PENDING".equalsIgnoreCase(status)) {
            nextOrderStatus = "PENDING";
        }

        int rows = orderDao.updateOrderStatus(orderId, nextOrderStatus);
        if (rows <= 0) {
            throw new RuntimeException("Failed to sync order status for Id: " + orderId);
        }

        return rows;
    }

    @Override
    @Transactional
    public Order cancelOrder(int orderId) {

        Order existing = getOrderById(orderId);

        // if someone tries to cancel a processed order
        if ("SHIPPED".equalsIgnoreCase(existing.getOrderStatus())
                || "DELIVERED".equalsIgnoreCase(existing.getOrderStatus())) {
            throw new OrderAlreadyProcessedException(orderId, existing.getOrderStatus());
        }

        PaymentDto payment = null;
        try {
            payment = paymentService.getPaymentByOrderId(orderId);
        } catch (RuntimeException e) {
            // no payment for this order is also fine (should be rare)
        }

        String finalOrderStatus = "CANCELLED";

        if (payment != null) {
            String payStatus = payment.getStatus();

            if ("PAID".equalsIgnoreCase(payStatus)) {
                // refund payment + mark order refunded
                paymentService.updatePaymentStatus(payment.getPayment_id(), "REFUNDED");
                finalOrderStatus = "REFUNDED";
            } else if (!"REFUNDED".equalsIgnoreCase(payStatus)) {
                // not paid and not refunded → mark payment failed
                paymentService.updatePaymentStatus(payment.getPayment_id(), "FAILED");
            }
        }

        // update order + sync again (idempotent on payment side)
        updateOrderStatus(orderId, finalOrderStatus);

        return getOrderById(orderId);
    }

    @Override
    @Transactional
    public void deleteOrder(int orderId) {
        getOrderById(orderId); // throws if not found

        int rows = orderDao.deleteById(orderId);
        if (rows <= 0) {
            throw new RuntimeException("Delete failed for orderId: " + orderId);
        }
    }

    @Override
    public int placeOrder(int buyerId, int productId) {
        int deliveryDays = deliveryService.calculateDeliveryDays(buyerId, productId);
        System.out.println("Estimated delivery days: " + deliveryDays);
        return deliveryDays;
    }

    @Override
    public List<Map<String, Object>> getOrdersWithItems(int userId) {
        return orderDao.getOrdersWithItems(userId);
    }

    @Override
    @Transactional
    public int expireOldPendingOrders() {
        LocalDateTime cutOff = LocalDateTime.now().minusMinutes(10);
        List<Order> oldPendingOrders = orderDao.findByStatusAndPlacedAtBefore("PENDING", cutOff);
        int expiredCount = 0;

        for (Order order : oldPendingOrders) {
            int orderId = order.getOrderId();

            List<OrderItem> items = orderItemDao.findByOrderId(orderId);
            for (OrderItem item : items) {
                int productId = item.getProductId();
                int quantity = item.getQuantity();
                productDao.increaseStock(productId, quantity);
            }

            updateOrderStatus(orderId, "EXPIRED");
            expiredCount++;
        }

        return expiredCount;
    }

    /**
     * Helper method that decides payment status from order status
     * and updates payment (if any) in a safe way.
     */
    private void syncPaymentWithOrder(int orderId, String newOrderStatus) {

        PaymentDto payment = paymentService.getPaymentByOrderId(orderId);
       
        if (payment == null) {
            return;
        }
String method = payment.getPaymentMethod();
        String current = payment.getStatus();
        String target = current;
        
        if("FAILED".equalsIgnoreCase(current) || "REFUNDED".equalsIgnoreCase(current)) {
        	return;
        }

        if ("COD".equalsIgnoreCase(method)) {
            if ("DELIVERED".equalsIgnoreCase(newOrderStatus)) {
         	   target = "PAID";
            } else if ("CANCELLED".equalsIgnoreCase(newOrderStatus)
         	   || "EXPIRED".equalsIgnoreCase(newOrderStatus)){
         		   target ="FAILED";
            }else if ("REFUNDED".equalsIgnoreCase(newOrderStatus)) {
         	   target = "REFUNDED";
            }
            else{
         	   target ="PENDING";
            }
         	 
            }
        
       
        
         else if ("UPI".equalsIgnoreCase(method)) {
               if ("CANCELLED".equalsIgnoreCase(newOrderStatus)) {
            	   target = "FAILED";
               } else if ("REFUNDED".equalsIgnoreCase(newOrderStatus)) {
            	   target = "REFUNDED";
               } else if ("EXPIRED".equalsIgnoreCase(newOrderStatus)) {
            	   target = "FAILED";
               } else if ("PENDING".equalsIgnoreCase(newOrderStatus)) {
            	   target = "PENDING";
               }else{
            	   target ="PAID";
               }
            	 
               }
             
        if (!target.equalsIgnoreCase(current)) {
            paymentService.updatePaymentStatus(payment.getPayment_id(), target);
        }
    }
}
