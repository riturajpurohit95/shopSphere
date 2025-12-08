package com.ShopSphere.shop_sphere.service;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.exception.ValidationException;
import com.ShopSphere.shop_sphere.model.CartItem;
import com.ShopSphere.shop_sphere.repository.CartItemDao;
import com.ShopSphere.shop_sphere.repository.ProductDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CartItemServiceImplTest {

    @Mock
    private CartItemDao cartItemDao;

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    // ─────────────────────────────────────────
    // addItem() tests
    // ─────────────────────────────────────────

    @Test
    void addItem_shouldThrowValidationException_whenQuantityIsZeroOrNegative() {
        CartItem item = new CartItem();
        item.setCartId(1);
        item.setProductId(10);
        item.setQuantity(0); // invalid

        assertThrows(ValidationException.class, () -> cartItemService.addItem(item));
        verifyNoInteractions(cartItemDao);
        verifyNoInteractions(productDao);
    }

    @Test
    void addItem_shouldThrowValidationException_whenDifferentSellerInCart() {
        int cartId = 1;
        int existingProductId = 10;
        int newProductId = 20;

        CartItem existingItem = new CartItem();
        existingItem.setCartItemsId(100);
        existingItem.setCartId(cartId);
        existingItem.setProductId(existingProductId);
        existingItem.setQuantity(1);

        CartItem newItem = new CartItem();
        newItem.setCartId(cartId);
        newItem.setProductId(newProductId);
        newItem.setQuantity(2);

        // existing items in cart
        when(cartItemDao.findAllByCartId(cartId))
                .thenReturn(List.of(existingItem));

        // different seller for new product vs existing product
        when(productDao.getSellerIdByProductId(newProductId)).thenReturn(2);
        when(productDao.getSellerIdByProductId(existingProductId)).thenReturn(1);

        assertThrows(ValidationException.class, () -> cartItemService.addItem(newItem));

        verify(cartItemDao).findAllByCartId(cartId);
        verify(productDao).getSellerIdByProductId(newProductId);
        verify(productDao).getSellerIdByProductId(existingProductId);
        verify(cartItemDao, never()).addItem(any());
        verify(cartItemDao, never()).updateItemQuantity(anyInt(), anyInt());
    }

    @Test
    void addItem_shouldUpdateQuantity_whenItemAlreadyExistsInCart() {
        int cartId = 1;
        int productId = 10;

        CartItem newItem = new CartItem();
        newItem.setCartId(cartId);
        newItem.setProductId(productId);
        newItem.setQuantity(2);

        CartItem existingItem = new CartItem(100, cartId, productId, 3);
        CartItem updatedItem = new CartItem(100, cartId, productId, 5); // 3 + 2

        // no single-seller conflict (empty list → skip seller check)
        when(cartItemDao.findAllByCartId(cartId))
                .thenReturn(Collections.emptyList());

        when(cartItemDao.existsInCart(cartId, productId))
                .thenReturn(true);

        // first call: existing item; second call: updated item
        when(cartItemDao.findByProductAndCart(cartId, productId))
                .thenReturn(Optional.of(existingItem))
                .thenReturn(Optional.of(updatedItem));

        CartItem result = cartItemService.addItem(newItem);

        assertNotNull(result);
        assertEquals(100, result.getCartItemsId());
        assertEquals(5, result.getQuantity());

        verify(cartItemDao).findAllByCartId(cartId);
        verify(cartItemDao).existsInCart(cartId, productId);
        verify(cartItemDao, times(2)).findByProductAndCart(cartId, productId);
        verify(cartItemDao).updateItemQuantity(100, 5);
        verify(cartItemDao, never()).addItem(any());
    }

    @Test
    void addItem_shouldAddNewItem_whenNotExistsInCart() {
        int cartId = 1;
        int productId = 10;

        CartItem newItem = new CartItem();
        newItem.setCartId(cartId);
        newItem.setProductId(productId);
        newItem.setQuantity(2);

        CartItem savedItem = new CartItem(101, cartId, productId, 2);

        // no single-seller conflict (empty existing cart)
        when(cartItemDao.findAllByCartId(cartId))
                .thenReturn(Collections.emptyList());

        when(cartItemDao.existsInCart(cartId, productId))
                .thenReturn(false);

        when(cartItemDao.addItem(newItem))
                .thenReturn(savedItem);

        CartItem result = cartItemService.addItem(newItem);

        assertNotNull(result);
        assertEquals(101, result.getCartItemsId());
        assertEquals(2, result.getQuantity());

        verify(cartItemDao).findAllByCartId(cartId);
        verify(cartItemDao).existsInCart(cartId, productId);
        verify(cartItemDao).addItem(newItem);
        verify(cartItemDao, never()).updateItemQuantity(anyInt(), anyInt());
    }

    // ─────────────────────────────────────────
    // updateItemQuantity() tests
    // ─────────────────────────────────────────

    @Test
    void updateItemQuantity_shouldThrowValidationException_whenQuantityInvalid() {
        assertThrows(
                ValidationException.class,
                () -> cartItemService.updateItemQuantity(1, 0)
        );

        verifyNoInteractions(cartItemDao);
    }

    @Test
    void updateItemQuantity_shouldThrowResourceNotFound_whenDaoReturnsZeroRows() {
        when(cartItemDao.updateItemQuantity(1, 3)).thenReturn(0);

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartItemService.updateItemQuantity(1, 3)
        );

        verify(cartItemDao).updateItemQuantity(1, 3);
        verify(cartItemDao, never()).findById(anyInt());
    }

    @Test
    void updateItemQuantity_shouldReturnUpdatedItem_whenSuccessful() {
        CartItem updated = new CartItem(1, 1, 10, 5);

        when(cartItemDao.updateItemQuantity(1, 5)).thenReturn(1);
        when(cartItemDao.findById(1)).thenReturn(Optional.of(updated));

        CartItem result = cartItemService.updateItemQuantity(1, 5);

        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        assertEquals(10, result.getProductId());

        verify(cartItemDao).updateItemQuantity(1, 5);
        verify(cartItemDao).findById(1);
    }

    // ─────────────────────────────────────────
    // deleteItem / deleteItemByProductId
    // ─────────────────────────────────────────

    @Test
    void deleteItem_shouldThrowResourceNotFound_whenNoRowsDeleted() {
        when(cartItemDao.deleteItem(1)).thenReturn(0);

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartItemService.deleteItem(1)
        );

        verify(cartItemDao).deleteItem(1);
    }

    @Test
    void deleteItem_shouldSucceed_whenRowDeleted() {
        when(cartItemDao.deleteItem(1)).thenReturn(1);

        assertDoesNotThrow(() -> cartItemService.deleteItem(1));

        verify(cartItemDao).deleteItem(1);
    }

    @Test
    void deleteItemByProductId_shouldThrowResourceNotFound_whenNoRowsDeleted() {
        when(cartItemDao.deleteItemByProductId(1, 10)).thenReturn(0);

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartItemService.deleteItemByProductId(1, 10)
        );

        verify(cartItemDao).deleteItemByProductId(1, 10);
    }

    @Test
    void deleteItemByProductId_shouldSucceed_whenRowDeleted() {
        when(cartItemDao.deleteItemByProductId(1, 10)).thenReturn(1);

        assertDoesNotThrow(() -> cartItemService.deleteItemByProductId(1, 10));

        verify(cartItemDao).deleteItemByProductId(1, 10);
    }

    // ─────────────────────────────────────────
    // getItemById / existsInCart basic tests
    // ─────────────────────────────────────────

    @Test
    void getItemById_shouldReturnItem_whenPresent() {
        CartItem item = new CartItem(1, 1, 10, 2);
        when(cartItemDao.findById(1)).thenReturn(Optional.of(item));

        CartItem result = cartItemService.getItemById(1);

        assertNotNull(result);
        assertEquals(10, result.getProductId());
        verify(cartItemDao).findById(1);
    }

    @Test
    void getItemById_shouldThrowResourceNotFound_whenNotPresent() {
        when(cartItemDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartItemService.getItemById(1)
        );

        verify(cartItemDao).findById(1);
    }

    @Test
    void existsInCart_shouldDelegateToDao() {
        when(cartItemDao.existsInCart(1, 10)).thenReturn(true);

        boolean result = cartItemService.existsInCart(1, 10);

        assertTrue(result);
        verify(cartItemDao).existsInCart(1, 10);
    }
}

