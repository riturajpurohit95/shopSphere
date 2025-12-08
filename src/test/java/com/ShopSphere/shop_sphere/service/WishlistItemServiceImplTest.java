package com.ShopSphere.shop_sphere.service;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;

import com.ShopSphere.shop_sphere.exception.BadRequestException;
import com.ShopSphere.shop_sphere.exception.DuplicateResourceException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Wishlist;
import com.ShopSphere.shop_sphere.model.WishlistItem;
import com.ShopSphere.shop_sphere.repository.WishlistDao;
import com.ShopSphere.shop_sphere.repository.WishlistItemDao;

public class WishlistItemServiceImplTest {

    @Mock
    private WishlistItemDao wishlistItemDao;

    @Mock
    private WishlistDao wishlistDao;

    @InjectMocks
    private WishlistItemServiceImpl service;

    private Wishlist wishlist;
    private WishlistItem item;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        wishlist = new Wishlist();
        wishlist.setWishlistId(10);
        wishlist.setUserId(5);

        item = new WishlistItem();
        item.setWishlistId(10);
        item.setProductId(100);
        item.setWishlistItemsId(0);
    }

    // ------------------------------------------------------------------------------------
    // addItemToWishlist Tests
    // ------------------------------------------------------------------------------------

    @Test
    void testAddItemToWishlist_Success() {
        when(wishlistDao.findById(10)).thenReturn(wishlist);
        when(wishlistItemDao.addItem(any(WishlistItem.class))).thenReturn(55);

        int id = service.addItemToWishlist(item);

        assertEquals(55, id);
        verify(wishlistDao, times(1)).findById(10);
        verify(wishlistItemDao, times(1)).addItem(any(WishlistItem.class));
    }

    @Test
    void testAddItemToWishlist_NullItem_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.addItemToWishlist(null));
        verify(wishlistItemDao, never()).addItem(any());
    }

    @Test
    void testAddItemToWishlist_InvalidWishlistId() {
        item.setWishlistId(0);
        assertThrows(BadRequestException.class, () -> service.addItemToWishlist(item));
        verify(wishlistItemDao, never()).addItem(any());
    }

    @Test
    void testAddItemToWishlist_InvalidProductId() {
        item.setProductId(0);
        assertThrows(BadRequestException.class, () -> service.addItemToWishlist(item));
        verify(wishlistItemDao, never()).addItem(any());
    }

    @Test
    void testAddItemToWishlist_WishlistNotFound_ThrowsResourceNotFound() {
        when(wishlistDao.findById(10)).thenReturn(null);

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> service.addItemToWishlist(item));
        assertTrue(ex.getMessage().contains("Wishlist not found"));
    }

    @Test
    void testAddItemToWishlist_AddItemFails_ThrowsBadRequest() {
        when(wishlistDao.findById(10)).thenReturn(wishlist);
        when(wishlistItemDao.addItem(any(WishlistItem.class))).thenReturn(0);

        BadRequestException ex =
            assertThrows(BadRequestException.class, () -> service.addItemToWishlist(item));
        assertTrue(ex.getMessage().contains("Product already exists"));
    }

    // ------------------------------------------------------------------------------------
    // getItemsByWishlistId Tests
    // ------------------------------------------------------------------------------------

    @Test
    void testGetItemsByWishlistId_Success() {
        WishlistItem i1 = new WishlistItem();
        WishlistItem i2 = new WishlistItem();

        when(wishlistItemDao.findByWishlistId(10)).thenReturn(Arrays.asList(i1, i2));

        List<WishlistItem> list = service.getItemsByWishlistId(10);
        assertEquals(2, list.size());
    }

    @Test
    void testGetItemsByWishlistId_InvalidId_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.getItemsByWishlistId(0));
        verify(wishlistItemDao, never()).findByWishlistId(anyInt());
    }

    @Test
    void testGetItemsByWishlistId_Empty_ThrowsResourceNotFound() {
        when(wishlistItemDao.findByWishlistId(10)).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class, () -> service.getItemsByWishlistId(10));
    }

    @Test
    void testGetItemsByWishlistId_NullList_ThrowsResourceNotFound() {
        when(wishlistItemDao.findByWishlistId(10)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> service.getItemsByWishlistId(10));
    }

    // ------------------------------------------------------------------------------------
    // deleteItem Tests
    // ------------------------------------------------------------------------------------

    @Test
    void testDeleteItem_Success() {
        when(wishlistItemDao.deleteItem(5)).thenReturn(1);
        assertDoesNotThrow(() -> service.deleteItem(5));
    }

    @Test
    void testDeleteItem_InvalidId_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.deleteItem(0));
        verify(wishlistItemDao, never()).deleteItem(anyInt());
    }

    @Test
    void testDeleteItem_NotFound_ThrowsResourceNotFound() {
        when(wishlistItemDao.deleteItem(5)).thenReturn(0);

        ResourceNotFoundException ex =
            assertThrows(ResourceNotFoundException.class, () -> service.deleteItem(5));
        assertTrue(ex.getMessage().contains("No wishlist item found"));
    }

    // ------------------------------------------------------------------------------------
    // Simple Pass-Through Methods
    // ------------------------------------------------------------------------------------

    @Test
    void testGetWishlistIdByItem() {
        when(wishlistItemDao.getWishlistIdByItem(5)).thenReturn(10);
        assertEquals(10, service.getWishlistIdByItem(5));
    }

    @Test
    void testGetWishlistOwnerId() {
        when(wishlistItemDao.getWishlistOwnerId(10)).thenReturn(999);
        assertEquals(999, service.getWishlistOwnerId(10));
    }

    // ------------------------------------------------------------------------------------
    // Parameterized Example
    // ------------------------------------------------------------------------------------
    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    void testParameterizedWishlistIds(int id) {
        when(wishlistItemDao.findByWishlistId(id)).thenReturn(List.of(new WishlistItem()));
        assertDoesNotThrow(() -> service.getItemsByWishlistId(id));
    }

    // ------------------------------------------------------------------------------------
    // Disabled Example (matches your pattern)
    // ------------------------------------------------------------------------------------
    @Disabled("Example disabled service test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled");
    }
}