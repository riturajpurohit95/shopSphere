/*package com.ShopSphere.shop_sphere.controller;

import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.repository.OrderDao;
import com.ShopSphere.shop_sphere.repository.ProductDao;
import com.ShopSphere.shop_sphere.service.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@CrossOrigin(origins = "http://localhost:5000")  // change to your React port
public class SellerController {

    private final ProductDao productDao;
    private final OrderDao orderDao;
    private final ProductService productService;

    public SellerController(ProductDao productDao,
                            OrderDao orderDao,
                            ProductService productService) {
        this.productDao = productDao;
        this.orderDao = orderDao;
        this.productService = productService;
    }

    // ------------ MAIN DASHBOARD API ------------
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(@RequestParam("userId") int userId) {

        // ALL seller data is based on these 2 lists:
        List<Product> products = productDao.findBySeller(userId);
        List<Order>   orders   = orderDao.findBySeller(userId);

        System.out.println("Dashboard for seller " + userId +
                " -> products: " + products.size() + ", orders: " + orders.size());

        // ---- stats ----
        BigDecimal todaySales   = BigDecimal.ZERO;
        int        totalOrders  = orders.size();
        int        activeProducts = 0;
        BigDecimal pendingPayout = BigDecimal.ZERO;

        LocalDate today = LocalDate.now();

        for (Order order : orders) {
            if (order.getPlacedAt() != null &&
                order.getPlacedAt().toLocalDate().isEqual(today) &&
                order.getTotalAmount() != null) {

                todaySales = todaySales.add(order.getTotalAmount());
            }

            if (order.getTotalAmount() != null &&
                "DELIVERED".equalsIgnoreCase(order.getOrderStatus())) {
                pendingPayout = pendingPayout.add(order.getTotalAmount());
            }
        }

        for (Product product : products) {
            if (product.getProductQuantity() != null &&
                product.getProductQuantity() > 0) {
                activeProducts++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("todaySales",    todaySales);
        stats.put("totalOrders",   totalOrders);
        stats.put("activeProducts", activeProducts);
        stats.put("pendingPayout", pendingPayout);

        // ---- low stock alerts (<= 5 qty) ----
        List<Map<String, Object>> lowStockAlerts = products.stream()
                .filter(p -> p.getProductQuantity() != null && p.getProductQuantity() <= 5)
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("productId",    p.getProductId());
                    m.put("productName",  p.getProductName());
                    m.put("currentStock", p.getProductQuantity());
                    return m;
                })
                .collect(Collectors.toList());

        System.out.println("Low stock for seller " + userId +
                ": " + lowStockAlerts.size());

        // ---- simple notifications ----
        List<Map<String, Object>> notifications = new ArrayList<>();

        long todayOrders = orders.stream()
                .filter(o -> o.getPlacedAt() != null &&
                             o.getPlacedAt().toLocalDate().isEqual(today))
                .count();

        if (todayOrders > 0) {
            Map<String, Object> n1 = new HashMap<>();
            n1.put("id", 1);
            n1.put("message", "You have " + todayOrders + " new order(s) today.");
            n1.put("time", "Today");
            notifications.add(n1);
        }

        if (!lowStockAlerts.isEmpty()) {
            Map<String, Object> n2 = new HashMap<>();
            n2.put("id", 2);
            n2.put("message", lowStockAlerts.size() + " product(s) are low on stock.");
            n2.put("time", "Inventory");
            notifications.add(n2);
        }

        // ---- final response ----
        Map<String, Object> response = new HashMap<>();
        response.put("stats",          stats);
        response.put("notifications",  notifications);
        response.put("lowStockAlerts", lowStockAlerts);

        return response;
    }

    // ------------ PRODUCTS APIs ------------

    @GetMapping("/products")
    public List<Product> getSellerProducts(@RequestParam("userId") int userId) {
        return productDao.findBySeller(userId);
    }

    @PostMapping("/products")
    public Product createSellerProduct(@RequestParam("userId") int userId,
                                       @RequestBody Product product) {

        product.setUserId(userId);

        // safe defaults
        if (product.getProductQuantity() == null) {
            product.setProductQuantity(0);
        }
        if (product.getProductAvgRating() == null) {
            product.setProductAvgRating(BigDecimal.ZERO);
        }
        if (product.getProductReviewsCount() == null) {
            product.setProductReviewsCount(0);
        }
        if (product.getImageUrl() == null) {
            product.setImageUrl("");
        }

        return productService.createProduct(product);
    }

    @PutMapping("/products/{productId}")
    public Product updateSellerProduct(@PathVariable int productId,
                                       @RequestParam("userId") int userId,
                                       @RequestBody Product product) {

        product.setProductId(productId);
        product.setUserId(userId);
        return productService.updateProduct(product);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteSellerProduct(@PathVariable int productId,
                                                    @RequestParam("userId") int userId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // ------------ RECENT ORDERS FOR SELLER ------------

    @GetMapping("/orders/recent")
    public List<Map<String, Object>> getRecentOrders(@RequestParam("userId") int userId) {
        List<Order> orders = orderDao.findBySeller(userId);

        System.out.println("Recent orders for seller " + userId +
                ": " + orders.size());

        return orders.stream()
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .limit(10)
                .map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("orderId",       o.getOrderId());
                    m.put("orderDate",     o.getPlacedAt());
                    m.put("productName",   o.getProductName());
                    m.put("totalAmount",   o.getTotalAmount());
                    m.put("status",        o.getOrderStatus());
                    m.put("paymentStatus", o.getStatus());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable int orderId,
                                                  @RequestBody Map<String, String> body) {
        String status = body.get("status");
        orderDao.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }
}*/
package com.ShopSphere.shop_sphere.controller;

