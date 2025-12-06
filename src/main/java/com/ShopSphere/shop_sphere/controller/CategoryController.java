package com.ShopSphere.shop_sphere.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.ShopSphere.shop_sphere.dto.CategoryDto;
import com.ShopSphere.shop_sphere.model.Category;
import com.ShopSphere.shop_sphere.security.AllowedRoles;
import com.ShopSphere.shop_sphere.security.SecurityUtil;
import com.ShopSphere.shop_sphere.service.CategoryService;
import com.ShopSphere.shop_sphere.exception.ForbiddenException;
import com.ShopSphere.shop_sphere.exception.ValidationException;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ---------------- DTO MAPPING ----------------
    private Category dtoToEntity(CategoryDto dto) {
        Category category = new Category();
        if (dto.getCategoryId() != null) {
            category.setCategoryId(dto.getCategoryId());
        }
        category.setCategoryName(dto.getCategoryName());
        return category;
    }

    private CategoryDto entityToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        return dto;
    }

    // ---------------- CONTROLLER ENDPOINTS ----------------

    @AllowedRoles({"ADMIN"})
    @PostMapping
    public CategoryDto createCategory(@RequestBody CategoryDto dto, HttpServletRequest request) {
        validateAdmin(request);

        if (dto.getCategoryName() == null || dto.getCategoryName().trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty");
        }

        Category saved = categoryService.createCategory(dtoToEntity(dto));
        return entityToDto(saved);
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/{categoryId}")
    public CategoryDto getCategoryById(@PathVariable int categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return entityToDto(category);
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping
    public List<CategoryDto> getAllCategories() {
        return categoryService.getAllCategories()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/name/{name}")
    public List<CategoryDto> getCategoryByName(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Search name cannot be empty");
        }

        return categoryService.getCategoryByName(name)
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @AllowedRoles({"BUYER", "ADMIN","SELLER"})
    @GetMapping("/search/{keyword}")
    public List<CategoryDto> searchCategoryByKeyword(@PathVariable String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException("Keyword cannot be empty");
        }

        return categoryService.searchCategoryByKeyword(keyword)
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @AllowedRoles({"ADMIN"})
    @PutMapping("/{categoryId}")
    public CategoryDto updateCategory(
            @PathVariable int categoryId,
            @RequestBody CategoryDto dto,
            HttpServletRequest request) {

        validateAdmin(request);

        if (dto.getCategoryName() == null || dto.getCategoryName().trim().isEmpty()) {
            throw new ValidationException("Category name cannot be empty");
        }

        Category updated = categoryService.updateCategory(categoryId, dtoToEntity(dto));
        return entityToDto(updated);
    }

    @AllowedRoles({"ADMIN"})
    @DeleteMapping("/{categoryId}")
    public String deleteCategory(@PathVariable int categoryId, HttpServletRequest request) {
        validateAdmin(request);
        categoryService.deleteCategory(categoryId);
        return "Category deleted successfully";
    }

    // ---------------- Security Helper ----------------
    private void validateAdmin(HttpServletRequest request) {
        if (!SecurityUtil.isAdmin(request)) {
        	throw new ForbiddenException("Admin access required");

        }
    }
}
