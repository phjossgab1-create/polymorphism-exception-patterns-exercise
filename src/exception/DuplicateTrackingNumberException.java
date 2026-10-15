package exception;

/**
 * Thrown when a tracking number is already used by another parcel.
 *
 * EXCEPTION HANDLING: custom checked exception that carries the duplicate
 * tracking number as extra context.
 *
 * INHERITANCE: extends the base ParcelException.
 */
public class DuplicateTrackingNumberException extends ParcelException {

    private static final long serialVersionUID = 1L;

    // ENCAPSULATION: private field, read-only through a getter.
    private final String trackingNumber;

    public DuplicateTrackingNumberException(String trackingNumber) {
        super("Tracking number " + trackingNumber + " already exists.");
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
