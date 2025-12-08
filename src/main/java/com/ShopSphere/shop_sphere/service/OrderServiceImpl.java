package com.ShopSphere.shop_sphere.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ShopSphere.shop_sphere.dto.OrderItemDto;
import com.ShopSphere.shop_sphere.dto.OrderRequest;
import com.ShopSphere.shop_sphere.dto.PaymentDto;
import com.ShopSphere.shop_sphere.exception.OrderAlreadyProcessedException;
import com.ShopSphere.shop_sphere.exception.PaymentMethodNotSupportedException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.model.Payment;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.repository.OrderDao;
import com.ShopSphere.shop_sphere.repository.OrderItemDao;
import com.ShopSphere.shop_sphere.repository.PaymentDao;
import com.ShopSphere.shop_sphere.repository.ProductDao;


@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final PaymentService paymentService;
    private final DeliveryService deliveryService;
    private final ProductDao productDao;
    private final OrderItemDao orderItemDao;
    private final PaymentDao paymentDao;

    public OrderServiceImpl(OrderDao orderDao,
                            PaymentService paymentService,
                            DeliveryService deliveryService,
                            ProductDao productDao,
                            OrderItemDao orderItemDao,
                            PaymentDao paymentDao) {
        this.orderDao = orderDao;
        this.paymentService = paymentService;
        this.deliveryService = deliveryService;
        this.productDao = productDao;
        this.orderItemDao = orderItemDao;
        this.paymentDao = paymentDao;
    }

    @Override
    @Transactional
    public Order createOrder(OrderRequest dto) {

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        // 1️⃣ Create order with temporary totalAmount = 0
        Order order = new Order();
        order.setUserId(dto.getUserId());
        order.setShippingAddress(dto.getShippingAddress());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setOrderStatus("PENDING");
        order.setPlacedAt(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.ZERO);

        orderDao.save(order); // generates orderId

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 2️⃣ Insert each order item
        for (OrderItemDto itemDto : dto.getItems()) {
            Product product = productDao.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            int qty = (itemDto.getQuantity() == null) ? 1 : itemDto.getQuantity();
            BigDecimal unitPrice = product.getProductPrice() != null ? product.getProductPrice() : BigDecimal.ZERO;
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(itemDto.getProductId());
            orderItem.setSellerId(product.getUserId());
            orderItem.setProductName(product.getProductName());
            orderItem.setQuantity(qty);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalItemPrice(itemTotal);

            orderItemDao.save(orderItem);

            totalAmount = totalAmount.add(itemTotal);
        }

        // 3️⃣ Update final totalAmount in orders table
        orderDao.updateTotalAmount(order.getOrderId(), totalAmount);
        order.setTotalAmount(totalAmount);

        // 4️⃣ Create payment row for **all payment methods** (mandatory!)
        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setUserId(order.getUserId());
        payment.setAmount(totalAmount);
        payment.setPaymentMethod(order.getPaymentMethod());
        payment.setCreatedAt(LocalDateTime.now());

        if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
            payment.setStatus("PENDING");
            order.setPaymentStatus("PENDING");

        } else if ("UPI".equalsIgnoreCase(order.getPaymentMethod())) {
            // Create UPI payment as PENDING first
            payment.setStatus("PENDING"); // <-- important, not PAID yet
            payment.setGatewayRef("UPI_" + order.getOrderId());
            payment.setUpiVpa("user@upi");

            order.setPaymentStatus("PENDING"); // frontend will show pending until user confirms
        }

        // 5️⃣ Save payment row
        paymentDao.save(payment);

        // 6️⃣ Make sure order contains payment status for response
        order.setPaymentStatus(payment.getStatus());

        return order;
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
        // 1️⃣ Fetch the existing order first
        Order existing = orderDao.getOrderById(orderId);

        if (existing == null) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        // 2️⃣ Prevent cancelling processed orders
        if ("SHIPPED".equalsIgnoreCase(existing.getOrderStatus()) 
                || "DELIVERED".equalsIgnoreCase(existing.getOrderStatus())) {
            throw new OrderAlreadyProcessedException(orderId, existing.getOrderStatus());
        }

        // 3️⃣ Check payment info (optional)
        PaymentDto payment = null;
        try {
            payment = paymentService.getPaymentByOrderId(orderId);
        } catch (RuntimeException e) {
            // No payment exists, ignore
        }

        // 4️⃣ Keep order status always CANCELLED
        String finalOrderStatus = "CANCELLED";

        if (payment != null) {
            String payStatus = payment.getStatus();

            if ("PAID".equalsIgnoreCase(payStatus)) {
                // Refund payment (but do NOT change order status)
                paymentService.updatePaymentStatus(payment.getPayment_id(), "REFUNDED");
            } else if (!"REFUNDED".equalsIgnoreCase(payStatus)) {
                // Not paid or refunded → mark payment FAILED
                paymentService.updatePaymentStatus(payment.getPayment_id(), "FAILED");
            }
        }

        // 5️⃣ Update order status via DAO
        orderDao.updateOrderStatus(orderId, finalOrderStatus);

        // 6️⃣ Fetch updated order and return
        return orderDao.getOrderById(orderId);
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
    
    @Override
    public void updateTotalAmount(int orderId, BigDecimal totalAmount) {
        orderDao.updateTotalAmount(orderId, totalAmount);
    }
    
    @Override
    public List<Order> getOrdersWithPaymentByUser(int userId) {
        List<Order> orders = orderDao.findOrdersWithPaymentByUserId(userId);

        // Optional: sort by date again if needed
        orders.sort((a, b) -> b.getPlacedAt().compareTo(a.getPlacedAt()));

        return orders;
    }

}