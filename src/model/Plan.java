package model;

import java.math.BigDecimal;

/**
 * Represents a telecom plan with name, monthly price, data limit, and call rate.
 */
public class Plan {
    private int id;
    private String name;
    private BigDecimal monthlyPrice;
    private BigDecimal dataLimitGb;
    private BigDecimal callRatePerMin;

    public Plan() {}

    public Plan(String name, BigDecimal monthlyPrice, BigDecimal dataLimitGb, BigDecimal callRatePerMin) {
        this.name = name;
        this.monthlyPrice = monthlyPrice;
        this.dataLimitGb = dataLimitGb;
        this.callRatePerMin = callRatePerMin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public void setMonthlyPrice(BigDecimal monthlyPrice) { this.monthlyPrice = monthlyPrice; }
    public BigDecimal getDataLimitGb() { return dataLimitGb; }
    public void setDataLimitGb(BigDecimal dataLimitGb) { this.dataLimitGb = dataLimitGb; }
    public BigDecimal getCallRatePerMin() { return callRatePerMin; }
    public void setCallRatePerMin(BigDecimal callRatePerMin) { this.callRatePerMin = callRatePerMin; }

    @Override
    public String toString() {
        return String.format("Plan[id=%d, name=%s, monthlyPrice=%s, dataLimit=%s GB, callRate=%s/min]",
                id, name, monthlyPrice, dataLimitGb, callRatePerMin);
    }
}
