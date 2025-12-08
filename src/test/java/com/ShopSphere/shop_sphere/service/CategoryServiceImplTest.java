package com.ShopSphere.shop_sphere.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Category;
import com.ShopSphere.shop_sphere.repository.CategoryDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryDao categoryDao;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // ─────────────────────────────────────────
    // createCategory()
    // ─────────────────────────────────────────

    @Test
    void createCategory_shouldThrowRuntimeException_whenNameAlreadyExists() {
        Category category = new Category();
        category.setCategoryName("Electronics");

        when(categoryDao.existsByName("Electronics")).thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> categoryService.createCategory(category)
        );

        assertTrue(ex.getMessage().contains("Category already exists"));
        verify(categoryDao).existsByName("Electronics");
        verify(categoryDao, never()).save(any(Category.class));
    }

    @Test
    void createCategory_shouldSaveAndReturn_whenNameDoesNotExist() {
        Category category = new Category();
        category.setCategoryName("Electronics");

        Category saved = new Category(1, "Electronics");

        when(categoryDao.existsByName("Electronics")).thenReturn(false);
        when(categoryDao.save(category)).thenReturn(saved);

        Category result = categoryService.createCategory(category);

        assertNotNull(result);
        assertEquals(1, result.getCategoryId());
        assertEquals("Electronics", result.getCategoryName());

        verify(categoryDao).existsByName("Electronics");
        verify(categoryDao).save(category);
    }

    // ─────────────────────────────────────────
    // getCategoryById()
    // ─────────────────────────────────────────

    @Test
    void getCategoryById_shouldReturnCategory_whenFound() {
        Category category = new Category(1, "Books");
        when(categoryDao.findById(1)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1);

        assertNotNull(result);
        assertEquals(1, result.getCategoryId());
        assertEquals("Books", result.getCategoryName());
        verify(categoryDao).findById(1);
    }

    @Test
    void getCategoryById_shouldThrowResourceNotFound_whenNotFound() {
        when(categoryDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(1)
        );

        verify(categoryDao).findById(1);
    }

    // ─────────────────────────────────────────
    // getAllCategories()
    // ─────────────────────────────────────────

    @Test
    void getAllCategories_shouldReturnListFromDao() {
        List<Category> list = Arrays.asList(
                new Category(1, "Books"),
                new Category(2, "Clothes")
        );
        when(categoryDao.findAll()).thenReturn(list);

        List<Category> result = categoryService.getAllCategories();

        assertEquals(2, result.size());
        verify(categoryDao).findAll();
    }

    // ─────────────────────────────────────────
    // getCategoryByName()
    // ─────────────────────────────────────────

    @Test
    void getCategoryByName_shouldReturnResultsFromDao() {
        List<Category> list = Collections.singletonList(new Category(1, "Books"));
        when(categoryDao.findByName("Books")).thenReturn(list);

        List<Category> result = categoryService.getCategoryByName("Books");

        assertEquals(1, result.size());
        assertEquals("Books", result.get(0).getCategoryName());
        verify(categoryDao).findByName("Books");
    }

    // ─────────────────────────────────────────
    // searchCategoryByKeyword()
    // ─────────────────────────────────────────

    @Test
    void searchCategoryByKeyword_shouldReturnResultsFromDao() {
        List<Category> list = Arrays.asList(
                new Category(1, "Books"),
                new Category(2, "Book Accessories")
        );
        when(categoryDao.searchByName("Book")).thenReturn(list);

        List<Category> result = categoryService.searchCategoryByKeyword("Book");

        assertEquals(2, result.size());
        verify(categoryDao).searchByName("Book");
    }

    // ─────────────────────────────────────────
    // updateCategory()
    // ─────────────────────────────────────────

    @Test
    void updateCategory_shouldThrowResourceNotFound_whenCategoryMissing() {
        when(categoryDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(1, new Category())
        );

        verify(categoryDao).findById(1);
        verify(categoryDao, never()).update(any(Category.class));
    }

    @Test
    void updateCategory_shouldThrowRuntimeException_whenUpdateReturnsZero() {
        Category existing = new Category(1, "Old");
        Category updateData = new Category();
        updateData.setCategoryName("New");

        when(categoryDao.findById(1)).thenReturn(Optional.of(existing));
        when(categoryDao.update(any(Category.class))).thenReturn(0);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> categoryService.updateCategory(1, updateData)
        );

        assertTrue(ex.getMessage().contains("Update failed"));
        verify(categoryDao).findById(1);
        verify(categoryDao).update(existing);
    }

    @Test
    void updateCategory_shouldUpdateAndReturn_whenSuccessful() {
        Category existing = new Category(1, "Old");
        Category updateData = new Category();
        updateData.setCategoryName("New");

        when(categoryDao.findById(1)).thenReturn(Optional.of(existing));
        when(categoryDao.update(existing)).thenReturn(1);

        Category result = categoryService.updateCategory(1, updateData);

        assertNotNull(result);
        assertEquals(1, result.getCategoryId());
        assertEquals("New", result.getCategoryName());

        verify(categoryDao).findById(1);
        verify(categoryDao).update(existing);
    }

    // ─────────────────────────────────────────
    // deleteCategory()
    // ─────────────────────────────────────────

    @Test
    void deleteCategory_shouldThrowResourceNotFound_whenCategoryMissing() {
        when(categoryDao.findById(1)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(1)
        );

        verify(categoryDao).findById(1);
        verify(categoryDao, never()).delete(anyInt());
    }

    @Test
    void deleteCategory_shouldThrowRuntimeException_whenDeleteReturnsZero() {
        Category existing = new Category(1, "Books");
        when(categoryDao.findById(1)).thenReturn(Optional.of(existing));
        when(categoryDao.delete(1)).thenReturn(0);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> categoryService.deleteCategory(1)
        );

        assertTrue(ex.getMessage().contains("Delete failed"));
        verify(categoryDao).findById(1);
        verify(categoryDao).delete(1);
    }

    @Test
    void deleteCategory_shouldSucceed_whenRowsDeleted() {
        Category existing = new Category(1, "Books");
        when(categoryDao.findById(1)).thenReturn(Optional.of(existing));
        when(categoryDao.delete(1)).thenReturn(1);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1));

        verify(categoryDao).findById(1);
        verify(categoryDao).delete(1);
    }
}
