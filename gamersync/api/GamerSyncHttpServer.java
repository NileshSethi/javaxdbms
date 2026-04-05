package gamersync.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import gamersync.dao.CustomerDAO;
import gamersync.dao.FoodOrderDAO;
import gamersync.dao.PaymentDAO;
import gamersync.dao.SessionDAO;
import gamersync.db.InvalidDataException;
import gamersync.model.Customer;
import gamersync.model.FoodOrder;
import gamersync.model.GamingSession;
import gamersync.model.Payment;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class GamerSyncHttpServer {

    private final CustomerDAO customerDAO;
    private final SessionDAO sessionDAO;
    private final PaymentDAO paymentDAO;
    private final FoodOrderDAO foodOrderDAO; 

    public GamerSyncHttpServer() {
        this.customerDAO = new CustomerDAO();
        this.sessionDAO = new SessionDAO();
        this.paymentDAO = new PaymentDAO();
        this.foodOrderDAO = new FoodOrderDAO(); 
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("GAMERSYNC_API_PORT", "8080"));
        try {
            new GamerSyncHttpServer().start(port);
        } catch (IOException e) {
            System.out.println("Failed to start API server: " + e.getMessage());
        }
        
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/foodorder/add", this::handleAddFoodOrder);
        server.createContext("/foodorder/view", this::handleViewFoodOrder);
        server.createContext("/foodorder/update", this::handleUpdateFoodOrder);
        server.createContext("/foodorder/delete", this::handleDeleteFoodOrder);

        server.createContext("/health", this::handleHealth);
        server.createContext("/customer/add", this::handleAddCustomer);
        server.createContext("/customer/view", this::handleViewCustomer);
        server.createContext("/customer/update", this::handleUpdateCustomer);
        
        server.createContext("/session/add", this::handleAddSession);
        server.createContext("/session/view", this::handleViewSession);
        server.createContext("/session/update", this::handleUpdateSession);
        server.createContext("/session/delete", this::handleDeleteSession);

        server.createContext("/payment/add", this::handleAddPayment);
        server.createContext("/payment/view", this::handleViewPayment);
        server.createContext("/payment/update", this::handleUpdatePayment);
        server.createContext("/payment/delete", this::handleDeletePayment);

       
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("GamerSync API started on http://localhost:" + port);
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) {
            return;
        }
        sendJson(exchange, 200, "{\"success\":true,\"message\":\"API is running\"}");
    }

    private void handleAddCustomer(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) {
            return;
        }
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> params = readParams(exchange);

        try {
            Customer customer = buildCustomerFromParams(params);
            customerDAO.addCustomer(customer);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Customer added successfully\",\"custId\":" + customer.getCustId() + "}");
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"custId must be a number\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (InvalidDataException e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleViewCustomer(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) {
            return;
        }
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> params = readParams(exchange);

        try {
            String idRaw = params.get("custId");
            if (idRaw != null && !idRaw.isBlank()) {
                int id = Integer.parseInt(idRaw);
                Customer customer = customerDAO.getCustomerById(id);
                if (customer == null) {
                    sendJson(exchange, 404, "{\"success\":false,\"message\":\"Customer not found\"}");
                    return;
                }
                sendJson(exchange, 200, "{\"success\":true,\"data\":" + customerToJson(customer) + "}");
                return;
            }

            List<Customer> customers = customerDAO.getAllCustomers();
            sendJson(exchange, 200, "{\"success\":true,\"count\":" + customers.size() + ",\"data\":" + customerListToJson(customers) + "}");
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"custId must be a number\"}");
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleUpdateCustomer(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) {
            return;
        }
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> params = readParams(exchange);

        try {
            Customer customer = buildCustomerFromParams(params);
            customerDAO.updateCustomer(customer);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Customer updated successfully\",\"custId\":" + customer.getCustId() + "}");
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"custId must be a number\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (InvalidDataException e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleDeleteCustomer(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) {
            return;
        }
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        Map<String, String> params = readParams(exchange);

        try {
            String idRaw = params.get("custId");
            if (idRaw == null || idRaw.isBlank()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"custId is required\"}");
                return;
            }

            int id = Integer.parseInt(idRaw);
            customerDAO.deleteCustomer(id);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Customer deleted successfully\",\"custId\":" + id + "}");
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"custId must be a number\"}");
        } catch (SQLException e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleAddSession(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> params = readParams(exchange);
        try {
            GamingSession session = buildSessionFromParams(params);
            sessionDAO.addSession(session);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Session added successfully\",\"sessionId\":" + session.getSessionId() + "}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleViewSession(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        try {
            List<GamingSession> sessions = sessionDAO.getAllSessions();
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + sessionListToJson(sessions) + "}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleUpdateSession(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> params = readParams(exchange);
        try {
            GamingSession session = buildSessionFromParams(params);
            sessionDAO.updateSession(session);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Session updated successfully\",\"sessionId\":" + session.getSessionId() + "}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleDeleteSession(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> params = readParams(exchange);
        try {
            String idRaw = params.get("sessionId");
            if (idRaw == null || idRaw.isBlank()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"sessionId is required\"}");
                return;
            }
            int id = Integer.parseInt(idRaw);
            sessionDAO.deleteSession(id);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Session deleted successfully\",\"sessionId\":" + id + "}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleAddPayment(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> params = readParams(exchange);
        try {
            Payment payment = buildPaymentFromParams(params);
            paymentDAO.insert(payment);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Payment added successfully\",\"paymentId\":" + payment.getPaymentId() + "}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleViewPayment(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        try {
            List<Payment> payments = paymentDAO.getAll();
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + paymentListToJson(payments) + "}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleUpdatePayment(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> params = readParams(exchange);
        try {
            Payment payment = buildPaymentFromParams(params);
            paymentDAO.update(payment);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Payment updated successfully\",\"paymentId\":" + payment.getPaymentId() + "}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleDeletePayment(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> params = readParams(exchange);
        try {
            String idRaw = params.get("paymentId");
            if (idRaw == null || idRaw.isBlank()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"paymentId is required\"}");
                return;
            }
            int id = Integer.parseInt(idRaw);
            paymentDAO.delete(id);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Payment deleted successfully\",\"paymentId\":" + id + "}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private Customer buildCustomerFromParams(Map<String, String> params) {
        int custId = Integer.parseInt(requiredParam(params, "custId"));
        String name = requiredParam(params, "name");
        String phone = requiredParam(params, "phone");
        String email = requiredParam(params, "email");
        String registeredDate = requiredParam(params, "registeredDate");
        return new Customer(custId, name, phone, email, registeredDate);
    }

    private GamingSession buildSessionFromParams(Map<String, String> params) {
        int sessionId = Integer.parseInt(requiredParam(params, "sessionId"));
        String startTime = requiredParam(params, "startTime");
        String endTime = requiredParam(params, "endTime");
        int duration = Integer.parseInt(requiredParam(params, "duration"));
        String gameName = requiredParam(params, "gameName");
        int custId = Integer.parseInt(requiredParam(params, "custId"));
        int pcId = Integer.parseInt(requiredParam(params, "pcId"));
        return new GamingSession(sessionId, startTime, endTime, duration, gameName, custId, pcId);
    }

    private Payment buildPaymentFromParams(Map<String, String> params) {
        int paymentId = Integer.parseInt(requiredParam(params, "paymentId"));
        int sessionId = Integer.parseInt(requiredParam(params, "sessionId"));
        double amount = Double.parseDouble(requiredParam(params, "amount"));
        String paymentMethod = requiredParam(params, "paymentMethod");
        String paymentDate = requiredParam(params, "paymentDate");
        return new Payment(paymentId, sessionId, amount, paymentMethod, paymentDate);
    }

    private String requiredParam(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value.trim();
    }

    private boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private boolean isAllowedMethod(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        return "GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method);
    }

    private Map<String, String> readParams(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();

        String query = exchange.getRequestURI().getRawQuery();
        if (query != null && !query.isBlank()) {
            params.putAll(parseUrlEncoded(query));
        }

        byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
        if (bodyBytes.length > 0) {
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            params.putAll(parseUrlEncoded(body));
        }

        return params;
    }

    private Map<String, String> parseUrlEncoded(String raw) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = raw.split("&");
        for (String pair : pairs) {
            if (pair == null || pair.isBlank()) {
                continue;
            }
            String[] kv = pair.split("=", 2);
            String key = urlDecode(kv[0]);
            String value = kv.length > 1 ? urlDecode(kv[1]) : "";
            params.put(key, value);
        }
        return params;
    }

    private String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private String customerListToJson(List<Customer> customers) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < customers.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(customerToJson(customers.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String sessionListToJson(List<GamingSession> sessions) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < sessions.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(sessionToJson(sessions.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String sessionToJson(GamingSession s) {
        return "{"
            + "\"sessionId\":" + s.getSessionId() + ","
            + "\"startTime\":\"" + escapeJson(s.getStartTime()) + "\","
            + "\"endTime\":\"" + escapeJson(s.getEndTime()) + "\","
            + "\"duration\":" + s.getDuration() + ","
            + "\"gameName\":\"" + escapeJson(s.getGameName()) + "\","
            + "\"custId\":" + s.getCustId() + ","
            + "\"pcId\":" + s.getPcId()
            + "}";
    }

        private String customerToJson(Customer c) {
        return "{"
            + "\"custId\":" + c.getCustId() + ","
            + "\"name\":\"" + escapeJson(c.getName()) + "\","
            + "\"phone\":\"" + escapeJson(c.getPhone()) + "\","
            + "\"email\":\"" + escapeJson(c.getEmail()) + "\","
            + "\"registeredDate\":\"" + escapeJson(c.getRegisteredDate()) + "\""
            + "}";
    }

    private String paymentListToJson(List<Payment> payments) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < payments.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(paymentToJson(payments.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String paymentToJson(Payment p) {
        return "{"
            + "\"paymentId\":" + p.getPaymentId() + ","
            + "\"sessionId\":" + p.getSessionId() + ","
            + "\"amount\":" + p.getAmount() + ","
            + "\"paymentMethod\":\"" + escapeJson(p.getPaymentMethod()) + "\","
            + "\"paymentDate\":\"" + escapeJson(p.getPaymentDate()) + "\""
            + "}";
    }
        private String foodOrderListToJson(List<FoodOrder> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(foodOrderToJson(orders.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String foodOrderToJson(FoodOrder f) {
        return "{"
            + "\"orderId\":" + f.getOrderId() + ","
            + "\"sessionId\":" + f.getSessionId() + ","
            + "\"itemName\":\"" + escapeJson(f.getItemName()) + "\","
            + "\"quantity\":" + f.getQuantity() + ","
            + "\"price\":" + f.getPrice()
            + "}";
    }
            private void handleAddFoodOrder(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> params = readParams(exchange);
        try {
            FoodOrder f = new FoodOrder(
                    Integer.parseInt(requiredParam(params, "orderId")),
                    Integer.parseInt(requiredParam(params, "sessionId")),
                    requiredParam(params, "itemName"),
                    Integer.parseInt(requiredParam(params, "quantity")),
                    Double.parseDouble(requiredParam(params, "price"))
            );
            foodOrderDAO.insert(f);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Food order added successfully\"}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleViewFoodOrder(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        try {
            List<FoodOrder> orders = foodOrderDAO.getAll();
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + foodOrderListToJson(orders) + "}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleUpdateFoodOrder(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> params = readParams(exchange);
        try {
            FoodOrder f = new FoodOrder(
                    Integer.parseInt(requiredParam(params, "orderId")),
                    Integer.parseInt(requiredParam(params, "sessionId")),
                    requiredParam(params, "itemName"),
                    Integer.parseInt(requiredParam(params, "quantity")),
                    Double.parseDouble(requiredParam(params, "price"))
            );
            foodOrderDAO.update(f);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Food order updated successfully\"}");
        } catch (Exception e) {
            sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleDeleteFoodOrder(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!isAllowedMethod(exchange)) {
            sendJson(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }
        Map<String, String> params = readParams(exchange);
        try {
            String idRaw = params.get("orderId");
            if (idRaw == null || idRaw.isBlank()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"orderId is required\"}");
                return;
            }
            int orderId = Integer.parseInt(idRaw);
            foodOrderDAO.delete(orderId);
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Food order deleted successfully\"}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
