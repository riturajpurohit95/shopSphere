package com.ShopSphere.shop_sphere.service;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;

import com.ShopSphere.shop_sphere.exception.BadRequestException;
import com.ShopSphere.shop_sphere.exception.DuplicateResourceException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Wishlist;
import com.ShopSphere.shop_sphere.repository.WishlistDao;

public class WishlistServiceImplTest {

    @Mock
    private WishlistDao wishlistDao;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private Wishlist wishlist;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        wishlist = new Wishlist();
        wishlist.setWishlistId(55);
        wishlist.setUserId(10);
    }

    // createWishlist
    @Test
    void testCreateWishlist_Success() {
        when(wishlistDao.wishlistExistsForUser(10)).thenReturn(false);
        when(wishlistDao.createWishlist(10)).thenReturn(55);
        when(wishlistDao.findById(55)).thenReturn(wishlist);

        Wishlist created = wishlistService.createWishlist(10);
        assertNotNull(created);
        assertEquals(55, created.getWishlistId());
        verify(wishlistDao, times(1)).createWishlist(10);
        verify(wishlistDao, times(1)).findById(55);
    }

    @Test
    void testCreateWishlist_AlreadyExists_ThrowsDuplicateResource() {
        when(wishlistDao.wishlistExistsForUser(10)).thenReturn(true);
        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> wishlistService.createWishlist(10));
        assertTrue(ex.getMessage().contains("Wishlist already exists for userId"));
        verify(wishlistDao, never()).createWishlist(anyInt());
    }

    @Test
    void testCreateWishlist_CreatedButNotFound_ThrowsResourceNotFound() {
        when(wishlistDao.wishlistExistsForUser(10)).thenReturn(false);
        when(wishlistDao.createWishlist(10)).thenReturn(999);
        when(wishlistDao.findById(999)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> wishlistService.createWishlist(10));
        assertTrue(ex.getMessage().contains("Wishlist created but not found"));
        verify(wishlistDao, times(1)).createWishlist(10);
        verify(wishlistDao, times(1)).findById(999);
    }

    @Test
    void testCreateWishlist_InvalidUserId_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> wishlistService.createWishlist(0));
        verify(wishlistDao, never()).createWishlist(anyInt());
    }

    // getWishlistByUserId
    @Test
    void testGetWishlistByUserId_Success() {
        when(wishlistDao.findByUserId(10)).thenReturn(wishlist);
        Wishlist res = wishlistService.getWishlistByUserId(10);
        assertNotNull(res);
        assertEquals(10, res.getUserId());
    }

    @Test
    void testGetWishlistByUserId_NotFound_ThrowsResourceNotFound() {
        when(wishlistDao.findByUserId(11)).thenReturn(null);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> wishlistService.getWishlistByUserId(11));
        assertTrue(ex.getMessage().contains("No wishlist found for userId"));
    }

    @Test
    void testGetWishlistByUserId_Invalid_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> wishlistService.getWishlistByUserId(-1));
    }

    // getWishlistById
    @Test
    void testGetWishlistById_Success() {
        when(wishlistDao.findById(55)).thenReturn(wishlist);
        Wishlist res = wishlistService.getWishlistById(55);
        assertNotNull(res);
        assertEquals(55, res.getWishlistId());
    }

    @Test
    void testGetWishlistById_NotFound_ThrowsResourceNotFound() {
        when(wishlistDao.findById(99)).thenReturn(null);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> wishlistService.getWishlistById(99));
        assertTrue(ex.getMessage().contains("No wishlist found for wishlistId"));
    }

    @Test
    void testGetWishlistById_Invalid_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> wishlistService.getWishlistById(0));
    }

    // getAllWishlists
    @Test
    void testGetAllWishlists_NonNull() {
        when(wishlistDao.getAllWishlists()).thenReturn(List.of(wishlist));
        List<Wishlist> list = wishlistService.getAllWishlists();
        assertEquals(1, list.size());
    }

    @Test
    void testGetAllWishlists_NullFromDao_ReturnsEmptyList() {
        when(wishlistDao.getAllWishlists()).thenReturn(null);
        List<Wishlist> list = wishlistService.getAllWishlists();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // deleteWishlist
    @Test
    void testDeleteWishlist_Success() {
        when(wishlistDao.findById(55)).thenReturn(wishlist);
        when(wishlistDao.deleteWishlist(55)).thenReturn(1);

        assertDoesNotThrow(() -> wishlistService.deleteWishlist(55));
        verify(wishlistDao, times(1)).deleteWishlist(55);
    }

    @Test
    void testDeleteWishlist_DeleteFails_ThrowsBadRequest() {
        when(wishlistDao.findById(55)).thenReturn(wishlist);
        when(wishlistDao.deleteWishlist(55)).thenReturn(0);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> wishlistService.deleteWishlist(55));
        assertTrue(ex.getMessage().contains("Failed to delete wishlistId"));
    }

    @Test
    void testDeleteWishlist_InvalidId_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> wishlistService.deleteWishlist(0));
        verify(wishlistDao, never()).deleteWishlist(anyInt());
    }

    // wishlistExistsForUser
    @Test
    void testWishlistExistsForUser_Success() {
        when(wishlistDao.wishlistExistsForUser(10)).thenReturn(true);
        boolean exists = wishlistService.wishlistExistsForUser(10);
        assertTrue(exists);
    }

    @Test
    void testWishlistExistsForUser_Invalid_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> wishlistService.wishlistExistsForUser(0));
    }

    // isWishlistEmpty
    @Test
    void testIsWishlistEmpty_Success() {
        when(wishlistDao.isWishlistEmpty(55)).thenReturn(true);
        assertTrue(wishlistService.isWishlistEmpty(55));
    }

    @Test
    void testIsWishlistEmpty_Invalid_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> wishlistService.isWishlistEmpty(0));
    }

    // getWishlistItems
    @Test
    void testGetWishlistItems_Success() {
        Map<String, Object> item = Map.of("product_id", 1, "product_name", "P");
        when(wishlistDao.getWishlistItems(10)).thenReturn(List.of(item));
        List<Map<String, Object>> items = wishlistService.getWishlistItems(10);
        assertNotNull(items);
        assertEquals(1, items.size());
    }

    @Test
    void testGetWishlistItems_Invalid_ThrowsBadRequest() {
        assertThrows(BadRequestException.class, () -> wishlistService.getWishlistItems(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void testParameterizedUserIds(int userId) {
        // just a simple parameterized check that calling the service with valid userId delegates to DAO
        when(wishlistDao.getWishlistItems(userId)).thenReturn(List.of());
        assertDoesNotThrow(() -> wishlistService.getWishlistItems(userId));
    }
}