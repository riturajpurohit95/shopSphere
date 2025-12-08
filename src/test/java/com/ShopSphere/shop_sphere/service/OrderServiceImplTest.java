package com.ShopSphere.shop_sphere.service;

import com.ShopSphere.shop_sphere.dto.OrderItemDto;
import com.ShopSphere.shop_sphere.dto.OrderRequest;
import com.ShopSphere.shop_sphere.dto.PaymentDto;
import com.ShopSphere.shop_sphere.exception.OrderAlreadyProcessedException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.model.Payment;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.repository.OrderDao;
import com.ShopSphere.shop_sphere.repository.OrderItemDao;
import com.ShopSphere.shop_sphere.repository.PaymentDao;
import com.ShopSphere.shop_sphere.repository.ProductDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDao orderDao;
    @Mock
    private PaymentService paymentService;
    @Mock
    private DeliveryService deliveryService;
    @Mock
    private ProductDao productDao;
    @Mock
    private OrderItemDao orderItemDao;
    @Mock
    private PaymentDao paymentDao;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product product;
    private OrderRequest orderRequest;
    private OrderItemDto itemDto;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId(10);
        product.setProductName("Test Product");
        product.setProductPrice(new BigDecimal("100.00"));
        product.setUserId(99); // sellerId

        itemDto = new OrderItemDto();
        // only fields used in service: productId, quantity
        itemDto.setProductId(10);
        itemDto.setQuantity(2);

        orderRequest = new OrderRequest();
        orderRequest.setUserId(1);
        orderRequest.setShippingAddress("Test Address");
        orderRequest.setPaymentMethod("COD");
        orderRequest.setItems(Collections.singletonList(itemDto));
    }

    // ──────────────────────────────────────────────
    // createOrder()
    // ──────────────────────────────────────────────

    @Test
    void createOrder_shouldThrow_whenItemsEmpty() {
        OrderRequest emptyReq = new OrderRequest();
        emptyReq.setUserId(1);
        emptyReq.setItems(Collections.emptyList());
        emptyReq.setShippingAddress("X");
        emptyReq.setPaymentMethod("COD");

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(emptyReq)
        );
    }

    @Test
    void createOrder_success_COD() {
        // OrderDao.save will set orderId in the entity, we simulate by side-effect
        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setOrderId(123);
            return 123;
        }).when(orderDao).save(any(Order.class));

        when(productDao.findById(10)).thenReturn(Optional.of(product));

        Order result = orderService.createOrder(orderRequest);

        assertNotNull(result);
        assertEquals(123, result.getOrderId());
        assertEquals(1, result.getUserId());
        assertEquals("COD", result.getPaymentMethod());
        // totalAmount should be 100 * 2 = 200
        assertEquals(new BigDecimal("200.00"), result.getTotalAmount());

        // verify items saved
        verify(orderItemDao, times(1)).save(any(OrderItem.class));
        // verify order total updated
        verify(orderDao).updateTotalAmount(123, new BigDecimal("200.00"));
        // verify payment saved
        verify(paymentDao).save(any(Payment.class));
    }

    // ──────────────────────────────────────────────
    // getOrderById()
    // ──────────────────────────────────────────────

    @Test
    void getOrderById_success() {
        Order o = new Order();
        o.setOrderId(10);
        when(orderDao.findById(10)).thenReturn(Optional.of(o));

        Order result = orderService.getOrderById(10);

        assertEquals(10, result.getOrderId());
        verify(orderDao).findById(10);
    }

    @Test
    void getOrderById_notFound() {
        when(orderDao.findById(10)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById(10)
        );
    }

    // ──────────────────────────────────────────────
    // getAllOrders / getOrdersByUserId / getOrdersBySellerId
    // ──────────────────────────────────────────────

    @Test
    void getAllOrders_delegatesToDao() {
        when(orderDao.findAll()).thenReturn(Arrays.asList(new Order(), new Order()));

        List<Order> list = orderService.getAllOrders();

        assertEquals(2, list.size());
        verify(orderDao).findAll();
    }

    @Test
    void getOrdersByUserId_delegatesToDao() {
        when(orderDao.findByUserId(1)).thenReturn(Collections.singletonList(new Order()));

        List<Order> list = orderService.getOrdersByUserId(1);

        assertEquals(1, list.size());
        verify(orderDao).findByUserId(1);
    }

    @Test
    void getOrdersBySellerId_delegatesToDao() {
        when(orderDao.findBySeller(99)).thenReturn(Collections.singletonList(new Order()));

        List<Order> list = orderService.getOrdersBySellerId(99);

        assertEquals(1, list.size());
        verify(orderDao).findBySeller(99);
    }

    // ──────────────────────────────────────────────
    // updateOrderStatus() → also triggers syncPaymentWithOrder()
    // ──────────────────────────────────────────────

    @Test
    void updateOrderStatus_success_noPayment() {
        when(orderDao.updateOrderStatus(10, "DELIVERED")).thenReturn(1);
        // syncPaymentWithOrder will call paymentService.getPaymentByOrderId
        when(paymentService.getPaymentByOrderId(10)).thenReturn(null);

        int rows = orderService.updateOrderStatus(10, "DELIVERED");

        assertEquals(1, rows);
        verify(orderDao).updateOrderStatus(10, "DELIVERED");
        verify(paymentService).getPaymentByOrderId(10);
        // no updatePaymentStatus because payment is null
        verify(paymentService, never()).updatePaymentStatus(anyInt(), anyString());
    }

    @Test
    void updateOrderStatus_shouldUpdatePayment_forCODDelivered() {
        when(orderDao.updateOrderStatus(10, "DELIVERED")).thenReturn(1);

        PaymentDto payment = new PaymentDto();
        payment.setPayment_id(5);
        payment.setPaymentMethod("COD");
        payment.setStatus("PENDING");

        when(paymentService.getPaymentByOrderId(10)).thenReturn(payment);

        orderService.updateOrderStatus(10, "DELIVERED");

        // COD + DELIVERED should lead to PAID
        verify(paymentService).updatePaymentStatus(5, "PAID");
    }

    // ──────────────────────────────────────────────
    // updatePaymentStatus()
    // ──────────────────────────────────────────────

    @Test
    void updatePaymentStatus_success_paidFromPending() {
        PaymentDto payment = new PaymentDto();
        payment.setPayment_id(7);
        payment.setStatus("PENDING");
        payment.setPaymentMethod("UPI");

        when(paymentService.getPaymentByOrderId(10)).thenReturn(payment);

        Order order = new Order();
        order.setOrderId(10);
        order.setOrderStatus("PENDING");
        when(orderDao.findById(10)).thenReturn(Optional.of(order));

        when(orderDao.updateOrderStatus(10, "PROCESSING")).thenReturn(1);

        int rows = orderService.updatePaymentStatus(10, "PAID");

        assertEquals(1, rows);
        verify(paymentService).updatePaymentStatus(7, "PAID");
        verify(orderDao).updateOrderStatus(10, "PROCESSING");
    }

    @Test
    void updatePaymentStatus_shouldThrow_whenPaymentMissing() {
        when(paymentService.getPaymentByOrderId(10)).thenReturn(null);

        assertThrows(
                RuntimeException.class,
                () -> orderService.updatePaymentStatus(10, "PAID")
        );
    }

    // ──────────────────────────────────────────────
    // cancelOrder()
    // ──────────────────────────────────────────────

    @Test
    void cancelOrder_shouldThrow_whenAlreadyDeliveredOrShipped() {
        Order o = new Order();
        o.setOrderId(10);
        o.setOrderStatus("DELIVERED");

        when(orderDao.findById(10)).thenReturn(Optional.of(o));

        assertThrows(
                OrderAlreadyProcessedException.class,
                () -> orderService.cancelOrder(10)
        );
    }

    @Test
    void cancelOrder_codPaid_shouldRefundAndSetRefunded() {
        Order o = new Order();
        o.setOrderId(10);
        o.setOrderStatus("PENDING");

        when(orderDao.findById(10)).thenReturn(Optional.of(o));

        PaymentDto p = new PaymentDto();
        p.setPayment_id(5);
        p.setStatus("PAID");
        p.setPaymentMethod("COD");

        when(paymentService.getPaymentByOrderId(10)).thenReturn(p);
        when(orderDao.updateOrderStatus(10, "REFUNDED")).thenReturn(1);

        Order after = new Order();
        after.setOrderId(10);
        after.setOrderStatus("REFUNDED");
        when(orderDao.findById(10)).thenReturn(Optional.of(after));

        Order result = orderService.cancelOrder(10);

        verify(paymentService).updatePaymentStatus(5, "REFUNDED");
        assertEquals("REFUNDED", result.getOrderStatus());
    }

    // ──────────────────────────────────────────────
    // deleteOrder()
    // ──────────────────────────────────────────────

    @Test
    void deleteOrder_success() {
        Order o = new Order();
        o.setOrderId(10);
        when(orderDao.findById(10)).thenReturn(Optional.of(o));
        when(orderDao.deleteById(10)).thenReturn(1);

        assertDoesNotThrow(() -> orderService.deleteOrder(10));

        verify(orderDao).deleteById(10);
    }

    @Test
    void deleteOrder_shouldThrow_whenDaoReturnsZero() {
        Order o = new Order();
        o.setOrderId(10);
        when(orderDao.findById(10)).thenReturn(Optional.of(o));
        when(orderDao.deleteById(10)).thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> orderService.deleteOrder(10)
        );
    }

    // ──────────────────────────────────────────────
    // placeOrder()
    // ──────────────────────────────────────────────

    @Test
    void placeOrder_shouldReturnDeliveryDays() {
        when(deliveryService.calculateDeliveryDays(1, 10)).thenReturn(4);

        int days = orderService.placeOrder(1, 10);

        assertEquals(4, days);
        verify(deliveryService).calculateDeliveryDays(1, 10);
    }

    // ──────────────────────────────────────────────
    // getOrdersWithItems()
    // ──────────────────────────────────────────────

    @Test
    void getOrdersWithItems_delegatesToDao() {
        List<Map<String, Object>> rows = new ArrayList<>();
        when(orderDao.getOrdersWithItems(1)).thenReturn(rows);

        List<Map<String, Object>> result = orderService.getOrdersWithItems(1);

        assertSame(rows, result);
        verify(orderDao).getOrdersWithItems(1);
    }

    // ──────────────────────────────────────────────
    // expireOldPendingOrders()
    // ──────────────────────────────────────────────

    @Test
    void expireOldPendingOrders_success() {
        Order o1 = new Order();
        o1.setOrderId(10);

        Order o2 = new Order();
        o2.setOrderId(20);

        List<Order> oldOrders = Arrays.asList(o1, o2);
        when(orderDao.findByStatusAndPlacedAtBefore(eq("PENDING"), any()))
                .thenReturn(oldOrders);

        OrderItem it1 = new OrderItem();
        it1.setProductId(5);
        it1.setQuantity(2);

        OrderItem it2 = new OrderItem();
        it2.setProductId(7);
        it2.setQuantity(1);

        when(orderItemDao.findByOrderId(10)).thenReturn(Collections.singletonList(it1));
        when(orderItemDao.findByOrderId(20)).thenReturn(Collections.singletonList(it2));

        when(orderDao.updateOrderStatus(anyInt(), eq("EXPIRED"))).thenReturn(1);
        when(paymentService.getPaymentByOrderId(anyInt())).thenReturn(null);

        int count = orderService.expireOldPendingOrders();

        assertEquals(2, count);
        verify(productDao).increaseStock(5, 2);
        verify(productDao).increaseStock(7, 1);
        verify(orderDao, times(2)).updateOrderStatus(anyInt(), eq("EXPIRED"));
    }

    // ──────────────────────────────────────────────
    // updateTotalAmount()
    // ──────────────────────────────────────────────

    @Test
    void updateTotalAmount_delegatesToDao() {
        BigDecimal amount = new BigDecimal("999.99");

        orderService.updateTotalAmount(10, amount);

        verify(orderDao).updateTotalAmount(10, amount);
    }

    // ──────────────────────────────────────────────
    // getOrdersWithPaymentByUser()
    // ──────────────────────────────────────────────

    @Test
    void getOrdersWithPaymentByUser_sortsByPlacedAtDesc() {
        Order o1 = new Order();
        o1.setOrderId(1);
        o1.setPlacedAt(LocalDateTime.now().minusDays(1));

        Order o2 = new Order();
        o2.setOrderId(2);
        o2.setPlacedAt(LocalDateTime.now());

        when(orderDao.findOrdersWithPaymentByUserId(1))
                .thenReturn(Arrays.asList(o1, o2));

        List<Order> result = orderService.getOrdersWithPaymentByUser(1);

        assertEquals(2, result.size());
        // most recent first
        assertEquals(2, result.get(0).getOrderId());
        verify(orderDao).findOrdersWithPaymentByUserId(1);
    }
}
