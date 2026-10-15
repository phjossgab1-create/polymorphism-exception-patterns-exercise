package strategy;

/**
 * STRATEGY PATTERN - concrete strategy: SAME DAY delivery.
 * Delivery fee = (Base fee x 2) + Php 100
 * Only for parcels with a chargeable weight of 10 kg or less.
 *
 * POLYMORPHISM (dynamic): overrides the interface methods. This is the only
 * strategy whose isAllowedFor(...) can return false.
 */
public class SameDayDelivery implements DeliveryStrategy {

    /** Highest chargeable weight (kg) allowed for Same Day delivery. */
    public static final double MAX_CHARGEABLE_WEIGHT = 10.0;

    private static final double MULTIPLIER = 2.0;
    private static final double SURCHARGE = 100.0;

    @Override
    public String getName() {
        return "Same Day";
    }

    @Override
    public boolean isAllowedFor(double chargeableWeight) {
        return chargeableWeight <= MAX_CHARGEABLE_WEIGHT; // 10 kg exactly is allowed
    }

    @Override
    public double calculateFee(double baseFee) {
        return (baseFee * MULTIPLIER) + SURCHARGE;
    }
}
