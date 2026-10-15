package strategy;

/**
 * STRATEGY PATTERN - the Strategy interface.
 *
 * Each delivery service (Standard, Express, Same Day) is a different
 * "algorithm" for computing the delivery fee. Every algorithm is put in its
 * own class that implements this interface, so they are interchangeable.
 *
 * The CONTEXT class is model.Parcel: it holds a reference to a
 * DeliveryStrategy and delegates the fee calculation to it. The strategy can
 * be swapped at runtime with Parcel.setDeliveryStrategy(...) - this is exactly
 * what "Change Delivery Service" does.
 *
 * ABSTRACTION (interface): a pure contract - it says WHAT a delivery service
 * can do, not HOW. All interface methods are implicitly public and abstract.
 *
 * Adding a new service later only needs a new class that implements this
 * interface (open/closed principle) - no existing class has to change.
 */
public interface DeliveryStrategy {

    /** Display name of the service, e.g. "Express". */
    String getName();

    /**
     * Whether this service may be used for a parcel with the given
     * chargeable weight (in kg).
     */
    boolean isAllowedFor(double chargeableWeight);

    /** Computes the delivery fee (in Php) from the parcel's base fee. */
    double calculateFee(double baseFee);
}
