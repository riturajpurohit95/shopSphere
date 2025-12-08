package com.ShopSphere.shop_sphere.repository;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.ShopSphere.shop_sphere.exception.ValidationException;
import com.ShopSphere.shop_sphere.model.Cart;
import com.ShopSphere.shop_sphere.util.CartRowMapper;

public class CartDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private cartDaoImpl cartDao; // matches your implementation class name

    private Cart cart1;
    private Cart cart2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cart1 = new Cart();
        cart1.setCartId(1);
        cart1.setUserId(10);

        cart2 = new Cart();
        cart2.setCartId(2);
        cart2.setUserId(20);
    }

    // ---------- createCart ----------
    @Test
    void testCreateCart_Success() {
        // Simulate GeneratedKeyHolder being filled by jdbcTemplate.update
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            // Mockito can't easily call setKey on the exact GeneratedKeyHolder class here,
            // but we can add an entry to the internal key list similar to other tests.
            // The caller reads kh.getKey() -> the test environment will see this if we populate key list.
            // Use reflection-free approach similar to other tests: call getKeyList and add map.
            kh.getKeyList().add(java.util.Map.of("GENERATED_KEY", 123));
            return 1;
        }).when(jdbcTemplate).update(any(), any(KeyHolder.class));

        int generatedId = cartDao.createCart(10);
        assertTrue(generatedId > 0);
        // Expect the same integer we 'injected' above (123)
        assertEquals(123, generatedId);
    }

    @Test
    void testCreateCart_InvalidUser_Throws() {
        assertThrows(ValidationException.class, () -> cartDao.createCart(0));
        verify(jdbcTemplate, never()).update(any(), any(KeyHolder.class));
    }

    // ---------- findByUserId ----------
    @Test
    void testFindByUserId_Found() {
        when(jdbcTemplate.queryForObject(anyString(), any(CartRowMapper.class), eq(10)))
                .thenReturn(cart1);

        Cart result = cartDao.findByUserId(10);
        assertNotNull(result);
        assertEquals(10, result.getUserId());
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), any(CartRowMapper.class), eq(10));
    }

    @Test
    void testFindByUserId_NotFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(CartRowMapper.class), eq(999)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Cart result = cartDao.findByUserId(999);
        assertNull(result);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), any(CartRowMapper.class), eq(999));
    }

    // ---------- findById ----------
    @Test
    void testFindById_Found() {
        when(jdbcTemplate.queryForObject(anyString(), any(CartRowMapper.class), eq(1)))
                .thenReturn(cart1);

        Cart result = cartDao.findById(1);
        assertNotNull(result);
        assertEquals(1, result.getCartId());
    }

    @Test
    void testFindById_NotFound() {
        when(jdbcTemplate.queryForObject(anyString(), any(CartRowMapper.class), eq(5000)))
                .thenThrow(new EmptyResultDataAccessException(1));

        Cart result = cartDao.findById(5000);
        assertNull(result);
    }

    // ---------- getAllCarts ----------
    @Test
    void testGetAllCarts() {
        when(jdbcTemplate.query(anyString(), any(CartRowMapper.class)))
                .thenReturn(Arrays.asList(cart1, cart2));

        List<Cart> all = cartDao.getAllCarts();
        assertEquals(2, all.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(CartRowMapper.class));
    }

    // ---------- deleteCart ----------
    @Test
    void testDeleteCart_Success() {
        when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);
        int rows = cartDao.deleteCart(1);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(1));
    }

    @Test
    void testDeleteCart_InvalidId_Throws() {
        assertThrows(ValidationException.class, () -> cartDao.deleteCart(0));
        verify(jdbcTemplate, never()).update(anyString(), anyInt());
    }

    // ---------- cartExistsForUser ----------
    @Test
    void testCartExistsForUser_True() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(10))).thenReturn(1);
        boolean exists = cartDao.cartExistsForUser(10);
        assertTrue(exists);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq(10));
    }

    @Test
    void testCartExistsForUser_False() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(99))).thenReturn(0);
        boolean exists = cartDao.cartExistsForUser(99);
        assertFalse(exists);
    }

    // ---------- isCartEmpty ----------
    @Test
    void testIsCartEmpty_Empty() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1))).thenReturn(0);
        boolean empty = cartDao.isCartEmpty(1);
        assertTrue(empty);
    }

    @Test
    void testIsCartEmpty_NotEmpty() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(2))).thenReturn(3);
        boolean empty = cartDao.isCartEmpty(2);
        assertFalse(empty);
    }

    // ---------- getCartItems ----------
    @Test
    void testGetCartItems_Success() {
        Map<String, Object> row = Map.of(
                "cart_items_id", 11,
                "cart_id", 1,
                "product_id", 200,
                "product_name", "Prod",
                "product_price", 99.99,
                "quantity", 2
        );

        when(jdbcTemplate.queryForList(anyString(), eq(10))).thenReturn(List.of(row));

        List<Map<String, Object>> items = cartDao.getCartItems(10);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(200, items.get(0).get("product_id"));
        verify(jdbcTemplate, times(1)).queryForList(anyString(), eq(10));
    }

    @Test
    void testGetCartItems_InvalidUser_Throws() {
        assertThrows(ValidationException.class, () -> cartDao.getCartItems(0));
        verify(jdbcTemplate, never()).queryForList(anyString(), anyInt());
    }

    // Parameterized example
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void parameterizedCartIds(int id) {
        when(jdbcTemplate.queryForObject(anyString(), any(CartRowMapper.class), eq(id)))
                .thenReturn(cart1);
        Cart c = cartDao.findById(id);
        assertNotNull(c);
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled and skipped");
    }
}