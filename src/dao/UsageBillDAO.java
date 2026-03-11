package dao;

import model.Bill;
import model.UsageRecord;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access for usage_records and bills: record usage, generate/query bills.
 */
public class UsageBillDAO {

    // --- Usage records ---

    public UsageRecord getOrCreateUsage(int customerId, int month, int year) throws SQLException {
        UsageRecord u = findUsage(customerId, month, year);
        if (u != null) return u;
        u = new UsageRecord();
        u.setCustomerId(customerId);
        u.setMonth(month);
        u.setYear(year);
        addUsage(u);
        return u;
    }

    public UsageRecord findUsage(int customerId, int month, int year) throws SQLException {
        String sql = "SELECT id, customer_id, month, year, call_minutes, sms_count, data_usage_gb FROM usage_records WHERE customer_id = ? AND month = ? AND year = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUsageRow(rs);
            }
        }
        return null;
    }

    public void addUsage(UsageRecord u) throws SQLException {
        String sql = "INSERT INTO usage_records (customer_id, month, year, call_minutes, sms_count, data_usage_gb) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, u.getCustomerId());
            ps.setInt(2, u.getMonth());
            ps.setInt(3, u.getYear());
            ps.setBigDecimal(4, u.getCallMinutes());
            ps.setInt(5, u.getSmsCount());
            ps.setBigDecimal(6, u.getDataUsageGb());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getInt(1));
            }
        }
    }

    public boolean updateUsage(UsageRecord u) throws SQLException {
        String sql = "UPDATE usage_records SET call_minutes = ?, sms_count = ?, data_usage_gb = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, u.getCallMinutes());
            ps.setInt(2, u.getSmsCount());
            ps.setBigDecimal(3, u.getDataUsageGb());
            ps.setInt(4, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Add or update usage: add call minutes, SMS, data to existing record (creates if missing). */
    public void recordUsage(int customerId, int month, int year, BigDecimal callMinutes, int smsCount, BigDecimal dataGb) throws SQLException {
        UsageRecord u = getOrCreateUsage(customerId, month, year);
        u.setCallMinutes(u.getCallMinutes().add(callMinutes != null ? callMinutes : BigDecimal.ZERO));
        u.setSmsCount(u.getSmsCount() + smsCount);
        u.setDataUsageGb(u.getDataUsageGb().add(dataGb != null ? dataGb : BigDecimal.ZERO));
        updateUsage(u);
    }

    /** Top N customers by data usage (any month). */
    public List<Object[]> topDataUsage(int limit) throws SQLException {
        String sql = "SELECT c.id, c.name, c.phone, SUM(u.data_usage_gb) AS total_gb FROM customers c JOIN usage_records u ON c.id = u.customer_id GROUP BY c.id ORDER BY total_gb DESC LIMIT ?";
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getBigDecimal("total_gb")
                    });
                }
            }
        }
        return list;
    }

    private static UsageRecord mapUsageRow(ResultSet rs) throws SQLException {
        UsageRecord u = new UsageRecord();
        u.setId(rs.getInt("id"));
        u.setCustomerId(rs.getInt("customer_id"));
        u.setMonth(rs.getInt("month"));
        u.setYear(rs.getInt("year"));
        u.setCallMinutes(rs.getBigDecimal("call_minutes"));
        u.setSmsCount(rs.getInt("sms_count"));
        u.setDataUsageGb(rs.getBigDecimal("data_usage_gb"));
        return u;
    }

    // --- Bills ---

    public int addBill(Bill b) throws SQLException {
        String sql = "INSERT INTO bills (customer_id, usage_record_id, month, year, total_amount, bill_breakdown) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getCustomerId());
            ps.setInt(2, b.getUsageRecordId());
            ps.setInt(3, b.getMonth());
            ps.setInt(4, b.getYear());
            ps.setBigDecimal(5, b.getTotalAmount());
            ps.setString(6, b.getBillBreakdown());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    b.setId(rs.getInt(1));
                    return b.getId();
                }
            }
        }
        return -1;
    }

    public List<Bill> findBillsByCustomer(int customerId) throws SQLException {
        String sql = "SELECT id, customer_id, usage_record_id, month, year, total_amount, bill_breakdown FROM bills WHERE customer_id = ? ORDER BY year DESC, month DESC";
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBillRow(rs));
            }
        }
        return list;
    }

    public List<Bill> findBillsByMonthYear(int month, int year) throws SQLException {
        String sql = "SELECT id, customer_id, usage_record_id, month, year, total_amount, bill_breakdown FROM bills WHERE month = ? AND year = ? ORDER BY customer_id";
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBillRow(rs));
            }
        }
        return list;
    }

    /** Monthly billing summary: total revenue and count for a given month/year. */
    public Object[] monthlySummary(int month, int year) throws SQLException {
        String sql = "SELECT COUNT(*) AS bill_count, COALESCE(SUM(total_amount), 0) AS total FROM bills WHERE month = ? AND year = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{rs.getInt("bill_count"), rs.getBigDecimal("total")};
                }
            }
        }
        return new Object[]{0, BigDecimal.ZERO};
    }

    public Bill findBillByUsage(int usageRecordId) throws SQLException {
        String sql = "SELECT id, customer_id, usage_record_id, month, year, total_amount, bill_breakdown FROM bills WHERE usage_record_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usageRecordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBillRow(rs);
            }
        }
        return null;
    }

    private static Bill mapBillRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setId(rs.getInt("id"));
        b.setCustomerId(rs.getInt("customer_id"));
        b.setUsageRecordId(rs.getInt("usage_record_id"));
        b.setMonth(rs.getInt("month"));
        b.setYear(rs.getInt("year"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setBillBreakdown(rs.getString("bill_breakdown"));
        return b;
    }
}
