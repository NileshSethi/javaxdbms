package gamersync.model;

public class FoodOrder {
    private int orderId;
    private int sessionId;
    private String itemName;
    private int quantity;
    private double price;

    public FoodOrder() {}
    public FoodOrder(int orderId, int sessionId, String itemName, int quantity, double price) {
        this.orderId = orderId; this.sessionId = sessionId; this.itemName = itemName; this.quantity = quantity; this.price = price;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}