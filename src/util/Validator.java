package util;

import exception.InvalidInputException;
import exception.InvalidParcelException;

/**
 * All validation rules of the program, kept in ONE place.
 *
 * Both the menu (while the user is typing) and the model classes (in their
 * constructors) use these methods, so a rule can never be different in two
 * places.
 *
 * Two kinds of methods:
 *  - parseXxx(String input): checks the text the user typed and converts it
 *    (e.g. "1.8" -> 1.8). Used by the menu.
 *  - checkXxx(value): checks an already-converted value. Used by the model
 *    constructors, and also called at the end of each parseXxx method.
 *
 * EXCEPTION HANDLING: every method throws a custom checked exception
 * ("throw new InvalidParcelException(...)") with a clear reason. The menu
 * catches it, shows "[Invalid] <reason>" and asks the same question again.
 *
 * Java 8 note: isBlank() and strip() do not exist in Java 8, so we use
 * trim().isEmpty() instead.
 */
public final class Validator {

    // "ABC" (uppercase) followed by exactly 4 digits, e.g. ABC0001
    private static final String TRACKING_PATTERN = "ABC[0-9]{4}";

    // Words made of letters, separated by exactly one space.
    // \p{L} = any letter, so names like "Pena" or "Peña" are accepted.
    private static final String NAME_PATTERN = "\\p{L}+( \\p{L}+)*";

    // Digits with an optional decimal point: "2", "1.8", "0.5", "5.", ".5"
    // No letters, no signs (+ or -), no spaces.
    private static final String WEIGHT_PATTERN = "[0-9]+\\.?[0-9]*|\\.[0-9]+";

    private static final String DIGITS_PATTERN = "[0-9]+";

    public static final int MIN_PAGES = 1;
    public static final int MAX_PAGES = 100;

    private Validator() {
        // utility class - no objects
    }

    // ------------------------------------------------------------------
    // Tracking number
    // ------------------------------------------------------------------

    /** Tracking number must be uppercase "ABC" + exactly 4 digits. */
    public static void checkTrackingNumber(String trackingNumber) throws InvalidParcelException {
        if (trackingNumber == null || trackingNumber.isEmpty()) {
            throw new InvalidParcelException("Tracking Number", "Tracking number cannot be empty.");
        }
        if (!trackingNumber.matches(TRACKING_PATTERN)) {
            throw new InvalidParcelException("Tracking Number",
                    "Tracking number must be uppercase \"ABC\" followed by exactly 4 digits (e.g. ABC0001).");
        }
    }

    // ------------------------------------------------------------------
    // Receiver name
    // ------------------------------------------------------------------

