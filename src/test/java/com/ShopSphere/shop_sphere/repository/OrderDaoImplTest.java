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
import java.sql.Timestamp;
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

import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.util.OrderRowMapper;

public class OrderDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private OrderDaoImpl orderDao;

    private Order sampleOrder;
    private Order olderOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleOrder = new Order();
        sampleOrder.setOrderId(1001);
        sampleOrder.setUserId(42);
        sampleOrder.setTotalAmount(new BigDecimal("250.00"));
        sampleOrder.setShippingAddress("221B Baker St");
        sampleOrder.setOrderStatus("PENDING");
        sampleOrder.setPlacedAt(LocalDateTime.now());
        sampleOrder.setPaymentMethod("ONLINE");

        olderOrder = new Order();
        olderOrder.setOrderId(1002);
        olderOrder.setUserId(42);
        olderOrder.setTotalAmount(new BigDecimal("150.00"));
        olderOrder.setShippingAddress("Somewhere");
        olderOrder.setOrderStatus("DELIVERED");
        olderOrder.setPlacedAt(LocalDateTime.now().minusDays(2));
        olderOrder.setPaymentMethod("COD");
    }

    // ---------- save ----------
    @Test
    void testSave_SetsGeneratedId() {
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            kh.getKeyList().add(java.util.Map.of("GENERATED_KEY", 555));
            return 1;
        }).when(jdbcTemplate).update(any(), any(KeyHolder.class));

        Order toSave = new Order();
        toSave.setUserId(42);
        toSave.setTotalAmount(new BigDecimal("100.00"));
        toSave.setShippingAddress("Addr");
        toSave.setOrderStatus("PENDING");
        toSave.setPlacedAt(LocalDateTime.now());
        toSave.setPaymentMethod("ONLINE");

        int id = orderDao.save(toSave);
        assertEquals(555, id);
        assertEquals(555, toSave.getOrderId());
        verify(jdbcTemplate, times(1)).update(any(), any(KeyHolder.class));
    }

    // ---------- findById ----------
    @Test
    void testFindById_Found() {
        when(jdbcTemplate.query(anyString(), any(OrderRowMapper.class), eq(1001)))
                .thenReturn(Arrays.asList(sampleOrder));

        Optional<Order> opt = orderDao.findById(1001);
        assertTrue(opt.isPresent());
        assertEquals(1001, opt.get().getOrderId());
    }

    @Test
    void testFindById_NotFound() {
        when(jdbcTemplate.query(anyString(), any(OrderRowMapper.class), eq(9999)))
                .thenReturn(Arrays.asList());
        Optional<Order> opt = orderDao.findById(9999);
        assertFalse(opt.isPresent());
    }

    // ---------- findByUserId ----------
    @Test
    void testFindByUserId_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(OrderRowMapper.class), eq(42)))
                .thenReturn(Arrays.asList(sampleOrder, olderOrder));

        List<Order> list = orderDao.findByUserId(42);
        assertEquals(2, list.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(OrderRowMapper.class), eq(42));
    }

    // ---------- findAll ----------
    @Test
    void testFindAll_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(OrderRowMapper.class)))
                .thenReturn(Arrays.asList(sampleOrder, olderOrder));
        List<Order> all = orderDao.findAll();
        assertEquals(2, all.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(OrderRowMapper.class));
    }

    // ---------- updateOrderStatus ----------
    @Test
    void testUpdateOrderStatus_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq("SHIPPED"), eq(1001))).thenReturn(1);
        int rows = orderDao.updateOrderStatus(1001, "SHIPPED");
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq("SHIPPED"), eq(1001));
    }

    // ---------- cancelOrder ----------
    @Test
    void testCancelOrder_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(1002))).thenReturn(1);
        Order rows = orderDao.cancelOrder(1002);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(1002));
    }

    // ---------- deleteById ----------
    @Test
    void testDeleteById_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(1001))).thenReturn(1);
        int rows = orderDao.deleteById(1001);
        assertEquals(1, rows);
    }

    // ---------- findByStatusAndPlacedAtBefore ----------
    @Test
    void testFindByStatusAndPlacedAtBefore_ReturnsList() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        when(jdbcTemplate.query(anyString(), any(OrderRowMapper.class), eq("PENDING"), any(Timestamp.class)))
                .thenReturn(Arrays.asList(sampleOrder));

        List<Order> list = orderDao.findByStatusAndPlacedAtBefore("PENDING", cutoff);
        assertEquals(1, list.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(OrderRowMapper.class), eq("PENDING"), any(Timestamp.class));
    }

    // ---------- updateRazorpayOrderId ----------
    @Test
    void testUpdateRazorpayOrderId_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq("rzp_123"), eq(1001))).thenReturn(1);
        int rows = orderDao.updateRazorpayOrderId(1001, "rzp_123");
        assertEquals(1, rows);
    }

    // ---------- getOrdersWithItems ----------
    @Test
    void testGetOrdersWithItems_ReturnsListOfMaps() {
        Map<String, Object> row = Map.of(
                "order_id", 1001,
                "total_amount", new BigDecimal("250.00"),
                "product_name", "Gadget",
                "quantity", 2,
                "unit_price", new BigDecimal("125.00")
        );
        when(jdbcTemplate.queryForList(anyString(), eq(42))).thenReturn(List.of(row));

        List<Map<String, Object>> items = orderDao.getOrdersWithItems(42);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(1001, items.get(0).get("order_id"));
        verify(jdbcTemplate, times(1)).queryForList(anyString(), eq(42));
    }

    // ---------- findBySeller ----------
    @Test
    void testFindBySeller_ReturnsOrders() {
        when(jdbcTemplate.query(anyString(), any(OrderRowMapper.class), eq(5)))
                .thenReturn(Arrays.asList(sampleOrder));
        List<Order> list = orderDao.findBySeller(5);
        assertEquals(1, list.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(OrderRowMapper.class), eq(5));
    }

    // ---------- updateTotalAmount ----------
    @Test
    void testUpdateTotalAmount_CallsJdbcUpdate() {
        doNothing().when(jdbcTemplate).update(anyString(), any(BigDecimal.class), anyInt());
        orderDao.updateTotalAmount(1001, new BigDecimal("300.00"));
        verify(jdbcTemplate, times(1)).update(anyString(), eq(new BigDecimal("300.00")), eq(1001));
    }

    // ---------- findOrdersWithPaymentByUserId ----------
    @Test
    void testFindOrdersWithPaymentByUserId_ReturnsOrdersWithPaymentStatus() {
        // simulate the custom row mapping inside method
        Order mapped = new Order();
        mapped.setOrderId(2001);
        mapped.setUserId(7);
        mapped.setTotalAmount(new BigDecimal("500.00"));
        mapped.setShippingAddress("Addr");
        mapped.setPlacedAt(LocalDateTime.now());
        mapped.setOrderStatus("DELIVERED");
        mapped.setPaymentMethod("ONLINE");
        mapped.setPaymentStatus("SUCCESS");

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(7)))
                .thenReturn(Arrays.asList(mapped));

        List<Order> result = orderDao.findOrdersWithPaymentByUserId(7);
        assertEquals(1, result.size());
        assertEquals("SUCCESS", result.get(0).getPaymentStatus());
        verify(jdbcTemplate, times(1)).query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(7));
    }

    // parameterized
    @ParameterizedTest
    @ValueSource(ints = {1001, 1002})
    void parameterizedOrderIds(int id) {
        when(jdbcTemplate.query(anyString(), any(OrderRowMapper.class), eq(id)))
                .thenReturn(Arrays.asList(sampleOrder));
        var opt = orderDao.findById(id);
        assertTrue(opt.isPresent());
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled and skipped");
    }
}