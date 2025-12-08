package com.ShopSphere.shop_sphere.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.ShopSphere.shop_sphere.model.WishlistItem;
import com.ShopSphere.shop_sphere.util.WishlistItemRowMapper;

public class WishlistItemDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private WishlistItemDaoImpl wishlistItemDao;

    private WishlistItem item;
    private WishlistItem item2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        item = new WishlistItem();
        item.setWishlistId(10);
        item.setProductId(100);

        item2 = new WishlistItem();
        item2.setWishlistItemsId(5);
        item2.setWishlistId(10);
        item2.setProductId(101);
    }

    @Test
    void testAddItem_ShouldReturnGeneratedKeyAndSetOnObject() {
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            // Simulate generated key being populated by JDBC
            Map<String, Object> m = new HashMap<>();
            m.put("GENERATED_KEY", 321);
            kh.getKeyList().add(m);
            return 1;
        }).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class));

        int id = wishlistItemDao.addItem(item);
        assertTrue(id > 0);
        assertEquals(id, item.getWishlistItemsId());
    }

    @Test
    void testAddItem_NoGeneratedKey_ReturnsZeroAndSetsZeroOnObject() {
        doAnswer(invocation -> {
            // leave KeyHolder empty
            return 1;
        }).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class));

        int id = wishlistItemDao.addItem(item);
        assertEquals(0, id);
        assertEquals(0, item.getWishlistItemsId());
    }

    @Test
    void testFindByWishlistId_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(WishlistItemRowMapper.class), eq(10)))
            .thenReturn(Arrays.asList(item2));
        List<WishlistItem> list = wishlistItemDao.findByWishlistId(10);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(101, list.get(0).getProductId());
    }

    @Test
    void testFindByWishlistId_EmptyListWhenNone() {
        when(jdbcTemplate.query(anyString(), any(WishlistItemRowMapper.class), eq(99)))
            .thenReturn(Arrays.asList());
        List<WishlistItem> list = wishlistItemDao.findByWishlistId(99);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void testDeleteItem_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(5))).thenReturn(1);
        int rows = wishlistItemDao.deleteItem(5);
        assertEquals(1, rows);
    }

    @Test
    void testGetWishlistIdByItem_ReturnsId() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(5))).thenReturn(10);
        int wid = wishlistItemDao.getWishlistIdByItem(5);
        assertEquals(10, wid);
    }

    @Test
    void testGetWishlistOwnerId_ReturnsUserId() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(10))).thenReturn(42);
        int owner = wishlistItemDao.getWishlistOwnerId(10);
        assertEquals(42, owner);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    void testParameterizedWishlistItemIds(int id) {
        // sanity parameterized test for getWishlistIdByItem path
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(id))).thenReturn(2 * id);
        int res = wishlistItemDao.getWishlistIdByItem(id);
        assertEquals(2 * id, res);
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled");
    }
}