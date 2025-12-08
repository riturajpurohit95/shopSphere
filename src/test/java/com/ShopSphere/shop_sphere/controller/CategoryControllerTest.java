package com.ShopSphere.shop_sphere.controller;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

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
import com.ShopSphere.shop_sphere.dto.CategoryDto;
import com.ShopSphere.shop_sphere.model.Category;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.CategoryService;

public class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Category sampleCategory;
    private CategoryDto sampleDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();

        sampleCategory = new Category();
        sampleCategory.setCategoryId(11);
        sampleCategory.setCategoryName("Electronics");

        sampleDto = new CategoryDto();
        sampleDto.setCategoryName("Electronics");
    }

    // ---------- createCategory (ADMIN) ----------
    @Test
    void testCreateCategory_AsAdmin_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);

            when(categoryService.createCategory(any(Category.class))).thenReturn(sampleCategory);

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.categoryId").value(11))
                    .andExpect(jsonPath("$.categoryName").value("Electronics"));

            verify(categoryService, times(1)).createCategory(any(Category.class));
        }
    }

    @Test
    void testCreateCategory_NotAdmin_ThrowsForbidden() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleDto)))
                    .andExpect(status().isInternalServerError()); // ForbiddenException -> 500 without advice

            verify(categoryService, never()).createCategory(any());
        }
    }

    @Test
    void testCreateCategory_InvalidName_ThrowsValidation() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);

            CategoryDto bad = new CategoryDto();
            bad.setCategoryName(" "); // invalid

            mockMvc.perform(post("/api/categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bad)))
                    .andExpect(status().isInternalServerError()); // ValidationException -> 500 without advice

            verify(categoryService, never()).createCategory(any());
        }
    }

    // ---------- getCategoryById ----------
    @Test
    void testGetCategoryById_Succeeds() throws Exception {
        when(categoryService.getCategoryById(11)).thenReturn(sampleCategory);

        mockMvc.perform(get("/api/categories/11"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.categoryId").value(11))
               .andExpect(jsonPath("$.categoryName").value("Electronics"));

        verify(categoryService, times(1)).getCategoryById(11);
    }

    // ---------- getAllCategories ----------
    @Test
    void testGetAllCategories_Succeeds() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(sampleCategory));

        mockMvc.perform(get("/api/categories"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].categoryName").value("Electronics"));

        verify(categoryService, times(1)).getAllCategories();
    }

    // ---------- getCategoryByName ----------
    @Test
    void testGetCategoryByName_Succeeds() throws Exception {
        when(categoryService.getCategoryByName("Electronics")).thenReturn(List.of(sampleCategory));

        mockMvc.perform(get("/api/categories/name/Electronics"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1))
               .andExpect(jsonPath("$[0].categoryName").value("Electronics"));

        verify(categoryService, times(1)).getCategoryByName("Electronics");
    }

    @Test
    void testGetCategoryByName_InvalidName_ThrowsValidation() throws Exception {
        mockMvc.perform(get("/api/categories/name/ "))
               .andExpect(status().isInternalServerError()); // ValidationException -> 500
        verify(categoryService, never()).getCategoryByName(any());
    }

    // ---------- searchCategoryByKeyword ----------
    @Test
    void testSearchCategoryByKeyword_Succeeds() throws Exception {
        when(categoryService.searchCategoryByKeyword("tron")).thenReturn(List.of(sampleCategory));

        mockMvc.perform(get("/api/categories/search/tron"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.size()").value(1));
        verify(categoryService, times(1)).searchCategoryByKeyword("tron");
    }

    @Test
    void testSearchCategoryByKeyword_Invalid_ThrowsValidation() throws Exception {
        mockMvc.perform(get("/api/categories/search/ "))
               .andExpect(status().isInternalServerError());
        verify(categoryService, never()).searchCategoryByKeyword(any());
    }

    // ---------- updateCategory (ADMIN) ----------
    @Test
    void testUpdateCategory_AsAdmin_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);

            Category updated = new Category();
            updated.setCategoryId(11);
            updated.setCategoryName("Electronics & Gadgets");

            when(categoryService.updateCategory(eq(11), any(Category.class))).thenReturn(updated);

            CategoryDto dto = new CategoryDto();
            dto.setCategoryName("Electronics & Gadgets");

            mockMvc.perform(put("/api/categories/11")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.categoryName").value("Electronics & Gadgets"));

            verify(categoryService, times(1)).updateCategory(eq(11), any(Category.class));
        }
    }

    @Test
    void testUpdateCategory_NotAdmin_ThrowsForbidden() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            CategoryDto dto = new CategoryDto();
            dto.setCategoryName("New");

            mockMvc.perform(put("/api/categories/11")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                   .andExpect(status().isInternalServerError());

            verify(categoryService, never()).updateCategory(anyInt(), any());
        }
    }

    @Test
    void testUpdateCategory_InvalidName_ThrowsValidation() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);

            CategoryDto bad = new CategoryDto();
            bad.setCategoryName(" ");

            mockMvc.perform(put("/api/categories/11")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bad)))
                   .andExpect(status().isInternalServerError());

            verify(categoryService, never()).updateCategory(anyInt(), any());
        }
    }

    // ---------- deleteCategory (ADMIN) ----------
    @Test
    void testDeleteCategory_AsAdmin_Succeeds() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(true);
            doNothing().when(categoryService).deleteCategory(11);

            mockMvc.perform(delete("/api/categories/11"))
                   .andExpect(status().isOk())
                   .andExpect(content().string(org.hamcrest.Matchers.containsString("Category deleted successfully")));

            verify(categoryService, times(1)).deleteCategory(11);
        }
    }

    @Test
    void testDeleteCategory_NotAdmin_ThrowsForbidden() throws Exception {
        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(() -> SecurityUtil.isAdmin(any(HttpServletRequest.class))).thenReturn(false);

            mockMvc.perform(delete("/api/categories/11"))
                   .andExpect(status().isInternalServerError());

            verify(categoryService, never()).deleteCategory(anyInt());
        }
    }

    // Parameterized sanity check
    @ParameterizedTest
    @ValueSource(ints = {1, 11, 42})
    void parameterizedCategoryIds(int id) throws Exception {
        when(categoryService.getCategoryById(id)).thenReturn(sampleCategory);
        mockMvc.perform(get("/api/categories/" + id)).andExpect(status().isOk());
    }

    @Disabled("Example disabled controller test")
    @Test
    void disabledTestExample() {
        fail("This test is disabled");
    }
}