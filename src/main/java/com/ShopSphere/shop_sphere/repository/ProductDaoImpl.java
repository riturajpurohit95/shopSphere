/*package com.ShopSphere.shop_sphere.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ShopSphere.shop_sphere.exception.OutOfStockException;

import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.util.OrderItemRowMapper;
import com.ShopSphere.shop_sphere.util.ProductRowMapper;

@Repository
public class ProductDaoImpl implements ProductDao {
	

	    private final JdbcTemplate jdbcTemplate;
		
		public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
			this.jdbcTemplate = jdbcTemplate;
		}
		
		
		@Override
		    public int save(Product product) {    	
		    	
		    	 String sql = "INSERT INTO products (user_id,category_id,product_name,product_price,product_mrp,"
		    	 		+ "product_quantity,product_avg_rating,product_reviews_count,brand,description,image_url) " +
		                "VALUES (?, ?, ?, ?, ?,?,?,?,?,?,?)";
		         KeyHolder keyHolder = new GeneratedKeyHolder(); 
		         jdbcTemplate.update(connection -> {                   
		            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		            //ps.setInt(1, product.getProductId());
		            ps.setInt(1, product.getUserId());
		            ps.setInt(2, product.getCategoryId());
		            ps.setString(3, product.getProductName());
		           
		            ps.setBigDecimal(4, product.getProductPrice());
		            ps.setBigDecimal(5, product.getProductMrp());
		            ps.setInt(6, product.getProductQuantity());
		            ps.setBigDecimal(7, product.getProductAvgRating());
		            ps.setInt(8, product.getProductReviewsCount());
		            ps.setString(9, product.getBrand());
		            ps.setString(10,product.getProductDescription());
		            ps.setString(11,product.getImageUrl());
		            return ps;
		            }, keyHolder);   
		            Number key = keyHolder.getKey();
		            if (key != null) {
		            	product.setProductId(key.intValue());
		            }
		            return product.getProductId(); 		
		    }
			//find by order id
		
			@Override
			public Optional<Product> findById(int productId) {		
				String sql = "select * from products where product_id = ?";
				List<Product> list = jdbcTemplate.query(sql, new ProductRowMapper(),productId);
				if (list.isEmpty()) {
					return Optional.empty();
				}
				return Optional.of(list.get(0));		
			}
			 //find by order id
			
			@Override
			public List<Product> findAll() {		
				String sql = "select * from products";
				return jdbcTemplate.query(sql, new ProductRowMapper());	
			}
	 //find by category id

			@Override
			public List<Product> findByCategory(int categoryId) {		
				String sql = "select * from products where category_id = ?";
			
						return jdbcTemplate.query(sql, new ProductRowMapper(),categoryId);
			}
			//find by seller
			@Override
			
			public List<Product> findBySeller(int userId) {		
				String sql = "select * from products where user_id=? ";
				return jdbcTemplate.query(sql, new ProductRowMapper(),userId);	
			}
			//search by name
			public List<Product> searchByName(String name) {	
				if(name==null || name.trim().isEmpty()) {
					return findAll();
				}
				String sql = "select * from products where LOWER(product_name) LIKE ?";
				String input = "%" + name.toLowerCase()+ "%";
				return jdbcTemplate.query(sql, new ProductRowMapper(),input);
				
			}
			//update product
			public int update(Product product){
				String sql= "Update products set user_id = ?,category_id = ?,product_name = ?,product_price = ?,"
						+ "product_mrp = ?,product_quantity = ?,product_avg_rating = ?,product_reviews_count= ?, "
						+ "brand =?, description= ?, image_url = ?  where product_id = ?";
					
				
				return jdbcTemplate.update(sql,
						
						product.getUserId(),
						product.getCategoryId(),
						product.getProductName(),
						product.getProductPrice(),
						product.getProductMrp(),
						product.getProductQuantity(),
						product.getProductAvgRating(),
						product.getProductReviewsCount(),
				        product.getBrand(),
				        product.getProductDescription(),
				        product.getImageUrl(),
				        product.getProductId());
			}
			
			//delete product
			@Override
			public int deleteById(int productId) {
				
				String sql = "delete from products where product_id =?";
				return jdbcTemplate.update(sql,productId);
			}
			
			//search by Brand
			@Override
			public List<Product> searchByBrand(String Brand) {	
				if(Brand==null || Brand.trim().isEmpty()) {
					return findAll();
				}
				String sql = "select * from products where LOWER(brand) LIKE ?";
				String pattern = "%" + Brand.toLowerCase()+ "%";
				return jdbcTemplate.query(sql, new ProductRowMapper(),pattern);
				
			}


			@Override
			public int getSellerIdByProductId(int productId) {
				String sql = "SELECT user_id FROM products WHERE product_id=?";
				return jdbcTemplate.queryForObject(sql, Integer.class, productId);
			}
			
			@Override
			public int decreaseStockIfAvailable(int productId, int quantity) {
				String sql = "Update products set product_quantity = product_quantity - ? where product_id =? "
						+ "AND product_quantity >=?";
				int rows =  jdbcTemplate.update(sql,quantity, productId, quantity);
				
				if(rows <=0) {
					throw new OutOfStockException("Not enough stock to reduce for productId: "+ productId);
				}
				return rows;
			}
			@Override
			public int increaseStock(int productId, int quantity) {
				String sql = "Update products set product_quantity = product_quantity + ? where product_id =? ";
				return jdbcTemplate.update(sql,quantity, productId);
			}
			@Override
			public List<Map<String, Object>> getProductsByCategory(int categoryId) {
			    String sql = "SELECT p.product_id, p.product_name, p.product_price, p.product_mrp, " +
			                 "c.category_name FROM products p " +
			                 "INNER JOIN categories c ON p.category_id = c.category_id " +
			                 "WHERE c.category_id = ?";

			    return jdbcTemplate.queryForList(sql, categoryId);
			}
			
			@Override
			public List<Map<String, Object>> getSellerProducts(int sellerId) {
			    String sql = "SELECT p.product_id, p.product_name, p.product_price, p.product_quantity, " +
			                 "u.name AS seller_name FROM products p " +
			                 "INNER JOIN user u ON p.user_id = u.user_id " +
			                 "WHERE p.user_id = ?";

			    return jdbcTemplate.queryForList(sql, sellerId);
			}
		
}*/
package com.ShopSphere.shop_sphere.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ShopSphere.shop_sphere.exception.OutOfStockException;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.util.ProductRowMapper;

