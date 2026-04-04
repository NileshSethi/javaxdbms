package gamersync.dao;

import gamersync.db.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

// Abstract base class - CustomerDAO and SessionDAO extend this (Inheritance)
public abstract class BaseDAO {
    protected Connection con; // shared by all child DAOs

    public BaseDAO() {
        try {
            this.con = DBConnection.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Database connection failed. Check DBConnection settings and MySQL server.", e);
        }
    }
}