import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.repository.OrderDao;
import com.ShopSphere.shop_sphere.repository.OrderItemDao;
import com.ShopSphere.shop_sphere.repository.ProductDao;
import com.ShopSphere.shop_sphere.service.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@CrossOrigin(origins = "http://localhost:3000")
public class SellerController {

    private final ProductDao productDao;
    private final OrderDao orderDao;
    private final ProductService productService;
    private final OrderItemDao orderItemDao;

    public SellerController(ProductDao productDao,
                            OrderDao orderDao,
                            ProductService productService,OrderItemDao orderItemDao) {
        this.productDao = productDao;
        this.orderDao = orderDao;
        this.productService = productService;
        this.orderItemDao = orderItemDao;
    }

    // ------------ MAIN DASHBOARD API ------------
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(@RequestParam("userId") int userId) {

        List<Product> products = Collections.emptyList();
        List<Order> orders = Collections.emptyList();

        // products for this seller
        try {
            products = productDao.findBySeller(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // orders that contain this seller's products
        try {
            orders = orderDao.findBySeller(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ---- stats ----
        BigDecimal todaySales     = BigDecimal.ZERO;
        int        totalOrders    = orders.size();
        int        activeProducts = 0;
        BigDecimal pendingPayout  = BigDecimal.ZERO;

        LocalDate today = LocalDate.now();

        for (Order order : orders) {
            if (order.getPlacedAt() != null &&
                order.getPlacedAt().toLocalDate().isEqual(today) &&
                order.getTotalAmount() != null) {

                todaySales = todaySales.add(order.getTotalAmount());
            }

            if (order.getTotalAmount() != null &&
                "DELIVERED".equalsIgnoreCase(order.getOrderStatus())) {
                pendingPayout = pendingPayout.add(order.getTotalAmount());
            }
        }

        for (Product product : products) {
            if (product.getProductQuantity() != null &&
                product.getProductQuantity() > 0) {
                activeProducts++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("todaySales",     todaySales);
        stats.put("totalOrders",    totalOrders);
        stats.put("activeProducts", activeProducts);
        stats.put("pendingPayout",  pendingPayout);

        // ---- low stock alerts (<= 5 qty) ----
        List<Map<String, Object>> lowStockAlerts = products.stream()
                .filter(p -> p.getProductQuantity() != null && p.getProductQuantity() <= 5)
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("productId",    p.getProductId());
                    m.put("productName",  p.getProductName());
                    m.put("currentStock", p.getProductQuantity());
                    return m;
                })
                .collect(Collectors.toList());

        // ---- simple notifications ----
        List<Map<String, Object>> notifications = new ArrayList<>();

        long todayOrders = orders.stream()
                .filter(o -> o.getPlacedAt() != null &&
                             o.getPlacedAt().toLocalDate().isEqual(today))
                .count();

        if (todayOrders > 0) {
            Map<String, Object> n1 = new HashMap<>();
            n1.put("id", 1);
            n1.put("message", "You have " + todayOrders + " new order(s) today.");
            n1.put("time", "Today");
            notifications.add(n1);
        }

        if (!lowStockAlerts.isEmpty()) {
            Map<String, Object> n2 = new HashMap<>();
            n2.put("id", 2);
            n2.put("message", lowStockAlerts.size() + " product(s) are low on stock.");
            n2.put("time", "Inventory");
            notifications.add(n2);
        }

        // ---- final response ----
        Map<String, Object> response = new HashMap<>();
        response.put("stats",          stats);
        response.put("notifications",  notifications);
        response.put("lowStockAlerts", lowStockAlerts);

        return response;
    }

    // ------------ PRODUCTS APIs ------------

    @GetMapping("/products")
    public List<Product> getSellerProducts(@RequestParam("userId") int userId) {
        return productDao.findBySeller(userId);
    }

    @PostMapping("/products")
    public Product createSellerProduct(@RequestParam("userId") int userId,
                                       @RequestBody Product product) {

        product.setUserId(userId);

        // Price <= MRP check
        if (product.getProductMrp() != null &&
            product.getProductPrice() != null &&
            product.getProductPrice().compareTo(product.getProductMrp()) > 0) {
            throw new IllegalArgumentException("Product price cannot be greater than MRP");
        }

        if (product.getProductQuantity() == null) {
            product.setProductQuantity(0);
        }
        if (product.getProductAvgRating() == null) {
            product.setProductAvgRating(BigDecimal.ZERO);
        }
        if (product.getProductReviewsCount() == null) {
            product.setProductReviewsCount(0);
        }
        if (product.getImageUrl() == null) {
            product.setImageUrl("");
        }

        return productService.createProduct(product);
    }

    @PutMapping("/products/{productId}")
    public Product updateSellerProduct(@PathVariable int productId,
                                       @RequestParam("userId") int userId,
                                       @RequestBody Product product) {

        product.setProductId(productId);
        product.setUserId(userId);

        if (product.getProductMrp() != null &&
            product.getProductPrice() != null &&
            product.getProductPrice().compareTo(product.getProductMrp()) > 0) {
            throw new IllegalArgumentException("Product price cannot be greater than MRP");
        }

        return productService.updateProduct(product);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteSellerProduct(@PathVariable int productId,
                                                    @RequestParam("userId") int userId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    // ------------ RECENT ORDERS FOR SELLER ------------

    @GetMapping("/orders/recent")
    public List<Map<String, Object>> getRecentOrders(@RequestParam("userId") int userId) {

        // fetch all orders for the user
        List<Order> orders = orderDao.findBySeller(userId);

        return orders.stream()
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed()) // sort by date descending
                .limit(100) // take only 10 most recent
                .map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("orderId", o.getOrderId());
                    m.put("orderDate", o.getPlacedAt());

                    // fetch all items for this order
                    List<OrderItem> items = orderItemDao.findByOrderId(o.getOrderId());

                    // get the first product name (or a comma-separated list if you prefer)
                    String productNames = items.stream()
                                               .map(OrderItem::getProductName)
                                               .collect(Collectors.joining(", "));
                    m.put("productName", productNames);

                    m.put("totalAmount", o.getTotalAmount());
                    m.put("status", o.getOrderStatus());
                    m.put("paymentStatus", o.getPaymentMethod());
                    return m;
                })
                .collect(Collectors.toList());
    }


    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable int orderId,
                                                  @RequestBody Map<String, String> body) {
        String status = body.get("status");
        orderDao.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }
}

