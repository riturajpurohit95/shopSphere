package com.ShopSphere.shop_sphere.repository;

import java.util.List;
import java.util.Optional;

import com.ShopSphere.shop_sphere.model.Category;

public interface CategoryDao {
	
	Category save(Category category);
	Optional<Category> findById(int categoryId);
	List<Category> findAll();
	int update(Category category);
	int delete(int categoryId);
	
	List<Category> findByName(String name);
	List<Category> searchByName(String keyword);
	boolean existsByName(String name);
		

}