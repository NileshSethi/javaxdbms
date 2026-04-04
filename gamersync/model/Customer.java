package gamersync.model;

// Customer entity - maps to CUSTOMER table in GamerSync DB
public class Customer {
    private int custId;
    private String name;
    private String phone;
    private String email;
    private String registeredDate;

    // Constructor
    public Customer(int custId, String name, String phone, String email, String registeredDate) {
        this.custId = custId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.registeredDate = registeredDate;
    }

    // Default constructor
    public Customer() {}

    // Getters
    public int getCustId() { return custId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getRegisteredDate() { return registeredDate; }

    // Setters
    public void setCustId(int custId) { this.custId = custId; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setRegisteredDate(String registeredDate) { this.registeredDate = registeredDate; }

    // Overriding toString() - polymorphism
    @Override
    public String toString() {
        return "Customer[ID=" + custId + ", Name=" + name + ", Phone=" + phone + ", Email=" + email + "]";
    }
}
