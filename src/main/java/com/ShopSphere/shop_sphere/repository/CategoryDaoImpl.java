package com.ShopSphere.shop_sphere.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ShopSphere.shop_sphere.model.Category;
import com.ShopSphere.shop_sphere.util.CategoryRowMapper;


@Repository
public class CategoryDaoImpl implements CategoryDao{
	
	private final JdbcTemplate jdbcTemplate;
	
	public CategoryDaoImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Category save(Category category) {
		String sql = "INSERT INTO categories(category_name) VALUES (?)";
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
				
		jdbcTemplate.update(connection ->{
			PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, category.getCategoryName());
			
				
				return ps;
			}, keyHolder);
		Number generatedId = keyHolder.getKey();
		if(generatedId != null) {
			category.setCategoryId(generatedId.intValue());
		}
		return category;
	}

	@Override
	public Optional<Category> findById(int categoryId) {
		String sql = "SELECT * from categories WHERE  category_id=?";
		
		List<Category> result = jdbcTemplate.query(sql, new CategoryRowMapper(), categoryId);
		if(result.isEmpty()) return Optional.empty();
		
		return Optional.of(result.get(0));
	}

	@Override
	public List<Category> findAll() {
		String sql = "SELECT * from categories";
		return jdbcTemplate.query(sql, new CategoryRowMapper());
	}

	@Override
	public int update(Category category) {
		String sql = "UPDATE categories SET category_name=? WHERE category_id=?";
		
		return jdbcTemplate.update(sql,
				
				category.getCategoryName(),
				category.getCategoryId()
				
				);
	}

	@Override
	public int delete(int categoryId) {
		String sql = "DELETE FROM categories WHERE category_id = ?";
		return jdbcTemplate.update(sql,categoryId);
	}

	@Override
	public List<Category> findByName(String name) {
		String sql = "SELECT category_id, category_name FROM categories WHERE category_name=?";
		return jdbcTemplate.query(sql, new CategoryRowMapper(), name);
	}

	@Override
	public List<Category> searchByName(String keyword) {
		String sql = "SELECT category_id, category_name FROM categories WHERE category_name LIKE ?";
		String searchPattern = "%"+keyword+"%";
		return jdbcTemplate.query(sql, new CategoryRowMapper(), searchPattern);
	}

	@Override
	public boolean existsByName(String name) {
		String sql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name);
		return count!=null && count>0;
	}

}
