package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.GamingSession;
import java.sql.SQLException;
import java.util.List;

// CO2: Interface — defines contract for Session operations
public interface ISessionDAO {
    void               addSession(GamingSession s) throws SQLException, InvalidDataException;
    List<GamingSession> getAllSessions()            throws SQLException;
    void               deleteSession(int sessionId) throws SQLException;
}
