package com.ShopSphere.shop_sphere.repository;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.util.UserRowMapper;

public class UserDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private UserDaoImpl userDao;

    private User user;
    private User user2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setPassword("pw");
        user.setRole("Buyer");
        user.setPhone("999");
        user.setLocationId(2);

        user2 = new User();
        user2.setUserId(10);
        user2.setName("Bob");
        user2.setEmail("bob@example.com");
        user2.setPassword("pw2");
        user2.setRole("Seller");
        user2.setPhone("888");
        user2.setLocationId(3);
    }

    @Test
    void testSave_ShouldReturnGeneratedKey() {
        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            Map<String, Object> keyMap = new HashMap<>();
            keyMap.put("GENERATED_KEY", 555);
            kh.getKeyList().add(keyMap);
            return 1;
        }).when(jdbcTemplate).update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class));

        int id = userDao.save(user);
        assertTrue(id > 0);
    }

    @Test
    void testFindById_Found() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq(10)))
            .thenReturn(user2);

        User u = userDao.findById(10);
        assertNotNull(u);
        assertEquals("Bob", u.getName());
    }

    @Test
    void testFindById_NotFound_ReturnsNull() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq(99)))
            .thenThrow(new EmptyResultDataAccessException(1));
        User u = userDao.findById(99);
        assertNull(u);
    }

    @Test
    void testFindByEmail_Found() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq("bob@example.com")))
            .thenReturn(user2);
        User u = userDao.findByEmail("bob@example.com");
        assertNotNull(u);
        assertEquals("bob@example.com", u.getEmail());
    }

    @Test
    void testFindByEmail_NotFound_ReturnsNull() {
        when(jdbcTemplate.queryForObject(anyString(), any(UserRowMapper.class), eq("missing@example.com")))
            .thenThrow(new EmptyResultDataAccessException(1));
        User u = userDao.findByEmail("missing@example.com");
        assertNull(u);
    }

    @Test
    void testFindAll() {
        when(jdbcTemplate.query(anyString(), any(UserRowMapper.class)))
            .thenReturn(Arrays.asList(user2, user));
        List<User> list = userDao.findAll();
        assertEquals(2, list.size());
    }

    @Test
    void testUpdate() {
        when(jdbcTemplate.update(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt()))
            .thenReturn(1);
        int rows = userDao.update(user2);
        assertEquals(1, rows);
    }

    @Test
    void testDelete() {
        when(jdbcTemplate.update(anyString(), anyInt())).thenReturn(1);
        int rows = userDao.delete(10);
        assertEquals(1, rows);
    }

    @Test
    void testGetLocationIdOfUser_Success() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(10))).thenReturn(3);
        int loc = userDao.getLocationIdOfUser(10);
        assertEquals(3, loc);
    }

    @Test
    void testGetLocationIdOfUser_NotFound_ThrowsRuntime() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(99)))
            .thenReturn(null);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userDao.getLocationIdOfUser(99));
        assertTrue(ex.getMessage().contains("User location not found"));
    }

    @Test
    void testGetUserWithLocation() {
        Map<String, Object> row = Map.of("user_id", 10, "name", "Bob", "city", "Delhi");
        when(jdbcTemplate.queryForMap(anyString(), eq(10))).thenReturn(row);

        Map<String, Object> result = userDao.getUserWithLocation(10);
        assertNotNull(result);
        assertEquals("Delhi", result.get("city"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice@example.com", "bob@example.com"})
    void testParameterizedEmails(String mail) {
        User u = new User();
        u.setEmail(mail);
        assertNotNull(u.getEmail());
        assertTrue(u.getEmail().contains("@"));
    }

    @Disabled("Example of disabled DAO test")
    @Test
    void disabledTest() {
        fail("This test is disabled");
    }
}