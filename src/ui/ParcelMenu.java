package ui;

import exception.InvalidParcelException;
import exception.ParcelAlreadyDeliveredException;
import exception.ParcelException;
import exception.ParcelNotFoundException;
import exception.SameDayNotAllowedException;
import model.Box;
import model.Document;
import model.Fragile;
import model.Parcel;
import service.ParcelManager;
import strategy.DeliveryStrategy;
import strategy.ExpressDelivery;
import strategy.SameDayDelivery;
import strategy.StandardDelivery;
import util.Formatter;
import util.Validator;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The console menu of the Parcel Delivery System.
 *
 * POLYMORPHISM: after a parcel is created, the menu ONLY uses the abstract
 * type Parcel (and the DeliveryStrategy interface). It never checks
 * "instanceof Box" and never switches on the parcel type to compute fees or
 * show data - each object runs its own overridden method at runtime.
 * The only type-specific code is createParcel(...), because the program has to
 * ask different questions (pages, dimensions, handling note) to build each type.
 *
 * SINGLETON: the parcels are stored in the one shared ParcelManager.
 * STRATEGY:  the user picks a DeliveryStrategy object; the parcel uses it.
 * EXCEPTION HANDLING: every operation catches the specific custom exceptions
 *                     and prints the assignment's messages word for word.
 */
public class ParcelMenu {

    // ------------------------------------------------------------------
    // Assignment messages (word for word)
    // "Parcel not found!" and "Same Day delivery is only for parcels up to 10 kg!"
    // come from the exceptions themselves (ParcelNotFoundException,
    // SameDayNotAllowedException) through e.getMessage().
    // ------------------------------------------------------------------
    private static final String MSG_ADDED = "Parcel added successfully! Delivery fee: ";
    private static final String MSG_ALREADY_DELIVERED = "Parcel already delivered!";
    private static final String MSG_SERVICE_CHANGED = "Delivery service changed! New fee: ";
    private static final String MSG_DELIVERED = "Parcel delivered successfully!";
    private static final String MSG_WAS_ALREADY_DELIVERED = "Parcel was already delivered.";
    private static final String MSG_AT_MAIN_MENU = "You are already at the main menu.";

    private static final int MAIN_MENU_OPTIONS = 5;

    // Parcel types, in the order shown to the user (1, 2, 3).
    private static final int TYPE_DOCUMENT = 1;
    private static final int TYPE_BOX = 2;
    private static final int TYPE_FRAGILE = 3;
    private static final String[] TYPE_NAMES = { "Document", "Box", "Fragile" };
    private static final double[] TYPE_MAX_WEIGHTS = { Document.MAX_WEIGHT, Box.MAX_WEIGHT, Fragile.MAX_WEIGHT };

    // The questions of "Add Parcel", one constant per question.
    private static final String STEP_TYPE = "type";
    private static final String STEP_TRACKING = "tracking";
    private static final String STEP_RECEIVER = "receiver";
    private static final String STEP_WEIGHT = "weight";
    private static final String STEP_PAGES = "pages";
    private static final String STEP_LENGTH = "length";
    private static final String STEP_WIDTH = "width";
    private static final String STEP_HEIGHT = "height";
    private static final String STEP_NOTE = "note";
    private static final String STEP_SERVICE = "service";

    private final InputReader input;
    private final ParcelManager manager;

    // STRATEGY PATTERN: the family of interchangeable delivery algorithms.
    // POLYMORPHISM: one array of the interface type holds three different classes.
    // A new service only needs a new class added here - nothing else changes.
    private final DeliveryStrategy[] services = {
            new StandardDelivery(),
            new ExpressDelivery(),
            new SameDayDelivery()
    };

    public ParcelMenu(InputReader input) {
        this.input = input;
        this.manager = ParcelManager.getInstance(); // SINGLETON: the shared instance
    }

    // ==================================================================
    // Main menu
    // ==================================================================

