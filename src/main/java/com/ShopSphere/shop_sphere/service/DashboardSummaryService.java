package com.ShopSphere.shop_sphere.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ShopSphere.shop_sphere.dto.DashboardSummaryDto;
import com.ShopSphere.shop_sphere.dto.DashboardSummaryDto.LowStockProductDto;
import com.ShopSphere.shop_sphere.dto.DashboardSummaryDto.OrdersByStatusDto;
import com.ShopSphere.shop_sphere.dto.DashboardSummaryDto.RecentOrderDto;
import com.ShopSphere.shop_sphere.dto.DashboardSummaryDto.RecentPaymentDto;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.Payment;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.model.User;

// IMPORTANT: import YOUR service interfaces
import com.ShopSphere.shop_sphere.service.OrderService;
import com.ShopSphere.shop_sphere.service.PaymentService;
import com.ShopSphere.shop_sphere.service.ProductService;
import com.ShopSphere.shop_sphere.service.UserService;

@Service
public class DashboardSummaryService {

    public DashboardSummaryService(OrderService orderService, PaymentService paymentService,
			ProductService productService, UserService userService) {
		super();
		this.orderService = orderService;
		this.paymentService = paymentService;
		this.productService = productService;
		this.userService = userService;
	}

	// BEST PRACTICE → final fields + constructor injection
    private  OrderService orderService;
    private  PaymentService paymentService;
    private  ProductService productService;
    private  UserService userService;

    // Constructor injection (Spring will auto-wire this)


    public DashboardSummaryDto getSummary() {

        // Fetch lists from services (DO NOT change these unless your method names differ)
        List<Order> allOrders = orderService.getAllOrders();
        List<Product> allProducts = productService.getAllProducts();
        List<User> allUsers = userService.getAllUsers();
        List<Payment> allPayments = paymentService.getAllPayments();

        // prevent null crashes
        if (allOrders == null) allOrders = new ArrayList<>();
        if (allProducts == null) allProducts = new ArrayList<>();
        if (allUsers == null) allUsers = new ArrayList<>();
        if (allPayments == null) allPayments = new ArrayList<>();

        DashboardSummaryDto dto = new DashboardSummaryDto();

        // ------------------ TOTAL METRICS ------------------

        long totalOrders = allOrders.size();
        long totalProducts = allProducts.size();
        long totalUsers = allUsers.size();

        long successfulPayments = allPayments.stream()
                .filter(p -> p.getStatus() != null &&
                             p.getStatus().equalsIgnoreCase("SUCCESS"))
                .count();

        BigDecimal totalRevenue = allPayments.stream()
                .filter(p -> p.getStatus() != null &&
                             p.getStatus().equalsIgnoreCase("SUCCESS"))
                .map(Payment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setTotalOrders(totalOrders);
        dto.setTotalProducts(totalProducts);
        dto.setTotalUsers(totalUsers);
        dto.setSuccessfulPayments(successfulPayments);
        dto.setTotalRevenue(totalRevenue);

        // ------------------ ORDERS BY STATUS ------------------

        long pending = countOrdersByStatus(allOrders, "PENDING");
        long confirmed = countOrdersByStatus(allOrders, "CONFIRMED");
        long shipped = countOrdersByStatus(allOrders, "SHIPPED");
        long delivered = countOrdersByStatus(allOrders, "DELIVERED");
        long cancelled = countOrdersByStatus(allOrders, "CANCELLED");

        OrdersByStatusDto byStatus = new OrdersByStatusDto(
                pending, confirmed, shipped, delivered, cancelled
        );
        dto.setOrdersByStatus(byStatus);

        // ------------------ RECENT ORDERS (TOP 5) ------------------

        List<Order> sortedOrders = allOrders.stream()
                .filter(o -> o.getPlacedAt() != null)
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<RecentOrderDto> recentOrderDtos = new ArrayList<>();
        for (Order order : sortedOrders) {

            User user = userService.getUserById(order.getUserId());
            String name = (user != null && user.getName() != null)
                    ? user.getName()
                    : "User #" + order.getUserId();

            recentOrderDtos.add(
                new RecentOrderDto(
                        order.getOrderId(),
                        name,
                        order.getTotalAmount(),
                        order.getOrderStatus(),
                        order.getPlacedAt()
                )
            );
        }
        dto.setRecentOrders(recentOrderDtos);

        // ------------------ LOW STOCK (≤ 5) ------------------

        List<LowStockProductDto> lowStockDtos = allProducts.stream()
                .filter(p -> p.getProductQuantity() != null && p.getProductQuantity() <= 5)
                .map(p -> new LowStockProductDto(
                        p.getProductId(),
                        p.getProductName(),
                        p.getProductQuantity()
                ))
                .collect(Collectors.toList());

        dto.setLowStockProducts(lowStockDtos);

        // ------------------ RECENT PAYMENTS (TOP 5) ------------------

        List<Payment> sortedPayments = allPayments.stream()
                .filter(p -> p.getCreatedAt() != null)
                .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<RecentPaymentDto> paymentDtos = new ArrayList<>();
        for (Payment p : sortedPayments) {
            paymentDtos.add(
                new RecentPaymentDto(
                        p.getPaymentId(),
                        p.getOrderId(),
                        p.getAmount(),
                        p.getStatus(),
                        p.getCreatedAt()
                )
            );
        }
        dto.setRecentPayments(paymentDtos);

        return dto;
    }

    // helper method
    private long countOrdersByStatus(List<Order> orders, String status) {
        return orders.stream()
                .filter(o -> o.getOrderStatus() != null &&
                             o.getOrderStatus().equalsIgnoreCase(status))
                .count();
    }
}
