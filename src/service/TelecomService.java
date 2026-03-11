package service;

import dao.CustomerDAO;
import dao.PlanDAO;
import dao.UsageBillDAO;
import model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic layer: orchestrates DAOs and implements billing calculation.
 * Bill = plan base price + overage (e.g. data over limit, or simple call charges).
 */
public class TelecomService {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PlanDAO planDAO = new PlanDAO();
    private final UsageBillDAO usageBillDAO = new UsageBillDAO();

    // --- Customer ---
    public int addCustomer(String name, String phone, String email) throws SQLException {
        Customer c = new Customer(name, phone, email);
        return customerDAO.add(c);
    }

    public Customer viewCustomer(int id) throws SQLException {
        return customerDAO.findById(id);
    }

    public boolean updateCustomer(int id, String name, String phone, String email) throws SQLException {
        Customer c = customerDAO.findById(id);
        if (c == null) return false;
        c.setName(name);
        c.setPhone(phone);
        c.setEmail(email);
        return customerDAO.update(c);
    }

    public boolean deleteCustomer(int id) throws SQLException {
        return customerDAO.delete(id);
    }

    public List<Customer> listAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    // --- Plans ---
    public int createPlan(String name, BigDecimal monthlyPrice, BigDecimal dataLimitGb, BigDecimal callRatePerMin) throws SQLException {
        Plan p = new Plan(name, monthlyPrice, dataLimitGb, callRatePerMin);
        return planDAO.add(p);
    }

    public List<Plan> listAllPlans() throws SQLException {
        return planDAO.findAll();
    }

    public boolean assignPlanToCustomer(int customerId, int planId) throws SQLException {
        if (planDAO.findById(planId) == null || customerDAO.findById(customerId) == null) return false;
        return customerDAO.assignPlan(customerId, planId);
    }

    // --- Usage ---
    public void recordUsage(int customerId, int month, int year, BigDecimal callMinutes, int smsCount, BigDecimal dataGb) throws SQLException {
        usageBillDAO.recordUsage(customerId, month, year,
                callMinutes != null ? callMinutes : BigDecimal.ZERO,
                smsCount,
                dataGb != null ? dataGb : BigDecimal.ZERO);
    }

    // --- Billing: base + overage for data; call rate applied to call minutes (simplified) ---
    public Bill generateBill(int customerId, int month, int year) throws SQLException {
        Customer cust = customerDAO.findById(customerId);
        if (cust == null) throw new IllegalArgumentException("Customer not found");
        if (cust.getPlanId() == null) throw new IllegalArgumentException("Customer has no plan assigned");
        Plan plan = planDAO.findById(cust.getPlanId());
        if (plan == null) throw new IllegalArgumentException("Plan not found");

        UsageRecord usage = usageBillDAO.getOrCreateUsage(customerId, month, year);
        if (usageBillDAO.findBillByUsage(usage.getId()) != null)
            throw new IllegalArgumentException("Bill already generated for this customer/month/year");

        BigDecimal base = plan.getMonthlyPrice();
        BigDecimal dataLimit = plan.getDataLimitGb();
        BigDecimal dataUsed = usage.getDataUsageGb();
        BigDecimal overData = dataUsed.subtract(dataLimit).max(BigDecimal.ZERO);
        // Simple overage: 2.00 per GB over limit
        BigDecimal overageCharge = overData.multiply(new BigDecimal("2.00")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal callCharge = usage.getCallMinutes().multiply(plan.getCallRatePerMin()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(overageCharge).add(callCharge).setScale(2, RoundingMode.HALF_UP);

        String breakdown = String.format("Base: %s | Calls: %s min x %s = %s | Data overage: %s GB x 2.00 = %s",
                base, usage.getCallMinutes(), plan.getCallRatePerMin(), callCharge, overData, overageCharge);

        Bill bill = new Bill();
        bill.setCustomerId(customerId);
        bill.setUsageRecordId(usage.getId());
        bill.setMonth(month);
        bill.setYear(year);
        bill.setTotalAmount(total);
        bill.setBillBreakdown(breakdown);
        usageBillDAO.addBill(bill);
        return bill;
    }

    public List<Bill> getBillingHistory(int customerId) throws SQLException {
        return usageBillDAO.findBillsByCustomer(customerId);
    }

    public List<Bill> getBillsForMonth(int month, int year) throws SQLException {
        return usageBillDAO.findBillsByMonthYear(month, year);
    }

    public List<Object[]> topDataUsageCustomers(int limit) throws SQLException {
        return usageBillDAO.topDataUsage(limit);
    }

    public Object[] monthlyBillingSummary(int month, int year) throws SQLException {
        return usageBillDAO.monthlySummary(month, year);
    }

    public Plan getPlan(int planId) throws SQLException {
        return planDAO.findById(planId);
    }
}
