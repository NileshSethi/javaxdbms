package gamersync.db;

public class ValidationHelper {

    // Phone: exactly 10 digits, no letters, no spaces
    public static void validatePhone(String phone) throws InvalidDataException {
        if (phone == null || !phone.matches("\\d{10}"))
            throw new InvalidDataException(
                "Phone must be exactly 10 digits (numbers only). Got: " + phone);
    }

    // Email: must contain @ and a dot after @
    public static void validateEmail(String email) throws InvalidDataException {
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
            throw new InvalidDataException(
                "Invalid email format. Example: name@example.com. Got: " + email);
    }

    // Date: must be YYYY-MM-DD
    public static void validateDate(String date, String fieldName) throws InvalidDataException {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}"))
            throw new InvalidDataException(
                fieldName + " must be in format YYYY-MM-DD. Got: " + date);
    }

    // DateTime: must be YYYY-MM-DD HH:MM:SS
    public static void validateDateTime(String dt, String fieldName) throws InvalidDataException {
        if (dt == null || !dt.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"))
            throw new InvalidDataException(
                fieldName + " must be in format YYYY-MM-DD HH:MM:SS. Got: " + dt);
    }

    // Amount/Fee: must be > 0
    public static void validatePositiveAmount(double amount, String fieldName)
            throws InvalidDataException {
        if (amount <= 0)
            throw new InvalidDataException(
                fieldName + " must be greater than 0. Got: " + amount);
    }

    // Integer ID: must be > 0
    public static void validatePositiveInt(int value, String fieldName)
            throws InvalidDataException {
        if (value <= 0)
            throw new InvalidDataException(
                fieldName + " must be a positive number. Got: " + value);
    }

    // Not empty string
    public static void validateNotEmpty(String value, String fieldName)
            throws InvalidDataException {
        if (value == null || value.trim().isEmpty())
            throw new InvalidDataException(fieldName + " cannot be empty.");
    }

    // Payment mode: only UPI, Cash, Card
    public static void validatePaymentMode(String mode) throws InvalidDataException {
        if (mode == null || (!mode.equals("UPI") && !mode.equals("Cash") && !mode.equals("Card")))
            throw new InvalidDataException(
                "Payment mode must be UPI, Cash, or Card. Got: " + mode);
    }

    // Membership type: only Hourly, Weekly, Monthly
    public static void validateMembershipType(String type) throws InvalidDataException {
        if (type == null || (!type.equals("Hourly") && !type.equals("Weekly") && !type.equals("Monthly")))
            throw new InvalidDataException(
                "Membership type must be Hourly, Weekly, or Monthly. Got: " + type);
    }

    // PC Status: only Available, In Use, Maintenance
    public static void validatePCStatus(String status) throws InvalidDataException {
        if (status == null || (!status.equals("Available") && !status.equals("In Use") && !status.equals("Maintenance")))
            throw new InvalidDataException(
                "PC status must be Available, In Use, or Maintenance. Got: " + status);
    }

    // Rank: only Bronze, Silver, Gold, Platinum, Diamond
    public static void validateRank(String rank) throws InvalidDataException {
        if (rank == null || rank.trim().isEmpty())
            throw new InvalidDataException("Rank cannot be empty.");
        String[] valid = {"Bronze", "Silver", "Gold", "Platinum", "Diamond"};
        for (String v : valid) if (v.equalsIgnoreCase(rank)) return;
        throw new InvalidDataException(
            "Rank must be Bronze, Silver, Gold, Platinum, or Diamond. Got: " + rank);
    }

    // Duration: must be between 1 and 720 minutes (12 hours max)
    public static void validateDuration(int duration) throws InvalidDataException {
        if (duration <= 0 || duration > 720)
            throw new InvalidDataException(
                "Duration must be between 1 and 720 minutes. Got: " + duration);
    }
}
