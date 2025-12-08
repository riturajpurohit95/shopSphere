package com.ShopSphere.shop_sphere.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.repository.OrderDao;
import com.ShopSphere.shop_sphere.repository.OrderItemDao;
import com.ShopSphere.shop_sphere.repository.ProductDao;
import com.ShopSphere.shop_sphere.service.ProductService;

public class SellerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductDao productDao;

    @Mock
    private OrderDao orderDao;

    @Mock
    private ProductService productService;

    @Mock
    private OrderItemDao orderItemDao;

    @InjectMocks
    private SellerController sellerController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Product p1;
    private Product pLowStock;
    private Order todayOrder;
    private Order olderOrder;
    private OrderItem oi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(sellerController).build();

        p1 = new Product();
        p1.setProductId(10);
        p1.setProductName("Product One");
        p1.setProductQuantity(20);
        p1.setProductPrice(new BigDecimal("100"));
        p1.setProductMrp(new BigDecimal("120"));
        p1.setUserId(5);

        pLowStock = new Product();
        pLowStock.setProductId(11);
        pLowStock.setProductName("Low Stock");
        pLowStock.setProductQuantity(3); // low stock <=5
        pLowStock.setProductPrice(new BigDecimal("50"));
        pLowStock.setProductMrp(new BigDecimal("60"));
        pLowStock.setUserId(5);

        // order placed today (LocalDateTime)
        todayOrder = new Order();
        todayOrder.setOrderId(1001);
        todayOrder.setUserId(5);
        todayOrder.setOrderStatus("PENDING");
        todayOrder.setPlacedAt(LocalDateTime.now());
        todayOrder.setTotalAmount(new BigDecimal("250.00"));
        todayOrder.setPaymentMethod("COD");

        // older order delivered (yesterday)
        olderOrder = new Order();
        olderOrder.setOrderId(1002);
        olderOrder.setUserId(5);
        olderOrder.setOrderStatus("DELIVERED");
        olderOrder.setPlacedAt(LocalDateTime.now().minusDays(1));
        olderOrder.setTotalAmount(new BigDecimal("150.00"));
        olderOrder.setPaymentMethod("ONLINE");

        oi = new OrderItem();
        oi.setOrderItemsId(1);
        oi.setOrderId(1001);
        oi.setProductId(10);
        oi.setProductName("Product One");
        oi.setQuantity(2);
    }

    // ---------------- Dashboard ----------------
    @Test
    void testGetDashboard_WithProductsAndOrders() throws Exception {
        when(productDao.findBySeller(5)).thenReturn(List.of(p1, pLowStock));
        when(orderDao.findByUserId(5)).thenReturn(List.of(todayOrder, olderOrder));

        mockMvc.perform(get("/api/seller/dashboard").param("userId", "5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.stats.totalOrders").value(2))
               .andExpect(jsonPath("$.stats.activeProducts").value(2))
               .andExpect(jsonPath("$.lowStockAlerts").isArray())
               .andExpect(jsonPath("$.notifications").isArray());

        verify(productDao, times(1)).findBySeller(5);
        verify(orderDao, times(1)).findByUserId(5);
    }

    @Test
    void testGetDashboard_NoProductsOrOrders() throws Exception {
        when(productDao.findBySeller(7)).thenReturn(Collections.emptyList());
        when(orderDao.findByUserId(7)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/seller/dashboard").param("userId", "7"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.stats.totalOrders").value(0))
               .andExpect(jsonPath("$.stats.activeProducts").value(0))
               .andExpect(jsonPath("$.lowStockAlerts").isArray());

        verify(productDao, times(1)).findBySeller(7);
        verify(orderDao, times(1)).findByUserId(7);
    }

    // ---------------- Seller Products endpoints ----------------
    @Test
    void testGetSellerProducts_ReturnsList() throws Exception {
        when(productDao.findBySeller(5)).thenReturn(List.of(p1, pLowStock));

        mockMvc.perform(get("/api/seller/products").param("userId", "5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(2))
               .andExpect(jsonPath("$[0].productId").value(10));

        verify(productDao, times(1)).findBySeller(5);
    }

    @Test
    void testCreateSellerProduct_Success_DefaultsAndReturn() throws Exception {
        Product incoming = new Product();
        incoming.setProductName("New Prod");
        incoming.setProductPrice(new BigDecimal("40"));
        incoming.setProductMrp(new BigDecimal("50"));
        // productQuantity null -> should default 0 inside controller

        Product created = new Product();
        created.setProductId(77);
        created.setUserId(5);
        created.setProductName("New Prod");
        created.setProductQuantity(0);

        when(productService.createProduct(any(Product.class))).thenReturn(created);

        mockMvc.perform(post("/api/seller/products")
                .param("userId", "5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incoming)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.productId").value(77))
               .andExpect(jsonPath("$.userId").value(5));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void testCreateSellerProduct_PriceGreaterThanMrp_Throws() throws Exception {
        Product incoming = new Product();
        incoming.setProductName("Bad Prod");
        incoming.setProductPrice(new BigDecimal("200"));
        incoming.setProductMrp(new BigDecimal("100")); // price > mrp => invalid

        mockMvc.perform(post("/api/seller/products")
                .param("userId", "5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incoming)))
               .andExpect(status().isInternalServerError()); // IllegalArgumentException -> 500

        verify(productService, never()).createProduct(any());
    }

    @Test
    void testUpdateSellerProduct_Success() throws Exception {
        Product incoming = new Product();
        incoming.setProductPrice(new BigDecimal("80"));
        incoming.setProductMrp(new BigDecimal("100"));

        Product updated = new Product();
        updated.setProductId(10);
        updated.setUserId(5);
        updated.setProductPrice(new BigDecimal("80"));

        when(productService.updateProduct(any(Product.class))).thenReturn(updated);

        mockMvc.perform(put("/api/seller/products/10")
                .param("userId", "5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incoming)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.productId").value(10))
               .andExpect(jsonPath("$.productPrice").value(80));

        verify(productService, times(1)).updateProduct(any(Product.class));
    }

    @Test
    void testUpdateSellerProduct_PriceGreaterThanMrp_Throws() throws Exception {
        Product incoming = new Product();
        incoming.setProductPrice(new BigDecimal("200"));
        incoming.setProductMrp(new BigDecimal("100"));

        mockMvc.perform(put("/api/seller/products/10")
                .param("userId", "5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incoming)))
               .andExpect(status().isInternalServerError());

        verify(productService, never()).updateProduct(any());
    }

    @Test
    void testDeleteSellerProduct_NoContent() throws Exception {
        doNothing().when(productService).deleteProduct(10);

        mockMvc.perform(delete("/api/seller/products/10").param("userId", "5"))
               .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(10);
    }

    // ---------------- Recent Orders ----------------
    @Test
    void testGetRecentOrders_ReturnsMappedList() throws Exception {
        // prepare orders
        Order o1 = todayOrder;
        Order o2 = olderOrder;
        when(orderDao.findByUserId(5)).thenReturn(List.of(o1, o2));
        when(orderItemDao.findByOrderId(1001)).thenReturn(List.of(oi));
        when(orderItemDao.findByOrderId(1002)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/seller/orders/recent").param("userId", "5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(2))
               .andExpect(jsonPath("$[0].orderId").value(1001))
               .andExpect(jsonPath("$[0].productName").isString());

        verify(orderDao, times(1)).findByUserId(5);
        verify(orderItemDao, times(1)).findByOrderId(1001);
    }

    // ---------------- Update Order Status ----------------
    @Test
    void testUpdateOrderStatus_CallsDao() throws Exception {
        Map<String, String> body = Map.of("status", "SHIPPED");

        doNothing().when(orderDao).updateOrderStatus(1001, "SHIPPED");

        mockMvc.perform(put("/api/seller/orders/1001/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
               .andExpect(status().isOk());

        verify(orderDao, times(1)).updateOrderStatus(1001, "SHIPPED");
    }

    // parameterized sanity check
    @ParameterizedTest
    @ValueSource(ints = {5, 10})
    void parameterizedSellerIds(int id) throws Exception {
        when(productDao.findBySeller(id)).thenReturn(List.of(p1));
        mockMvc.perform(get("/api/seller/products").param("userId", String.valueOf(id))).andExpect(status().isOk());
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        org.junit.jupiter.api.Assertions.fail("This test is disabled");
    }
}