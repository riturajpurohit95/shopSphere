package com.ShopSphere.shop_sphere.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ShopSphere.shop_sphere.model.Product;

public interface ProductDao {
	
	int save(Product product);
	Optional<Product> findById(int productId);
	List<Product> findAll();
	List<Product> findByCategory(int categoryId);
	List<Product> findBySeller(int userId);
	List<Product> searchByName(String name);
	int update(Product product);
	int deleteById(int productId);
    List<Product> searchByBrand(String Brand);
	int getSellerIdByProductId(int productId);
	
	int decreaseStockIfAvailable(int productId, int quantity);
	int increaseStock(int productId, int quantity);
	List<Map<String, Object>> getProductsByCategory(int categoryId);
	List<Map<String, Object>> getSellerProducts(int sellerId);
}
