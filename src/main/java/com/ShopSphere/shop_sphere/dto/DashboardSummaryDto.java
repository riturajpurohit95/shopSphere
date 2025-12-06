package com.ShopSphere.shop_sphere.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardSummaryDto {

    public static class OrdersByStatusDto {
        private long pending;
        private long confirmed;
        private long shipped;
        private long delivered;
        private long cancelled;

        public OrdersByStatusDto() {}

        public OrdersByStatusDto(long pending, long confirmed, long shipped, long delivered, long cancelled) {
            this.pending = pending;
            this.confirmed = confirmed;
            this.shipped = shipped;
            this.delivered = delivered;
            this.cancelled = cancelled;
        }

        public long getPending() {
            return pending;
        }

        public void setPending(long pending) {
            this.pending = pending;
        }

        public long getConfirmed() {
            return confirmed;
        }

        public void setConfirmed(long confirmed) {
            this.confirmed = confirmed;
        }

        public long getShipped() {
            return shipped;
        }

        public void setShipped(long shipped) {
            this.shipped = shipped;
        }

        public long getDelivered() {
            return delivered;
        }

        public void setDelivered(long delivered) {
            this.delivered = delivered;
        }

        public long getCancelled() {
            return cancelled;
        }

        public void setCancelled(long cancelled) {
            this.cancelled = cancelled;
        }
    }

    public static class RecentOrderDto {
        private int orderId;
        private String customerName;
        private BigDecimal totalAmount;
        private String orderStatus;
        private LocalDateTime placedAt;

        public RecentOrderDto() {}

        public RecentOrderDto(int orderId, String customerName, BigDecimal totalAmount,
                              String orderStatus, LocalDateTime placedAt) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.totalAmount = totalAmount;
            this.orderStatus = orderStatus;
            this.placedAt = placedAt;
        }

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getOrderStatus() {
            return orderStatus;
        }

        public void setOrderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
        }

        public LocalDateTime getPlacedAt() {
            return placedAt;
        }

        public void setPlacedAt(LocalDateTime placedAt) {
            this.placedAt = placedAt;
        }
    }

    public static class LowStockProductDto {
        private int productId;
        private String productName;
        private Integer productQuantity;

        public LowStockProductDto() {}

        public LowStockProductDto(int productId, String productName, Integer productQuantity) {
            this.productId = productId;
            this.productName = productName;
            this.productQuantity = productQuantity;
        }

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getProductQuantity() {
            return productQuantity;
        }

        public void setProductQuantity(Integer productQuantity) {
            this.productQuantity = productQuantity;
        }
    }

    public static class RecentPaymentDto {
        private int paymentId;
        private int orderId;
        private BigDecimal amount;
        private String status;
        private LocalDateTime createdAt;

        public RecentPaymentDto() {}

        public RecentPaymentDto(int paymentId, int orderId, BigDecimal amount,
                                String status, LocalDateTime createdAt) {
            this.paymentId = paymentId;
            this.orderId = orderId;
            this.amount = amount;
            this.status = status;
            this.createdAt = createdAt;
        }

        public int getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(int paymentId) {
            this.paymentId = paymentId;
        }

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalProducts;
    private long totalUsers;
    private long successfulPayments;
    private OrdersByStatusDto ordersByStatus;
    private List<RecentOrderDto> recentOrders;
    private List<LowStockProductDto> lowStockProducts;
    private List<RecentPaymentDto> recentPayments;

    public DashboardSummaryDto() {}

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getSuccessfulPayments() {
        return successfulPayments;
    }

    public void setSuccessfulPayments(long successfulPayments) {
        this.successfulPayments = successfulPayments;
    }

    public OrdersByStatusDto getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(OrdersByStatusDto ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public List<RecentOrderDto> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<RecentOrderDto> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<LowStockProductDto> getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(List<LowStockProductDto> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public List<RecentPaymentDto> getRecentPayments() {
        return recentPayments;
    }

    public void setRecentPayments(List<RecentPaymentDto> recentPayments) {
        this.recentPayments = recentPayments;
    }
}
