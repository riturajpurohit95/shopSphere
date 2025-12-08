package com.ShopSphere.shop_sphere.repository;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.ShopSphere.shop_sphere.model.Location;
import com.ShopSphere.shop_sphere.util.LocationRowMapper;

public class LocationDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private LocationDaoImpl locationDao;

    private Location loc1;
    private Location loc2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        loc1 = new Location();
        loc1.setLocationId(1);
        loc1.setCity("Mumbai");
        loc1.setHubValue(10);

        loc2 = new Location();
        loc2.setLocationId(2);
        loc2.setCity("Delhi");
        loc2.setHubValue(20);
    }

    // ---------- save ----------
    @Test
    void testSave_SetsGeneratedId() {
        Location toSave = new Location();
        toSave.setCity("Bengaluru");
        toSave.setHubValue(15);

        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            kh.getKeyList().add(Map.of("GENERATED_KEY", 55));
            return 1;
        }).when(jdbcTemplate).update(any(), any(KeyHolder.class));

        Location saved = locationDao.save(toSave);
        assertNotNull(saved);
        assertEquals(55, saved.getLocationId());
        assertEquals("Bengaluru", saved.getCity());
        verify(jdbcTemplate, times(1)).update(any(), any(KeyHolder.class));
    }

    // ---------- finndById (note spelling in impl) ----------
    @Test
    void testFinndById_Found() {
        when(jdbcTemplate.query(anyString(), any(LocationRowMapper.class), eq(1)))
                .thenReturn(Arrays.asList(loc1));

        var opt = locationDao.finndById(1);
        assertTrue(opt.isPresent());
        assertEquals("Mumbai", opt.get().getCity());
        verify(jdbcTemplate, times(1)).query(anyString(), any(LocationRowMapper.class), eq(1));
    }

    @Test
    void testFinndById_NotFound() {
        when(jdbcTemplate.query(anyString(), any(LocationRowMapper.class), eq(999)))
                .thenReturn(Arrays.asList());

        var opt = locationDao.finndById(999);
        assertFalse(opt.isPresent());
    }

    // ---------- finAll ----------
    @Test
    void testFinAll_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(LocationRowMapper.class)))
                .thenReturn(Arrays.asList(loc1, loc2));

        List<Location> all = locationDao.finAll();
        assertEquals(2, all.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(LocationRowMapper.class));
    }

    // ---------- findByCity ----------
    @Test
    void testFindByCity_ReturnsMatches() {
        when(jdbcTemplate.query(anyString(), any(LocationRowMapper.class), eq("mumbai")))
                .thenReturn(Arrays.asList(loc1));

        List<Location> res = locationDao.findByCity("mumbai");
        assertEquals(1, res.size());
        assertEquals("Mumbai", res.get(0).getCity());
        verify(jdbcTemplate, times(1)).query(anyString(), any(LocationRowMapper.class), eq("mumbai"));
    }

    // ---------- searchByCity ----------
    @Test
    void testSearchByCity_ReturnsMatches() {
        when(jdbcTemplate.query(anyString(), any(LocationRowMapper.class), anyString()))
                .thenReturn(Arrays.asList(loc2));

        List<Location> res = locationDao.searchByCity("del");
        assertEquals(1, res.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(LocationRowMapper.class), anyString());
    }

    // ---------- existsByCity ----------
    @Test
    void testExistsByCity_TrueAndFalse() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("mumbai"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("unknown"))).thenReturn(0);

        assertTrue(locationDao.existsByCity("mumbai"));
        assertFalse(locationDao.existsByCity("unknown"));

        verify(jdbcTemplate, times(2)).queryForObject(anyString(), eq(Integer.class), anyString());
    }

    // ---------- countLocations ----------
    @Test
    void testCountLocations_ReturnsCount() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(7);
        int cnt = locationDao.countLocations();
        assertEquals(7, cnt);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class));
    }

    // ---------- getHubValue ----------
    @Test
    void testGetHubValue_ReturnsHub() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(2))).thenReturn(20);
        int hub = locationDao.getHubValue(2);
        assertEquals(20, hub);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq(2));
    }

    // Parameterized example
    @ParameterizedTest
    @ValueSource(strings = {"Mumbai", "Delhi", "Kolkata"})
    void parameterizedCityNames(String city) {
        when(jdbcTemplate.query(anyString(), any(LocationRowMapper.class), eq(city)))
                .thenReturn(Arrays.asList(new Location(5, city, 9)));
        List<Location> res = locationDao.findByCity(city);
        assertFalse(res.isEmpty());
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled and skipped");
    }
}