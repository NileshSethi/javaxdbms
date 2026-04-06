package gamersync.db;

// Custom checked exception — thrown when user enters invalid input
// Demonstrates: specific exception handling (CO3 - 3 marks)
public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
}
