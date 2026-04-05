package gamersync.dao;

import gamersync.db.InvalidDataException;
import java.sql.SQLException;
import java.util.List;

// Generic CRUD contract used by all DAOs
public interface CRUDOperations<T, ID> {
    void insert(T entity) throws SQLException, InvalidDataException;
    void update(T entity) throws SQLException, InvalidDataException;
    void delete(ID id) throws SQLException;
    List<T> getAll() throws SQLException;
}