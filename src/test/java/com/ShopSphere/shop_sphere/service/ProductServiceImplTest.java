package com.ShopSphere.shop_sphere.service;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.repository.ProductDao;
import com.ShopSphere.shop_sphere.repository.UserDao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private User seller;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId(1);
        product.setUserId(10);
        product.setCategoryId(5);
        product.setProductName("Test Product");
        product.setProductPrice(new BigDecimal("100.00"));
        product.setProductMrp(new BigDecimal("120.00"));
        product.setProductQuantity(5);
        product.setProductAvgRating(new BigDecimal("4.5"));
        product.setProductReviewsCount(10);
        product.setBrand("TestBrand");
        product.setProductDescription("Nice product");
        product.setImageUrl("test.jpg");

        seller = new User();
        seller.setUserId(10);
        seller.setRole("SELLER");
        seller.setName("Test Seller");
    }

    // ----------------------------------------------------
    // createProduct()
    // ----------------------------------------------------

    @Test
    void createProduct_shouldThrow_whenProductNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(null)
        );
    }

    @Test
    void createProduct_shouldThrow_whenUserNotFound() {
        when(userDao.findById(10)).thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.createProduct(product)
        );

        verify(userDao).findById(10);
        verify(productDao, never()).save(any());
    }

    @Test
    void createProduct_shouldThrow_whenUserNotSeller() {
        User normalUser = new User();
        normalUser.setUserId(10);
        normalUser.setRole("CUSTOMER");

        when(userDao.findById(10)).thenReturn(normalUser);

        assertThrows(
                RuntimeException.class,
                () -> productService.createProduct(product)
        );

        verify(userDao).findById(10);
        verify(productDao, never()).save(any());
    }

    @Test
    void createProduct_shouldSetSafeDefaults_whenNullFields() {
        // product with some nulls
        Product p = new Product();
        p.setUserId(10);
        p.setCategoryId(5);
        p.setProductName("Null Product");
        p.setProductPrice(new BigDecimal("50.00"));
        p.setProductMrp(new BigDecimal("60.00"));
        // quantity, avgRating, reviewsCount, imageUrl left null

        when(userDao.findById(10)).thenReturn(seller);
        when(productDao.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setProductId(123);
            return 1;
        });

        Product result = productService.createProduct(p);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productDao).save(captor.capture());

        Product savedArg = captor.getValue();

        assertNotNull(savedArg.getProductQuantity());
        assertNotNull(savedArg.getProductAvgRating());
        assertNotNull(savedArg.getProductReviewsCount());
        assertNotNull(savedArg.getImageUrl());

        assertEquals(123, result.getProductId());
    }

    @Test
    void createProduct_daoSaveFailure_shouldThrow() {
        when(userDao.findById(10)).thenReturn(seller);
        when(productDao.save(any(Product.class))).thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> productService.createProduct(product)
        );
    }

    @Test
    void createProduct_success() {
        when(userDao.findById(10)).thenReturn(seller);
        when(productDao.save(any(Product.class))).thenReturn(1);

        Product result = productService.createProduct(product);

        assertEquals(product, result);
        verify(productDao).save(product);
    }

    // ----------------------------------------------------
    // getProductById()
    // ----------------------------------------------------

    @Test
    void getProductById_success() {
        when(productDao.findById(1)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1);

        assertSame(product, result);
        verify(productDao).findById(1);
    }

    @Test
    void getProductById_notFound_shouldThrow() {
        when(productDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(1)
        );
    }

    // ----------------------------------------------------
    // getAllProducts()
    // ----------------------------------------------------

    @Test
    void getAllProducts_shouldReturnList() {
        List<Product> list = Arrays.asList(product);
        when(productDao.findAll()).thenReturn(list);

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertSame(product, result.get(0));
        verify(productDao).findAll();
    }

    // ----------------------------------------------------
    // getProductsByCategory()
    // ----------------------------------------------------

    @Test
    void getProductsByCategory_shouldDelegateToDao() {
        List<Product> list = Arrays.asList(product);
        when(productDao.findByCategory(5)).thenReturn(list);

        List<Product> result = productService.getProductsByCategory(5);

        assertEquals(1, result.size());
        verify(productDao).findByCategory(5);
    }

    // ----------------------------------------------------
    // getProductsBySeller()
    // ----------------------------------------------------

    @Test
    void getProductsBySeller_shouldDelegateToDao() {
        List<Product> list = Arrays.asList(product);
        when(productDao.findBySeller(10)).thenReturn(list);

        List<Product> result = productService.getProductsBySeller(10);

        assertEquals(1, result.size());
        verify(productDao).findBySeller(10);
    }

    // ----------------------------------------------------
    // searchProductsByName / searchProductsByBrand
    // ----------------------------------------------------

    @Test
    void searchProductsByName_shouldDelegateToDao() {
        List<Product> list = Arrays.asList(product);
        when(productDao.searchByName("Test")).thenReturn(list);

        List<Product> result = productService.searchProductsByName("Test");

        assertEquals(1, result.size());
        verify(productDao).searchByName("Test");
    }

    @Test
    void searchProductsByBrand_shouldDelegateToDao() {
        List<Product> list = Arrays.asList(product);
        when(productDao.searchByBrand("Brand")).thenReturn(list);

        List<Product> result = productService.searchProductsByBrand("Brand");

        assertEquals(1, result.size());
        verify(productDao).searchByBrand("Brand");
    }

    // ----------------------------------------------------
    // updateProduct()
    // ----------------------------------------------------

    @Test
    void updateProduct_shouldThrow_whenProductNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(null)
        );
    }

    @Test
    void updateProduct_shouldThrow_whenProductNotFound() {
        when(productDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(product)
        );
    }

    @Test
    void updateProduct_daoUpdateFailure_shouldThrow() {
        when(productDao.findById(1)).thenReturn(Optional.of(product));
        when(productDao.update(product)).thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> productService.updateProduct(product)
        );
    }

    @Test
    void updateProduct_success() {
        when(productDao.findById(1)).thenReturn(Optional.of(product));
        when(productDao.update(product)).thenReturn(1);

        Product result = productService.updateProduct(product);

        assertSame(product, result);
        verify(productDao).update(product);
    }

    // ----------------------------------------------------
    // fetchProductsByCategory / fetchSellerProducts
    // ----------------------------------------------------

    @Test
    void fetchProductsByCategory_shouldReturnMapList() {
        List<Map<String, Object>> rows = new ArrayList<>();
        when(productDao.getProductsByCategory(5)).thenReturn(rows);

        List<Map<String, Object>> result = productService.fetchProductsByCategory(5);

        assertSame(rows, result);
        verify(productDao).getProductsByCategory(5);
    }

    @Test
    void fetchSellerProducts_shouldReturnMapList() {
        List<Map<String, Object>> rows = new ArrayList<>();
        when(productDao.getSellerProducts(10)).thenReturn(rows);

        List<Map<String, Object>> result = productService.fetchSellerProducts(10);

        assertSame(rows, result);
        verify(productDao).getSellerProducts(10);
    }

    // ----------------------------------------------------
    // deleteProduct()
    // ----------------------------------------------------

    @Test
    void deleteProduct_shouldThrow_whenProductNotFound() {
        when(productDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProduct(1)
        );

        verify(productDao, never()).deleteById(anyInt());
    }

    @Test
    void deleteProduct_daoDeleteFailure_shouldThrow() {
        when(productDao.findById(1)).thenReturn(Optional.of(product));
        when(productDao.deleteById(1)).thenReturn(0);

        assertThrows(
                RuntimeException.class,
                () -> productService.deleteProduct(1)
        );
    }

    @Test
    void deleteProduct_success() {
        when(productDao.findById(1)).thenReturn(Optional.of(product));
        when(productDao.deleteById(1)).thenReturn(1);

        productService.deleteProduct(1);

        verify(productDao).deleteById(1);
    }
}