package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.FoodOrder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodOrderDAO extends BaseDAO implements CRUDOperations<FoodOrder, Integer> {

    
    @Override
    public void insert(FoodOrder f) throws SQLException, InvalidDataException {
        String sql = "INSERT INTO FOOD_ORDER (ORDER_ID, SESSION_ID, ITEM_NAME, QUANTITY, PRICE) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, f.getOrderId());
            ps.setInt(2, f.getSessionId());
            ps.setString(3, f.getItemName());
            ps.setInt(4, f.getQuantity());
            ps.setDouble(5, f.getPrice());
            ps.executeUpdate();
        }
    }

    @Override
    public List<FoodOrder> getAll() throws SQLException {
        List<FoodOrder> list = new ArrayList<>();
        String sql = "SELECT ORDER_ID, SESSION_ID, ITEM_NAME, QUANTITY, PRICE FROM FOOD_ORDER";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new FoodOrder(rs.getInt("ORDER_ID"), rs.getInt("SESSION_ID"), rs.getString("ITEM_NAME"), rs.getInt("QUANTITY"), rs.getDouble("PRICE")));
            }
        }
        return list;
    }

    @Override
    public void update(FoodOrder f) throws SQLException, InvalidDataException {
        String sql = "UPDATE FOOD_ORDER SET SESSION_ID=?, ITEM_NAME=?, QUANTITY=?, PRICE=? WHERE ORDER_ID=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, f.getSessionId());
            ps.setString(2, f.getItemName());
            ps.setInt(3, f.getQuantity());
            ps.setDouble(4, f.getPrice());
            ps.setInt(5, f.getOrderId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws SQLException {
        String sql = "DELETE FROM FOOD_ORDER WHERE ORDER_ID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) { ps.setInt(1, id); ps.executeUpdate(); }
    }
}