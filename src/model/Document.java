package model;

import exception.InvalidParcelException;
import util.Validator;

/**
 * DOCUMENT parcel.
 *  - Additional field: Number of Pages (1 to 100)
 *  - Maximum weight: 0.5 kg
 *  - Chargeable weight = actual weight
 *  - Base fee = Php 50 flat
 *
 * INHERITANCE: Document IS-A Parcel (single inheritance, "extends").
 * POLYMORPHISM: overrides the four abstract methods of Parcel.
 */
public class Document extends Parcel {

    public static final double MAX_WEIGHT = 0.5;
    public static final double BASE_FEE = 50.0;

    // ENCAPSULATION: private field with a getter only.
    private final int pages;

    public Document(String trackingNumber, String receiverName, double weight, int pages)
            throws InvalidParcelException {
        super(trackingNumber, receiverName, weight); // INHERITANCE: parent constructor runs first
        Validator.checkPages(pages);
        this.pages = pages;
    }

    public int getPages() {
        return pages;
    }

    // POLYMORPHISM (dynamic / runtime): method overriding

    @Override
    public String getType() {
        return "Document";
    }

    @Override
    public double getMaxWeight() {
        return MAX_WEIGHT;
    }

    @Override
    public double getChargeableWeight() {
        return getWeight(); // chargeable weight = actual weight
    }

    @Override
    public double getBaseFee() {
        return BASE_FEE; // flat fee
    }
}
