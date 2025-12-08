package com.ShopSphere.shop_sphere.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.MockedStatic;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ShopSphere.shop_sphere.dto.UserDto;
import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.UserService;

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User existingUser;
    private UserDto dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        existingUser = new User();
        existingUser.setUserId(10);
        existingUser.setName("Bob");
        existingUser.setEmail("bob@example.com");
        existingUser.setPassword("secret");
        existingUser.setRole("Buyer");
        existingUser.setPhone("9999999999");
        existingUser.setLocationId(3);

        dto = new UserDto();
        dto.setName("Bob Updated");
        dto.setPhone("1111111111");
        dto.setRole("Buyer");
        dto.setLocationId(5);
        // dto.email intentionally omitted for update (controller keeps existing email)
    }

    @Test
    void testGetUserById_AsOwner_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(userService.getUserById(10)).thenReturn(existingUser);

            mockMvc.perform(get("/api/users/10"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.userId").value(10))
                   .andExpect(jsonPath("$.email").value("bob@example.com"));

            verify(userService, times(1)).getUserById(10);
        }
    }

    @Test
    void testGetUserById_AsAdmin_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(999);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);

            when(userService.getUserById(10)).thenReturn(existingUser);

            mockMvc.perform(get("/api/users/10"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.name").value("Bob"));

            verify(userService, times(1)).getUserById(10);
        }
    }

    @Test
    void testGetUserByEmail_Succeeds() throws Exception {
        when(userService.getUserByEmail("bob@example.com")).thenReturn(existingUser);

        mockMvc.perform(get("/api/users/email/bob@example.com"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.email").value("bob@example.com"));

        verify(userService, times(1)).getUserByEmail("bob@example.com");
    }

    @Test
    void testGetAllUsers_Succeeds() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(existingUser));

        mockMvc.perform(get("/api/users"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].name").value("Bob"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testUpdateUser_AsOwner_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(userService.getUserById(10)).thenReturn(existingUser);
            when(userService.updateUser(any(User.class))).thenReturn(1);

            mockMvc.perform(put("/api/users/10")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("User updated successfully")));

            verify(userService, times(1)).getUserById(10);
            verify(userService, times(1)).updateUser(any(User.class));
        }
    }

    @Test
    void testUpdateUser_Unauthorized_ThrowsSecurity() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(2);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            mockMvc.perform(put("/api/users/10")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isInternalServerError()); // SecurityException is not mapped by controller advice here

            verify(userService, never()).updateUser(any());
        }
    }

    @Test
    void testDeleteUser_Succeeds() throws Exception {
        doNothing().when(userService).deleteUser(10);

        mockMvc.perform(delete("/api/users/10"))
               .andExpect(status().isOk())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("User deleted successfully")));

        verify(userService, times(1)).deleteUser(10);
    }

    @Test
    void testUserExists_ReturnsBoolean() throws Exception {
        when(userService.userExistsByEmail("e@x.com")).thenReturn(true);

        mockMvc.perform(get("/api/users/exists/e@x.com"))
               .andExpect(status().isOk())
               .andExpect(content().string("true"));

        verify(userService, times(1)).userExistsByEmail("e@x.com");
    }

    @Test
    void testGetUserProfile_Success() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            Map<String, Object> profile = Map.of("user_id", 10, "name", "Bob", "city", "Delhi");
            when(userService.getUserWithLocation(10)).thenReturn(profile);

            mockMvc.perform(get("/api/users/10/profile"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.city").value("Delhi"));

            verify(userService, times(1)).getUserWithLocation(10);
        }
    }

    @Test
    void testGetUserProfile_NotFound_Returns404() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(10);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(userService.getUserWithLocation(99)).thenThrow(new EmptyResultDataAccessException(1));

            mockMvc.perform(get("/api/users/99/profile"))
                   .andExpect(status().isNotFound())
                   .andExpect(content().string(org.hamcrest.Matchers.containsString("User not found with id: 99")));

            verify(userService, times(1)).getUserWithLocation(99);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    void parameterizedUserIds(int id) throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.getLoggedInUserId(any(HttpServletRequest.class))).thenReturn(id);
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            when(userService.getUserById(id)).thenReturn(existingUser);
            mockMvc.perform(get("/api/users/" + id)).andExpect(status().isOk());
        }
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled");
    }
}