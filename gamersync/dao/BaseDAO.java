package gamersync.dao;

import gamersync.db.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDAO {
    protected Connection con;

    public BaseDAO() {
        try {
            this.con = DBConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("[ERROR] DB connection failed: " + e.getMessage());
        }
    }
}
