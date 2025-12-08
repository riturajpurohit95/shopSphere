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
import com.ShopSphere.shop_sphere.dto.WishlistDto;
import com.ShopSphere.shop_sphere.model.Wishlist;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.WishlistService;

public class WishlistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private WishlistController wishlistController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Wishlist sampleWishlist;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(wishlistController).build();

        sampleWishlist = new Wishlist();
        sampleWishlist.setWishlistId(55);
        sampleWishlist.setUserId(10);
    }

    @Test
    void testCreateWishlist_AsLoggedUser_ReturnsCreated() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistService.createWishlist(10)).thenReturn(sampleWishlist);

            mockMvc.perform(post("/api/wishlist"))
                   .andExpect(status().isCreated())
                   .andExpect(jsonPath("$.wishlistId").value(55))
                   .andExpect(jsonPath("$.userId").value(10));

            verify(wishlistService, times(1)).createWishlist(10);
        }
    }

    @Test
    void testGetMyWishlist_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistService.getWishlistByUserId(10)).thenReturn(sampleWishlist);

            mockMvc.perform(get("/api/wishlist/my"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.userId").value(10));

            verify(wishlistService, times(1)).getWishlistByUserId(10);
        }
    }

    @Test
    void testGetWishlistById_AsOwner_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistService.getWishlistById(55)).thenReturn(sampleWishlist);

            mockMvc.perform(get("/api/wishlist/55"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.wishlistId").value(55))
                   .andExpect(jsonPath("$.userId").value(10));

            verify(wishlistService, times(1)).getWishlistById(55);
        }
    }

    @Test
    void testGetWishlistById_AsOtherUser_ThrowsSecurity() throws Exception {
        // logged user is 2, wishlist owner is 10 -> should throw SecurityException in controller
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(2);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistService.getWishlistById(55)).thenReturn(sampleWishlist);

            mockMvc.perform(get("/api/wishlist/55"))
                   .andExpect(status().isInternalServerError()); // SecurityException isn't mapped by controller advice here

            verify(wishlistService, times(1)).getWishlistById(55);
        }
    }

    @Test
    void testGetAllWishlists_Admin_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            // getAllWishlists endpoint doesn't check SecurityUtil in controller itself (AllowedRoles is a separate concern),
            // so we just stub service.
            when(wishlistService.getAllWishlists()).thenReturn(List.of(sampleWishlist));

            mockMvc.perform(get("/api/wishlist"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.size()").value(1))
                   .andExpect(jsonPath("$[0].wishlistId").value(55));

            verify(wishlistService, times(1)).getAllWishlists();
        }
    }

    @Test
    void testDeleteWishlist_AsOwner_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistService.getWishlistById(55)).thenReturn(sampleWishlist);
            doNothing().when(wishlistService).deleteWishlist(55);

            mockMvc.perform(delete("/api/wishlist/55"))
                   .andExpect(status().isOk())
                   .andExpect(content().string(org.hamcrest.Matchers.containsString("Wishlist deleted successfully")));

            verify(wishlistService, times(1)).getWishlistById(55);
            verify(wishlistService, times(1)).deleteWishlist(55);
        }
    }

    @Test
    void testWishlistExists_ReturnsBoolean() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistService.wishlistExistsForUser(10)).thenReturn(true);

            mockMvc.perform(get("/api/wishlist/exists"))
                   .andExpect(status().isOk())
                   .andExpect(content().string("true"));

            verify(wishlistService, times(1)).wishlistExistsForUser(10);
        }
    }

    @Test
    void testIsWishlistEmpty_ReturnsValue() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistService.getWishlistByUserId(10)).thenReturn(sampleWishlist);
            when(wishlistService.isWishlistEmpty(55)).thenReturn(true);

            mockMvc.perform(get("/api/wishlist/is-empty"))
                   .andExpect(status().isOk())
                   .andExpect(content().string("true"));

            verify(wishlistService, times(1)).getWishlistByUserId(10);
            verify(wishlistService, times(1)).isWishlistEmpty(55);
        }
    }

    @Test
    void testGetWishlistItems_ReturnsList() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            Map<String, Object> item = Map.of("product_id", 1, "product_name", "P");
            when(wishlistService.getWishlistItems(10)).thenReturn(List.of(item));

            mockMvc.perform(get("/api/wishlist/items"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.size()").value(1))
                   .andExpect(jsonPath("$[0].product_name").value("P"));

            verify(wishlistService, times(1)).getWishlistItems(10);
        }
    }

    // Parameterized simple sanity test
    @ParameterizedTest
    @ValueSource(ints = {10, 20, 30})
    void parameterizedUserIds(int userId) throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(userId);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(wishlistService.getWishlistByUserId(userId)).thenReturn(sampleWishlist);
            mockMvc.perform(get("/api/wishlist/my")).andExpect(status().isOk());
        }
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled");
    }
}