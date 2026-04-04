package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.GamingSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// SessionDAO extends BaseDAO (Inheritance) and implements ISessionDAO (Interface)
public class SessionDAO extends BaseDAO implements ISessionDAO {

    // INSERT session
    @Override
    public void addSession(GamingSession s) throws SQLException, InvalidDataException {
        if (s.getDuration() <= 0) {
            throw new InvalidDataException("Session duration must be greater than 0.");
        }
        if (s.getGameName() == null || s.getGameName().trim().isEmpty()) {
            throw new InvalidDataException("Game name cannot be empty.");
        }
        String sql = "INSERT INTO GAMING_SESSION (SESSION_ID, START_TIME, END_TIME, DURATION, GAME_NAME, CUST_ID, PC_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
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

    // SELECT all sessions
    @Override
    public List<GamingSession> getAllSessions() throws SQLException {
        List<GamingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM GAMING_SESSION";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            GamingSession s = new GamingSession(
                rs.getInt("SESSION_ID"),
                rs.getString("START_TIME"),
                rs.getString("END_TIME"),
                rs.getInt("DURATION"),
                rs.getString("GAME_NAME"),
                rs.getInt("CUST_ID"),
                rs.getInt("PC_ID")
            );
            list.add(s);
        }
        rs.close();
        ps.close();
        return list;
    }

    // DELETE session
    @Override
    public void deleteSession(int sessionId) throws SQLException {
        // Delete dependent food_order records first (FK constraint)
        String deleteFoodSql = "DELETE FROM FOOD_ORDER WHERE SESSION_ID = ?";
        PreparedStatement ps1 = con.prepareStatement(deleteFoodSql);
        ps1.setInt(1, sessionId);
        ps1.executeUpdate();
        ps1.close();

        // Delete dependent payment records (FK constraint)
        String deletePaySql = "DELETE FROM PAYMENT WHERE SESSION_ID = ?";
        PreparedStatement ps2 = con.prepareStatement(deletePaySql);
        ps2.setInt(1, sessionId);
        ps2.executeUpdate();
        ps2.close();

        // Now delete the session
        String sql = "DELETE FROM GAMING_SESSION WHERE SESSION_ID = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ps.executeUpdate();
        ps.close();
    }
}
