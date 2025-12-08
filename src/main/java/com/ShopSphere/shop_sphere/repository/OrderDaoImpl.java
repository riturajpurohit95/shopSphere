package com.ShopSphere.shop_sphere.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
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

//    @Override
//    public List<Order> findByUserId(int userId) {
//        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY placed_at DESC";
//        return jdbcTemplate.query(sql, new OrderRowMapper(), userId);
//    }
    
    @Override
    public List<Order> findByUserId(int userId) {

        String sql = """
            SELECT o.*, p.status AS payment_status
            FROM orders o
            LEFT JOIN payments p ON o.order_id = p.order_id
            WHERE o.user_id = ?
            ORDER BY o.placed_at DESC
        """;

        return jdbcTemplate.query(sql, new OrderRowMapper(), userId);
    }
    
//    @Override
//    public Order getOrderById(int orderId) {
//        String sql = "SELECT * FROM orders WHERE order_id = ?";
//        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Order.class), orderId);
//    }

    @Override
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        return jdbcTemplate.queryForObject(sql, new OrderRowMapper(), orderId);
    }




    @Override
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

//    @Override
//    public int cancelOrder(int orderId) {
//        String sql = "UPDATE orders SET orderStatus = 'CANCELLED' WHERE order_id = ?";
//        return jdbcTemplate.update(sql, orderId);
//    }
    
    public Order cancelOrder(int orderId) {
        // Update the order
        String updateSql = "UPDATE orders SET orderStatus = 'CANCELLED' WHERE order_id = ?";
        int rows = jdbcTemplate.update(updateSql, orderId);

        if (rows == 0) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }

        // Fetch the updated order
        String selectSql = "SELECT * FROM orders WHERE order_id = ?";
//        return jdbcTemplate.queryForObject(selectSql, new BeanPropertyRowMapper<>(Order.class), orderId);
        return jdbcTemplate.queryForObject(selectSql, new OrderRowMapper(), orderId);

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
//    @Override
//    public List<Order> findBySeller(int userId) {
//    	String sql = 
//    		    "SELECT o.order_id AS orderId, " +
//    		    "o.placed_at AS orderDate, " +
//    		    "oi.product_name AS productName, " +
//    		    "o.total_amount AS totalAmount, " +
//    		    "o.orderStatus AS orderStatus, " +
//    		    "p.status AS paymentStatus " +
//    		    "FROM orders o " +
//    		    "JOIN order_items oi ON o.order_id = oi.order_id " +
//    		    "LEFT JOIN payments p ON o.order_id = p.order_id " +
//    		    "WHERE oi.seller_id = ? " +
//    		    "ORDER BY o.placed_at DESC";
//
//        return jdbcTemplate.query(sql, new OrderRowMapper(), userId);
//    }
    
    @Override
    public List<Order> findBySeller(int userId) {

    	String sql =
    		    "SELECT o.order_id, " +
    		    " o.user_id, " +
    		    " o.total_amount, " +
    		    " o.shipping_address, " +
    		    " o.placed_at, " +
    		    " o.orderStatus, " +
    		    " o.payment_method, " +
    		    " o.razorpay_order_id, " +
    		    " p.status AS payment_status " +   
    		    " FROM orders o " +
    		    " JOIN order_items oi ON o.order_id = oi.order_id " +
    		    " LEFT JOIN payments p ON o.order_id = p.order_id " +
    		    " WHERE oi.seller_id = ? " +
    		    " ORDER BY o.placed_at DESC";


        return jdbcTemplate.query(sql, new OrderRowMapper(), userId);
    }
    
    
     
//        public List<Order> findBySeller(int sellerId) {
//     
//            String sql =
//                "SELECT o.order_id, o.user_id, o.total_amount, o.shipping_address, " +
//                "       o.placed_at, o.orderStatus, o.payment_method, o.razorpay_order_id, " +
//                "       p.status AS payment_status " +
//                "FROM orders o " +
//                "JOIN order_items oi ON o.order_id = oi.order_id " +
//                "LEFT JOIN payments p ON o.order_id = p.order_id " +
//                "WHERE oi.seller_id = ? " +
//                "GROUP BY o.order_id " +
//                "ORDER BY o.placed_at DESC";
//     
//            return jdbcTemplate.query(sql, new SellerOrderRowMapper(), sellerId);
//        }
    


    
    @Override
    public void updateTotalAmount(int orderId, BigDecimal totalAmount) {
        String sql = "UPDATE orders SET total_amount = ? WHERE order_id = ?";
        jdbcTemplate.update(sql, totalAmount, orderId);
    }

    
    @Override
    public List<Order> findOrdersWithPaymentByUserId(int userId) {
        String sql = "SELECT o.order_id, o.user_id, o.total_amount, o.shipping_address, " +
                     "o.placed_at, o.orderStatus, o.payment_method, " +
                     "p.status AS payment_status " +
                     "FROM orders o " +
                     "LEFT JOIN payments p ON o.order_id = p.order_id " +
                     "WHERE o.user_id = ? " +
                     "ORDER BY o.placed_at DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order order = new Order();
            order.setOrderId(rs.getInt("order_id"));
            order.setUserId(rs.getInt("user_id"));
            order.setTotalAmount(rs.getBigDecimal("total_amount"));
            order.setShippingAddress(rs.getString("shipping_address"));
            order.setPlacedAt(rs.getTimestamp("placed_at").toLocalDateTime());
            order.setOrderStatus(rs.getString("orderStatus"));
            order.setPaymentMethod(rs.getString("payment_method"));
            order.setPaymentStatus(rs.getString("payment_status")); // <-- important
            return order;
        }, userId);
    }
}


