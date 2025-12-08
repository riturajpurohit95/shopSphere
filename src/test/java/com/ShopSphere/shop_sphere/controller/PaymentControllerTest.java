package com.ShopSphere.shop_sphere.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
import com.ShopSphere.shop_sphere.dto.ConfirmUpiRequest;
import com.ShopSphere.shop_sphere.dto.PaymentDto;
import com.ShopSphere.shop_sphere.service.PaymentService;

public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private PaymentDto samplePayment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();

        samplePayment = new PaymentDto();
        // assume setters exist on PaymentDto
        samplePayment.setPayment_id(1);
        samplePayment.setOrderId(101);
        samplePayment.setAmount(new BigDecimal("499.99"));
        samplePayment.setStatus("PENDING");
        samplePayment.setPaymentMethod("UPI");
    }

    @Test
    void testCreatePayment_ReturnsCreatedWithLocation() throws Exception {
        PaymentDto req = new PaymentDto();
        req.setOrderId(101);
        req.setAmount(new BigDecimal("499.99"));
        req.setPaymentMethod("UPI");

        when(paymentService.createPayment(any(PaymentDto.class))).thenReturn(samplePayment);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isCreated())
               .andExpect(header().string("Location", "/api/payments/1"))
               .andExpect(jsonPath("$.payment_id").value(1))
               .andExpect(jsonPath("$.orderId").value(101))
               .andExpect(jsonPath("$.status").value("PENDING"));

        verify(paymentService, times(1)).createPayment(any(PaymentDto.class));
    }

    @Test
    void testConfirmUpiPaymentOrder_Succeeds() throws Exception {
        ConfirmUpiRequest req = new ConfirmUpiRequest();
        req.setUpiVpa("user@upi");

        when(paymentService.confirmUpiPaymentOrder(eq(201), any(), any(), eq("user@upi"))).thenReturn(samplePayment);

        mockMvc.perform(post("/api/payments/confirm-upi/201")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.payment_id").value(1));

        verify(paymentService, times(1)).confirmUpiPaymentOrder(eq(201), any(), any(), eq("user@upi"));
    }

    @Test
    void testGetPaymentById_ReturnsDto() throws Exception {
        when(paymentService.getPaymentById(1)).thenReturn(samplePayment);

        mockMvc.perform(get("/api/payments/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.payment_id").value(1))
               .andExpect(jsonPath("$.orderId").value(101));

        verify(paymentService, times(1)).getPaymentById(1);
    }

    @Test
    void testGetPaymentByOrderId_ReturnsDto() throws Exception {
        when(paymentService.getPaymentByOrderId(101)).thenReturn(samplePayment);

        mockMvc.perform(get("/api/payments/order/101"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.payment_id").value(1))
               .andExpect(jsonPath("$.orderId").value(101));

        verify(paymentService, times(1)).getPaymentByOrderId(101);
    }

    @Test
    void testUpdatePaymentStatus_ReturnsUpdatedDto() throws Exception {
        PaymentDto updated = new PaymentDto();
        updated.setPayment_id(1);
        updated.setOrderId(101);
        updated.setStatus("PAID");

        when(paymentService.updatePaymentStatus(1, "PAID")).thenReturn(updated);

        mockMvc.perform(put("/api/payments/1/status")
                .param("status", "PAID"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("PAID"));

        verify(paymentService, times(1)).updatePaymentStatus(1, "PAID");
    }

    @Test
    void testUpdateGatewayDetails_ReturnsUpdatedDto() throws Exception {
        PaymentDto updated = new PaymentDto();
        updated.setPayment_id(2);
        updated.setOrderId(202);
        updated.setPaymentMethod("RAZORPAY");
        updated.setStatus("PAID");

        when(paymentService.updateGatewayDetails(eq(2), eq("gw-xyz"), eq("user@upi"), eq("payload")))
                .thenReturn(updated);

        mockMvc.perform(put("/api/payments/2/gateway")
                .param("gatewayRef", "gw-xyz")
                .param("upiVpa", "user@upi")
                .param("responsePayload", "payload"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.payment_id").value(2))
               .andExpect(jsonPath("$.paymentMethod").value("RAZORPAY"));

        verify(paymentService, times(1)).updateGatewayDetails(2, "gw-xyz", "user@upi", "payload");
    }

    @Test
    void testGetPaymentsByUserId_NotFound() throws Exception {
        when(paymentService.getPaymentDetails(7)).thenReturn(List.of());

        mockMvc.perform(get("/api/payments/user/7"))
               .andExpect(status().isNotFound())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("No payment records found for userId: 7")));

        verify(paymentService, times(1)).getPaymentDetails(7);
    }

    @Test
    void testGetPaymentsByUserId_Found() throws Exception {
        Map<String, Object> row = Map.of("payment_id", 1, "amount", 499.99, "orderId", 101);
        when(paymentService.getPaymentDetails(5)).thenReturn(List.of(row));

        mockMvc.perform(get("/api/payments/user/5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].payment_id").value(1));

        verify(paymentService, times(1)).getPaymentDetails(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void parameterizedPaymentIds(int id) throws Exception {
        when(paymentService.getPaymentById(id)).thenReturn(samplePayment);
        mockMvc.perform(get("/api/payments/" + id)).andExpect(status().isOk());
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        org.junit.jupiter.api.Assertions.fail("This test is disabled");
    }
}