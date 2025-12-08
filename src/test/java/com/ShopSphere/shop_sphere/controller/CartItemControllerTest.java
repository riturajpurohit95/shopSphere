package com.ShopSphere.shop_sphere.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ShopSphere.shop_sphere.dto.CartItemDto;
import com.ShopSphere.shop_sphere.exception.ForbiddenException;
import com.ShopSphere.shop_sphere.exception.OutOfStockException;
import com.ShopSphere.shop_sphere.model.Cart;
import com.ShopSphere.shop_sphere.model.CartItem;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.CartItemService;
import com.ShopSphere.shop_sphere.service.CartService;
import com.ShopSphere.shop_sphere.service.ProductService;

public class CartItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private CartService cartService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartItemController cartItemController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private CartItem item;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cartItemController).build();

        item = new CartItem();
        item.setCartItemsId(5);
        item.setCartId(10);
        item.setProductId(100);
        item.setQuantity(2);

        product = new Product();
        product.setProductId(100);
        product.setProductName("Product A");
        product.setProductPrice(new BigDecimal("99.99"));
        product.setProductQuantity(10);
        product.setImageUrl("img.jpg");

        cart = new Cart();
        cart.setCartId(10);
        cart.setUserId(42);
    }

    @Test
    void testAddItem_Success() throws Exception {
        CartItemDto dto = new CartItemDto(0, 10, 100, 2, null, null, null);

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            // logged user is owner of cart
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(productService.getProductById(100)).thenReturn(product);
            when(cartService.getCartById(10)).thenReturn(cart);
            when(cartItemService.addItem(any(CartItem.class))).thenReturn(item);

            mockMvc.perform(post("/api/cart-items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItemsId").value(5))
                    .andExpect(jsonPath("$.productId").value(100))
                    .andExpect(jsonPath("$.productName").value("Product A"))
                    .andExpect(jsonPath("$.image").value("img.jpg"));

            verify(cartItemService, times(1)).addItem(any(CartItem.class));
        }
    }

    @Test
    void testAddItem_InvalidRequest_QuantityZero_Throws() throws Exception {
        CartItemDto dto = new CartItemDto(0, 10, 100, 0, null, null, null);

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            mockMvc.perform(post("/api/cart-items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError()); // IllegalArgumentException -> 500

            verify(cartItemService, never()).addItem(any());
        }
    }

    @Test
    void testAddItem_OutOfStock_ThrowsConflict() throws Exception {
        CartItemDto dto = new CartItemDto(0, 10, 100, 20, null, null, null); // request more than stock

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            Product lowStock = new Product();
            lowStock.setProductId(100);
            lowStock.setProductQuantity(1);

            when(productService.getProductById(100)).thenReturn(lowStock);
            when(cartService.getCartById(10)).thenReturn(cart);

            mockMvc.perform(post("/api/cart-items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError()); // OutOfStockException -> 500 in absence of advice

            verify(cartItemService, never()).addItem(any());
        }
    }

    @Test
    void testAddItem_Forbidden_WhenNotOwner() throws Exception {
        CartItemDto dto = new CartItemDto(0, 10, 100, 1, null, null, null);

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            // logged user is not owner and not admin
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(2);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(cartService.getCartById(10)).thenReturn(cart);
            when(productService.getProductById(100)).thenReturn(product);

            mockMvc.perform(post("/api/cart-items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError()); // ForbiddenException -> 500

            verify(cartItemService, never()).addItem(any());
        }
    }

    @Test
    void testGetItemsByCartId_Success() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(cartService.getCartById(10)).thenReturn(cart);
            when(cartItemService.getItemsByCartId(10)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/cart-items/cart/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1))
                    .andExpect(jsonPath("$[0].cartItemsId").value(5));

            verify(cartItemService, times(1)).getItemsByCartId(10);
        }
    }

    @Test
    void testGetItem_Success() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(cartItemService.getItemById(5)).thenReturn(item);
            when(cartService.getCartById(10)).thenReturn(cart);
            when(productService.getProductById(100)).thenReturn(product);

            mockMvc.perform(get("/api/cart-items/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItemsId").value(5))
                    .andExpect(jsonPath("$.productName").value("Product A"));

            verify(cartItemService, times(1)).getItemById(5);
        }
    }

    @Test
    void testGetItem_NotFound_Throws() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(cartItemService.getItemById(999)).thenReturn(null);

            mockMvc.perform(get("/api/cart-items/999"))
                    .andExpect(status().isInternalServerError()); // IllegalArgumentException -> 500

            verify(cartItemService, times(1)).getItemById(999);
        }
    }

    @Test
    void testUpdateItemQuantity_Success() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            CartItem existing = new CartItem();
            existing.setCartItemsId(5);
            existing.setCartId(10);
            existing.setProductId(100);
            existing.setQuantity(1);

            when(cartItemService.getItemById(5)).thenReturn(existing);
            when(cartService.getCartById(10)).thenReturn(cart);
            Product p = new Product();
            p.setProductId(100);
            p.setProductQuantity(10);
            when(productService.getProductById(100)).thenReturn(p);

            CartItem updated = new CartItem();
            updated.setCartItemsId(5);
            updated.setCartId(10);
            updated.setProductId(100);
            updated.setQuantity(3);

            when(cartItemService.updateItemQuantity(5, 3)).thenReturn(updated);

            mockMvc.perform(put("/api/cart-items/5/quantity/3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(3));

            verify(cartItemService, times(1)).updateItemQuantity(5, 3);
        }
    }

    @Test
    void testUpdateItemQuantity_OutOfStock_Throws() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            CartItem existing = new CartItem();
            existing.setCartItemsId(5);
            existing.setCartId(10);
            existing.setProductId(100);
            existing.setQuantity(1);

            when(cartItemService.getItemById(5)).thenReturn(existing);
            when(cartService.getCartById(10)).thenReturn(cart);

            Product p = new Product();
            p.setProductId(100);
            p.setProductQuantity(2);
            when(productService.getProductById(100)).thenReturn(p);

            // request quantity 5 > available 2 -> OutOfStockException thrown by controller
            mockMvc.perform(put("/api/cart-items/5/quantity/5"))
                    .andExpect(status().isInternalServerError());

            verify(cartItemService, never()).updateItemQuantity(anyInt(), anyInt());
        }
    }

    @Test
    void testDeleteItem_Success() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(cartItemService.getItemById(5)).thenReturn(item);
            when(cartService.getCartById(10)).thenReturn(cart);
            doNothing().when(cartItemService).deleteItem(5);

            mockMvc.perform(delete("/api/cart-items/5"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Cart item deleted successfully")));

            verify(cartItemService, times(1)).deleteItem(5);
        }
    }

    @Test
    void testCalculateTotalAmount_Success() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(cartService.getCartById(10)).thenReturn(cart);
            when(cartItemService.calculateTotalAmount(10)).thenReturn(299.97);

            mockMvc.perform(get("/api/cart-items/total/10"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("299.97"));

            verify(cartItemService, times(1)).calculateTotalAmount(10);
        }
    }

    @Test
    void testExistsInCart_Success() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(cartService.getCartById(10)).thenReturn(cart);
            when(cartItemService.existsInCart(10, 100)).thenReturn(true);

            mockMvc.perform(get("/api/cart-items/exists/10/100"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(cartItemService, times(1)).existsInCart(10, 100);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10})
    void parameterizedCartIds(int cartId) throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(42);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(cartService.getCartById(cartId)).thenReturn(cart);
            when(cartItemService.getItemsByCartId(cartId)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/cart-items/cart/" + cartId))
                   .andExpect(status().isOk());
        }
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled");
    }
}