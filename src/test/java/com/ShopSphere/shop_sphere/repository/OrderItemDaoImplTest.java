package com.ShopSphere.shop_sphere.repository;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.ShopSphere.shop_sphere.exception.OutOfStockException;
import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.repository.OrderItemDaoImpl;
import com.ShopSphere.shop_sphere.repository.ProductDao;

public class OrderItemDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private OrderItemDaoImpl orderItemDao;

    private OrderItem sampleItem;
    private OrderItem savedItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleItem = new OrderItem();
        sampleItem.setOrderId(200);
        sampleItem.setProductId(101);
        sampleItem.setSellerId(300);
        sampleItem.setProductName("Widget");
        sampleItem.setQuantity(2);
        sampleItem.setUnitPrice(new BigDecimal("50.00"));
        sampleItem.setTotalItemPrice(new BigDecimal("100.00"));

        savedItem = new OrderItem();
        savedItem.setOrderItemsId(11);
        savedItem.setOrderId(200);
        savedItem.setProductId(101);
        savedItem.setSellerId(300);
        savedItem.setProductName("Widget");
        savedItem.setQuantity(2);
        savedItem.setUnitPrice(new BigDecimal("50.00"));
        savedItem.setTotalItemPrice(new BigDecimal("100.00"));
    }

    // ---------- save ----------
    @Test
    void testSave_Success() {
        // productDao must return >0 for stock decrease
        when(productDao.decreaseStockIfAvailable(eq(101), eq(2))).thenReturn(1);

        // simulate jdbcTemplate.update filling KeyHolder
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            kh.getKeyList().add(Map.of("GENERATED_KEY", 77));
            return 1;
        }).when(jdbcTemplate).update(any(), any(KeyHolder.class));

        // After insert, method sets orderItem.orderItemsId from keyHolder and returns it.
        int id = orderItemDao.save(sampleItem);
        assertEquals(77, id);
        assertEquals(77, sampleItem.getOrderItemsId());
        verify(productDao, times(1)).decreaseStockIfAvailable(101, 2);
        verify(jdbcTemplate, times(1)).update(any(), any(KeyHolder.class));
    }

    @Test
    void testSave_OutOfStock_Throws() {
        when(productDao.decreaseStockIfAvailable(eq(101), eq(2))).thenReturn(0);

        assertThrows(OutOfStockException.class, () -> orderItemDao.save(sampleItem));
        verify(productDao, times(1)).decreaseStockIfAvailable(101, 2);
        verify(jdbcTemplate, never()).update(any(), any(KeyHolder.class));
    }

    // ---------- findById ----------
    @Test
    void testFindById_Found() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(11)))
                .thenReturn(Arrays.asList(savedItem));

        Optional<OrderItem> opt = orderItemDao.findById(11);
        assertTrue(opt.isPresent());
        assertEquals(11, opt.get().getOrderItemsId());
    }

    @Test
    void testFindById_NotFound() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(9999)))
                .thenReturn(Arrays.asList());

        Optional<OrderItem> opt = orderItemDao.findById(9999);
        assertFalse(opt.isPresent());
    }

    // ---------- findByOrderId ----------
    @Test
    void testFindByOrderId_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(200)))
                .thenReturn(Arrays.asList(savedItem));

        List<OrderItem> list = orderItemDao.findByOrderId(200);
        assertEquals(1, list.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(200));
    }

    // ---------- findByProductId ----------
    @Test
    void testFindByProductId_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(101)))
                .thenReturn(Arrays.asList(savedItem));

        List<OrderItem> list = orderItemDao.findByProductId(101);
        assertEquals(1, list.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(101));
    }

    // ---------- updateQuantityANDTotalPrice ----------
    @Test
    void testUpdateQuantity_NotFound_ReturnsZero() {
        // findById inside method returns empty list -> query returns empty list
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(99)))
                .thenReturn(Arrays.asList());

        int rows = orderItemDao.updateQuantityANDTotalPrice(99, 5, new BigDecimal("250.00"));
        assertEquals(0, rows);
    }

    @Test
    void testUpdateQuantity_IncreaseStockNotAvailable_Throws() {
        // existing item has quantity 2
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(11)))
                .thenReturn(Arrays.asList(savedItem));

        // request to increase to 10 -> diff = 8, productDao returns 0 -> OutOfStockException
        when(productDao.decreaseStockIfAvailable(eq(101), eq(8))).thenReturn(0);

        assertThrows(OutOfStockException.class, () -> orderItemDao.updateQuantityANDTotalPrice(11, 10, new BigDecimal("500.00")));
        verify(productDao, times(1)).decreaseStockIfAvailable(101, 8);
    }

    @Test
    void testUpdateQuantity_DecreaseStock_RestoresAndUpdates() {
        // existing item has quantity 2
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(11)))
                .thenReturn(Arrays.asList(savedItem));

        // request to reduce to 1 -> diff = -1 -> productDao.increaseStock called
        doNothing().when(productDao).increaseStock(eq(101), eq(1));
        when(jdbcTemplate.update(anyString(), eq(1), any(BigDecimal.class), eq(11))).thenReturn(1);

        int rows = orderItemDao.updateQuantityANDTotalPrice(11, 1, new BigDecimal("50.00"));
        assertEquals(1, rows);
        verify(productDao, times(1)).increaseStock(101, 1);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(1), any(BigDecimal.class), eq(11));
    }

    @Test
    void testUpdateQuantity_IncreaseStock_AvailableAndUpdates() {
        // existing item has quantity 2
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(11)))
                .thenReturn(Arrays.asList(savedItem));

        // request to increase to 4 -> diff = 2, productDao.decreaseStockIfAvailable returns 1
        when(productDao.decreaseStockIfAvailable(eq(101), eq(2))).thenReturn(1);
        when(jdbcTemplate.update(anyString(), eq(4), any(BigDecimal.class), eq(11))).thenReturn(1);

        int rows = orderItemDao.updateQuantityANDTotalPrice(11, 4, new BigDecimal("200.00"));
        assertEquals(1, rows);
        verify(productDao, times(1)).decreaseStockIfAvailable(101, 2);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(4), any(BigDecimal.class), eq(11));
    }

    // ---------- deleteById ----------
    @Test
    void testDeleteById_NotFound_ReturnsZero() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(9999)))
                .thenReturn(Arrays.asList());

        int rows = orderItemDao.deleteById(9999);
        assertEquals(0, rows);
        verify(productDao, never()).increaseStock(anyInt(), anyInt());
    }

    @Test
    void testDeleteById_Found_RestoresStockAndDeletes() {
        // findById returns existing item
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(11)))
                .thenReturn(Arrays.asList(savedItem));

        // increaseStock called
        doNothing().when(productDao).increaseStock(eq(101), eq(2));

        when(jdbcTemplate.update(anyString(), eq(11))).thenReturn(1);

        int rows = orderItemDao.deleteById(11);
        assertEquals(1, rows);
        verify(productDao, times(1)).increaseStock(101, 2);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(11));
    }

    // parameterized
    @ParameterizedTest
    @ValueSource(ints = {11, 12})
    void parameterizedOrderItemIds(int id) {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(id)))
                .thenReturn(Arrays.asList(savedItem));
        Optional<OrderItem> res = orderItemDao.findById(id);
        assertTrue(res.isPresent());
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled and skipped");
    }
}