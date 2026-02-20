package com.demo.observability.service;

import com.demo.observability.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing users.
 * All database queries here will be automatically tracked by the observability system.
 */
@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("city")
    );

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }

    public Optional<User> findById(Long id) {
        List<User> users = jdbcTemplate.query(
            "SELECT * FROM users WHERE id = ?",
            userRowMapper,
            id
        );
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findByEmail(String email) {
        List<User> users = jdbcTemplate.query(
            "SELECT * FROM users WHERE email = ?",
            userRowMapper,
            email
        );
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public List<User> findByCity(String city) {
        return jdbcTemplate.query(
            "SELECT * FROM users WHERE city = ?",
            userRowMapper,
            city
        );
    }

    public User create(User user) {
        jdbcTemplate.update(
            "INSERT INTO users (name, email, city) VALUES (?, ?, ?)",
            user.getName(),
            user.getEmail(),
            user.getCity()
        );

        // Get the last inserted ID
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        user.setId(id);

        return user;
    }

    public void update(User user) {
        jdbcTemplate.update(
            "UPDATE users SET name = ?, email = ?, city = ? WHERE id = ?",
            user.getName(),
            user.getEmail(),
            user.getCity(),
            user.getId()
        );
    }

    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    public long count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
    }
}
