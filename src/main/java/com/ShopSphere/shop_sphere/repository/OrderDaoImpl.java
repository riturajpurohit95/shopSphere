package com.ShopSphere.shop_sphere.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ShopSphere.shop_sphere.model.Order;
import com.ShopSphere.shop_sphere.model.Product;
import com.ShopSphere.shop_sphere.util.OrderRowMapper;
import com.ShopSphere.shop_sphere.util.ProductRowMapper;
import com.ShopSphere.shop_sphere.util.SellerOrderRowMapper;

@Repository
public class OrderDaoImpl implements OrderDao {

    private final JdbcTemplate jdbcTemplate;

    public OrderDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int save(Order order) {

        String sql = "INSERT INTO orders (user_id, total_amount, shipping_address, orderStatus, placed_at, payment_method) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getUserId());
            ps.setBigDecimal(2, order.getTotalAmount());
            ps.setString(3, order.getShippingAddress());
            ps.setString(4, order.getOrderStatus());
            ps.setTimestamp(5, Timestamp.valueOf(order.getPlacedAt()));
            ps.setString(6, order.getPaymentMethod());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            order.setOrderId(key.intValue());
        }

        return order.getOrderId();
    }

    @Override
    public Optional<Order> findById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        List<Order> list = jdbcTemplate.query(sql, new OrderRowMapper(), orderId);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    @Override
    public List<Order> findByUserId(int userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY placed_at DESC";
        return jdbcTemplate.query(sql, new OrderRowMapper(), userId);
    }
    public List<Order> findAll() {		
		String sql = "select * from orders";
		return jdbcTemplate.query(sql, new OrderRowMapper());	
	}

    @Override
    public int updateOrderStatus(int orderId, String orderStatus) {
        String sql = "UPDATE orders SET orderStatus = ? WHERE order_id = ?";
        return jdbcTemplate.update(sql, orderStatus, orderId);
    }

    // ❌ removed wrong updatePaymentStatus() here

    @Override
    public int cancelOrder(int orderId) {
        String sql = "UPDATE orders SET orderStatus = 'CANCELLED' WHERE order_id = ?";
        return jdbcTemplate.update(sql, orderId);
    }

    @Override
    public int deleteById(int orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        return jdbcTemplate.update(sql, orderId);
    }

    @Override
    public List<Order> findByStatusAndPlacedAtBefore(String status, LocalDateTime cutOffTime) {
        String sql = "SELECT * FROM orders WHERE orderStatus = ? AND placed_at < ?";
        return jdbcTemplate.query(sql, new OrderRowMapper(), status, Timestamp.valueOf(cutOffTime));
    }

    @Override
    public int updateRazorpayOrderId(int orderId, String razorpayOrderId) {
        String sql = "UPDATE orders SET razorpay_order_id = ? WHERE order_id = ?";
        return jdbcTemplate.update(sql, razorpayOrderId, orderId);
    }

    @Override
    public List<Map<String, Object>> getOrdersWithItems(int userId) {
        String sql = "SELECT o.order_id, o.total_amount, o.shipping_address, "
                + "oi.product_name, oi.quantity, oi.unit_price "
                + "FROM orders o "
                + "INNER JOIN order_items oi ON o.order_id = oi.order_id "
                + "WHERE o.user_id = ?";
        return jdbcTemplate.queryForList(sql, userId);
    }
    //@Override
   /* public List<Order> findBySeller(int userId) {
        String sql =
            "SELECT o.order_id, o.user_id, o.total_amount, o.shipping_address, " +
            "       o.placed_at, o.orderStatus, o.payment_method, " +
            "       oi.product_name, oi.quantity, oi.unit_price " +
            "FROM orders o " +
            "JOIN order_items oi ON o.order_id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.product_id " +
            "WHERE p.user_id = ? " +
            "ORDER BY o.placed_at DESC";

        return jdbcTemplate.query(sql, new OrderRowMapper(), userId);
    }*/
    @Override
    public List<Order> findBySeller(int userId) {
        String sql =
            "SELECT o.* " +
            "FROM orders o " +
            "WHERE o.order_id IN ( " +
            "  SELECT DISTINCT oi.order_id " +
            "  FROM order_items oi " +
            "  JOIN products p ON oi.product_id = p.product_id " +
            "  WHERE p.user_id = ? " +
            ") " +
            "ORDER BY o.placed_at DESC";

        return jdbcTemplate.query(sql, new OrderRowMapper(), userId);
    }

}