@Repository
public class ProductDaoImpl implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    public ProductDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int save(Product product) {

        String sql = "INSERT INTO products (user_id,category_id,product_name,product_price,product_mrp,"
                + "product_quantity,product_avg_rating,product_reviews_count,brand,description,image_url) "
                + "VALUES (?, ?, ?, ?, ?,?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, product.getUserId());
            ps.setInt(2, product.getCategoryId());
            ps.setString(3, product.getProductName());
            ps.setBigDecimal(4, product.getProductPrice());
            ps.setBigDecimal(5, product.getProductMrp());
            ps.setInt(6, product.getProductQuantity());
            ps.setBigDecimal(7, product.getProductAvgRating());
            ps.setInt(8, product.getProductReviewsCount());
            ps.setString(9, product.getBrand());
            ps.setString(10, product.getProductDescription());
            ps.setString(11, product.getImageUrl());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) {
            product.setProductId(key.intValue());
        }
        return product.getProductId();
    }

    @Override
    public Optional<Product> findById(int productId) {
        String sql = "select * from products where product_id = ?";
        List<Product> list = jdbcTemplate.query(sql, new ProductRowMapper(), productId);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    @Override
    public List<Product> findAll() {
        String sql = "select * from products";
        return jdbcTemplate.query(sql, new ProductRowMapper());
    }

    @Override
    public List<Product> findByCategory(int categoryId) {
        String sql = "select * from products where category_id = ?";
        return jdbcTemplate.query(sql, new ProductRowMapper(), categoryId);
    }

    @Override
    public List<Product> findBySeller(int userId) {
        String sql = "select * from products where user_id=? ";
        return jdbcTemplate.query(sql, new ProductRowMapper(), userId);
    }

    // ⭐ SMART SEARCH:
    // - no description search
    // - len < 3: only product_name prefix
    // - len >= 3: product_name + brand (contains), category only on exact match
    @Override
    public List<Product> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return findAll();
        }

        String q = name.trim().toLowerCase();
        int len = q.length();

        if (len < 3) {
            // VERY SHORT QUERY (1–2 letters):
            // Only search product names that START WITH the letters.
            String prefix = q + "%";

            String sql =
                "SELECT p.* " +
                "FROM products p " +
                "WHERE LOWER(p.product_name) LIKE ? " +
                "ORDER BY LOWER(p.product_name)";

            return jdbcTemplate.query(sql, new ProductRowMapper(), prefix);
        } else {
            // LONGER QUERY (>= 3 letters):
            // - product_name contains (but prioritize starts-with)
            // - brand contains (but only kicks in now)
            // - category_name only on exact match (full category typed)
            String contains = "%" + q + "%";
            String prefix = q + "%";

            String sql =
                "SELECT p.* " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "WHERE LOWER(p.product_name) LIKE ? " +      // contains in name
                "   OR LOWER(p.brand) LIKE ? " +             // contains in brand
                "   OR LOWER(c.category_name) = ? " +        // exact category name
                "ORDER BY " +
                "   CASE " +
                "       WHEN LOWER(p.product_name) LIKE ? THEN 0 " +  // name starts with
                "       WHEN LOWER(p.brand) LIKE ? THEN 1 " +         // brand starts with
                "       WHEN LOWER(c.category_name) = ? THEN 2 " +    // exact category
                "       ELSE 3 " +
                "   END, " +
                "   LOWER(p.product_name)";

            return jdbcTemplate.query(sql, new ProductRowMapper(),
                    contains, contains, q,
                    prefix, prefix, q);
        }
    }

    @Override
    public int update(Product product){
        String sql= "Update products set user_id = ?,category_id = ?,product_name = ?,product_price = ?,"
                + "product_mrp = ?,product_quantity = ?,product_avg_rating = ?,product_reviews_count= ?, "
                + "brand =?, description= ?, image_url = ?  where product_id = ?";

        return jdbcTemplate.update(sql,
                product.getUserId(),
                product.getCategoryId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductMrp(),
                product.getProductQuantity(),
                product.getProductAvgRating(),
                product.getProductReviewsCount(),
                product.getBrand(),
                product.getProductDescription(),
                product.getImageUrl(),
                product.getProductId());
    }

    @Override
    public int deleteById(int productId) {
        String sql = "delete from products where product_id =?";
        return jdbcTemplate.update(sql,productId);
    }

    @Override
    public List<Product> searchByBrand(String Brand) {
        if (Brand == null || Brand.trim().isEmpty()) {
            return findAll();
        }
        String sql = "select * from products where LOWER(brand) LIKE ?";
        String pattern = "%" + Brand.toLowerCase() + "%";
        return jdbcTemplate.query(sql, new ProductRowMapper(), pattern);
    }

    @Override
    public int getSellerIdByProductId(int productId) {
        String sql = "SELECT user_id FROM products WHERE product_id=?";
        return jdbcTemplate.queryForObject(sql, Integer.class, productId);
    }

    @Override
    public int decreaseStockIfAvailable(int productId, int quantity) {
        String sql = "Update products set product_quantity = product_quantity - ? where product_id =? "
                + "AND product_quantity >=?";
        int rows =  jdbcTemplate.update(sql,quantity, productId, quantity);

        if(rows <=0) {
            throw new OutOfStockException("Not enough stock to reduce for productId: "+ productId);
        }
        return rows;
    }

    @Override
    public int increaseStock(int productId, int quantity) {
        String sql = "Update products set product_quantity = product_quantity + ? where product_id =? ";
        return jdbcTemplate.update(sql,quantity, productId);
    }

    @Override
    public List<Map<String, Object>> getProductsByCategory(int categoryId) {
        String sql = "SELECT p.product_id, p.product_name, p.product_price, p.product_mrp, " +
                     "c.category_name FROM products p " +
                     "INNER JOIN categories c ON p.category_id = c.category_id " +
                     "WHERE c.category_id = ?";

        return jdbcTemplate.queryForList(sql, categoryId);
    }

    @Override
    public List<Map<String, Object>> getSellerProducts(int sellerId) {
        String sql = "SELECT p.product_id, p.product_name, p.product_price, p.product_quantity, " +
                     "u.name AS seller_name FROM products p " +
                     "INNER JOIN user u ON p.user_id = u.user_id " +
                     "WHERE p.user_id = ?";

        return jdbcTemplate.queryForList(sql, sellerId);
    }
}


