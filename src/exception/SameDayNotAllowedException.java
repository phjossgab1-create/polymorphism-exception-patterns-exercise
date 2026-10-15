package exception;

/**
 * Thrown when Same Day delivery is chosen for a parcel whose chargeable
 * weight is more than 10 kg.
 *
 * The message is the assignment's exact text:
 * "Same Day delivery is only for parcels up to 10 kg!"
 *
 * INHERITANCE: extends the base ParcelException.
 */
public class SameDayNotAllowedException extends ParcelException {

    private static final long serialVersionUID = 1L;

    public static final String MESSAGE = "Same Day delivery is only for parcels up to 10 kg!";

    // ENCAPSULATION: private field, read-only through a getter.
    private final double chargeableWeight;

    public SameDayNotAllowedException(double chargeableWeight) {
        super(MESSAGE);
        this.chargeableWeight = chargeableWeight;
    }

    /** The chargeable weight that was too heavy for Same Day delivery. */
    public double getChargeableWeight() {
        return chargeableWeight;
    }
}
