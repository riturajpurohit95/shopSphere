package com.ShopSphere.shop_sphere.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
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
import com.ShopSphere.shop_sphere.dto.OrderRequest;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.service.OrderService;

public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();

        sampleOrder = new Order();
        sampleOrder.setOrderId(101);
        sampleOrder.setUserId(5);
        sampleOrder.setTotalAmount(new BigDecimal("1234.50"));
        sampleOrder.setShippingAddress("Some Address");
        sampleOrder.setOrderStatus("PENDING");
        sampleOrder.setPlacedAt(LocalDateTime.now());
        sampleOrder.setPaymentMethod("COD");
        sampleOrder.setRazorpayOrderId("rzp_123");
        sampleOrder.setPaymentStatus("UNPAID");
    }

    @Test
    void testCreateOrder_ReturnsCreatedWithLocation() throws Exception {
        OrderRequest req = new OrderRequest();
        // set minimal fields on OrderRequest if it has any; tests use any(OrderRequest.class) when stubbing
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/api/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/101"))
                .andExpect(jsonPath("$.orderId").value(101))
                .andExpect(jsonPath("$.userId").value(5));

        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void testGetOrderById_ReturnsDto() throws Exception {
        when(orderService.getOrderById(101)).thenReturn(sampleOrder);

        mockMvc.perform(get("/api/orders/101"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.order_id").value(101))
               .andExpect(jsonPath("$.userId").value(5))
               .andExpect(jsonPath("$.orderStatus").value("PENDING"));

        verify(orderService, times(1)).getOrderById(101);
    }

    @Test
    void testGetOrdersByUserId_ReturnsList() throws Exception {
        when(orderService.getOrdersByUserId(5)).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders/user/5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].order_id").value(101));

        verify(orderService, times(1)).getOrdersByUserId(5);
    }

    @Test
    void testUpdateOrderStatus_Succeeds() throws Exception {
        when(orderService.updateOrderStatus(101, "SHIPPED")).thenReturn(null); // method returns void in controller usage
        when(orderService.getOrderById(101)).thenReturn(sampleOrder);

        mockMvc.perform(put("/api/orders/101/status")
                .param("orderStatus", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order_id").value(101));

        verify(orderService, times(1)).updateOrderStatus(101, "SHIPPED");
        verify(orderService, times(1)).getOrderById(101);
    }

    @Test
    void testUpdatePaymentStatus_Succeeds() throws Exception {
        when(orderService.updatePaymentStatus(101, "PAID")).thenReturn(null);
        when(orderService.getOrderById(101)).thenReturn(sampleOrder);

        mockMvc.perform(put("/api/orders/101/payment-status")
                .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order_id").value(101));

        verify(orderService, times(1)).updatePaymentStatus(101, "PAID");
        verify(orderService, times(1)).getOrderById(101);
    }

    @Test
    void testCancelOrder_ReturnsDto() throws Exception {
        when(orderService.cancelOrder(101)).thenReturn(sampleOrder);

        mockMvc.perform(post("/api/orders/101/cancel"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.order_id").value(101));

        verify(orderService, times(1)).cancelOrder(101);
    }

    @Test
    void testDeleteOrder_NoContent() throws Exception {
        doNothing().when(orderService).deleteOrder(101);

        mockMvc.perform(delete("/api/orders/101"))
               .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(101);
    }

    @Test
    void testEstimateDelivery_ReturnsInt() throws Exception {
        when(orderService.placeOrder(5, 11)).thenReturn(7);

        mockMvc.perform(get("/api/orders/estimate")
                .param("buyerId", "5")
                .param("productId", "11"))
               .andExpect(status().isOk())
               .andExpect(content().string("7"));

        verify(orderService, times(1)).placeOrder(5, 11);
    }

    @Test
    void testGetAllOrders_ReturnsList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].order_id").value(101));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void testGetOrdersBySellerId_ReturnsList() throws Exception {
        when(orderService.getOrdersBySellerId(20)).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders/seller/20"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1));

        verify(orderService, times(1)).getOrdersBySellerId(20);
    }

    @Test
    void testGetOrdersWithItems_NotFound() throws Exception {
        when(orderService.getOrdersWithItems(5)).thenReturn(List.of());

        mockMvc.perform(get("/api/orders/userOrder/5"))
               .andExpect(status().isNotFound())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("No orders found for userId: 5")));

        verify(orderService, times(1)).getOrdersWithItems(5);
    }

    @Test
    void testGetOrdersWithItems_Found() throws Exception {
        Map<String, Object> row = Map.of("order_id", 101, "item_count", 2);
        when(orderService.getOrdersWithItems(5)).thenReturn(List.of(row));

        mockMvc.perform(get("/api/orders/userOrder/5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].order_id").value(101));

        verify(orderService, times(1)).getOrdersWithItems(5);
    }

    @Test
    void testGetOrdersWithPayment_ReturnsDtos() throws Exception {
        Order o1 = new Order();
        o1.setOrderId(201);
        o1.setTotalAmount(new BigDecimal("50.00"));
        o1.setOrderStatus("DELIVERED");
        o1.setPlacedAt(LocalDateTime.now());
        o1.setPaymentMethod("ONLINE");
        o1.setPaymentStatus("PAID");

        when(orderService.getOrdersWithPaymentByUser(5)).thenReturn(List.of(o1));

        mockMvc.perform(get("/api/orders/user-with-payment/5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].order_id").value(201))
               .andExpect(jsonPath("$[0].paymentStatus").value("PAID"));

        verify(orderService, times(1)).getOrdersWithPaymentByUser(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void parameterizedOrderIds(int id) throws Exception {
        when(orderService.getOrderById(id)).thenReturn(sampleOrder);
        mockMvc.perform(get("/api/orders/" + id)).andExpect(status().isOk());
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        throw new AssertionError("disabled");
    }
}