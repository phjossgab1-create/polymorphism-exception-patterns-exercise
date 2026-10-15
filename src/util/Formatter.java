package util;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Formatting helpers for money, weights and table columns.
 *
 * This is a utility class: it only has static methods, so its constructor is
 * private (nobody needs to create a Formatter object).
 *
 * Java 8 note: String.repeat() does not exist in Java 8, so repeat(...) is
 * written by hand below.
 */
public final class Formatter {

    private Formatter() {
        // utility class - no objects
    }

    /** Money with 2 decimals, e.g. 330.0 -> "Php 330.00". */
    public static String money(double amount) {
        // Locale.US makes sure the decimal separator is always a dot.
        return String.format(Locale.US, "Php %.2f", amount);
    }

    /**
     * A number without unnecessary trailing zeros,
     * e.g. 0.5 -> "0.5", 3.0 -> "3", 0.125 -> "0.125".
     */
    public static String number(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    /** A weight with its unit, e.g. 3.0 -> "3 kg", 0.3 -> "0.3 kg". */
    public static String weight(double kg) {
        return number(kg) + " kg";
    }

    /** Returns the character c repeated count times (Java 8 has no String.repeat). */
    public static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    // POLYMORPHISM (static / compile-time): pad(...) is OVERLOADED.
    // Same method name, different parameter lists; the compiler picks the version.

    /** Pads text with spaces on the right (left-aligned) up to the given width. */
    public static String pad(String text, int width) {
        return pad(text, width, false);
    }

    /** Pads text with spaces up to the given width, aligned left or right. */
    public static String pad(String text, int width, boolean alignRight) {
        String spaces = repeat(' ', Math.max(0, width - text.length()));
        return alignRight ? spaces + text : text + spaces;
    }
}
