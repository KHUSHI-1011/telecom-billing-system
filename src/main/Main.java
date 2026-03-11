package main;

import model.Bill;
import model.Customer;
import model.Plan;
import service.TelecomService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Console entry point: menu-driven interface for the Telecom Billing System.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TelecomService service = new TelecomService();

    public static void main(String[] args) {
        System.out.println("=== Telecom Billing System ===\n");
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            if (choice.isEmpty()) continue;
            try {
                switch (choice) {
                    case "1" -> addCustomer();
                    case "2" -> viewCustomers();
                    case "3" -> createPlan();
                    case "4" -> assignPlan();
                    case "5" -> recordUsage();
                    case "6" -> generateBill();
                    case "7" -> viewBillingHistory();
                    case "8" -> {
                        System.out.println("Goodbye.");
                        return;
                    }
                    default -> System.out.println("Invalid option. Enter 1-8.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("1. Add Customer");
        System.out.println("2. View Customers");
        System.out.println("3. Create Plan");
        System.out.println("4. Assign Plan");
        System.out.println("5. Record Usage");
        System.out.println("6. Generate Bill");
        System.out.println("7. View Billing History");
        System.out.println("8. Exit");
        System.out.print("Choice: ");
    }

    private static void addCustomer() throws SQLException {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        int id = service.addCustomer(name, phone, email);
        System.out.println("Customer added with ID: " + id);
    }

    private static void viewCustomers() throws SQLException {
        List<Customer> list = service.listAllCustomers();
        if (list.isEmpty()) {
            System.out.println("No customers.");
            return;
        }
        System.out.println("--- All customers ---");
        for (Customer c : list) {
            System.out.println("ID: " + c.getId() + " | " + c.getName() + " | " + c.getPhone() + " | " + c.getEmail() + " | Plan ID: " + c.getPlanId());
        }
        // View details / Update / Delete
        System.out.print("Enter customer ID for details (or 0 to skip): ");
        String idStr = scanner.nextLine().trim();
        if (!idStr.equals("0") && !idStr.isEmpty()) {
            int id = Integer.parseInt(idStr);
            Customer c = service.viewCustomer(id);
            if (c == null) {
                System.out.println("Customer not found.");
                return;
            }
            System.out.println("--- Customer details ---");
            System.out.println(c);
            System.out.print("Update (u) / Delete (d) / Skip (Enter): ");
            String action = scanner.nextLine().trim().toLowerCase();
            if (action.equals("d")) {
                if (service.deleteCustomer(id)) System.out.println("Customer deleted.");
                else System.out.println("Delete failed.");
            } else if (action.equals("u")) {
                System.out.print("New name (Enter to keep): ");
                String name = scanner.nextLine().trim();
                if (name.isEmpty()) name = c.getName();
                System.out.print("New phone (Enter to keep): ");
                String phone = scanner.nextLine().trim();
                if (phone.isEmpty()) phone = c.getPhone();
                System.out.print("New email (Enter to keep): ");
                String email = scanner.nextLine().trim();
                if (email.isEmpty()) email = c.getEmail();
                if (service.updateCustomer(id, name, phone, email)) System.out.println("Customer updated.");
                else System.out.println("Update failed.");
            }
        }
        // Top data usage
        List<Object[]> top = service.topDataUsageCustomers(5);
        if (!top.isEmpty()) {
            System.out.println("\n--- Top 5 by data usage ---");
            for (Object[] row : top) {
                System.out.println("ID: " + row[0] + " " + row[1] + " | " + row[2] + " | Total data: " + row[3] + " GB");
            }
        }
    }

    private static void createPlan() throws SQLException {
        System.out.print("Plan name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Monthly price: ");
        BigDecimal price = new BigDecimal(scanner.nextLine().trim());
        System.out.print("Data limit (GB): ");
        BigDecimal dataLimit = new BigDecimal(scanner.nextLine().trim());
        System.out.print("Call rate per minute: ");
        BigDecimal callRate = new BigDecimal(scanner.nextLine().trim());
        int id = service.createPlan(name, price, dataLimit, callRate);
        System.out.println("Plan created with ID: " + id);
    }

    private static void assignPlan() throws SQLException {
        System.out.print("Customer ID: ");
        int custId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Plan ID: ");
        int planId = Integer.parseInt(scanner.nextLine().trim());
        if (service.assignPlanToCustomer(custId, planId))
            System.out.println("Plan assigned.");
        else
            System.out.println("Failed (check customer ID and plan ID).");
    }

    private static void recordUsage() throws SQLException {
        System.out.print("Customer ID: ");
        int customerId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Year: ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Call minutes to add: ");
        BigDecimal callMin = new BigDecimal(scanner.nextLine().trim());
        System.out.print("SMS count to add: ");
        int sms = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Data usage (GB) to add: ");
        BigDecimal dataGb = new BigDecimal(scanner.nextLine().trim());
        service.recordUsage(customerId, month, year, callMin, sms, dataGb);
        System.out.println("Usage recorded.");
    }

    private static void generateBill() throws SQLException {
        System.out.print("Customer ID: ");
        int customerId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Year: ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        Bill bill = service.generateBill(customerId, month, year);
        System.out.println("Bill generated: " + bill.getTotalAmount());
        System.out.println("Breakdown: " + bill.getBillBreakdown());
    }

    private static void viewBillingHistory() throws SQLException {
        System.out.println("1) By customer  2) By month (summary + list)");
        String sub = scanner.nextLine().trim();
        if ("1".equals(sub)) {
            System.out.print("Customer ID: ");
            int customerId = Integer.parseInt(scanner.nextLine().trim());
            List<Bill> bills = service.getBillingHistory(customerId);
            if (bills.isEmpty()) {
                System.out.println("No bills for this customer.");
                return;
            }
            for (Bill b : bills) {
                System.out.println(b.getMonth() + "/" + b.getYear() + " -> " + b.getTotalAmount() + " | " + b.getBillBreakdown());
            }
        } else if ("2".equals(sub)) {
            System.out.print("Month (1-12): ");
            int month = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Year: ");
            int year = Integer.parseInt(scanner.nextLine().trim());
            Object[] summary = service.monthlyBillingSummary(month, year);
            System.out.println("Bills count: " + summary[0] + " | Total revenue: " + summary[1]);
            List<Bill> bills = service.getBillsForMonth(month, year);
            for (Bill b : bills) {
                System.out.println("  Customer " + b.getCustomerId() + " -> " + b.getTotalAmount());
            }
        } else {
            System.out.println("Invalid option.");
        }
    }
}
