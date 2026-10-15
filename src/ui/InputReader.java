package ui;

import exception.ParcelException;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Reads the user's answers from the console.
 *
 * Rules applied to EVERY prompt:
 * - Input is read exactly as typed (nothing is trimmed or changed).
 * - Typing \ returns InputReader.BACK so the menu can go back one question.
 * - An invalid answer shows "[Invalid] <reason>" and asks the SAME question
 *   again - the user is never sent back to the main menu because of an error.
 *
 * Going back with \ does NOT use exceptions: exceptions are for errors,
 * not for normal flow control (Exception Handling lesson, best practices).
 */
public class InputReader {

    /** Returned by ask(...) when the user types \ (a single backslash). */
    public static final String BACK = "\\";

    // ENCAPSULATION: private field.
    private final BufferedReader reader;

    public InputReader(BufferedReader reader) {
        this.reader = reader;
    }

    /** True if the answer means "go back one question". */
    public static boolean isBack(String answer) {
        return BACK.equals(answer);
    }

    /**
     * Shows the question and keeps asking until the answer is valid
     * or the user types \ to go back.
     *
     * @param question  the text to show
     * @param validator the rule the answer must pass
     * @return the valid answer exactly as typed, or BACK
     */
    public String ask(String question, InputValidator validator) {
        while (true) {
            System.out.print(question);
            System.out.flush();
            String answer = readLine();

            if (isBack(answer)) {
                return BACK; // the caller decides where "back" goes
            }

            // EXCEPTION HANDLING: try the rule; if it throws, show why and ask again.
            try {
                validator.validate(answer);
                return answer;
            } catch (ParcelException e) {
                System.out.println("[Invalid] " + e.getMessage());
            }
        }
    }

    /**
     * Reads one line exactly as typed.
     *
     * If the input ends (for example Ctrl+Z / Ctrl+D) or cannot be read, an
     * UncheckedIOException is thrown so Main can close the program cleanly.
     */
    private String readLine() {
        String line;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            // EXCEPTION HANDLING: wrap the checked IOException so every caller
            // does not need "throws IOException".
            throw new UncheckedIOException(e);
        }
        if (line == null) {
            throw new UncheckedIOException(new EOFException("No more input."));
        }
        return line;
    }
}
