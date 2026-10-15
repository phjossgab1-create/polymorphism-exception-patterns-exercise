package strategy;

/**
 * STRATEGY PATTERN - concrete strategy: STANDARD delivery.
 * Delivery fee = Base fee
 *
 * POLYMORPHISM (dynamic): overrides the interface methods; the right version
 * is chosen at runtime by the object the Parcel is holding.
 */
public class StandardDelivery implements DeliveryStrategy {

    @Override
    public String getName() {
        return "Standard";
    }

    @Override
    public boolean isAllowedFor(double chargeableWeight) {
        return true; // Standard is available for every parcel
    }

    @Override
    public double calculateFee(double baseFee) {
        return baseFee;
    }
}
