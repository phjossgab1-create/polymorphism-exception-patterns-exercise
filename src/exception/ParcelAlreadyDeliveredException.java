package exception;

/**
 * Thrown when someone tries to change the delivery service of, or mark as
 * delivered, a parcel that is already delivered.
 *
 * Note: the assignment uses two different messages for this situation
 * ("Parcel already delivered!" in Change Delivery Service and
 * "Parcel was already delivered." in Mark as Delivered), so the menu chooses
 * the correct text for each operation when it catches this exception.
 *
 * INHERITANCE: extends the base ParcelException.
 */
public class ParcelAlreadyDeliveredException extends ParcelException {

    private static final long serialVersionUID = 1L;

    // ENCAPSULATION: private field, read-only through a getter.
    private final String trackingNumber;

    public ParcelAlreadyDeliveredException(String trackingNumber) {
        super("Parcel already delivered!");
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
