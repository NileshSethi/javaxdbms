package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.GamingSession;
import java.sql.SQLException;
import java.util.List;

// Interface defining the contract for all Gaming Session DB operations
public interface ISessionDAO extends CRUDOperations<GamingSession, Integer> {
    void addSession(GamingSession s) throws SQLException, InvalidDataException;
    List<GamingSession> getAllSessions() throws SQLException;
    void updateSession(GamingSession s) throws SQLException, InvalidDataException;
    void deleteSession(int sessionId) throws SQLException;
}
