package gamersync.db;

// Custom checked exception for input validation
// Thrown when user enters invalid data (empty name, negative amount, etc.)
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
