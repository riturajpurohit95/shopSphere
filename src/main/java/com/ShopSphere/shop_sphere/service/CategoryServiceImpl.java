package com.ShopSphere.shop_sphere.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Category;
import com.ShopSphere.shop_sphere.repository.CategoryDao;

@Service
public class CategoryServiceImpl implements CategoryService{
	
	private final CategoryDao categoryDao;
	
	public CategoryServiceImpl(CategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}

	@Override
	public Category createCategory(Category category) {
		if(categoryDao.existsByName(category.getCategoryName())) {
			throw new RuntimeException("Category already exists with name:"+category.getCategoryName());
		}
		return categoryDao.save(category);
	}

	@Override
	public Category getCategoryById(int categoryId) {
		return categoryDao.findById(categoryId)
					.orElseThrow(()-> new ResourceNotFoundException("Category not found with id: "+categoryId));
	}

	@Override
	public List<Category> getAllCategories() {
		return categoryDao.findAll();
	}

	@Override
	public List<Category> getCategoryByName(String name) {
		return categoryDao.findByName(name);
	}

	@Override
	public List<Category> searchCategoryByKeyword(String keyword) {
		return categoryDao.searchByName(keyword);
	}

	@Override
	public Category updateCategory(int categoryId, Category category) {
		Category existing = getCategoryById(categoryId);
		existing.setCategoryName(category.getCategoryName());
		int rows = categoryDao.update(existing);
		if(rows<=0) {
			throw new RuntimeException("Update failed for category id: "+categoryId);
		}
		return existing;
	}

	@Override
	public void deleteCategory(int categoryId) {
		getCategoryById(categoryId);
		int rows = categoryDao.delete(categoryId);
		if(rows<=0) {
			throw new RuntimeException("Delete failed for categoryId: "+categoryId);
		}
	}
	
	

}
