package com.ShopSphere.shop_sphere.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import com.ShopSphere.shop_sphere.dto.OrderItemDto;
import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.service.OrderItemsService;

public class OrderItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderItemsService orderItemsService;

    @InjectMocks
    private OrderItemController orderItemController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private OrderItem sampleItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderItemController).build();

        sampleItem = new OrderItem();
        sampleItem.setOrderItemsId(1);
        sampleItem.setOrderId(100);
        sampleItem.setProductId(200);
        sampleItem.setProductName("Widget");
        sampleItem.setQuantity(3);
        sampleItem.setUnitPrice(new BigDecimal("49.99"));
        sampleItem.setTotalItemPrice(new BigDecimal("149.97"));
    }

    @Test
    void testCreateOrderItem_ReturnsCreatedWithLocation() throws Exception {
        OrderItemDto req = new OrderItemDto();
        req.setOrderId(100);
        req.setProductId(200);
        req.setProductName("Widget");
        req.setQuantity(3);
        req.setUnitPrice(new BigDecimal("49.99"));

        when(orderItemsService.createOrderItem(any(OrderItem.class))).thenReturn(sampleItem);

        mockMvc.perform(post("/api/order-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders-items/1"))
                .andExpect(jsonPath("$.orderItemsid").value(1))
                .andExpect(jsonPath("$.orderId").value(100))
                .andExpect(jsonPath("$.productId").value(200))
                .andExpect(jsonPath("$.productName").value("Widget"));

        verify(orderItemsService, times(1)).createOrderItem(any(OrderItem.class));
    }

    @Test
    void testGetOrderItemById_ReturnsDto() throws Exception {
        when(orderItemsService.getOrderItemById(1)).thenReturn(sampleItem);

        mockMvc.perform(get("/api/order-items/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.orderItemsid").value(1))
               .andExpect(jsonPath("$.productName").value("Widget"));

        verify(orderItemsService, times(1)).getOrderItemById(1);
    }

    @Test
    void testGetOrderItemsByOrderId_ReturnsList() throws Exception {
        when(orderItemsService.getOrderItemsByOrderId(100)).thenReturn(List.of(sampleItem));

        mockMvc.perform(get("/api/order-items/order/100"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].orderId").value(100));

        verify(orderItemsService, times(1)).getOrderItemsByOrderId(100);
    }

    @Test
    void testGetOrderItemsByProductId_ReturnsList() throws Exception {
        when(orderItemsService.getOrderItemsByProductId(200)).thenReturn(List.of(sampleItem));

        mockMvc.perform(get("/api/order-items/product/200"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].productId").value(200));

        verify(orderItemsService, times(1)).getOrderItemsByProductId(200);
    }

    @Test
    void testUpdateItemQuantity_ReturnsUpdatedDto() throws Exception {
        OrderItem updated = new OrderItem();
        updated.setOrderItemsId(1);
        updated.setOrderId(100);
        updated.setProductId(200);
        updated.setProductName("Widget");
        updated.setQuantity(5);
        updated.setUnitPrice(new BigDecimal("49.99"));
        updated.setTotalItemPrice(new BigDecimal("249.95"));

        when(orderItemsService.updateItemQuantity(1, 5)).thenReturn(updated);

        mockMvc.perform(put("/api/order-items/1/quantity")
                .param("quantity", "5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.orderItemsid").value(1))
               .andExpect(jsonPath("$.quantity").value(5));

        verify(orderItemsService, times(1)).updateItemQuantity(1, 5);
    }

    @Test
    void testDeleteOrderItem_NoContent() throws Exception {
        doNothing().when(orderItemsService).deleteOrderItem(1);

        mockMvc.perform(delete("/api/order-items/1"))
               .andExpect(status().isNoContent());

        verify(orderItemsService, times(1)).deleteOrderItem(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    void parameterizedOrderItemIds(int id) throws Exception {
        when(orderItemsService.getOrderItemById(id)).thenReturn(sampleItem);
        mockMvc.perform(get("/api/order-items/" + id)).andExpect(status().isOk());
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        // matches your project's pattern
        org.junit.jupiter.api.Assertions.fail("This test is disabled");
    }
}