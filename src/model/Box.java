package model;

import exception.InvalidParcelException;
import util.Validator;

/**
 * BOX parcel.
 *  - Additional fields: Length, Width, Height in cm (whole numbers > 0)
 *  - Maximum weight: 50 kg
 *  - Volumetric weight = (Length x Width x Height) / 3500
 *  - Chargeable weight = the bigger of actual weight and volumetric weight,
 *    rounded UP to the next whole kg (2.0 -> 2, 2.1 -> 3)
 *  - Base fee = Php 40 x chargeable weight
 *
 * INHERITANCE: Box IS-A Parcel, and Box is also the PARENT of Fragile
 *              (multilevel inheritance: Parcel -> Box -> Fragile).
 * POLYMORPHISM: overrides the four abstract methods of Parcel.
 */
public class Box extends Parcel {

    public static final double MAX_WEIGHT = 50.0;
    public static final double VOLUMETRIC_DIVISOR = 3500.0;
    public static final double RATE_PER_KG = 40.0;

    // ENCAPSULATION: private fields with getters only.
    // Fragile reads them through these getters (private fields are NOT inherited).
    private final int length;
    private final int width;
    private final int height;

    public Box(String trackingNumber, String receiverName, double weight,
               int length, int width, int height) throws InvalidParcelException {
        super(trackingNumber, receiverName, weight); // INHERITANCE: parent constructor runs first
        Validator.checkDimension(length, "Length");
        Validator.checkDimension(width, "Width");
        Validator.checkDimension(height, "Height");
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** (Length x Width x Height) / 3500, in kg. */
    public double getVolumetricWeight() {
        // Cast to double FIRST so a big L x W x H cannot overflow an int.
        return ((double) length * width * height) / VOLUMETRIC_DIVISOR;
    }

    // POLYMORPHISM (dynamic / runtime): method overriding

    @Override
    public String getType() {
        return "Box";
    }

    @Override
    public double getMaxWeight() {
        return MAX_WEIGHT;
    }

    @Override
    public double getChargeableWeight() {
        // Bigger of actual and volumetric weight, rounded UP to a whole kg.
        return Math.ceil(Math.max(getWeight(), getVolumetricWeight()));
    }

    @Override
    public double getBaseFee() {
        return RATE_PER_KG * getChargeableWeight();
    }
}
