package com.ShopSphere.shop_sphere.repository;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.ShopSphere.shop_sphere.model.Payment;

public class PaymentDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PaymentDaoImpl paymentDao;

    private Payment payment;
    private Payment savedPayment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        payment = new Payment();
        payment.setOrderId(500);
        payment.setUserId(42);
        payment.setAmount(new BigDecimal("299.99"));
        payment.setCurrency("INR");
        payment.setPaymentMethod("UPI");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus("PENDING");

        savedPayment = new Payment();
        savedPayment.setPaymentId(77);
        savedPayment.setOrderId(500);
        savedPayment.setUserId(42);
        savedPayment.setAmount(new BigDecimal("299.99"));
        savedPayment.setCurrency("INR");
        savedPayment.setPaymentMethod("UPI");
        savedPayment.setCreatedAt(LocalDateTime.now());
        savedPayment.setStatus("SUCCESS");
    }

    // ---------- save ----------
    @Test
    void testSave_SetsGeneratedId() {
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            kh.getKeyList().add(Map.of("GENERATED_KEY", 123));
            return 1;
        }).when(jdbcTemplate).update(any(), any(KeyHolder.class));

        int id = paymentDao.save(payment);
        assertEquals(123, id);
        assertEquals(123, payment.getPaymentId());
        verify(jdbcTemplate, times(1)).update(any(), any(KeyHolder.class));
    }

    // ---------- findById ----------
    @Test
    void testFindById_Found() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(77)))
                .thenReturn(Arrays.asList(savedPayment));

        Optional<Payment> opt = paymentDao.findById(77);
        assertTrue(opt.isPresent());
        assertEquals(77, opt.get().getPaymentId());
        verify(jdbcTemplate, times(1)).query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(77));
    }

    @Test
    void testFindById_NotFound() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(9999)))
                .thenReturn(Arrays.asList());

        Optional<Payment> opt = paymentDao.findById(9999);
        assertFalse(opt.isPresent());
    }

    // ---------- findAll ----------
    @Test
    void testFindAll_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(Arrays.asList(savedPayment));

        List<Payment> list = paymentDao.findAll();
        assertEquals(1, list.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(org.springframework.jdbc.core.RowMapper.class));
    }

    // ---------- findByOrderId ----------
    @Test
    void testFindByOrderId_Found() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(500)))
                .thenReturn(Arrays.asList(savedPayment));

        Optional<Payment> opt = paymentDao.findByOrderId(500);
        assertTrue(opt.isPresent());
        assertEquals(500, opt.get().getOrderId());
    }

    @Test
    void testFindByOrderId_NotFound() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(12345)))
                .thenReturn(Arrays.asList());

        Optional<Payment> opt = paymentDao.findByOrderId(12345);
        assertFalse(opt.isPresent());
    }

    // ---------- updateStatus ----------
    @Test
    void testUpdateStatus_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq("SUCCESS"), eq(77))).thenReturn(1);
        int rows = paymentDao.updateStatus(77, "SUCCESS");
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq("SUCCESS"), eq(77));
    }

    // ---------- updateGatewayDetails ----------
    @Test
    void testUpdateGatewayDetails_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq("gw_123"), eq("vpa@upi"), eq("payload"), eq(77))).thenReturn(1);
        int rows = paymentDao.updateGatewayDetails(77, "gw_123", "vpa@upi", "payload");
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq("gw_123"), eq("vpa@upi"), eq("payload"), eq(77));
    }

    // ---------- getPaymentDetails ----------
    @Test
    void testGetPaymentDetails_ReturnsList() {
        Map<String, Object> row = Map.of(
                "payment_id", 77,
                "amount", new BigDecimal("299.99"),
                "status", "SUCCESS",
                "order_id", 500,
                "total_amount", new BigDecimal("299.99")
        );

        when(jdbcTemplate.queryForList(anyString(), eq(42))).thenReturn(List.of(row));

        List<Map<String, Object>> res = paymentDao.getPaymentDetails(42);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(77, res.get(0).get("payment_id"));
        verify(jdbcTemplate, times(1)).queryForList(anyString(), eq(42));
    }

    // parameterized
    @ParameterizedTest
    @ValueSource(ints = {77, 88})
    void parameterizedPaymentIds(int id) {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(id)))
                .thenReturn(Arrays.asList(savedPayment));
        Optional<Payment> p = paymentDao.findById(id);
        assertTrue(p.isPresent());
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled and skipped");
    }
}