package gamersync.model;

public class Payment {
    private int paymentId;
    private int sessionId;
    private double amount;
    private String paymentMethod;
    private String paymentDate;

    public Payment() {}

    public Payment(int paymentId, int sessionId, double amount, String paymentMethod, String paymentDate) {
        this.paymentId = paymentId;
        this.sessionId = sessionId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
}
