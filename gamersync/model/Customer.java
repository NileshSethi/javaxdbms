package gamersync.model;

// CO1: Class with private fields, access specifiers, methods
// CO2: toString() overrides = polymorphism
public class Customer {
    private int    custId;
    private String name;
    private String phone;
    private String email;
    private String registeredDate;

    public Customer() {}

    public Customer(int custId, String name, String phone, String email, String registeredDate) {
        this.custId         = custId;
        this.name           = name;
        this.phone          = phone;
        this.email          = email;
        this.registeredDate = registeredDate;
    }

    // Getters
    public int    getCustId()         { return custId; }
    public String getName()           { return name; }
    public String getPhone()          { return phone; }
    public String getEmail()          { return email; }
    public String getRegisteredDate() { return registeredDate; }

    // Setters
    public void setCustId(int custId)                 { this.custId = custId; }
    public void setName(String name)                  { this.name = name; }
    public void setPhone(String phone)                { this.phone = phone; }
    public void setEmail(String email)                { this.email = email; }
    public void setRegisteredDate(String registeredDate) { this.registeredDate = registeredDate; }

    @Override // CO2: Polymorphism - method overriding
    public String toString() {
        return String.format("| %-4d | %-15s | %-12s | %-25s | %-12s |",
            custId, name, phone, email, registeredDate);
    }
}
