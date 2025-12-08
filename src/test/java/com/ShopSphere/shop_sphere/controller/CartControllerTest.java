package com.ShopSphere.shop_sphere.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ShopSphere.shop_sphere.dto.CartItemDto;
import com.ShopSphere.shop_sphere.dto.CartDto;
import com.ShopSphere.shop_sphere.model.Cart;
import com.ShopSphere.shop_sphere.service.CartService;

public class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Cart sampleCart;
    private CartDto sampleCartDto;
    private CartItemDto sampleCartItemDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();

        sampleCart = new Cart();
        sampleCart.setCartId(55);
        sampleCart.setUserId(7);

        sampleCartDto = new CartDto();
        sampleCartDto.setCartId(55);
        sampleCartDto.setUserId(7);

        sampleCartItemDto = new CartItemDto();
        sampleCartItemDto.setCartItemsId(101);
        sampleCartItemDto.setCartId(55);
        sampleCartItemDto.setProductId(202);
        sampleCartItemDto.setQuantity(2);
        sampleCartItemDto.setProductName("Test Product");
        sampleCartItemDto.setProductPrice(499.99);
        sampleCartItemDto.setImageUrl("img.png");
    }

    @Test
    void testCreateCart_ReturnsCartDto() throws Exception {
        when(cartService.createCart(7)).thenReturn(sampleCart);

        mockMvc.perform(post("/api/carts/7")
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.cartId").value(55))
               .andExpect(jsonPath("$.userId").value(7));

        verify(cartService, times(1)).createCart(7);
    }

    @Test
    void testGetCartByUserId_ReturnsCartDto() throws Exception {
        when(cartService.getCartByUserId(7)).thenReturn(sampleCart);

        mockMvc.perform(get("/api/carts/user/7"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.cartId").value(55))
               .andExpect(jsonPath("$.userId").value(7));

        verify(cartService, times(1)).getCartByUserId(7);
    }

    @Test
    void testGetCartById_ReturnsCartDto() throws Exception {
        when(cartService.getCartById(55)).thenReturn(sampleCart);

        mockMvc.perform(get("/api/carts/55"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.cartId").value(55));

        verify(cartService, times(1)).getCartById(55);
    }

    @Test
    void testGetAllCarts_ReturnsList() throws Exception {
        when(cartService.getAllCarts()).thenReturn(List.of(sampleCart));

        mockMvc.perform(get("/api/carts"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].cartId").value(55));

        verify(cartService, times(1)).getAllCarts();
    }

    @Test
    void testDeleteCart_NoContent() throws Exception {
        doNothing().when(cartService).deleteCart(55);

        mockMvc.perform(delete("/api/carts/55"))
               .andExpect(status().isOk())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("Cart deleted successfully")));

        verify(cartService, times(1)).deleteCart(55);
    }

    @Test
    void testCartExistsForUser_ReturnsBoolean() throws Exception {
        when(cartService.cartExistsForUser(7)).thenReturn(true);

        mockMvc.perform(get("/api/carts/exists/7"))
               .andExpect(status().isOk())
               .andExpect(content().string("true"));

        verify(cartService, times(1)).cartExistsForUser(7);
    }

    @Test
    void testGetCartItems_ReturnsCartItemDtos() throws Exception {
        List<CartItemDto> items = List.of(sampleCartItemDto);
        when(cartService.getCartItemsByUserId(7)).thenReturn(items);

        mockMvc.perform(get("/api/carts/userCart/7"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].cartItemsId").value(101))
               .andExpect(jsonPath("$[0].productId").value(202))
               .andExpect(jsonPath("$[0].productName").value("Test Product"))
               .andExpect(jsonPath("$[0].productPrice").value(499.99));

        verify(cartService, times(1)).getCartItemsByUserId(7);
    }

    // Parameterized sanity check
    @ParameterizedTest
    @ValueSource(ints = {7, 11})
    void parameterizedUserIds(int userId) throws Exception {
        when(cartService.getCartByUserId(userId)).thenReturn(sampleCart);
        mockMvc.perform(get("/api/carts/user/" + userId)).andExpect(status().isOk());
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        org.junit.jupiter.api.Assertions.fail("This test is disabled");
    }
}