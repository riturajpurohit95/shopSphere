package com.ShopSphere.shop_sphere.repository;

import com.ShopSphere.shop_sphere.model.User;
import com.ShopSphere.shop_sphere.util.UserRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String INSERT_SQL =
            "INSERT INTO user (name, email, password, phone, role, location_id) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT user_id, name, email, password, phone, role, location_id FROM user WHERE user_id = ?";

    private static final String SELECT_BY_EMAIL_SQL =
            "SELECT user_id, name, email, password, phone, role, location_id FROM user WHERE email = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT user_id, name, email, password, phone, role, location_id FROM user ORDER BY user_id";

    private static final String UPDATE_SQL =
            "UPDATE user SET name = ?, phone = ?, password = ?, location_id = ? WHERE user_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM user WHERE user_id = ?";

    private static final String GET_USER_WITH_LOCATION_SQL =
            "SELECT u.user_id, u.name, u.email, u.phone, u.role, l.city, l.hub_value " +
            "FROM user u LEFT JOIN locations l ON u.location_id = l.location_id WHERE u.user_id = ?";

    @Override
    public int save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int affected = jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());

            if (user.getPhone() != null) {
                ps.setString(4, user.getPhone());
            } else {
                ps.setNull(4, java.sql.Types.VARCHAR);
            }

            ps.setString(5, user.getRole());

            if (user.getLocationId() != null) {
                ps.setInt(6, user.getLocationId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }

            return ps;
        }, keyHolder);

        if (affected > 0 && keyHolder.getKey() != null) {
            user.setUserId(keyHolder.getKey().intValue());
            return user.getUserId();
        }
        return 0;
    }

    @Override
    public User findById(int userId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_BY_ID_SQL, new UserRowMapper(), userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public User findByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(SELECT_BY_EMAIL_SQL, new UserRowMapper(), email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(SELECT_ALL_SQL, new UserRowMapper());
    }

    @Override
    public int update(User user) {
        return jdbcTemplate.update(UPDATE_SQL,
                user.getName(),
                user.getPhone(),
                user.getPassword(),
                user.getLocationId(),
                user.getUserId()
        );
    }

    @Override
    public int delete(int userId) {
        return jdbcTemplate.update(DELETE_SQL, userId);
    }
    
    @Override
    public int getLocationIdOfUser(int userId) {
        String sql = "SELECT location_id FROM user WHERE user_id = ?";
        Integer locationId = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        if (locationId == null) {
            throw new RuntimeException("User location not found for userId: " + userId);
        }
        return locationId;
    }


    @Override
    public Map<String, Object> getUserWithLocation(int userId) {
        return jdbcTemplate.queryForMap(GET_USER_WITH_LOCATION_SQL, userId);
    }
}
