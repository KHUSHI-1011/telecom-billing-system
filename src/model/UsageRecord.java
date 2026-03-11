package model;

import java.math.BigDecimal;

/**
 * Usage for one customer in a given month: call minutes, SMS count, data in GB.
 */
public class UsageRecord {
    private int id;
    private int customerId;
    private int month;
    private int year;
    private BigDecimal callMinutes;
    private int smsCount;
    private BigDecimal dataUsageGb;

    public UsageRecord() {
        this.callMinutes = BigDecimal.ZERO;
        this.smsCount = 0;
        this.dataUsageGb = BigDecimal.ZERO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public BigDecimal getCallMinutes() { return callMinutes; }
    public void setCallMinutes(BigDecimal callMinutes) { this.callMinutes = callMinutes; }
    public int getSmsCount() { return smsCount; }
    public void setSmsCount(int smsCount) { this.smsCount = smsCount; }
    public BigDecimal getDataUsageGb() { return dataUsageGb; }
    public void setDataUsageGb(BigDecimal dataUsageGb) { this.dataUsageGb = dataUsageGb; }

    @Override
    public String toString() {
        return String.format("Usage[customerId=%d, %d/%d, calls=%s min, sms=%d, data=%s GB]",
                customerId, month, year, callMinutes, smsCount, dataUsageGb);
    }
}
