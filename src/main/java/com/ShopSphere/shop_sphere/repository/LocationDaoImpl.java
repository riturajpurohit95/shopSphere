package com.ShopSphere.shop_sphere.repository;
 
import java.sql.PreparedStatement;

import java.sql.Statement;

import java.util.List;

import java.util.Optional;
 
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.jdbc.support.GeneratedKeyHolder;

import org.springframework.jdbc.support.KeyHolder;

import org.springframework.stereotype.Repository;
 
import com.ShopSphere.shop_sphere.model.Location;

import com.ShopSphere.shop_sphere.util.LocationRowMapper;
 
@Repository

public class LocationDaoImpl implements LocationDao{

	private final JdbcTemplate jdbcTemplate;

	public LocationDaoImpl(JdbcTemplate jdbcTemplate) {

		this.jdbcTemplate = jdbcTemplate;

	}
 
	@Override

	public Location save(Location location) {

		String sql = "INSERT INTO locations (city, hub_value)"

				+ " VALUES (?, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection ->{

			PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);

				ps.setString(1, location.getCity());

				ps.setInt(2, location.getHubValue());

				return ps;

			}, keyHolder);

		Number generatedId = keyHolder.getKey();
		if(generatedId != null) {
			location.setLocationId(generatedId.intValue());
		}
		return location;

	}
 
	@Override

	public Optional<Location> finndById(int location) {

		String sql = "SELECT * from locations WHERE  location_id=?";

		List<Location> result = jdbcTemplate.query(sql, new LocationRowMapper(), location);

		if(result.isEmpty()) return Optional.empty();

		return Optional.of(result.get(0));

	}
 
	@Override

	public List<Location> finAll() {

		String sql = "SELECT * FROM locations";

		return jdbcTemplate.query(sql, new LocationRowMapper());

	}
 
	@Override

	public List<Location> findByCity(String city) {

		String sql = "SELECT location_id, city, hub_value FROM locations WHERE LOWER(city)=LOWER(?)";

		return jdbcTemplate.query(sql, new LocationRowMapper(), city);

	}
 
	@Override

	public List<Location> searchByCity(String keyword) {

		String sql = "SELECT location_id, city, hub_value FROM locations WHERE LOWER(city) LIKE ?";

		String searchPattern = "%"+keyword+"%";

		return jdbcTemplate.query(sql, new LocationRowMapper(), searchPattern);

	}
 
	@Override

	public boolean existsByCity(String city) {

		String sql = "SELECT COUNT(*) FROM locations WHERE city=LOWER(?)";

		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, city);

		return count!=null && count>0;

	}
 
	@Override

	public int countLocations() {

		String sql = "SELECT COUNT(*) FROM locations";

		Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

		return count != null ? count :0;

	}
 
	@Override

	public int getHubValue(int locationId) {

		String sql = "SELECT hub_value FROM locations WHERE location_id = ?";

		return jdbcTemplate.queryForObject(sql, Integer.class, locationId);

	}
 
}

 