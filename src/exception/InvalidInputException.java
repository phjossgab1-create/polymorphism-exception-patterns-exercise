package exception;

/**
 * Thrown when a menu choice is not one of the allowed options
 * (for example "01", " 1", "1 " or "7" when only 1 to 5 are allowed).
 *
 * This is kept separate from InvalidParcelException because a menu choice
 * is not parcel data.
 *
 * INHERITANCE: extends the base ParcelException.
 */
public class InvalidInputException extends ParcelException {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
