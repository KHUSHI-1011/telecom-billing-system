package dao;

import model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for customers table: CRUD and list all.
 */
public class CustomerDAO {

    public int add(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, email, plan_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setObject(4, c.getPlanId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                    return c.getId();
                }
            }
        }
        return -1;
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT id, name, phone, email, plan_id FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT id, name, phone, email, plan_id FROM customers ORDER BY id";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET name = ?, phone = ?, email = ?, plan_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setObject(4, c.getPlanId());
            ps.setInt(5, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean assignPlan(int customerId, int planId) throws SQLException {
        Customer c = findById(customerId);
        if (c == null) return false;
        c.setPlanId(planId);
        return update(c);
    }

    private static Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setPlanId(rs.getObject("plan_id") != null ? rs.getInt("plan_id") : null);
        return c;
    }
}
