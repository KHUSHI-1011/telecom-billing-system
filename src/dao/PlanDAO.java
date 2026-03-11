package dao;

import model.Plan;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for plans table.
 */
public class PlanDAO {

    public int add(Plan p) throws SQLException {
        String sql = "INSERT INTO plans (name, monthly_price, data_limit_gb, call_rate_per_min) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setBigDecimal(2, p.getMonthlyPrice());
            ps.setBigDecimal(3, p.getDataLimitGb());
            ps.setBigDecimal(4, p.getCallRatePerMin());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                    return p.getId();
                }
            }
        }
        return -1;
    }

    public Plan findById(int id) throws SQLException {
        String sql = "SELECT id, name, monthly_price, data_limit_gb, call_rate_per_min FROM plans WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Plan> findAll() throws SQLException {
        String sql = "SELECT id, name, monthly_price, data_limit_gb, call_rate_per_min FROM plans ORDER BY id";
        List<Plan> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private static Plan mapRow(ResultSet rs) throws SQLException {
        Plan p = new Plan();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setMonthlyPrice(rs.getBigDecimal("monthly_price"));
        p.setDataLimitGb(rs.getBigDecimal("data_limit_gb"));
        p.setCallRatePerMin(rs.getBigDecimal("call_rate_per_min"));
        return p;
    }
}
