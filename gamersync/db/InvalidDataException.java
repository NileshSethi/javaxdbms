package gamersync.db;

// Custom checked exception — thrown when user enters invalid input
// Demonstrates: specific exception handling 
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
