package exception;

/**
 * Thrown when a parcel field has an invalid value
 * (bad tracking number format, empty name, weight over the maximum, etc.).
 *
 * EXCEPTION HANDLING: custom checked exception that carries extra context -
 * the name of the field that failed - just like the lesson's
 * InsufficientFundsException carries the amount.
 *
 * INHERITANCE: extends the base ParcelException.
 */
public class InvalidParcelException extends ParcelException {

    private static final long serialVersionUID = 1L;

    // ENCAPSULATION: private field, read-only through a getter.
    private final String fieldName;

    // POLYMORPHISM (static): overloaded constructors.

    /** @param fieldName the field that failed validation, e.g. "Weight" */
    public InvalidParcelException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    /**
     * Used when we catch a lower-level exception (e.g. NumberFormatException)
     * and wrap it, keeping the original as the cause.
     */
    public InvalidParcelException(String fieldName, String message, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
