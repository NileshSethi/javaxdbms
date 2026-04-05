package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO extends BaseDAO implements CRUDOperations<Payment, Integer> {

    @Override
    public void insert(Payment p) throws SQLException, InvalidDataException {
        String sql = "INSERT INTO PAYMENT (PAYMENT_ID, SESSION_ID, AMOUNT, PAYMENT_METHOD, PAYMENT_DATE) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getPaymentId());
            ps.setInt(2, p.getSessionId());
            ps.setDouble(3, p.getAmount());
            ps.setString(4, p.getPaymentMethod());
            ps.setString(5, p.getPaymentDate());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Payment> getAll() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT PAYMENT_ID, SESSION_ID, AMOUNT, PAYMENT_METHOD, PAYMENT_DATE FROM PAYMENT";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Payment(
                    rs.getInt("PAYMENT_ID"),
                    rs.getInt("SESSION_ID"),
                    rs.getDouble("AMOUNT"),
                    rs.getString("PAYMENT_METHOD"),
                    rs.getString("PAYMENT_DATE")
                ));
            }
        }
        return list;
    }

    @Override
    public void update(Payment p) throws SQLException, InvalidDataException {
        String sql = "UPDATE PAYMENT SET SESSION_ID=?, AMOUNT=?, PAYMENT_METHOD=?, PAYMENT_DATE=? WHERE PAYMENT_ID=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getSessionId());
            ps.setDouble(2, p.getAmount());
            ps.setString(3, p.getPaymentMethod());
            ps.setString(4, p.getPaymentDate());
            ps.setInt(5, p.getPaymentId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM PAYMENT WHERE PAYMENT_ID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
