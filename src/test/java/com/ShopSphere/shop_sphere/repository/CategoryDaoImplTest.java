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

import com.ShopSphere.shop_sphere.model.Category;
import com.ShopSphere.shop_sphere.util.CategoryRowMapper;

public class CategoryDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CategoryDaoImpl categoryDao;

    private Category cat1;
    private Category cat2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cat1 = new Category();
        cat1.setCategoryId(1);
        cat1.setCategoryName("Electronics");

        cat2 = new Category();
        cat2.setCategoryId(2);
        cat2.setCategoryName("Books");
    }

    // ---------- save ----------
    @Test
    void testSave_SetsGeneratedId() {
        Category toSave = new Category();
        toSave.setCategoryName("Toys");

        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            kh.getKeyList().add(Map.of("GENERATED_KEY", 77));
            return 1;
        }).when(jdbcTemplate).update(any(), any(KeyHolder.class));

        Category saved = categoryDao.save(toSave);
        assertNotNull(saved);
        assertEquals(77, saved.getCategoryId());
        assertEquals("Toys", saved.getCategoryName());
        verify(jdbcTemplate, times(1)).update(any(), any(KeyHolder.class));
    }

    // ---------- findById ----------
    @Test
    void testFindById_Found() {
        when(jdbcTemplate.query(anyString(), any(CategoryRowMapper.class), eq(1)))
                .thenReturn(Arrays.asList(cat1));

        var opt = categoryDao.findById(1);
        assertTrue(opt.isPresent());
        assertEquals("Electronics", opt.get().getCategoryName());
        verify(jdbcTemplate, times(1)).query(anyString(), any(CategoryRowMapper.class), eq(1));
    }

    @Test
    void testFindById_NotFound() {
        when(jdbcTemplate.query(anyString(), any(CategoryRowMapper.class), eq(99)))
                .thenReturn(Arrays.asList());

        var opt = categoryDao.findById(99);
        assertFalse(opt.isPresent());
    }

    // ---------- findAll ----------
    @Test
    void testFindAll() {
        when(jdbcTemplate.query(anyString(), any(CategoryRowMapper.class)))
                .thenReturn(Arrays.asList(cat1, cat2));

        List<Category> list = categoryDao.findAll();
        assertEquals(2, list.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(CategoryRowMapper.class));
    }

    // ---------- update ----------
    @Test
    void testUpdate_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(cat1.getCategoryName()), eq(cat1.getCategoryId())))
                .thenReturn(1);

        int rows = categoryDao.update(cat1);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(cat1.getCategoryName()), eq(cat1.getCategoryId()));
    }

    // ---------- delete ----------
    @Test
    void testDelete_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(2))).thenReturn(1);
        int rows = categoryDao.delete(2);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(2));
    }

    // ---------- findByName ----------
    @Test
    void testFindByName_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(CategoryRowMapper.class), eq("Books")))
                .thenReturn(Arrays.asList(cat2));

        List<Category> result = categoryDao.findByName("Books");
        assertEquals(1, result.size());
        assertEquals("Books", result.get(0).getCategoryName());
        verify(jdbcTemplate, times(1)).query(anyString(), any(CategoryRowMapper.class), eq("Books"));
    }

    // ---------- searchByName ----------
    @Test
    void testSearchByName_ReturnsMatches() {
        when(jdbcTemplate.query(anyString(), any(CategoryRowMapper.class), anyString()))
                .thenReturn(Arrays.asList(cat1));

        List<Category> result = categoryDao.searchByName("Elect");
        assertEquals(1, result.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(CategoryRowMapper.class), anyString());
    }

    // ---------- existsByName ----------
    @Test
    void testExistsByName_TrueAndFalse() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("Electronics"))).thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("NonExistent"))).thenReturn(0);

        assertTrue(categoryDao.existsByName("Electronics"));
        assertFalse(categoryDao.existsByName("NonExistent"));

        verify(jdbcTemplate, times(2)).queryForObject(anyString(), eq(Integer.class), anyString());
    }

    // parameterized example
    @ParameterizedTest
    @ValueSource(strings = {"Electronics", "Books", "Toys"})
    void parameterizedCategoryNames(String name) {
        when(jdbcTemplate.query(anyString(), any(CategoryRowMapper.class), eq(name)))
                .thenReturn(Arrays.asList(new Category(5, name)));
        List<Category> res = categoryDao.findByName(name);
        assertFalse(res.isEmpty());
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled and skipped");
    }
}