package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.GamingSession;
import java.sql.*;
import java.util.*;

// CO2: extends BaseDAO (Inheritance) + implements ISessionDAO (Interface)
// CO4: DML + DRL
public class SessionDAO extends BaseDAO implements ISessionDAO {

    // ── INSERT (DML) ─────────────────────────────────────────────────────────
    @Override
    public void addSession(GamingSession s) throws SQLException, InvalidDataException {
        if (s.getGameName() == null || s.getGameName().trim().isEmpty())
            throw new InvalidDataException("Game name cannot be empty.");
        if (s.getDuration() <= 0)
            throw new InvalidDataException("Duration must be greater than 0 minutes.");

        String sql = "INSERT INTO GAMING_SESSION (SESSION_ID,START_TIME,END_TIME,DURATION,GAME_NAME,CUST_ID,PC_ID) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, s.getSessionId());
        ps.setString(2, s.getStartTime());
        ps.setString(3, s.getEndTime());
        ps.setInt(4, s.getDuration());
        ps.setString(5, s.getGameName());
        ps.setInt(6, s.getCustId());
        ps.setInt(7, s.getPcId());
        ps.executeUpdate();
        ps.close();
    }

    // ── SELECT ALL (DRL) ─────────────────────────────────────────────────────
    @Override
    public List<GamingSession> getAllSessions() throws SQLException {
        List<GamingSession> list = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM GAMING_SESSION");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new GamingSession(
                rs.getInt("SESSION_ID"), rs.getString("START_TIME"),
                rs.getString("END_TIME"), rs.getInt("DURATION"),
                rs.getString("GAME_NAME"), rs.getInt("CUST_ID"), rs.getInt("PC_ID")
            ));
        }
        rs.close(); ps.close();
        return list;
    }

    @Override
    public GamingSession getSessionById(int sessionId) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM GAMING_SESSION WHERE SESSION_ID = ?");
        ps.setInt(1, sessionId);
        ResultSet rs = ps.executeQuery();
        GamingSession s = null;
        if (rs.next()) {
            s = new GamingSession(
                rs.getInt("SESSION_ID"), rs.getString("START_TIME"),
                rs.getString("END_TIME"), rs.getInt("DURATION"),
                rs.getString("GAME_NAME"), rs.getInt("CUST_ID"), rs.getInt("PC_ID")
            );
        }
        rs.close(); ps.close();
        return s;
    }

    // ── UPDATE (DML) ─────────────────────────────────────────────────────────
    @Override
    public void updateSession(GamingSession s) throws SQLException, InvalidDataException {
        String sql = "UPDATE GAMING_SESSION SET GAME_NAME=?, START_TIME=?, END_TIME=?, DURATION=?, CUST_ID=?, PC_ID=? WHERE SESSION_ID=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, s.getGameName());
        ps.setString(2, s.getStartTime());
        ps.setString(3, s.getEndTime());
        ps.setInt(4, s.getDuration());
        ps.setInt(5, s.getCustId());
        ps.setInt(6, s.getPcId());
        ps.setInt(7, s.getSessionId());
        int rows = ps.executeUpdate();
        ps.close();
        if (rows == 0) throw new InvalidDataException("No session found with ID: " + s.getSessionId());
    }

    // ── DELETE (DML) ─────────────────────────────────────────────────────────
    @Override
    public void deleteSession(int sessionId) throws SQLException {
        // Remove FK-dependent rows first
        PreparedStatement p1 = con.prepareStatement("DELETE FROM FOOD_ORDER WHERE SESSION_ID=?");
        p1.setInt(1, sessionId); p1.executeUpdate(); p1.close();

        PreparedStatement p2 = con.prepareStatement("DELETE FROM PAYMENT WHERE SESSION_ID=?");
        p2.setInt(1, sessionId); p2.executeUpdate(); p2.close();

        PreparedStatement ps = con.prepareStatement("DELETE FROM GAMING_SESSION WHERE SESSION_ID=?");
        ps.setInt(1, sessionId); ps.executeUpdate(); ps.close();
    }
}
