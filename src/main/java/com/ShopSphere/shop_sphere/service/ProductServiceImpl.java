package com.ShopSphere.shop_sphere.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ShopSphere.shop_sphere.exception.ResourceNotFoundException;
import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.repository.OrderDao;
import com.ShopSphere.shop_sphere.repository.ProductDao;
import com.ShopSphere.shop_sphere.repository.UserDao;

@Service
public class ProductServiceImpl implements ProductService {
	

	

    public ProductServiceImpl(ProductDao productDao, UserDao userDao) {
		super();
		this.productDao = productDao;
		this.userDao = userDao;
	}


	private  ProductDao productDao;
	private   UserDao  userDao;
	
	
	/*@Override
	public Product createProduct(Product product) {
		if(product == null) {
			throw new IllegalArgumentException("Product must not be null");
		}
		User user = userDao.findById(product.getUserId());
		if(user == null) {
			throw new ResourceNotFoundException("User not found with id: "+ product.getUserId());
		}
		
		if(!"SELLER".equalsIgnoreCase(user.getRole())) {
			throw new RuntimeException("Only SELLER users can be assigned as prdouct owner. Current role: "+ user.getRole());
		}
		int rows = productDao.save(product);
		if(rows <=0) {
			throw new RuntimeException("Create failed for Product");
		}
		return product;
	}*/
	@Override
	public Product createProduct(Product product) {
	    if (product == null) {
	        throw new IllegalArgumentException("Product must not be null");
	    }

	    // --------- SAFE DEFAULTS so INSERT doesn't break ----------
	    if (product.getProductQuantity() == null) {
	        product.setProductQuantity(0);
	    }
	    if (product.getProductAvgRating() == null) {
	        product.setProductAvgRating(java.math.BigDecimal.ZERO);
	    }
	    if (product.getProductReviewsCount() == null) {
	        product.setProductReviewsCount(0);
	    }
	    if (product.getImageUrl() == null) {
	        product.setImageUrl(""); // or some default image URL
	    }

	    // --------- VALIDATE USER / ROLE ----------
	    // product.getUserId() is set in SellerController before calling this
	    User user = userDao.findById(product.getUserId());
	    if (user == null) {
	        throw new ResourceNotFoundException("User not found with id: " + product.getUserId());
	    }

	    if (!"SELLER".equalsIgnoreCase(user.getRole())) {
	        throw new RuntimeException(
	                "Only SELLER users can be assigned as product owner. Current role: " + user.getRole()
	        );
	    }

	    // --------- SAVE PRODUCT ----------
	    int rows = productDao.save(product);
	    if (rows <= 0) {
	        throw new RuntimeException("Create failed for Product");
	    }

	    return product;
	}

	
	
	
	@Override
	public Product getProductById(int productId) {
		return productDao.findById(productId).orElseThrow(()-> 
		new ResourceNotFoundException("Product not found with id: "+productId));
	}
	@Override
	public List<Product> getAllProducts(){
		return productDao.findAll();
	}
	
	@Override
	public List<Product> getProductsByCategory(int categoryId){
		return productDao.findByCategory(categoryId);
	}
	
	@Override
	public List<Product> getProductsBySeller(int userId){
		return productDao.findBySeller(userId);
	}
	
	
	@Override
	public List<Product> searchProductsByName(String product_name){
		return productDao.searchByName(product_name);
	}
	public List<Product> searchProductsByBrand(String brand){
		return productDao.searchByBrand(brand);
	}

	@Override
	public Product updateProduct(Product product) {
	 if(product == null) {
		 throw new IllegalArgumentException("Product must not be null");
	 }
		getProductById(product.getProductId());
		
		int rows = productDao.update(product);
		if(rows <=0) {
			throw new RuntimeException("Update  failed for product Id: "+ product.getProductId());
		}
		
		return product;
	}
	
	@Override
	public List<Map<String, Object>> fetchProductsByCategory(int id) {
	    return productDao.getProductsByCategory(id);
	}

	@Override
	public List<Map<String, Object>> fetchSellerProducts(int sellerId) {
	    return productDao.getSellerProducts(sellerId);
	}
	
	
	@Override
	public void deleteProduct(int productId) {
		getProductById(productId);
		
		
		int rows = productDao.deleteById(productId);
		if(rows <=0) {
			throw new RuntimeException("Delete failed for product Id: "+ productId);
		}
		
	}
	

}
