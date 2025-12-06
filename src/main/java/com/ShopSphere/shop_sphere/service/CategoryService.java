package com.ShopSphere.shop_sphere.service;

import java.util.List;

import com.ShopSphere.shop_sphere.model.Category;

public interface CategoryService {
	
	Category createCategory(Category category);
	Category getCategoryById(int categoryId);
	List<Category> getAllCategories();
	List<Category> getCategoryByName(String name);
	List<Category> searchCategoryByKeyword(String keyword);
	Category updateCategory(int categoryId, Category category);
	void deleteCategory(int categoryId);

}
