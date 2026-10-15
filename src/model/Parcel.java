package model;

import exception.InvalidParcelException;
import exception.ParcelAlreadyDeliveredException;
import exception.SameDayNotAllowedException;
import strategy.DeliveryStrategy;
import strategy.StandardDelivery;
import util.Validator;

/**
 * The common parent of every parcel type (Document, Box, Fragile).
 *
 * ABSTRACTION (abstract class):
 * - Parcel is abstract, so nobody can create a plain "Parcel" object.
 * - It declares abstract methods (getType, getMaxWeight, getChargeableWeight,
 *   getBaseFee): it says WHAT every parcel can do, and each subclass decides HOW.
 * - It also has concrete shared methods (getDeliveryFee, markDelivered, ...)
 *   that all subclasses reuse.
 *
 * ENCAPSULATION:
 * - All fields are private. Outside code can only read them through getters.
 * - There is no setter for trackingNumber, receiverName or weight, so they can
 *   never change after the parcel is created.
 * - Status and delivery service can only change through methods that check the
 *   rules first (markDelivered, setDeliveryStrategy).
 *
 * INHERITANCE: Document and Box extend Parcel (hierarchical inheritance);
 *              Fragile extends Box (multilevel: Parcel -> Box -> Fragile).
 *
 * POLYMORPHISM: the menu and the ParcelManager only use "Parcel" references.
 *               Calling getBaseFee() on a Parcel runs the Document, Box or
 *               Fragile version depending on the real object (dynamic dispatch).
 *
 * STRATEGY PATTERN: Parcel is the CONTEXT. It holds a DeliveryStrategy and
 *                   delegates the delivery fee calculation to it.
 */
public abstract class Parcel {

    // ENCAPSULATION: private fields
    private final String trackingNumber;
    private final String receiverName;
    private final double weight;
    private ParcelStatus status;
    private DeliveryStrategy deliveryStrategy; // STRATEGY PATTERN: the current strategy

    /**
     * Creates a parcel after validating the common fields.
     *
     * Note: this constructor calls getMaxWeight() and getType(), which are
     * overridden by the subclasses. This is safe here because every subclass
     * returns a constant from those methods (they do not use subclass fields,
     * which are not set yet while this parent constructor is running).
     *
     * @throws InvalidParcelException if any field is invalid
     */
    public Parcel(String trackingNumber, String receiverName, double weight)
            throws InvalidParcelException {
        // EXCEPTION HANDLING: each check throws InvalidParcelException on bad data,
        // so an invalid Parcel object can never be created.
        Validator.checkTrackingNumber(trackingNumber);
        Validator.checkReceiverName(receiverName);
        Validator.checkWeight(weight, getMaxWeight(), getType()); // POLYMORPHISM: subclass max/type

        this.trackingNumber = trackingNumber;
        this.receiverName = receiverName;
        this.weight = weight;
        this.status = ParcelStatus.PENDING;               // every new parcel starts as Pending
        this.deliveryStrategy = new StandardDelivery();   // default service until one is chosen
    }

    // ------------------------------------------------------------------
    // ABSTRACTION: abstract methods - no body here, each subclass overrides them.
    // ------------------------------------------------------------------

    /** "Document", "Box" or "Fragile". */
    public abstract String getType();

    /** Maximum actual weight allowed for this parcel type, in kg. */
    public abstract double getMaxWeight();

    /** Weight used to compute the fee, in kg. */
    public abstract double getChargeableWeight();

    /** Base fee in Php, before the delivery service is applied. */
    public abstract double getBaseFee();

    // ------------------------------------------------------------------
    // Concrete shared methods (inherited by every subclass)
    // ------------------------------------------------------------------

    /**
     * Delivery fee = the chosen delivery service applied to this parcel's base fee.
     *
     * STRATEGY PATTERN: the parcel does not know the formula - it delegates
     * to whatever DeliveryStrategy it currently holds.
     * POLYMORPHISM: getBaseFee() runs the subclass version (Document/Box/Fragile),
     * and calculateFee() runs the strategy's version (Standard/Express/Same Day).
     */
    public double getDeliveryFee() {
        return deliveryStrategy.calculateFee(getBaseFee());
    }

    /**
     * STRATEGY PATTERN: swaps the delivery service at runtime.
     *
     * If the new service is not allowed, an exception is thrown and the parcel
     * KEEPS its old service.
     *
     * @throws ParcelAlreadyDeliveredException if the parcel is already delivered
     * @throws SameDayNotAllowedException      if Same Day is chosen and the
     *                                         chargeable weight is over 10 kg
     */
    public void setDeliveryStrategy(DeliveryStrategy newStrategy)
            throws ParcelAlreadyDeliveredException, SameDayNotAllowedException {
        if (newStrategy == null) {
            // Unchecked exception: passing null is a programming error, not a user error.
            throw new IllegalArgumentException("Delivery strategy cannot be null.");
        }
        if (isDelivered()) {
            throw new ParcelAlreadyDeliveredException(trackingNumber);
        }
        if (!newStrategy.isAllowedFor(getChargeableWeight())) {
            // Only Same Day has a weight limit, so this is the Same Day case.
            throw new SameDayNotAllowedException(getChargeableWeight());
        }
        this.deliveryStrategy = newStrategy;
    }

    /**
     * Marks the parcel as delivered.
     *
     * @throws ParcelAlreadyDeliveredException if it was already delivered
     */
    public void markDelivered() throws ParcelAlreadyDeliveredException {
        if (isDelivered()) {
            throw new ParcelAlreadyDeliveredException(trackingNumber);
        }
        this.status = ParcelStatus.DELIVERED;
    }

    public boolean isDelivered() {
        return status == ParcelStatus.DELIVERED;
    }

    // ------------------------------------------------------------------
    // ENCAPSULATION: getters (read-only access to private fields)
    // ------------------------------------------------------------------

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public double getWeight() {
        return weight;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public DeliveryStrategy getDeliveryStrategy() {
        return deliveryStrategy;
    }
}
