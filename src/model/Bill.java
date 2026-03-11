package model;

import java.math.BigDecimal;

/**
 * A generated bill for a customer for a given month, with total and optional breakdown.
 */
public class Bill {
    private int id;
    private int customerId;
    private int usageRecordId;
    private int month;
    private int year;
    private BigDecimal totalAmount;
    private String billBreakdown;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getUsageRecordId() { return usageRecordId; }
    public void setUsageRecordId(int usageRecordId) { this.usageRecordId = usageRecordId; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getBillBreakdown() { return billBreakdown; }
    public void setBillBreakdown(String billBreakdown) { this.billBreakdown = billBreakdown; }

    @Override
    public String toString() {
        return String.format("Bill[id=%d, customerId=%d, %d/%d, total=%s]", id, customerId, month, year, totalAmount);
    }
}
