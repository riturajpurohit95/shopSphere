package com.ShopSphere.shop_sphere.service;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.repository.UserDao;

public class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private User existingUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPassword("password123");
        user.setRole("Buyer");
        user.setPhone("9999999999");
        user.setLocationId(5);

        existingUser = new User();
        existingUser.setUserId(10);
        existingUser.setName("Existing");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("oldpass");
        existingUser.setRole("Seller");
        existingUser.setPhone("8888888888");
        existingUser.setLocationId(3);
    }

    // ---------- createUser tests ----------
    @Test
    void testCreateUser_Success() {
        when(userDao.findByEmail("alice@example.com")).thenReturn(null);
        when(userDao.save(any(User.class))).thenReturn(101);

        int id = userService.createUser(user);
        assertEquals(101, id);
        verify(userDao, times(1)).findByEmail("alice@example.com");
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateEmail_ThrowsRuntime() {
        when(userDao.findByEmail("alice@example.com")).thenReturn(existingUser);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(user));
        assertTrue(ex.getMessage().contains("User with email already exists"));
        verify(userDao, times(1)).findByEmail("alice@example.com");
        verify(userDao, never()).save(any());
    }

    @Test
    void testCreateUser_DaoFailure_ThrowsRuntime() {
        when(userDao.findByEmail("alice@example.com")).thenReturn(null);
        when(userDao.save(any(User.class))).thenReturn(0);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(user));
        assertTrue(ex.getMessage().contains("Failed to create user"));
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_InvalidEmail_ThrowsIllegalArgument() {
        user.setEmail("not-an-email");
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        verify(userDao, never()).save(any());
    }

    @Test
    void testCreateUser_MissingNameOrPassword_ThrowsIllegalArgument() {
        user.setName("");
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
        user.setName("Alice");
        user.setPassword("");
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
    }

    // ---------- getUserById tests ----------
    @Test
    void testGetUserById_Found() {
        when(userDao.findById(10)).thenReturn(existingUser);
        User u = userService.getUserById(10);
        assertNotNull(u);
        assertEquals(10, u.getUserId());
        verify(userDao, times(1)).findById(10);
    }

    @Test
    void testGetUserById_NotFound_ThrowsResourceNotFound() {
        when(userDao.findById(99)).thenReturn(null);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99));
        assertTrue(ex.getMessage().contains("No user found for userId"));
        verify(userDao, times(1)).findById(99);
    }

    @Test
    void testGetUserById_InvalidId() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(0));
        verify(userDao, never()).findById(anyInt());
    }

    // ---------- getUserByEmail tests ----------
    @Test
    void testGetUserByEmail_Found() {
        when(userDao.findByEmail("existing@example.com")).thenReturn(existingUser);
        User u = userService.getUserByEmail("existing@example.com");
        assertNotNull(u);
        assertEquals("existing@example.com", u.getEmail());
    }

    @Test
    void testGetUserByEmail_NotFound_ThrowsResourceNotFound() {
        when(userDao.findByEmail("missing@example.com")).thenReturn(null);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("missing@example.com"));
        assertTrue(ex.getMessage().contains("No user found for email"));
    }

    @Test
    void testGetUserByEmail_InvalidEmail_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail(""));
        assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail("bad-email"));
    }

    // ---------- getAllUsers ----------
    @Test
    void testGetAllUsers() {
        when(userDao.findAll()).thenReturn(Arrays.asList(existingUser, user));
        List<User> list = userService.getAllUsers();
        assertEquals(2, list.size());
        verify(userDao, times(1)).findAll();
    }

    // ---------- updateUser ----------
    @Test
    void testUpdateUser_Success() {
        User toUpdate = new User();
        toUpdate.setUserId(10);
        toUpdate.setName("Updated Name");
        toUpdate.setPassword("newpass");
        toUpdate.setPhone("7777777777");
        toUpdate.setLocationId(8);
        toUpdate.setEmail("shouldnotchange@example.com"); // will be overwritten
        toUpdate.setRole("Admin"); // will be overwritten

        when(userDao.findById(10)).thenReturn(existingUser);
        when(userDao.update(any(User.class))).thenReturn(1);

        int rows = userService.updateUser(toUpdate);

        assertEquals(1, rows);
        verify(userDao, times(1)).findById(10);
        verify(userDao, times(1)).update(any(User.class));
    }

    @Test
    void testUpdateUser_NotFound_ThrowsResourceNotFound() {
        User toUpdate = new User();
        toUpdate.setUserId(99);
        toUpdate.setName("Name");
        toUpdate.setPassword("pass");
        when(userDao.findById(99)).thenReturn(null);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(toUpdate));
        assertTrue(ex.getMessage().contains("Cannot update; user not found"));
        verify(userDao, times(1)).findById(99);
        verify(userDao, never()).update(any());
    }

    @Test
    void testUpdateUser_InvalidInput_ThrowsIllegalArgument() {
        User bad = new User();
        bad.setUserId(-1);
        bad.setName("");
        bad.setPassword("");
        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(bad));
    }

    @Test
    void testUpdateUser_UpdateFails_ThrowsRuntime() {
        User toUpdate = new User();
        toUpdate.setUserId(10);
        toUpdate.setName("Updated Name");
        toUpdate.setPassword("newpass");
        when(userDao.findById(10)).thenReturn(existingUser);
        when(userDao.update(any(User.class))).thenReturn(0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateUser(toUpdate));
        assertTrue(ex.getMessage().contains("Update failed for userId"));
    }

    // ---------- deleteUser ----------
    @Test
    void testDeleteUser_Success() {
        when(userDao.delete(10)).thenReturn(1);
        assertDoesNotThrow(() -> userService.deleteUser(10));
        verify(userDao, times(1)).delete(10);
    }

    @Test
    void testDeleteUser_NotFound_ThrowsResourceNotFound() {
        when(userDao.delete(99)).thenReturn(0);
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99));
        assertTrue(ex.getMessage().contains("Delete failed; user not found"));
    }

    @Test
    void testDeleteUser_InvalidId() {
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(0));
        verify(userDao, never()).delete(anyInt());
    }

    // ---------- userExistsByEmail ----------
    @Test
    void testUserExistsByEmail_WhenEmailEmpty_ReturnsFalse() {
        assertFalse(userService.userExistsByEmail(""));
    }

    @Test
    void testUserExistsByEmail_WhenExists_ReturnsTrue() {
        when(userDao.findByEmail("existing@example.com")).thenReturn(existingUser);
        assertTrue(userService.userExistsByEmail("existing@example.com"));
    }

    // ---------- getLocationIdOfUser & getUserWithLocation ----------
    @Test
    void testGetLocationIdOfUser() {
        when(userDao.getLocationIdOfUser(10)).thenReturn(3);
        int loc = userService.getLocationIdOfUser(10);
        assertEquals(3, loc);
        verify(userDao, times(1)).getLocationIdOfUser(10);
    }

    @Test
    void testGetUserWithLocation() {
        Map<String, Object> map = Map.of("user_id", 10, "name", "Existing", "city", "Mumbai");
        when(userDao.getUserWithLocation(10)).thenReturn(map);
        Map<String, Object> result = userService.getUserWithLocation(10);
        assertNotNull(result);
        assertEquals("Mumbai", result.get("city"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Admin", "Seller", "Buyer", "admin", " buyer "})
    void testValidateRole_AcceptsValidRoles(String role) {
        User u = new User();
        u.setName("Name");
        u.setEmail("roletest@example.com");
        u.setPassword("pass");
        u.setRole(role);
        when(userDao.findByEmail(anyString())).thenReturn(null);
        when(userDao.save(any(User.class))).thenReturn(1);
        assertDoesNotThrow(() -> userService.createUser(u));
    }
}