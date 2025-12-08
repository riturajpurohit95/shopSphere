package com.ShopSphere.shop_sphere.repository;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ShopSphere.shop_sphere.model.CartItem;

public class CartItemDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CartItemDaoImpl cartItemDao;

    private CartItem item1;
    private CartItem item2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        item1 = new CartItem();
        item1.setCartItemsId(11);
        item1.setCartId(1);
        item1.setProductId(101);
        item1.setQuantity(2);

        item2 = new CartItem();
        item2.setCartItemsId(12);
        item2.setCartId(1);
        item2.setProductId(102);
        item2.setQuantity(3);
    }

    @Test
    void testAddItem_Success_ReturnsFoundItem() {
        // when adding, jdbcTemplate.update returns int rows affected
        when(jdbcTemplate.update(anyString(), eq(item1.getCartId()), eq(item1.getProductId()), eq(item1.getQuantity())))
                .thenReturn(1);

        // after insert, addItem calls findByProductAndCart -> jdbcTemplate.queryForObject
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class),
                eq(item1.getCartId()), eq(item1.getProductId())))
            .thenReturn(item1);

        CartItem saved = cartItemDao.addItem(item1);
        assertNotNull(saved);
        assertEquals(11, saved.getCartItemsId());
        verify(jdbcTemplate, times(1)).update(anyString(), eq(item1.getCartId()), eq(item1.getProductId()), eq(item1.getQuantity()));
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class),
                eq(item1.getCartId()), eq(item1.getProductId()));
    }

    @Test
    void testFindByCartId_Found() {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(1)))
                .thenReturn(item1);

        Optional<CartItem> opt = cartItemDao.findByCartId(1);
        assertTrue(opt.isPresent());
        assertEquals(101, opt.get().getProductId());
    }

    @Test
    void testFindByCartId_NotFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(999)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Optional<CartItem> opt = cartItemDao.findByCartId(999);
        assertFalse(opt.isPresent());
    }

    @Test
    void testFindByProductAndCart_Found() {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class),
                eq(1), eq(101)))
            .thenReturn(item1);

        Optional<CartItem> opt = cartItemDao.findByProductAndCart(1, 101);
        assertTrue(opt.isPresent());
        assertEquals(11, opt.get().getCartItemsId());
    }

    @Test
    void testFindByProductAndCart_NotFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class),
                eq(1), eq(9999)))
            .thenThrow(new EmptyResultDataAccessException(1));

        Optional<CartItem> opt = cartItemDao.findByProductAndCart(1, 9999);
        assertFalse(opt.isPresent());
    }

    @Test
    void testExistsInCart_TrueAndFalse() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1), eq(101))).thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1), eq(999))).thenReturn(0);

        assertTrue(cartItemDao.existsInCart(1, 101));
        assertFalse(cartItemDao.existsInCart(1, 999));

        verify(jdbcTemplate, times(2)).queryForObject(anyString(), eq(Integer.class), anyInt(), anyInt());
    }

    @Test
    void testFindAllByCartId() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(1)))
                .thenReturn(Arrays.asList(item1, item2));

        List<CartItem> items = cartItemDao.findAllByCartId(1);
        assertEquals(2, items.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(1));
    }

    @Test
    void testUpdateItemQuantity_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(5), eq(11))).thenReturn(1);
        int rows = cartItemDao.updateItemQuantity(11, 5);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(5), eq(11));
    }

    @Test
    void testDeleteItem_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(11))).thenReturn(1);
        int rows = cartItemDao.deleteItem(11);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(11));
    }

    @Test
    void testDeleteItemByProductId_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(1), eq(101))).thenReturn(1);
        int rows = cartItemDao.deleteItemByProductId(1, 101);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(1), eq(101));
    }

    @Test
    void testCalculateTotalAmount_ReturnsSum() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Double.class), eq(1))).thenReturn(299.97);
        double total = cartItemDao.calculateTotalAmount(1);
        assertEquals(299.97, total, 0.0001);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Double.class), eq(1));
    }

    @Test
    void testFindById_FoundAndNotFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(11)))
                .thenReturn(item1);
        Optional<CartItem> opt = cartItemDao.findById(11);
        assertTrue(opt.isPresent());
        assertEquals(11, opt.get().getCartItemsId());

        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(9999)))
                .thenThrow(new EmptyResultDataAccessException(1));
        Optional<CartItem> opt2 = cartItemDao.findById(9999);
        assertFalse(opt2.isPresent());
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 12, 13})
    void parameterizedCartItemIds(int id) {
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(id)))
                .thenReturn(item1);
        Optional<CartItem> res = cartItemDao.findById(id);
        assertTrue(res.isPresent());
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled and skipped");
    }
}