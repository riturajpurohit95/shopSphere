package com.ShopSphere.shop_sphere.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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
import com.ShopSphere.shop_sphere.dto.ProductDto;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.service.ProductService;

public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

        sampleProduct = new Product();
        sampleProduct.setProductId(1001);
        sampleProduct.setUserId(10);
        sampleProduct.setCategoryId(5);
        sampleProduct.setProductName("Cool Gadget");
        sampleProduct.setProductPrice(new BigDecimal("999.99"));
        sampleProduct.setProductMrp(new BigDecimal("1299.99"));
        sampleProduct.setProductQuantity(50);
        sampleProduct.setProductAvgRating(new BigDecimal("4.5"));
        sampleProduct.setProductReviewsCount(120);
        sampleProduct.setBrand("Acme");
        sampleProduct.setProductDescription("A very cool gadget");
        sampleProduct.setImageUrl("img.png");
    }

    @Test
    void testCreateProduct_ReturnsCreatedWithLocation() throws Exception {
        ProductDto req = new ProductDto();
        req.setUserId(10);
        req.setCategoryId(5);
        req.setProductName("Cool Gadget");
        req.setProductPrice(new BigDecimal("999.99"));

        when(productService.createProduct(any(Product.class))).thenReturn(sampleProduct);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
               .andExpect(status().isCreated())
               .andExpect(header().string("Location", "/api/products/1001"))
               // productName and brand are stable mapped fields — assert them in the body
               .andExpect(content().string(org.hamcrest.Matchers.containsString("Cool Gadget")))
               .andExpect(content().string(org.hamcrest.Matchers.containsString("Acme")));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void testGetProductById_ReturnsDto() throws Exception {
        when(productService.getProductById(1001)).thenReturn(sampleProduct);

        mockMvc.perform(get("/api/products/1001"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.productName").value("Cool Gadget"))
               .andExpect(jsonPath("$.brand").value("Acme"));

        verify(productService, times(1)).getProductById(1001);
    }

    @Test
    void testGetAllProducts_ReturnsList() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(sampleProduct));

        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].productName").value("Cool Gadget"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void testGetProductByCategoryId_ReturnsList() throws Exception {
        when(productService.getProductsByCategory(5)).thenReturn(List.of(sampleProduct));

        mockMvc.perform(get("/api/products/category/5"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].categoryId").value(5));

        verify(productService, times(1)).getProductsByCategory(5);
    }

    @Test
    void testGetProductBySeller_ReturnsList() throws Exception {
        when(productService.getProductsBySeller(10)).thenReturn(List.of(sampleProduct));

        mockMvc.perform(get("/api/products/seller/10"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].userId").value(10));

        verify(productService, times(1)).getProductsBySeller(10);
    }

    @Test
    void testSearchProductsByName_WithQueryParam() throws Exception {
        when(productService.searchProductsByName("gadget")).thenReturn(List.of(sampleProduct));

        mockMvc.perform(get("/api/products/search").param("name", "gadget"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1));

        verify(productService, times(1)).searchProductsByName("gadget");
    }

    @Test
    void testSearchProductsByBrand_WithParam() throws Exception {
        when(productService.searchProductsByBrand("Acme")).thenReturn(List.of(sampleProduct));

        mockMvc.perform(get("/api/products/search/brand").param("Brand", "Acme"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].brand").value("Acme"));

        verify(productService, times(1)).searchProductsByBrand("Acme");
    }

    @Test
    void testUpdateProduct_ReturnsUpdated() throws Exception {
        Product updated = new Product();
        updated.setProductId(1001);
        updated.setProductName("Cool Gadget V2");
        updated.setUserId(10);
        updated.setProductPrice(new BigDecimal("1099.99"));

        ProductDto dto = new ProductDto();
        dto.setProductName("Cool Gadget V2");
        dto.setUserId(10);
        dto.setProductPrice(new BigDecimal("1099.99"));

        when(productService.updateProduct(any(Product.class))).thenReturn(updated);

        mockMvc.perform(put("/api/products/1001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.productName").value("Cool Gadget V2"))
               .andExpect(jsonPath("$.productPrice").value(1099.99));

        verify(productService, times(1)).updateProduct(any(Product.class));
    }

    @Test
    void testDeleteProduct_NoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1001);

        mockMvc.perform(delete("/api/products/1001"))
               .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1001);
    }

    @Test
    void testGetCategoryProducts_ReturnsListOfMaps() throws Exception {
        Map<String, Object> row = Map.of("product_id", 1001, "product_name", "Cool Gadget");
        when(productService.fetchProductsByCategory(5)).thenReturn(List.of(row));

        mockMvc.perform(get("/api/products/categories/5/products"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].product_id").value(1001));

        verify(productService, times(1)).fetchProductsByCategory(5);
    }

    @Test
    void testGetSellerProducts_ReturnsListOfMaps() throws Exception {
        Map<String, Object> row = Map.of("product_id", 1001, "product_name", "Cool Gadget");
        when(productService.fetchSellerProducts(10)).thenReturn(List.of(row));

        mockMvc.perform(get("/api/products/seller/10/products"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1));

        verify(productService, times(1)).fetchSellerProducts(10);
    }

    @ParameterizedTest
    @ValueSource(ints = {1001, 2002})
    void parameterizedProductIds(int id) throws Exception {
        when(productService.getProductById(id)).thenReturn(sampleProduct);
        mockMvc.perform(get("/api/products/" + id)).andExpect(status().isOk());
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        org.junit.jupiter.api.Assertions.fail("This test is disabled");
    }
}