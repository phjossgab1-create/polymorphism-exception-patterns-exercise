package exception;

/**
 * Base class of every custom exception in the Parcel Delivery System.
 *
 * EXCEPTION HANDLING:
 * - This is a CHECKED exception (it extends Exception, not RuntimeException).
 *   Any method that throws it must declare it with "throws", and the compiler
 *   forces the caller to handle it with try/catch.
 * - We use checked exceptions because every problem in this program is
 *   recoverable: the user can simply type the value again or pick another option.
 *
 * INHERITANCE:
 * - All custom exceptions extend this ONE base class, so a single
 *   "catch (ParcelException e)" can handle any of them when needed.
 */
public class ParcelException extends Exception {

    // Exception is Serializable; this ID silences the "serialVersionUID" warning.
    private static final long serialVersionUID = 1L;

    // POLYMORPHISM (static / compile-time): overloaded constructors.
    // Same name, different parameter lists - the compiler picks the right one.

    /** Creates an exception with a message only. */
    public ParcelException(String message) {
        super(message);
    }

    /** Creates an exception with a message and the original cause (for wrapping). */
    public ParcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
