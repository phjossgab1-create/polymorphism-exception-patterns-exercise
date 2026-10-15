package model;

import exception.InvalidParcelException;
import util.Validator;

/**
 * FRAGILE parcel (must extend BOX).
 *  - Additional field: Handling Note (not empty)
 *  - Maximum weight: 30 kg
 *  - Chargeable weight = same rule as BOX
 *  - Base fee = BOX base fee + Php 100 handling fee
 *
 * INHERITANCE (multilevel): Fragile -> Box -> Parcel.
 *   Fragile inherits the dimensions and the chargeable weight rule from Box,
 *   and uses super.method() to reuse Box's logic instead of copying it.
 * POLYMORPHISM: overrides getType, getMaxWeight, getChargeableWeight and getBaseFee.
 */
public class Fragile extends Box {

    public static final double MAX_WEIGHT = 30.0;
    public static final double HANDLING_FEE = 100.0;

    // ENCAPSULATION: private field with a getter only.
    private final String handlingNote;

    public Fragile(String trackingNumber, String receiverName, double weight,
                   int length, int width, int height, String handlingNote)
            throws InvalidParcelException {
        // INHERITANCE: super(...) must be the first line. It calls Box's
        // constructor, which in turn calls Parcel's constructor.
        super(trackingNumber, receiverName, weight, length, width, height);
        Validator.checkHandlingNote(handlingNote);
        this.handlingNote = handlingNote;
    }

    public String getHandlingNote() {
        return handlingNote;
    }

    // POLYMORPHISM (dynamic / runtime): method overriding

    @Override
    public String getType() {
        return "Fragile";
    }

    @Override
    public double getMaxWeight() {
        return MAX_WEIGHT; // 30 kg instead of Box's 50 kg
    }

    @Override
    public double getChargeableWeight() {
        return super.getChargeableWeight(); // same rule as BOX
    }

    @Override
    public double getBaseFee() {
        return super.getBaseFee() + HANDLING_FEE; // BOX base fee + Php 100
    }
}