    /** Letters only, one space allowed between words, not empty. */
    public static void checkReceiverName(String name) throws InvalidParcelException {
        if (name == null || name.isEmpty()) {
            throw new InvalidParcelException("Receiver Name", "Receiver name cannot be empty.");
        }
        if (name.trim().isEmpty()) {
            throw new InvalidParcelException("Receiver Name", "Receiver name cannot be only spaces.");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetter(c) && c != ' ') {
                throw new InvalidParcelException("Receiver Name",
                        "Receiver name must contain letters only (no numbers or symbols).");
            }
        }
        if (!name.matches(NAME_PATTERN)) {
            throw new InvalidParcelException("Receiver Name",
                    "Receiver name can only have one space between words (no spaces at the start or end).");
        }
    }

    // ------------------------------------------------------------------
    // Weight
    // ------------------------------------------------------------------

    /**
     * Converts the typed weight and checks it.
     *
     * @param input     the text the user typed
     * @param maxWeight the maximum weight for the parcel type, in kg
     * @param typeName  "Document", "Box" or "Fragile" (used in the message)
     */
    public static double parseWeight(String input, double maxWeight, String typeName)
            throws InvalidParcelException {
        if (input == null || input.isEmpty()) {
            throw new InvalidParcelException("Weight", "Weight cannot be empty.");
        }
        if (!input.matches(WEIGHT_PATTERN)) {
            throw new InvalidParcelException("Weight",
                    "Weight must be a number using digits and an optional decimal point only "
                    + "(no letters, signs or spaces).");
        }
        double weight;
        try {
            weight = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            // EXCEPTION HANDLING: wrap the low-level exception in our own,
            // keeping the original as the cause.
            throw new InvalidParcelException("Weight", "Weight is not a valid number.", e);
        }
        checkWeight(weight, maxWeight, typeName);
        return weight;
    }

    /** Weight must be greater than 0 and not more than the type's maximum. */
    public static void checkWeight(double weight, double maxWeight, String typeName)
            throws InvalidParcelException {
        if (Double.isNaN(weight)) {
            throw new InvalidParcelException("Weight", "Weight must be a valid number.");
        }
        if (weight <= 0) {
            throw new InvalidParcelException("Weight", "Weight must be greater than 0.");
        }
        if (weight > maxWeight) {
            throw new InvalidParcelException("Weight",
                    "Weight cannot exceed " + Formatter.number(maxWeight)
                    + " kg (the maximum for a " + typeName + " parcel).");
        }
    }

    // ------------------------------------------------------------------
    // Number of pages (Document)
    // ------------------------------------------------------------------

    /** Whole number from 1 to 100. */
    public static int parsePages(String input) throws InvalidParcelException {
        if (input == null || input.isEmpty()) {
            throw new InvalidParcelException("Number of Pages", "Number of pages cannot be empty.");
        }
        if (!input.matches(DIGITS_PATTERN)) {
            throw new InvalidParcelException("Number of Pages",
                    "Number of pages must be a whole number (digits only).");
        }
        int pages;
        try {
            pages = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            // A number too big for an int (e.g. 99999999999) is certainly over 100.
            throw new InvalidParcelException("Number of Pages",
                    "Number of pages must be from " + MIN_PAGES + " to " + MAX_PAGES + ".", e);
        }
        checkPages(pages);
        return pages;
    }

    public static void checkPages(int pages) throws InvalidParcelException {
        if (pages < MIN_PAGES || pages > MAX_PAGES) {
            throw new InvalidParcelException("Number of Pages",
                    "Number of pages must be from " + MIN_PAGES + " to " + MAX_PAGES + ".");
        }
    }

    // ------------------------------------------------------------------
    // Length, width, height (Box and Fragile)
    // ------------------------------------------------------------------

    /**
     * Whole number greater than 0, with no leading zeros.
     *
     * @param input     the text the user typed
     * @param fieldName "Length", "Width" or "Height" (used in the message)
     */
    public static int parseDimension(String input, String fieldName) throws InvalidParcelException {
        if (input == null || input.isEmpty()) {
            throw new InvalidParcelException(fieldName, fieldName + " cannot be empty.");
        }
        if (!input.matches(DIGITS_PATTERN)) {
            throw new InvalidParcelException(fieldName,
                    fieldName + " must be a whole number in cm (digits only - no decimals, signs or spaces).");
        }
        if (input.charAt(0) == '0') {
            if (input.matches("0+")) {
                throw new InvalidParcelException(fieldName, fieldName + " must be greater than 0.");
            }
            throw new InvalidParcelException(fieldName, fieldName + " must not have leading zeros.");
        }
        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            // EXCEPTION HANDLING (nested try-catch): the digits are valid but the
            // number does not fit in an int, so we wrap it in our own exception.
            throw new InvalidParcelException(fieldName,
                    fieldName + " is too large (maximum is " + Integer.MAX_VALUE + ").", e);
        }
        checkDimension(value, fieldName);
        return value;
    }

    public static void checkDimension(int value, String fieldName) throws InvalidParcelException {
        if (value <= 0) {
            throw new InvalidParcelException(fieldName, fieldName + " must be greater than 0.");
        }
    }

    // ------------------------------------------------------------------
    // Handling note (Fragile)
    // ------------------------------------------------------------------

    /** Not empty and not only spaces. */
    public static void checkHandlingNote(String note) throws InvalidParcelException {
        if (note == null || note.isEmpty()) {
            throw new InvalidParcelException("Handling Note", "Handling note cannot be empty.");
        }
        if (note.trim().isEmpty()) {
            throw new InvalidParcelException("Handling Note", "Handling note cannot be only spaces.");
        }
    }

    // ------------------------------------------------------------------
    // Menu choices
    // ------------------------------------------------------------------

    /**
     * Accepts ONLY the exact text "1", "2", ... up to max.
     * "01", " 1", "1 " or any extra characters are rejected.
     */
    public static int parseMenuChoice(String input, int max) throws InvalidInputException {
        if (input == null || input.isEmpty()) {
            throw new InvalidInputException("Please enter a choice from 1 to " + max + ".");
        }
        for (int option = 1; option <= max; option++) {
            if (input.equals(String.valueOf(option))) {
                return option;
            }
        }
        throw new InvalidInputException("Please enter only a number from 1 to " + max
                + " (no spaces, leading zeros or extra characters).");
    }
}
