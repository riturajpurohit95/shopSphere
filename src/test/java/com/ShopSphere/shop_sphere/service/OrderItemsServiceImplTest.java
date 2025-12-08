
package com.ShopSphere.shop_sphere.service;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.repository.OrderItemDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemsServiceImplTest {

    @Mock
    private OrderItemDao orderItemDao;

    @InjectMocks
    private OrderItemsServiceImpl orderItemsService;

    private OrderItem item;

    @BeforeEach
    void setup() {
        item = new OrderItem();
        item.setOrderItemsId(1);
        item.setOrderId(10);
        item.setProductId(5);
        item.setSellerId(3);
        item.setProductName("Phone Case");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("150.00"));
        item.setTotalItemPrice(new BigDecimal("300.00"));
    }

    // ──────────────────────────────────────────────
    // createOrderItem()
    // ──────────────────────────────────────────────

    @Test
    void createOrderItem_success() {
        when(orderItemDao.save(item)).thenReturn(1);

        OrderItem saved = orderItemsService.createOrderItem(item);

        assertNotNull(saved);
        assertEquals(2, saved.getQuantity());
        verify(orderItemDao).save(item);
    }

    @Test
    void createOrderItem_shouldThrow_whenNullPassed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderItemsService.createOrderItem(null)
        );
        verify(orderItemDao, never()).save(any());
    }

    @Test
    void createOrderItem_shouldThrow_whenDaoReturnsZero() {
        when(orderItemDao.save(item)).thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> orderItemsService.createOrderItem(item)
        );
    }

    // ──────────────────────────────────────────────
    // getOrderItemById()
    // ──────────────────────────────────────────────

    @Test
    void getOrderItemById_success() {
        when(orderItemDao.findById(1)).thenReturn(Optional.of(item));

        OrderItem result = orderItemsService.getOrderItemById(1);

        assertEquals("Phone Case", result.getProductName());
        verify(orderItemDao).findById(1);
    }

    @Test
    void getOrderItemById_notFound() {
        when(orderItemDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderItemsService.getOrderItemById(1)
        );
    }

    // ──────────────────────────────────────────────
    // getOrderItemsByOrderId()
    // ──────────────────────────────────────────────

    @Test
    void getOrderItemsByOrderId_success() {
        when(orderItemDao.findByOrderId(10))
                .thenReturn(Arrays.asList(item));

        var list = orderItemsService.getOrderItemsByOrderId(10);

        assertEquals(1, list.size());
        verify(orderItemDao).findByOrderId(10);
    }

    // ──────────────────────────────────────────────
    // getOrderItemsByProductId()
    // ──────────────────────────────────────────────

    @Test
    void getOrderItemsByProductId_success() {
        when(orderItemDao.findByProductId(5))
                .thenReturn(Arrays.asList(item));

        var list = orderItemsService.getOrderItemsByProductId(5);

        assertEquals(1, list.size());
        assertEquals(5, list.get(0).getProductId());
        verify(orderItemDao).findByProductId(5);
    }

    // ──────────────────────────────────────────────
    // updateItemQuantity()
    // ──────────────────────────────────────────────

    @Test
    void updateItemQuantity_success() {
        // existing item
        when(orderItemDao.findById(1)).thenReturn(Optional.of(item));
        // DAO returns 1 row updated
        when(orderItemDao.updateQuantityANDTotalPrice(eq(1), eq(5), any(BigDecimal.class)))
                .thenReturn(1);

        OrderItem updated = orderItemsService.updateItemQuantity(1, 5);

        assertEquals(5, updated.getQuantity());
        assertEquals(new BigDecimal("750.00"), updated.getTotalItemPrice());
        verify(orderItemDao).updateQuantityANDTotalPrice(eq(1), eq(5), any(BigDecimal.class));
    }

    @Test
    void updateItemQuantity_shouldThrow_whenQuantityZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderItemsService.updateItemQuantity(1, 0)
        );
    }

    @Test
    void updateItemQuantity_shouldThrow_whenNotFound() {
        when(orderItemDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderItemsService.updateItemQuantity(1, 5)
        );
    }

    @Test
    void updateItemQuantity_shouldThrow_whenDaoFails() {
        when(orderItemDao.findById(1)).thenReturn(Optional.of(item));
        when(orderItemDao.updateQuantityANDTotalPrice(eq(1), eq(5), any(BigDecimal.class)))
                .thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> orderItemsService.updateItemQuantity(1, 5)
        );
    }

    // ──────────────────────────────────────────────
    // deleteOrderItem()
    // ──────────────────────────────────────────────

    @Test
    void deleteOrderItem_success() {
        when(orderItemDao.findById(1)).thenReturn(Optional.of(item));
        when(orderItemDao.deleteById(1)).thenReturn(1);

        assertDoesNotThrow(() -> orderItemsService.deleteOrderItem(1));
        verify(orderItemDao).deleteById(1);
    }

    @Test
    void deleteOrderItem_shouldThrow_whenNotFound() {
        when(orderItemDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> orderItemsService.deleteOrderItem(1)
        );
    }

    @Test
    void deleteOrderItem_shouldThrow_whenDaoFails() {
        when(orderItemDao.findById(1)).thenReturn(Optional.of(item));
        when(orderItemDao.deleteById(1)).thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> orderItemsService.deleteOrderItem(1)
        );
    }
}
