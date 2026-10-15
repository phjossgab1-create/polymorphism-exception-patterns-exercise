package exception;

/**
 * Thrown when no parcel has the given tracking number.
 *
 * The message is the assignment's exact text: "Parcel not found!"
 *
 * INHERITANCE: extends the base ParcelException.
 */
public class ParcelNotFoundException extends ParcelException {

    private static final long serialVersionUID = 1L;

    public static final String MESSAGE = "Parcel not found!";

    // ENCAPSULATION: private field, read-only through a getter.
    private final String trackingNumber;

    public ParcelNotFoundException(String trackingNumber) {
        super(MESSAGE);
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
