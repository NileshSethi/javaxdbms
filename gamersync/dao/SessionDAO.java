package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.GamingSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// SessionDAO extends BaseDAO (Inheritance) and implements ISessionDAO (Interface)
public class SessionDAO extends BaseDAO implements ISessionDAO {

    // INSERT session via generic CRUD interface
    @Override
    public void insert(GamingSession s) throws SQLException, InvalidDataException {
        validateSession(s);
        String sql = "INSERT INTO GAMING_SESSION (SESSION_ID, START_TIME, END_TIME, DURATION, GAME_NAME, CUST_ID, PC_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, s.getSessionId());
            ps.setString(2, s.getStartTime());
            ps.setString(3, s.getEndTime());
            ps.setInt(4, s.getDuration());
            ps.setString(5, s.getGameName());
            ps.setInt(6, s.getCustId());
            ps.setInt(7, s.getPcId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No session row inserted for ID: " + s.getSessionId());
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to insert gaming session: " + e.getMessage(), e);
        }
    }

    // SELECT all sessions via generic CRUD interface
    @Override
    public List<GamingSession> getAll() throws SQLException {
        List<GamingSession> list = new ArrayList<>();
        String sql = "SELECT SESSION_ID, START_TIME, END_TIME, DURATION, GAME_NAME, CUST_ID, PC_ID FROM GAMING_SESSION";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapSession(rs));
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to fetch gaming sessions: " + e.getMessage(), e);
        }

        return list;
    }

    // UPDATE session via generic CRUD interface
    @Override
    public void update(GamingSession s) throws SQLException, InvalidDataException {
        validateSession(s);
        String sql = "UPDATE GAMING_SESSION SET START_TIME=?, END_TIME=?, DURATION=?, GAME_NAME=?, CUST_ID=?, PC_ID=? WHERE SESSION_ID=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getStartTime());
            ps.setString(2, s.getEndTime());
            ps.setInt(3, s.getDuration());
            ps.setString(4, s.getGameName());
            ps.setInt(5, s.getCustId());
            ps.setInt(6, s.getPcId());
            ps.setInt(7, s.getSessionId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No session row updated for ID: " + s.getSessionId());
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to update gaming session: " + e.getMessage(), e);
        }
    }

    // DELETE session via generic CRUD interface
    @Override
    public void delete(Integer sessionId) throws SQLException {
        if (sessionId == null) {
            throw new SQLException("Session ID cannot be null for delete.");
        }

        boolean previousAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            // Delete dependent food_order records first (FK constraint)
            String deleteFoodSql = "DELETE FROM FOOD_ORDER WHERE SESSION_ID = ?";
            try (PreparedStatement ps1 = con.prepareStatement(deleteFoodSql)) {
                ps1.setInt(1, sessionId);
                ps1.executeUpdate();
            }

            // Delete dependent payment records (FK constraint)
            String deletePaySql = "DELETE FROM PAYMENT WHERE SESSION_ID = ?";
            try (PreparedStatement ps2 = con.prepareStatement(deletePaySql)) {
                ps2.setInt(1, sessionId);
                ps2.executeUpdate();
            }

            // Now delete the session
            String sql = "DELETE FROM GAMING_SESSION WHERE SESSION_ID = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("No session row deleted for ID: " + sessionId);
                }
            }

            con.commit();
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                throw new SQLException("Failed to delete session and rollback failed: " + rollbackEx.getMessage(), rollbackEx);
            }
            throw new SQLException("Failed to delete gaming session: " + e.getMessage(), e);
        } finally {
            con.setAutoCommit(previousAutoCommit);
        }
    }

    // Alias methods retained for existing UI/service calls
    @Override
    public void addSession(GamingSession s) throws SQLException, InvalidDataException {
        insert(s);
    }

    @Override
    public List<GamingSession> getAllSessions() throws SQLException {
        return getAll();
    }

    @Override
    public void updateSession(GamingSession s) throws SQLException, InvalidDataException {
        update(s);
    }

    @Override
    public void deleteSession(int sessionId) throws SQLException {
        delete(sessionId);
    }

    private void validateSession(GamingSession s) throws InvalidDataException {
        if (s == null) {
            throw new InvalidDataException("Session object cannot be null.");
        }
        if (s.getSessionId() <= 0) {
            throw new InvalidDataException("Session ID must be greater than 0.");
        }
        if (s.getDuration() <= 0) {
            throw new InvalidDataException("Session duration must be greater than 0.");
        }
        if (s.getGameName() == null || s.getGameName().trim().isEmpty()) {
            throw new InvalidDataException("Game name cannot be empty.");
        }
    }

    private GamingSession mapSession(ResultSet rs) throws SQLException {
        return new GamingSession(
            rs.getInt("SESSION_ID"),
            rs.getString("START_TIME"),
            rs.getString("END_TIME"),
            rs.getInt("DURATION"),
            rs.getString("GAME_NAME"),
            rs.getInt("CUST_ID"),
            rs.getInt("PC_ID")
        );
    }
}
