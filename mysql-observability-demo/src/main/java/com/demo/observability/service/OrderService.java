package com.demo.observability.service;

import com.demo.observability.model.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing orders.
 * All database queries here will be automatically tracked by the observability system.
 */
@Service
public class OrderService {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> new Order(
        rs.getLong("id"),
        rs.getLong("user_id"),
        rs.getString("product"),
        rs.getBigDecimal("amount"),
        rs.getString("status"),
        rs.getTimestamp("created_at").toLocalDateTime()
    );

    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Order> findAll() {
        return jdbcTemplate.query("SELECT * FROM orders ORDER BY created_at DESC", orderRowMapper);
    }

    public Optional<Order> findById(Long id) {
        List<Order> orders = jdbcTemplate.query(
            "SELECT * FROM orders WHERE id = ?",
            orderRowMapper,
            id
        );
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
    }

    public List<Order> findByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC",
            orderRowMapper,
            userId
        );
    }

    public List<Order> findByStatus(String status) {
        return jdbcTemplate.query(
            "SELECT * FROM orders WHERE status = ? ORDER BY created_at DESC",
            orderRowMapper,
            status
        );
    }

    public Order create(Order order) {
        jdbcTemplate.update(
            "INSERT INTO orders (user_id, product, amount, status, created_at) VALUES (?, ?, ?, ?, NOW())",
            order.getUserId(),
            order.getProduct(),
            order.getAmount(),
            order.getStatus()
        );

        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        order.setId(id);

        return order;
    }

    public void updateStatus(Long id, String status) {
        jdbcTemplate.update(
            "UPDATE orders SET status = ? WHERE id = ?",
            status,
            id
        );
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM orders WHERE id = ?", id);
    }

    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Long.class);
    }
}
