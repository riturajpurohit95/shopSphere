package com.ShopSphere.shop_sphere.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.OrderDto;
import com.ShopSphere.shop_sphere.dto.OrderRequest;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.service.OrderService;

import jakarta.validation.Valid;


@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Order dtoToEntity(OrderDto dto) {
        Order order = new Order();

        if (dto.getOrder_id() != null && dto.getOrder_id() > 0) {
            order.setOrderId(dto.getOrder_id());
        }

        order.setUserId(dto.getUserId());
        order.setTotalAmount(dto.getTotalAmount());
        order.setShippingAddress(dto.getShippingAddress());
        order.setOrderStatus(dto.getOrderStatus());
        order.setPlacedAt(dto.getPlacedAt());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setRazorpayOrderId(dto.getRazorpayOrderId());
        order.setPaymentStatus(dto.getPaymentStatus());


        return order;
    }

    private OrderDto entityToDto(Order order) {
        OrderDto dto = new OrderDto();

        dto.setOrder_id(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPlacedAt(order.getPlacedAt());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setRazorpayOrderId(order.getRazorpayOrderId());
        dto.setPaymentStatus(order.getPaymentStatus());


        return dto;
    }
    @AllowedRoles({"BUYER"})
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest dto) {
        Order created = orderService.createOrder(dto);
        return ResponseEntity
                .created(URI.create("/api/orders/" + created.getOrderId()))
                .body(created);
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable("id") int orderId) {
        return ResponseEntity.ok(entityToDto(orderService.getOrderById(orderId)));
    }

    @AllowedRoles({"BUYER", "ADMIN"})
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@PathVariable int userId) {
        return ResponseEntity.ok(
                orderService.getOrdersByUserId(userId).stream().map(this::entityToDto).collect(Collectors.toList())
        );
    }
//http://localhost:8888/api/orders/1/status?orderStatus=PENDING
//    @AllowedRoles({"ADMIN","SELLER"})
//    @PutMapping("/{id}/status")
//    public ResponseEntity<OrderDto> updateOrderStatus(
//            @PathVariable("id") int orderId,
//            @RequestParam("orderStatus") String orderStatus) {
//
//        orderService.updateOrderStatus(orderId, orderStatus);
//        return ResponseEntity.ok(entityToDto(orderService.getOrderById(orderId)));
//    }

    @AllowedRoles({"ADMIN","SELLER"})
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable("id") int orderId,
            @RequestBody Map<String, String> body) {

        String orderStatus = body.get("orderStatus");
        if (orderStatus == null) {
            return ResponseEntity.badRequest().build();
        }

        // optional: log input for debugging
        System.out.println("UpdateOrderStatus called: id=" + orderId + " status=" + orderStatus);

        orderService.updateOrderStatus(orderId, orderStatus);
        return ResponseEntity.ok(entityToDto(orderService.getOrderById(orderId)));
    }

    @AllowedRoles({ "ADMIN","BUYER"})
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<OrderDto> updatePaymentStatus(
            @PathVariable("id") int orderId,
            @RequestParam("status") String status) {

        orderService.updatePaymentStatus(orderId, status);
        return ResponseEntity.ok(entityToDto(orderService.getOrderById(orderId)));
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable("id") int orderId) {
        return ResponseEntity.ok(entityToDto(orderService.cancelOrder(orderId)));
    }

    @AllowedRoles({"ADMIN"})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") int orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
  //http://localhost:8888/api/orders/estimate?buyerId=2&productId=11
    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/estimate")
    public int estimateDelivery(@RequestParam int buyerId, @RequestParam int productId) {
        return orderService.placeOrder(buyerId, productId);
    }
    @AllowedRoles({"ADMIN","SELLER"})
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderDto> dtoList = orders.stream()
                                       .map(this::entityToDto)
                                       .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @AllowedRoles({"ADMIN","SELLER"})
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<OrderDto>> getOrdersBySellerId(@PathVariable int sellerId) {
        List<Order> orders = orderService.getOrdersBySellerId(sellerId);
        List<OrderDto> dtoList = orders.stream()
                                       .map(this::entityToDto)
                                       .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
    
    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/userOrder/{userId}")
    public ResponseEntity<?> getOrdersWithItems(@PathVariable int userId) {
        List<Map<String, Object>> result = orderService.getOrdersWithItems(userId);

        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("No orders found for userId: " + userId);
        }
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/user-with-payment/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersWithPayment(@PathVariable int userId) {
        List<Order> orders = orderService.getOrdersWithPaymentByUser(userId);

        // Convert to DTOs
        List<OrderDto> dtos = orders.stream().map(order -> {
            OrderDto dto = new OrderDto();
            dto.setOrder_id(order.getOrderId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setOrderStatus(order.getOrderStatus());
            dto.setPlacedAt(order.getPlacedAt());
            dto.setPaymentMethod(order.getPaymentMethod());
            dto.setPaymentStatus(order.getPaymentStatus()); // <-- now frontend gets it
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}

	


