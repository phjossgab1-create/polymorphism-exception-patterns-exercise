package ui;

import exception.ParcelException;

/**
 * A rule that checks one answer typed by the user.
 *
 * ABSTRACTION (interface): InputReader only knows "something that can
 * validate a String". Each question passes its own rule, usually as a
 * lambda or a method reference, e.g. Validator::checkReceiverName.
 *
 * EXCEPTION HANDLING: validate(...) throws a ParcelException (or any of its
 * subclasses) with the reason when the answer is invalid.
 */
@FunctionalInterface
public interface InputValidator {

    void validate(String input) throws ParcelException;
}
