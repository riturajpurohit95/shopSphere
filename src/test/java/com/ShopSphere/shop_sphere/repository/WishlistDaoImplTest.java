package com.ShopSphere.shop_sphere.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.ShopSphere.shop_sphere.model.Wishlist;
import com.ShopSphere.shop_sphere.util.WishlistRowMapper;

public class WishlistDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private WishlistDaoImpl wishlistDao;

    private Wishlist wishlist;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        wishlist = new Wishlist();
        wishlist.setWishlistId(55);
        wishlist.setUserId(10);
    }

    @Test
    void testCreateWishlist_Success_ReturnsGeneratedKey() {
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            Map<String, Object> keyMap = new HashMap<>();
            keyMap.put("GENERATED_KEY", 777);
            kh.getKeyList().add(keyMap);
            return 1;
        }).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class));

        int id = wishlistDao.createWishlist(10);
        assertTrue(id > 0);
    }

    @Test
    void testCreateWishlist_DuplicateKey_FallbackToExisting() {
        // Simulate DuplicateKeyException on update -> then findByUserId returns existing wishlist
        doThrow(new DuplicateKeyException("duplicate")).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class));
        when(jdbcTemplate.queryForObject(anyString(), any(WishlistRowMapper.class), eq(10))).thenReturn(wishlist);

        int id = wishlistDao.createWishlist(10);
        assertEquals(55, id);
    }

    @Test
    void testCreateWishlist_NoKeyAndNoExisting_ReturnsZero() {
        // Simulate update returns 1 but keyHolder empty -> fallback findByUserId returns null
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            // leave keyList empty
            return 1;
        }).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class));

        when(jdbcTemplate.queryForObject(anyString(), any(WishlistRowMapper.class), eq(10))).thenThrow(new EmptyResultDataAccessException(1));
        int id = wishlistDao.createWishlist(10);
        assertEquals(0, id);
    }

    @Test
    void testFindByUserId_Found() {
        when(jdbcTemplate.queryForObject(anyString(), any(WishlistRowMapper.class), eq(10))).thenReturn(wishlist);
        Wishlist w = wishlistDao.findByUserId(10);
        assertNotNull(w);
        assertEquals(10, w.getUserId());
    }

    @Test
    void testFindByUserId_NotFound_ReturnsNull() {
        when(jdbcTemplate.queryForObject(anyString(), any(WishlistRowMapper.class), eq(99)))
            .thenThrow(new EmptyResultDataAccessException(1));
        Wishlist w = wishlistDao.findByUserId(99);
        assertNull(w);
    }

    @Test
    void testFindById_Found() {
        when(jdbcTemplate.queryForObject(anyString(), any(WishlistRowMapper.class), eq(55))).thenReturn(wishlist);
        Wishlist w = wishlistDao.findById(55);
        assertNotNull(w);
    }

    @Test
    void testGetAllWishlists() {
        when(jdbcTemplate.query(anyString(), any(WishlistRowMapper.class))).thenReturn(Arrays.asList(wishlist));
        List<Wishlist> all = wishlistDao.getAllWishlists();
        assertEquals(1, all.size());
    }

    @Test
    void testDeleteWishlist() {
        when(jdbcTemplate.update(anyString(), anyInt())).thenReturn(1);
        int rows = wishlistDao.deleteWishlist(55);
        assertEquals(1, rows);
    }

    @Test
    void testWishlistExistsForUser_True() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(10))).thenReturn(1);
        assertTrue(wishlistDao.wishlistExistsForUser(10));
    }

    @Test
    void testWishlistExistsForUser_False() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(99))).thenReturn(0);
        assertFalse(wishlistDao.wishlistExistsForUser(99));
    }

    @Test
    void testIsWishlistEmpty_TrueWhenZero() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(55))).thenReturn(0);
        assertTrue(wishlistDao.isWishlistEmpty(55));
    }

    @Test
    void testIsWishlistEmpty_FalseWhenCountPositive() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(55))).thenReturn(3);
        assertFalse(wishlistDao.isWishlistEmpty(55));
    }

    @Test
    void testGetWishlistItems() {
        Map<String, Object> row = Map.of("product_id", 1, "product_name", "P");
        when(jdbcTemplate.queryForList(anyString(), eq(10))).thenReturn(Arrays.asList(row));
        List<Map<String, Object>> items = wishlistDao.getWishlistItems(10);
        assertNotNull(items);
        assertEquals(1, items.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 20})
    void testParameterizedWishlistIds(int id) {
        // simple sanity check for dao call paths
        when(jdbcTemplate.queryForList(anyString(), eq(id))).thenReturn(List.of());
        List<Map<String,Object>> res = wishlistDao.getWishlistItems(id);
        assertNotNull(res);
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTest() {
        fail("disabled");
    }
}