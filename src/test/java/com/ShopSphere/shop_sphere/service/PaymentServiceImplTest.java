package com.ShopSphere.shop_sphere.service;

import com.ShopSphere.shop_sphere.dto.PaymentDto;
import com.ShopSphere.shop_sphere.exception.PaymentAlreadyCompletedException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Payment;
import com.ShopSphere.shop_sphere.repository.PaymentDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentDao paymentDao;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentDto dto;
    private Payment storedPayment;

    @BeforeEach
    void setUp() {
        dto = new PaymentDto();
        dto.setOrderId(100);
        dto.setUserId(1);
        dto.setAmount(new BigDecimal("200.00"));
        dto.setCurrency("INR");
        dto.setPaymentMethod("COD"); // changed per test
        dto.setStatus("PENDING");

        storedPayment = new Payment();
        storedPayment.setPaymentId(10);
        storedPayment.setOrderId(100);
        storedPayment.setUserId(1);
        storedPayment.setAmount(new BigDecimal("200.00"));
        storedPayment.setCurrency("INR");
        storedPayment.setPaymentMethod("COD");
        storedPayment.setStatus("PENDING");
        storedPayment.setCreatedAt(LocalDateTime.now());
    }

    // ---------------------------------------------------
    // createPayment()
    // ---------------------------------------------------

    @Test
    void createPayment_shouldFail_whenDtoNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(null)
        );
    }

    @Test
    void createPayment_shouldFail_whenOrderIdNull() {
        dto.setOrderId(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.createPayment(dto)
        );
    }

    @Test
    void createPayment_COD_shouldSetPending() {
        dto.setPaymentMethod("COD");

        // simulate dao.save sets paymentId
        doAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setPaymentId(10);
            return 10;
        }).when(paymentDao).save(any(Payment.class));

        when(paymentDao.findById(10)).thenReturn(Optional.of(storedPayment));

        PaymentDto result = paymentService.createPayment(dto);

        assertEquals(10, result.getPayment_id());
        assertEquals("PENDING", result.getStatus());
        verify(paymentDao).save(any(Payment.class));
    }

    @Test
    void createPayment_UPI_shouldMockPaidOrFailed() {
        dto.setPaymentMethod("UPI");

        doAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setPaymentId(11);
            return 11;
        }).when(paymentDao).save(any(Payment.class));

        storedPayment.setStatus("PAID"); // mock final state
        when(paymentDao.findById(11)).thenReturn(Optional.of(storedPayment));

        PaymentDto result = paymentService.createPayment(dto);

        assertEquals("PAID", result.getStatus());
        assertEquals("UPI_100", result.getGatewayRef());
        verify(paymentDao).save(any(Payment.class));
    }

    @Test
    void createPayment_CancelledShouldBecomeRefunded() {
        dto.setStatus("CANCELLED");

        doAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setPaymentId(12);
            return 12;
        }).when(paymentDao).save(any(Payment.class));

        storedPayment.setStatus("REFUNDED");
        when(paymentDao.findById(12)).thenReturn(Optional.of(storedPayment));

        PaymentDto result = paymentService.createPayment(dto);

        assertEquals("REFUNDED", result.getStatus());
    }

    @Test
    void createPayment_daoSaveFailure_shouldThrow() {
        when(paymentDao.save(any(Payment.class))).thenReturn(0);
        assertThrows(
                RuntimeException.class,
                () -> paymentService.createPayment(dto)
        );
    }

    // ---------------------------------------------------
    // getPaymentById()
    // ---------------------------------------------------

    @Test
    void getPaymentById_success() {
        when(paymentDao.findById(10)).thenReturn(Optional.of(storedPayment));

        PaymentDto result = paymentService.getPaymentById(10);

        assertEquals(10, result.getPayment_id());
    }

    @Test
    void getPaymentById_notFound() {
        when(paymentDao.findById(10)).thenReturn(Optional.empty());
        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getPaymentById(10)
        );
    }

    // ---------------------------------------------------
    // getPaymentByOrderId()
    // ---------------------------------------------------

    @Test
    void getPaymentByOrderId_success() {
        when(paymentDao.findByOrderId(100))
                .thenReturn(Optional.of(storedPayment));

        PaymentDto result = paymentService.getPaymentByOrderId(100);

        assertNotNull(result);
        assertEquals(100, result.getOrderId());
    }

    @Test
    void getPaymentByOrderId_nullIfMissing() {
        when(paymentDao.findByOrderId(100)).thenReturn(Optional.empty());

        PaymentDto result = paymentService.getPaymentByOrderId(100);

        assertNull(result);
    }

    // ---------------------------------------------------
    // updatePaymentStatus()
    // ---------------------------------------------------

    @Test
    void updatePaymentStatus_success() {
        storedPayment.setStatus("PENDING");
        when(paymentDao.findById(10)).thenReturn(Optional.of(storedPayment));
        when(paymentDao.updateStatus(10, "PAID")).thenReturn(1);

        PaymentDto updated = new PaymentDto();
        updated.setPayment_id(10);
        updated.setStatus("PAID");
        when(paymentDao.findById(10)).thenReturn(Optional.of(storedPayment));

        PaymentDto result = paymentService.updatePaymentStatus(10, "PAID");

        verify(paymentDao).updateStatus(10, "PAID");
    }

    @Test
    void updatePaymentStatus_sameStatus_shouldReturnExisting() {
        storedPayment.setStatus("PAID");
        when(paymentDao.findById(10)).thenReturn(Optional.of(storedPayment));

        PaymentDto result = paymentService.updatePaymentStatus(10, "PAID");

        verify(paymentDao, never()).updateStatus(anyInt(), anyString());
        assertEquals("PAID", result.getStatus());
    }

    @Test
    void updatePaymentStatus_shouldThrow_whenStatusAlreadyFinal() {
        storedPayment.setStatus("REFUNDED");
        when(paymentDao.findById(10)).thenReturn(Optional.of(storedPayment));

        assertThrows(
                PaymentAlreadyCompletedException.class,
                () -> paymentService.updatePaymentStatus(10, "PAID")
        );
    }

    @Test
    void updatePaymentStatus_daoFails_shouldThrow() {
        storedPayment.setStatus("PENDING");
        when(paymentDao.findById(10)).thenReturn(Optional.of(storedPayment));
        when(paymentDao.updateStatus(10, "PAID")).thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> paymentService.updatePaymentStatus(10, "PAID")
        );
    }

    // ---------------------------------------------------
    // confirmUpiPaymentOrder()
    // ---------------------------------------------------

    @Test
    void confirmUpiPaymentOrder_shouldUpdateStatusAndGatewayFields() {
        storedPayment.setPaymentId(10);
        storedPayment.setStatus("PENDING");

        when(paymentDao.findByOrderId(100))
                .thenReturn(Optional.of(storedPayment));

        when(paymentDao.updateGatewayDetails(eq(10), anyString(), anyString(), anyString()))
                .thenReturn(1);
        when(paymentDao.updateStatus(10, "PAID")).thenReturn(1);

        PaymentDto result = paymentService.confirmUpiPaymentOrder(100, null, null, "test@upi");

        verify(paymentDao).updateGatewayDetails(eq(10), anyString(), eq("test@upi"), anyString());
        verify(paymentDao).updateStatus(eq(10), anyString());
    }

    @Test
    void confirmUpiPaymentOrder_shouldThrow_ifPaymentMissing() {
        when(paymentDao.findByOrderId(100)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.confirmUpiPaymentOrder(100, null, null, "x@upi")
        );
    }

    // ---------------------------------------------------
    // updateGatewayDetails()
    // ---------------------------------------------------

    @Test
    void updateGatewayDetails_success() {
        when(paymentDao.updateGatewayDetails(10, "GREF", "UPI", "RAW"))
                .thenReturn(1);
        when(paymentDao.findById(10))
                .thenReturn(Optional.of(storedPayment));

        PaymentDto result = paymentService.updateGatewayDetails(10, "GREF", "UPI", "RAW");

        verify(paymentDao).updateGatewayDetails(10, "GREF", "UPI", "RAW");
        assertEquals(storedPayment.getOrderId(), result.getOrderId());
    }

    @Test
    void updateGatewayDetails_daoFails_shouldThrow() {
        when(paymentDao.updateGatewayDetails(10, "G", "U", "R"))
                .thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> paymentService.updateGatewayDetails(10, "G", "U", "R")
        );
    }

    // ---------------------------------------------------
    // getPaymentDetails()
    // ---------------------------------------------------

    @Test
    void getPaymentDetails_success() {
        List<Map<String, Object>> rows = new ArrayList<>();
        when(paymentDao.getPaymentDetails(1)).thenReturn(rows);

        List<Map<String, Object>> result = paymentService.getPaymentDetails(1);

        assertSame(rows, result);
        verify(paymentDao).getPaymentDetails(1);
    }
}
