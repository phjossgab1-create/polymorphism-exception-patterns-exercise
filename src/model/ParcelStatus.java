package model;

/**
 * The two possible states of a parcel: Pending or Delivered.
 *
 * An enum is safer than a String: the status can never be misspelled or set
 * to an invalid value.
 */
public enum ParcelStatus {

    PENDING("Pending"),
    DELIVERED("Delivered");

    // ENCAPSULATION: private field with a getter.
    private final String displayName;

    ParcelStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