    /** Shows the main menu until the user chooses Exit. */
    public void run() {
        System.out.println("========================================");
        System.out.println("        PARCEL DELIVERY SYSTEM");
        System.out.println("========================================");
        System.out.println("Tip: type \\ at any prompt to go back one question.");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = askMainMenuChoice();

            // EXCEPTION HANDLING: specific catch first, general catch last.
            try {
                switch (choice) {
                    case 1:
                        addParcel();
                        break;
                    case 2:
                        viewAllParcels();
                        break;
                    case 3:
                        changeDeliveryService();
                        break;
                    case 4:
                        markAsDelivered();
                        break;
                    default: // 5
                        System.out.println();
                        System.out.println("Thank you for using the Parcel Delivery System. Goodbye!");
                        running = false;
                        break;
                }
            } catch (UncheckedIOException e) {
                throw e; // input ended - Main closes the program
            } catch (RuntimeException e) {
                // Never swallow an error silently: report it and keep the program running.
                System.out.println("[Error] Something unexpected happened: " + e);
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("--------------- MAIN MENU --------------");
        System.out.println("1. Add Parcel");
        System.out.println("2. View All Parcels");
        System.out.println("3. Change Delivery Service");
        System.out.println("4. Mark as Delivered");
        System.out.println("5. Exit");
        System.out.println("----------------------------------------");
    }

    private int askMainMenuChoice() {
        while (true) {
            String answer = input.ask("Enter your choice: ",
                    s -> Validator.parseMenuChoice(s, MAIN_MENU_OPTIONS));
            if (InputReader.isBack(answer)) {
                System.out.println(MSG_AT_MAIN_MENU);
                continue; // ask the same question again
            }
            return Integer.parseInt(answer); // already validated: "1" to "5"
        }
    }

    private void printTitle(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
        System.out.println("(Type \\ to go back one question.)");
    }

    // ==================================================================
    // 1. Add Parcel
    // ==================================================================

    /**
     * Asks the questions one by one. "index" is the current question.
     * Typing \ moves back one question; on the first question it returns
     * to the main menu.
     */
    private void addParcel() {
        printTitle("ADD PARCEL");

        Map<String, String> answers = new HashMap<String, String>();
        List<String> steps = stepsFor(0); // only the "type" question until a type is chosen
        int index = 0;

        while (index < steps.size()) {
            String step = steps.get(index);

            // Last question: delivery service (it also saves the parcel).
            if (STEP_SERVICE.equals(step)) {
                boolean saved = askServiceAndSave(answers);
                if (saved) {
                    return; // done - back to the main menu
                }
                index--;    // user typed \ - go back to the previous question
                continue;
            }

            String answer = askAddParcelQuestion(step, answers);
            if (InputReader.isBack(answer)) {
                if (index == 0) {
                    return; // \ on the first question - back to the main menu
                }
                index--;
                continue;
            }

            answers.put(step, answer);
            if (STEP_TYPE.equals(step)) {
                // The rest of the questions depend on the chosen type.
                steps = stepsFor(Integer.parseInt(answer));
            }
            index++;
        }
    }

    /** The list of questions for a parcel type (0 = type not chosen yet). */
    private List<String> stepsFor(int type) {
        List<String> steps = new ArrayList<String>();
        steps.add(STEP_TYPE);
        if (type == 0) {
            return steps;
        }
        steps.add(STEP_TRACKING);
        steps.add(STEP_RECEIVER);
        steps.add(STEP_WEIGHT);
        if (type == TYPE_DOCUMENT) {
            steps.add(STEP_PAGES);
        } else { // Box and Fragile both have dimensions
            steps.add(STEP_LENGTH);
            steps.add(STEP_WIDTH);
            steps.add(STEP_HEIGHT);
            if (type == TYPE_FRAGILE) {
                steps.add(STEP_NOTE);
            }
        }
        steps.add(STEP_SERVICE);
        return steps;
    }

    /** Asks one question of "Add Parcel" and returns the valid answer, or BACK. */
    private String askAddParcelQuestion(String step, Map<String, String> answers) {
        int type = answers.containsKey(STEP_TYPE) ? Integer.parseInt(answers.get(STEP_TYPE)) : 0;

        switch (step) {
            case STEP_TYPE:
                return input.ask("Select parcel type:\n"
                                + "  1. Document\n"
                                + "  2. Box\n"
                                + "  3. Fragile\n"
                                + "Enter choice: ",
                        s -> Validator.parseMenuChoice(s, TYPE_NAMES.length));

            case STEP_TRACKING:
                return input.ask("Enter tracking number (e.g. ABC0001): ", s -> {
                    Validator.checkTrackingNumber(s);          // format
                    manager.checkTrackingNumberAvailable(s);   // duplicate check
                });

            case STEP_RECEIVER:
                return input.ask("Enter receiver name: ", Validator::checkReceiverName);

            case STEP_WEIGHT: {
                final double maxWeight = TYPE_MAX_WEIGHTS[type - 1];
                final String typeName = TYPE_NAMES[type - 1];
                return input.ask("Enter weight in kg (max " + Formatter.number(maxWeight) + " kg): ",
                        s -> Validator.parseWeight(s, maxWeight, typeName));
            }

            case STEP_PAGES:
                return input.ask("Enter number of pages (" + Validator.MIN_PAGES + "-"
                        + Validator.MAX_PAGES + "): ", Validator::parsePages);

            case STEP_LENGTH:
                return input.ask("Enter length in cm: ", s -> Validator.parseDimension(s, "Length"));

            case STEP_WIDTH:
                return input.ask("Enter width in cm: ", s -> Validator.parseDimension(s, "Width"));

            case STEP_HEIGHT:
                return input.ask("Enter height in cm: ", s -> Validator.parseDimension(s, "Height"));

            case STEP_NOTE:
                return input.ask("Enter handling note: ", Validator::checkHandlingNote);

            default:
                throw new IllegalStateException("Unknown question: " + step);
        }
    }

    /**
     * Builds the parcel from the answers, asks for the delivery service and
     * saves the parcel.
     *
     * @return true if the parcel was saved, false if the user typed \ (go back)
     */
    private boolean askServiceAndSave(Map<String, String> answers) {
        Parcel parcel; // POLYMORPHISM: a Parcel reference to a Document/Box/Fragile object
        try {
            parcel = createParcel(answers);
        } catch (InvalidParcelException e) {
            // Should not happen (every answer was already validated), but never ignore it.
            System.out.println("[Invalid] " + e.getMessage());
            return false;
        }

        // POLYMORPHISM: getChargeableWeight() runs the Document, Box or Fragile version.
        System.out.println("Chargeable weight: " + Formatter.weight(parcel.getChargeableWeight()));

        while (true) {
            String answer = askService("Select delivery service:");
            if (InputReader.isBack(answer)) {
                return false;
            }
            DeliveryStrategy service = services[Integer.parseInt(answer) - 1];

            // EXCEPTION HANDLING: most specific exception first, base exception last.
            try {
                parcel.setDeliveryStrategy(service); // STRATEGY: may throw SameDayNotAllowedException
                manager.addParcel(parcel);
                System.out.println(MSG_ADDED + Formatter.money(parcel.getDeliveryFee()));
                return true;
            } catch (SameDayNotAllowedException e) {
                System.out.println(e.getMessage()); // exact assignment message, then select again
            } catch (ParcelException e) {
                // Not expected for a new parcel (e.g. duplicate), but report it and go back.
                System.out.println("[Invalid] " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * Creates the right parcel object from the answers.
     * This is the only place in the menu that knows the concrete classes.
     */
    private Parcel createParcel(Map<String, String> answers) throws InvalidParcelException {
        int type = Integer.parseInt(answers.get(STEP_TYPE));
        String trackingNumber = answers.get(STEP_TRACKING);
        String receiverName = answers.get(STEP_RECEIVER);
        double weight = Double.parseDouble(answers.get(STEP_WEIGHT));

        if (type == TYPE_DOCUMENT) {
            int pages = Integer.parseInt(answers.get(STEP_PAGES));
            return new Document(trackingNumber, receiverName, weight, pages); // upcast to Parcel
        }

        int length = Integer.parseInt(answers.get(STEP_LENGTH));
        int width = Integer.parseInt(answers.get(STEP_WIDTH));
        int height = Integer.parseInt(answers.get(STEP_HEIGHT));

        if (type == TYPE_BOX) {
            return new Box(trackingNumber, receiverName, weight, length, width, height);
        }
        return new Fragile(trackingNumber, receiverName, weight, length, width, height,
                answers.get(STEP_NOTE));
    }

    /** Shows the delivery services and returns the choice ("1" to "3"), or BACK. */
    private String askService(String title) {
        StringBuilder question = new StringBuilder(title).append("\n");
        for (int i = 0; i < services.length; i++) {
            // POLYMORPHISM: getName() runs each strategy's own version.
            question.append("  ").append(i + 1).append(". ").append(services[i].getName()).append("\n");
        }
        question.append("Enter choice: ");
        return input.ask(question.toString(), s -> Validator.parseMenuChoice(s, services.length));
    }

    // ==================================================================
    // 2. View All Parcels
    // ==================================================================

    private void viewAllParcels() {
        System.out.println();
        System.out.println("--- ALL PARCELS ---");

        List<Parcel> parcels = manager.getAllParcels();
        if (parcels.isEmpty()) {
            System.out.println("No parcels to display.");
            return;
        }

        String[] headers = { "Tracking No.", "Type", "Receiver", "Chargeable Weight",
                             "Service", "Delivery Fee", "Status" };
        boolean[] alignRight = { false, false, false, true, false, true, false };

        // POLYMORPHISM: one loop over Parcel references - each object answers
        // getType(), getChargeableWeight() and getDeliveryFee() in its own way.
        List<String[]> rows = new ArrayList<String[]>();
        for (Parcel parcel : parcels) {
            rows.add(new String[] {
                    parcel.getTrackingNumber(),
                    parcel.getType(),
                    parcel.getReceiverName(),
                    Formatter.weight(parcel.getChargeableWeight()),
                    parcel.getDeliveryStrategy().getName(),
                    Formatter.money(parcel.getDeliveryFee()),
                    parcel.getStatus().getDisplayName()
            });
        }

        // Each column is as wide as its longest value.
        int[] widths = new int[headers.length];
        for (int c = 0; c < headers.length; c++) {
            widths[c] = headers[c].length();
            for (String[] row : rows) {
                widths[c] = Math.max(widths[c], row[c].length());
            }
        }

        String border = tableBorder(widths);
        System.out.println(border);
        System.out.println(tableRow(headers, widths, new boolean[headers.length]));
        System.out.println(border);
        for (String[] row : rows) {
            System.out.println(tableRow(row, widths, alignRight));
        }
        System.out.println(border);
        System.out.println("Total parcels: " + parcels.size());
    }

    private String tableBorder(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : widths) {
            sb.append(Formatter.repeat('-', width + 2)).append("+");
        }
        return sb.toString();
    }

    private String tableRow(String[] cells, int[] widths, boolean[] alignRight) {
        StringBuilder sb = new StringBuilder("|");
        for (int c = 0; c < cells.length; c++) {
            sb.append(" ").append(Formatter.pad(cells[c], widths[c], alignRight[c])).append(" |");
        }
        return sb.toString();
    }

    // ==================================================================
    // 3. Change Delivery Service
    // ==================================================================

    private void changeDeliveryService() {
        printTitle("CHANGE DELIVERY SERVICE");

        Parcel parcel = null;
        int step = 0; // 0 = tracking number, 1 = new service

        while (true) {
            if (step == 0) {
                String trackingNumber = input.ask("Enter tracking number: ", Validator::checkTrackingNumber);
                if (InputReader.isBack(trackingNumber)) {
                    return; // \ on the first question - back to the main menu
                }
                try {
                    parcel = manager.findPendingParcel(trackingNumber);
                } catch (ParcelNotFoundException e) {
                    System.out.println(e.getMessage()); // "Parcel not found!"
                    return;
                } catch (ParcelAlreadyDeliveredException e) {
                    System.out.println(MSG_ALREADY_DELIVERED); // "Parcel already delivered!"
                    return;
                }
                step = 1;
            } else {
                System.out.println("Current service: " + parcel.getDeliveryStrategy().getName()
                        + " (" + Formatter.money(parcel.getDeliveryFee()) + ")");
                String answer = askService("Select new delivery service:");
                if (InputReader.isBack(answer)) {
                    step = 0; // back to the tracking number question
                    continue;
                }
                DeliveryStrategy newService = services[Integer.parseInt(answer) - 1];

                try {
                    // STRATEGY PATTERN: swap the algorithm at runtime.
                    double newFee = manager.changeDeliveryService(parcel.getTrackingNumber(), newService);
                    System.out.println(MSG_SERVICE_CHANGED + Formatter.money(newFee));
                } catch (SameDayNotAllowedException e) {
                    System.out.println(e.getMessage()); // the parcel keeps its old service
                } catch (ParcelNotFoundException e) {
                    System.out.println(e.getMessage());
                } catch (ParcelAlreadyDeliveredException e) {
                    System.out.println(MSG_ALREADY_DELIVERED);
                }
                return;
            }
        }
    }

    // ==================================================================
    // 4. Mark as Delivered
    // ==================================================================

    private void markAsDelivered() {
        printTitle("MARK AS DELIVERED");

        String trackingNumber = input.ask("Enter tracking number: ", Validator::checkTrackingNumber);
        if (InputReader.isBack(trackingNumber)) {
            return; // \ on the first question - back to the main menu
        }

        try {
            manager.markAsDelivered(trackingNumber);
            System.out.println(MSG_DELIVERED);              // "Parcel delivered successfully!"
        } catch (ParcelNotFoundException e) {
            System.out.println(e.getMessage());             // "Parcel not found!"
        } catch (ParcelAlreadyDeliveredException e) {
            System.out.println(MSG_WAS_ALREADY_DELIVERED);  // "Parcel was already delivered."
        }
    }
}
