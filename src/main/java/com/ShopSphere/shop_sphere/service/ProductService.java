package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.Product;

public interface ProductService {

	Product createProduct(Product product);
	Product getProductById(int productId);
	List<Product> getAllProducts();
	List<Product> getProductsByCategory(int categoryId);
	List<Product> getProductsBySeller(int userId);
	List<Product> searchProductsByName(String name);
	List<Product> searchProductsByBrand(String Brand);
	Product updateProduct(Product product);
	void deleteProduct(int ProductId);
	List<Map<String, Object>> fetchProductsByCategory(int id);
	List<Map<String, Object>> fetchSellerProducts(int sellerId);
}
