package com.ShopSphere.shop_sphere.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import com.ShopSphere.shop_sphere.dto.WishlistItemDto;
import com.ShopSphere.shop_sphere.model.WishlistItem;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.WishlistItemService;

public class WishlistItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WishlistItemService wishlistItemService;

    @InjectMocks
    private WishlistItemController wishlistItemController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private WishlistItemDto dto;
    private WishlistItem item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(wishlistItemController).build();

        dto = new WishlistItemDto();
        dto.setWishlistId(10);
        dto.setProductId(100);

        item = new WishlistItem();
        item.setWishlistItemsId(55);
        item.setWishlistId(10);
        item.setProductId(100);
    }

    @Test
    void testAddItem_Success_AsOwner() throws Exception {
        // Owner (logged user == owner id)
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(5);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            // Controller's validateUserOrAdmin calls wishlistItemService.getWishlistOwnerId(wishlistId)
            when(wishlistItemService.getWishlistOwnerId(10)).thenReturn(5);
            when(wishlistItemService.addItemToWishlist(any(WishlistItem.class))).thenReturn(55);

            mockMvc.perform(post("/api/wishlist-items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Product added to wishlist successfully"))
                    .andExpect(jsonPath("$.id").value(55));

            verify(wishlistItemService, times(1)).getWishlistOwnerId(10);
            verify(wishlistItemService, times(1)).addItemToWishlist(any(WishlistItem.class));
        }
    }

    @Test
    void testAddItem_Conflict_WhenBadRequest() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(5);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistItemService.getWishlistOwnerId(10)).thenReturn(5);
            when(wishlistItemService.addItemToWishlist(any(WishlistItem.class)))
                    .thenThrow(new com.ShopSphere.shop_sphere.exception.BadRequestException("already present"));

            mockMvc.perform(post("/api/wishlist-items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("already present"));

            verify(wishlistItemService, times(1)).addItemToWishlist(any(WishlistItem.class));
        }
    }

    @Test
    void testAddItem_Unauthorized_WhenNotOwnerOrAdmin() throws Exception {
        // logged user 2, owner is 5 and not admin -> should throw SecurityException in controller
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(2);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistItemService.getWishlistOwnerId(10)).thenReturn(5);

            mockMvc.perform(post("/api/wishlist-items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError()); // SecurityException not mapped -> 500

            verify(wishlistItemService, times(1)).getWishlistOwnerId(10);
            verify(wishlistItemService, never()).addItemToWishlist(any());
        }
    }

    @Test
    void testGetItemsByWishlist_AsOwner_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(5);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistItemService.getWishlistOwnerId(10)).thenReturn(5);
            // service returns domain objects; controller maps them to DTOs
            when(wishlistItemService.getItemsByWishlistId(10)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/wishlist-items/wishlist/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1))
                    .andExpect(jsonPath("$[0].wishlistItemsId").value(55))
                    .andExpect(jsonPath("$[0].productId").value(100));

            verify(wishlistItemService, times(1)).getItemsByWishlistId(10);
        }
    }

    @Test
    void testGetItemsByWishlist_Unauthorized() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(2);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistItemService.getWishlistOwnerId(10)).thenReturn(5);
            when(wishlistItemService.getItemsByWishlistId(10)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/wishlist-items/wishlist/10"))
                    .andExpect(status().isInternalServerError()); // SecurityException -> 500

            // still verify owner check was attempted
            verify(wishlistItemService, times(1)).getWishlistOwnerId(10);
            verify(wishlistItemService, never()).getItemsByWishlistId(10);
        }
    }

    @Test
    void testDeleteItem_AsOwner_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(5);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            // delete flow in controller:
            // 1) wishlistItemService.getWishlistIdByItem(wishlistItemId)
            // 2) validateUserOrAdmin => wishlistItemService.getWishlistOwnerId(wishlistId)
            when(wishlistItemService.getWishlistIdByItem(55)).thenReturn(10);
            when(wishlistItemService.getWishlistOwnerId(10)).thenReturn(5);
            doNothing().when(wishlistItemService).deleteItem(55);

            mockMvc.perform(delete("/api/wishlist-items/55"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Wishlist item deleted successfully")));

            verify(wishlistItemService, times(1)).getWishlistIdByItem(55);
            verify(wishlistItemService, times(1)).deleteItem(55);
        }
    }

    @Test
    void testDeleteItem_Unauthorized() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(2);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistItemService.getWishlistIdByItem(55)).thenReturn(10);
            when(wishlistItemService.getWishlistOwnerId(10)).thenReturn(5);

            mockMvc.perform(delete("/api/wishlist-items/55"))
                    .andExpect(status().isInternalServerError()); // SecurityException -> 500

            verify(wishlistItemService, times(1)).getWishlistIdByItem(55);
            verify(wishlistItemService, never()).deleteItem(55);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 20})
    void parameterizedWishlistIds(int wishlistId) throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(5);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistItemService.getWishlistOwnerId(wishlistId)).thenReturn(5);
            when(wishlistItemService.getItemsByWishlistId(wishlistId)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/wishlist-items/wishlist/" + wishlistId))
                   .andExpect(status().isOk());
        }
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTest() {
        fail("disabled");
    }
}