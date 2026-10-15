package strategy;

/**
 * STRATEGY PATTERN - concrete strategy: EXPRESS delivery.
 * Delivery fee = Base fee x 1.5
 *
 * POLYMORPHISM (dynamic): overrides the interface methods.
 */
public class ExpressDelivery implements DeliveryStrategy {

    private static final double MULTIPLIER = 1.5;

    @Override
    public String getName() {
        return "Express";
    }

    @Override
    public boolean isAllowedFor(double chargeableWeight) {
        return true; // Express is available for every parcel
    }

    @Override
    public double calculateFee(double baseFee) {
        return baseFee * MULTIPLIER;
    }
}
