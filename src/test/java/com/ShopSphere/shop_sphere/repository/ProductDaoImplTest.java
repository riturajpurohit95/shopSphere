package com.ShopSphere.shop_sphere.repository;

//import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import com.ShopSphere.shop_sphere.exception.OutOfStockException;
import com.ShopSphere.shop_sphere.model.Product;

public class ProductDaoImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ProductDaoImpl productDao;

    private Product p1;
    private Product p2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        p1 = new Product();
        p1.setProductId(1);
        p1.setUserId(10);
        p1.setCategoryId(100);
        p1.setProductName("Alpha Gadget");
        p1.setProductPrice(new BigDecimal("199.99"));
        p1.setProductMrp(new BigDecimal("249.99"));
        p1.setProductQuantity(20);
        p1.setProductAvgRating(new BigDecimal("4.5"));
        p1.setProductReviewsCount(10);
        p1.setBrand("BrandA");
        p1.setProductDescription("Nice gadget");
        p1.setImageUrl("img1.png");

        p2 = new Product();
        p2.setProductId(2);
        p2.setUserId(11);
        p2.setCategoryId(101);
        p2.setProductName("Beta Widget");
        p2.setProductPrice(new BigDecimal("99.99"));
        p2.setProductMrp(new BigDecimal("129.99"));
        p2.setProductQuantity(5);
        p2.setProductAvgRating(new BigDecimal("3.9"));
        p2.setProductReviewsCount(3);
        p2.setBrand("BrandB");
        p2.setProductDescription("Useful widget");
        p2.setImageUrl("img2.png");
    }

    // ---------- save ----------
    @Test
    void testSave_SetsGeneratedId() {
        Product toSave = new Product();
        toSave.setUserId(10);
        toSave.setCategoryId(100);
        toSave.setProductName("New Prod");
        toSave.setProductPrice(new BigDecimal("49.99"));
        toSave.setProductMrp(new BigDecimal("59.99"));
        toSave.setProductQuantity(10);
        toSave.setProductAvgRating(new BigDecimal("0.0"));
        toSave.setProductReviewsCount(0);
        toSave.setBrand("BrandX");
        toSave.setProductDescription("desc");
        toSave.setImageUrl("");

        doAnswer(invocation -> {
            KeyHolder kh = invocation.getArgument(1);
            kh.getKeyList().add(Map.of("GENERATED_KEY", 333));
            return 1;
        }).when(jdbcTemplate).update(any(), any(KeyHolder.class));

        int id = productDao.save(toSave);
        assertEquals(333, id);
        assertEquals(333, toSave.getProductId());
        verify(jdbcTemplate, times(1)).update(any(), any(KeyHolder.class));
    }

    // ---------- findById ----------
    @Test
    void testFindById_Found() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), eq(1)))
                .thenReturn(Arrays.asList(p1));

        Optional<Product> opt = productDao.findById(1);
        assertTrue(opt.isPresent());
        assertEquals("Alpha Gadget", opt.get().getProductName());
        verify(jdbcTemplate, times(1)).query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), eq(1));
    }

    @Test
    void testFindById_NotFound() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), eq(999)))
                .thenReturn(Arrays.asList());
        Optional<Product> opt = productDao.findById(999);
        assertFalse(opt.isPresent());
    }

    // ---------- findAll ----------
    @Test
    void testFindAll_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class)))
                .thenReturn(Arrays.asList(p1, p2));
        List<Product> all = productDao.findAll();
        assertEquals(2, all.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class));
    }

    // ---------- findByCategory ----------
    @Test
    void testFindByCategory_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), eq(100)))
                .thenReturn(Arrays.asList(p1));
        List<Product> res = productDao.findByCategory(100);
        assertEquals(1, res.size());
        assertEquals(100, res.get(0).getCategoryId().intValue());
    }

    // ---------- findBySeller ----------
    @Test
    void testFindBySeller_ReturnsList() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), eq(10)))
                .thenReturn(Arrays.asList(p1));
        List<Product> res = productDao.findBySeller(10);
        assertEquals(1, res.size());
        assertEquals(10, res.get(0).getUserId());
    }

    // ---------- searchByName (short query <3) ----------
    @Test
    void testSearchByName_ShortPrefix() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), anyString()))
                .thenReturn(Arrays.asList(p1));
        List<Product> res = productDao.searchByName("Al");
        assertEquals(1, res.size());
        verify(jdbcTemplate, times(1)).query(contains("product_name"), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), anyString());
    }

    // ---------- searchByName (long query >=3) ----------
    @Test
    void testSearchByName_LongQuery() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList(p1, p2));

        List<Product> res = productDao.searchByName("gad");
        assertEquals(2, res.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testSearchByName_NullOrEmpty_ReturnsAll() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class)))
                .thenReturn(Arrays.asList(p1, p2));
        List<Product> res1 = productDao.searchByName(null);
        List<Product> res2 = productDao.searchByName(" ");
        assertEquals(2, res1.size());
        assertEquals(2, res2.size());
    }

    // ---------- update ----------
    @Test
    void testUpdate_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(),
                anyInt(), anyInt(), anyString(), any(BigDecimal.class),
                any(BigDecimal.class), anyInt(), any(BigDecimal.class), anyInt(),
                anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(1);

        int rows = productDao.update(p1);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), anyInt(), anyInt(), anyString(), any(BigDecimal.class),
                any(BigDecimal.class), anyInt(), any(BigDecimal.class), anyInt(),
                anyString(), anyString(), anyString(), anyInt());
    }

    // ---------- deleteById ----------
    @Test
    void testDeleteById_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(2))).thenReturn(1);
        int rows = productDao.deleteById(2);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(2));
    }

    // ---------- searchByBrand ----------
    @Test
    void testSearchByBrand_ReturnsMatches() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), anyString()))
                .thenReturn(Arrays.asList(p2));
        List<Product> res = productDao.searchByBrand("brandb");
        assertEquals(1, res.size());
        verify(jdbcTemplate, times(1)).query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), anyString());
    }

    @Test
    void testSearchByBrand_NullOrEmpty_ReturnsAll() {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class)))
                .thenReturn(Arrays.asList(p1, p2));
        List<Product> res = productDao.searchByBrand(null);
        assertEquals(2, res.size());
    }

    // ---------- getSellerIdByProductId ----------
    @Test
    void testGetSellerIdByProductId_ReturnsId() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(1))).thenReturn(10);
        int sellerId = productDao.getSellerIdByProductId(1);
        assertEquals(10, sellerId);
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq(1));
    }

    // ---------- decreaseStockIfAvailable (success) ----------
    @Test
    void testDecreaseStockIfAvailable_Success() {
        when(jdbcTemplate.update(anyString(), eq(3), eq(1), eq(3))).thenReturn(1);
        int rows = productDao.decreaseStockIfAvailable(1, 3);
        assertEquals(1, rows);
    }

    // ---------- decreaseStockIfAvailable (failure throws) ----------
    @Test
    void testDecreaseStockIfAvailable_OutOfStock_Throws() {
        when(jdbcTemplate.update(anyString(), eq(100), eq(1), eq(100))).thenReturn(0);
        assertThrows(OutOfStockException.class, () -> productDao.decreaseStockIfAvailable(1, 100));
    }

    // ---------- increaseStock ----------
    @Test
    void testIncreaseStock_ReturnsRowsAffected() {
        when(jdbcTemplate.update(anyString(), eq(5), eq(1))).thenReturn(1);
        int rows = productDao.increaseStock(1, 5);
        assertEquals(1, rows);
        verify(jdbcTemplate, times(1)).update(anyString(), eq(5), eq(1));
    }

    // ---------- getProductsByCategory ----------
    @Test
    void testGetProductsByCategory_ReturnsListOfMaps() {
        Map<String, Object> row = Map.of("product_id", 1, "product_name", "Alpha", "product_price", new BigDecimal("199.99"),
                "product_mrp", new BigDecimal("249.99"), "category_name", "Electronics");
        when(jdbcTemplate.queryForList(anyString(), eq(100))).thenReturn(List.of(row));
        List<Map<String, Object>> res = productDao.getProductsByCategory(100);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(1, res.get(0).get("product_id"));
    }

    // ---------- getSellerProducts ----------
    @Test
    void testGetSellerProducts_ReturnsListOfMaps() {
        Map<String, Object> row = Map.of("product_id", 2, "product_name", "Beta", "product_price", new BigDecimal("99.99"),
                "product_quantity", 5, "seller_name", "SellerX");
        when(jdbcTemplate.queryForList(anyString(), eq(11))).thenReturn(List.of(row));
        List<Map<String, Object>> res = productDao.getSellerProducts(11);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(2, res.get(0).get("product_id"));
    }

    // parameterized example
    @ParameterizedTest
    @ValueSource(strings = {"Alpha Gadget", "Beta Widget"})
    void parameterizedProductNames(String name) {
        when(jdbcTemplate.query(anyString(), any(com.ShopSphere.shop_sphere.util.ProductRowMapper.class), any()))
                .thenReturn(Arrays.asList(new Product(5, 10, 100, name, new BigDecimal("10.0"),
                        new BigDecimal("12.0"), 1, new BigDecimal("0.0"), 0, "B", "d", "i")));
        List<Product> res = productDao.searchByName(name.length() < 3 ? name : name);
        assertFalse(res.isEmpty());
    }

    @Disabled("Example disabled DAO test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled and skipped");
    }
}