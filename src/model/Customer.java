package model;

/**
 * Represents a telecom customer. planId may be null until a plan is assigned.
 */
public class Customer {
    private int id;
    private String name;
    private String phone;
    private String email;
    private Integer planId;

    public Customer() {}

    public Customer(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    @Override
    public String toString() {
        return String.format("Customer[id=%d, name=%s, phone=%s, email=%s, planId=%s]",
                id, name, phone, email, planId);
    }
}
