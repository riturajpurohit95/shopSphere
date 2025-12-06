package com.ShopSphere.shop_sphere.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ShopSphere.shop_sphere.model.Payment;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.util.PaymentRowMapper;
import com.ShopSphere.shop_sphere.util.ProductRowMapper;

@Repository
public class PaymentDaoImpl implements PaymentDao {

    private final JdbcTemplate jdbcTemplate;

    public PaymentDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int save(Payment payment) {

        String sql = "INSERT INTO payments (order_id, user_id, amount, currency, payment_method, created_at, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, payment.getOrderId());
            ps.setInt(2, payment.getUserId());
            ps.setBigDecimal(3, payment.getAmount());
            ps.setString(4, payment.getCurrency());
            ps.setString(5, payment.getPaymentMethod());
            ps.setTimestamp(6, Timestamp.valueOf(payment.getCreatedAt()));
            ps.setString(7, payment.getStatus());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            payment.setPaymentId(key.intValue());
        }

        return payment.getPaymentId();
    }

    @Override
    public Optional<Payment> findById(int paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        List<Payment> list = jdbcTemplate.query(sql, new PaymentRowMapper(), paymentId);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
    public List<Payment> findAll() {		
		String sql = "select * from payments";
		return jdbcTemplate.query(sql, new PaymentRowMapper());	
	}

    @Override
    public Optional<Payment> findByOrderId(int orderId) {
        String sql = "SELECT * FROM payments WHERE order_id = ? ORDER BY created_at DESC";
        List<Payment> list = jdbcTemplate.query(sql, new PaymentRowMapper(), orderId);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    @Override
    public int updateStatus(int paymentId, String status) {
        String sql = "UPDATE payments SET status = ? WHERE payment_id = ?";
        return jdbcTemplate.update(sql, status, paymentId);
    }

    @Override
    public int updateGatewayDetails(int paymentId, String gatewayRef, String upiVpa, String responsePayLoad) {
        String sql = "UPDATE payments SET gateway_ref = ?, upi_vpa = ?, response_payload = ? WHERE payment_id = ?";
        return jdbcTemplate.update(sql, gatewayRef, upiVpa, responsePayLoad, paymentId);
    }

    @Override
    public List<Map<String, Object>> getPaymentDetails(int userId) {
        String sql = "SELECT p.payment_id, p.amount, p.status, "
                + "o.order_id, o.total_amount "
                + "FROM payments p "
                + "INNER JOIN orders o ON p.order_id = o.order_id "
                + "WHERE p.user_id = ?";
        return jdbcTemplate.queryForList(sql, userId);
    }
}

