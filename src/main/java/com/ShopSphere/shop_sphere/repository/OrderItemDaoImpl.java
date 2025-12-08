package com.ShopSphere.shop_sphere.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ShopSphere.shop_sphere.exception.OutOfStockException;
import com.ShopSphere.shop_sphere.model.OrderItem;
import com.ShopSphere.shop_sphere.util.OrderItemRowMapper;

@Repository
public class OrderItemDaoImpl implements OrderItemDao {

    private final JdbcTemplate jdbcTemplate;
    private final ProductDao productDao;

    public OrderItemDaoImpl(JdbcTemplate jdbcTemplate, ProductDao productDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.productDao = productDao;
    }

    @Override
    public int save(OrderItem orderItem) {

        // reduce stock (keep this here)
        int updatedRows = productDao.decreaseStockIfAvailable(
                orderItem.getProductId(),
                orderItem.getQuantity());
        if (updatedRows == 0) {
            throw new OutOfStockException("Not enough stock for productId: " + orderItem.getProductId());
        }

        String sql = "INSERT INTO order_items " +
                     "(order_id, product_id, seller_id, product_name, quantity, unit_price, total_item_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, orderItem.getOrderId());
            ps.setInt(2, orderItem.getProductId());
            ps.setInt(3, orderItem.getSellerId());               // NEW
            ps.setString(4, orderItem.getProductName());
            ps.setInt(5, orderItem.getQuantity());
            ps.setBigDecimal(6, orderItem.getUnitPrice());
            ps.setBigDecimal(7, orderItem.getTotalItemPrice());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            orderItem.setOrderItemsId(key.intValue());
        }

        return orderItem.getOrderItemsId();
    }



    @Override
    public Optional<OrderItem> findById(int orderItemsId) {
        String sql = "SELECT * FROM order_items WHERE order_items_id = ?";
        List<OrderItem> list = jdbcTemplate.query(sql, new OrderItemRowMapper(), orderItemsId);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    @Override
    public List<OrderItem> findByOrderId(int orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        return jdbcTemplate.query(sql, new OrderItemRowMapper(), orderId);
    }

    @Override
    public List<OrderItem> findByProductId(int productId) {
        String sql = "SELECT * FROM order_items WHERE product_id = ?";
        return jdbcTemplate.query(sql, new OrderItemRowMapper(), productId);
    }

    @Override
    public int updateQuantityANDTotalPrice(int orderItemsId, int newQuantity, BigDecimal totalPrice) {

        Optional<OrderItem> optional = findById(orderItemsId);
        if (!optional.isPresent()) {
            return 0;
        }

        OrderItem existing = optional.get();
        int oldQuantity = existing.getQuantity();
        int diff = newQuantity - oldQuantity;

        if (diff > 0) {
            // increase in quantity → need more stock
            int updatedRows = productDao.decreaseStockIfAvailable(existing.getProductId(), diff);
            if (updatedRows == 0) {
                throw new OutOfStockException("Not enough stock for productId: " + existing.getProductId());
            }
        } else if (diff < 0) {
            // decrease in quantity → return stock
            int restoreQty = -diff;
            productDao.increaseStock(existing.getProductId(), restoreQty);
        }

        String sql = "UPDATE order_items SET quantity = ?, total_item_price = ? WHERE order_items_id = ?";
        return jdbcTemplate.update(sql, newQuantity, totalPrice, orderItemsId);
    }

    @Override
    public int deleteById(int orderItemsId) {

        Optional<OrderItem> optional = findById(orderItemsId);
        if (!optional.isPresent()) {
            return 0;
        }

        OrderItem item = optional.get();

        productDao.increaseStock(item.getProductId(), item.getQuantity());

        String sql = "DELETE FROM order_items WHERE order_items_id = ?";
        return jdbcTemplate.update(sql, orderItemsId);
    }
}

