package com.ShopSphere.shop_sphere.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ShopSphere.shop_sphere.dto.CartItemDto;
import com.ShopSphere.shop_sphere.exception.CartNotFoundException;
import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.exception.ValidationException;
import com.ShopSphere.shop_sphere.model.Cart;
import com.ShopSphere.shop_sphere.repository.CartDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartDao cartDao;

    @InjectMocks
    private CartServiceImpl cartService;

    // ─────────────────────────────────────────
    // createCart()
    // ─────────────────────────────────────────

    @Test
    void createCart_shouldThrowValidationException_whenUserIdInvalid() {
        assertThrows(ValidationException.class, () -> cartService.createCart(0));
        verifyNoInteractions(cartDao);
    }

    @Test
    void createCart_shouldThrowValidationException_whenCartAlreadyExistsForUser() {
        int userId = 1;

        when(cartDao.cartExistsForUser(userId)).thenReturn(true);

        assertThrows(ValidationException.class, () -> cartService.createCart(userId));

        verify(cartDao).cartExistsForUser(userId);
        verify(cartDao, never()).createCart(anyInt());
    }

    @Test
    void createCart_shouldThrowRuntimeException_whenCreatedCartNotFoundAfterInsert() {
        int userId = 1;
        int newCartId = 10;

        when(cartDao.cartExistsForUser(userId)).thenReturn(false);
        when(cartDao.createCart(userId)).thenReturn(newCartId);
        when(cartDao.findById(newCartId)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> cartService.createCart(userId));

        verify(cartDao).cartExistsForUser(userId);
        verify(cartDao).createCart(userId);
        verify(cartDao).findById(newCartId);
    }

    @Test
    void createCart_shouldReturnCart_whenSuccessful() {
        int userId = 1;
        int newCartId = 10;
        Cart created = new Cart(newCartId, userId);

        when(cartDao.cartExistsForUser(userId)).thenReturn(false);
        when(cartDao.createCart(userId)).thenReturn(newCartId);
        when(cartDao.findById(newCartId)).thenReturn(created);

        Cart result = cartService.createCart(userId);

        assertNotNull(result);
        assertEquals(newCartId, result.getCartId());
        assertEquals(userId, result.getUserId());

        verify(cartDao).cartExistsForUser(userId);
        verify(cartDao).createCart(userId);
        verify(cartDao).findById(newCartId);
    }

    // ─────────────────────────────────────────
    // getCartByUserId()
    // ─────────────────────────────────────────

    @Test
    void getCartByUserId_shouldThrowValidationException_whenUserIdInvalid() {
        assertThrows(ValidationException.class, () -> cartService.getCartByUserId(0));
        verifyNoInteractions(cartDao);
    }

    @Test
    void getCartByUserId_shouldThrowResourceNotFound_whenCartNull() {
        int userId = 1;
        when(cartDao.findByUserId(userId)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                     () -> cartService.getCartByUserId(userId));

        verify(cartDao).findByUserId(userId);
    }

    @Test
    void getCartByUserId_shouldReturnCart_whenFound() {
        int userId = 1;
        Cart cart = new Cart(10, userId);
        when(cartDao.findByUserId(userId)).thenReturn(cart);

        Cart result = cartService.getCartByUserId(userId);

        assertNotNull(result);
        assertEquals(10, result.getCartId());
        assertEquals(userId, result.getUserId());
        verify(cartDao).findByUserId(userId);
    }

    // ─────────────────────────────────────────
    // getCartById()
    // ─────────────────────────────────────────

    @Test
    void getCartById_shouldThrowValidationException_whenCartIdInvalid() {
        assertThrows(ValidationException.class, () -> cartService.getCartById(0));
        verifyNoInteractions(cartDao);
    }

    @Test
    void getCartById_shouldThrowCartNotFound_whenDaoReturnsNull() {
        int cartId = 5;
        when(cartDao.findById(cartId)).thenReturn(null);

        assertThrows(CartNotFoundException.class,
                     () -> cartService.getCartById(cartId));

        verify(cartDao).findById(cartId);
    }

    @Test
    void getCartById_shouldReturnCart_whenFound() {
        int cartId = 5;
        Cart cart = new Cart(cartId, 1);
        when(cartDao.findById(cartId)).thenReturn(cart);

        Cart result = cartService.getCartById(cartId);

        assertNotNull(result);
        assertEquals(cartId, result.getCartId());
        verify(cartDao).findById(cartId);
    }

    // ─────────────────────────────────────────
    // getAllCarts()
    // ─────────────────────────────────────────

    @Test
    void getAllCarts_shouldReturnListFromDao() {
        List<Cart> list = Arrays.asList(
                new Cart(1, 10),
                new Cart(2, 20)
        );
        when(cartDao.getAllCarts()).thenReturn(list);

        List<Cart> result = cartService.getAllCarts();

        assertEquals(2, result.size());
        verify(cartDao).getAllCarts();
    }

    // ─────────────────────────────────────────
    // deleteCart()
    // ─────────────────────────────────────────

    @Test
    void deleteCart_shouldThrowValidationException_whenCartIdInvalid() {
        assertThrows(ValidationException.class, () -> cartService.deleteCart(0));
        verifyNoInteractions(cartDao);
    }

    @Test
    void deleteCart_shouldThrowCartNotFound_whenCartDoesNotExist() {
        int cartId = 10;
        when(cartDao.findById(cartId)).thenReturn(null);

        // getCartById will throw CartNotFoundException
        assertThrows(CartNotFoundException.class, () -> cartService.deleteCart(cartId));

        verify(cartDao).findById(cartId);
        verify(cartDao, never()).deleteCart(anyInt());
    }

    @Test
    void deleteCart_shouldThrowValidationException_whenNoRowsDeleted() {
        int cartId = 10;
        Cart existing = new Cart(cartId, 1);

        when(cartDao.findById(cartId)).thenReturn(existing);
        when(cartDao.deleteCart(cartId)).thenReturn(0);

        assertThrows(ValidationException.class, () -> cartService.deleteCart(cartId));

        verify(cartDao).findById(cartId);
        verify(cartDao).deleteCart(cartId);
    }

    @Test
    void deleteCart_shouldSucceed_whenRowsDeleted() {
        int cartId = 10;
        Cart existing = new Cart(cartId, 1);

        when(cartDao.findById(cartId)).thenReturn(existing);
        when(cartDao.deleteCart(cartId)).thenReturn(1);

        assertDoesNotThrow(() -> cartService.deleteCart(cartId));

        verify(cartDao).findById(cartId);
        verify(cartDao).deleteCart(cartId);
    }

    // ─────────────────────────────────────────
    // cartExistsForUser()
    // ─────────────────────────────────────────

    @Test
    void cartExistsForUser_shouldThrowValidationException_whenUserIdInvalid() {
        assertThrows(ValidationException.class, () -> cartService.cartExistsForUser(0));
        verifyNoInteractions(cartDao);
    }

    @Test
    void cartExistsForUser_shouldReturnDaoResult_whenUserIdValid() {
        int userId = 1;
        when(cartDao.cartExistsForUser(userId)).thenReturn(true);

        boolean exists = cartService.cartExistsForUser(userId);

        assertTrue(exists);
        verify(cartDao).cartExistsForUser(userId);
    }

    // ─────────────────────────────────────────
    // isCarEmpty()  (method name in service)
    // ─────────────────────────────────────────

    @Test
    void isCarEmpty_shouldThrowValidationException_whenCartIdInvalid() {
        assertThrows(ValidationException.class, () -> cartService.isCarEmpty(0));
        verifyNoInteractions(cartDao);
    }

    @Test
    void isCarEmpty_shouldThrowCartNotFound_whenCartMissing() {
        int cartId = 10;
        when(cartDao.findById(cartId)).thenReturn(null);

        assertThrows(CartNotFoundException.class, () -> cartService.isCarEmpty(cartId));

        verify(cartDao).findById(cartId);
        verify(cartDao, never()).isCartEmpty(anyInt());
    }

    @Test
    void isCarEmpty_shouldReturnTrueOrFalse_basedOnDao() {
        int cartId = 10;
        Cart existing = new Cart(cartId, 1);

        when(cartDao.findById(cartId)).thenReturn(existing);
        when(cartDao.isCartEmpty(cartId)).thenReturn(true);

        boolean result = cartService.isCarEmpty(cartId);

        assertTrue(result);
        verify(cartDao).findById(cartId);
        verify(cartDao).isCartEmpty(cartId);
    }

    // ─────────────────────────────────────────
    // getCartItemsByUserId()
    // ─────────────────────────────────────────

    @Test
    void getCartItemsByUserId_shouldThrowValidationException_whenUserIdInvalid() {
        assertThrows(ValidationException.class, () -> cartService.getCartItemsByUserId(0));
        verifyNoInteractions(cartDao);
    }

    @Test
    void getCartItemsByUserId_shouldThrowResourceNotFound_whenCartDoesNotExist() {
        int userId = 1;
        when(cartDao.cartExistsForUser(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                     () -> cartService.getCartItemsByUserId(userId));

        verify(cartDao).cartExistsForUser(userId);
        verify(cartDao, never()).getCartItems(anyInt());
    }

    @Test
    void getCartItemsByUserId_shouldMapRowsToCartItemDto_whenSuccessful() {
        int userId = 1;
        when(cartDao.cartExistsForUser(userId)).thenReturn(true);

        Map<String, Object> row = new HashMap<>();
        row.put("cart_items_id", 100);
        row.put("cart_id", 10);
        row.put("product_id", 50);
        row.put("quantity", 3);
        row.put("product_name", "Test Product");
        row.put("product_price", 999.0);
        row.put("image_url", "image.jpg");

        when(cartDao.getCartItems(userId)).thenReturn(Collections.singletonList(row));

        List<CartItemDto> result = cartService.getCartItemsByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());

        CartItemDto dto = result.get(0);
        assertEquals(100, dto.getCartItemsId());
        assertEquals(10, dto.getCartId());
        assertEquals(50, dto.getProductId());
        assertEquals(3, dto.getQuantity());
        assertEquals("Test Product", dto.getProductName());
        assertEquals(999.0, dto.getProductPrice());
        assertEquals("image.jpg", dto.getImageUrl());

        verify(cartDao).cartExistsForUser(userId);
        verify(cartDao).getCartItems(userId);
    }
}
