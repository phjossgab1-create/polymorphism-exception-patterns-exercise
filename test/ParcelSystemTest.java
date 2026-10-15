import exception.DuplicateTrackingNumberException;
import exception.InvalidInputException;
import exception.InvalidParcelException;
import exception.ParcelAlreadyDeliveredException;
import exception.ParcelException;
import exception.ParcelNotFoundException;
import exception.SameDayNotAllowedException;
import model.Box;
import model.Document;
import model.Fragile;
import model.Parcel;
import model.ParcelStatus;
import service.ParcelManager;
import strategy.DeliveryStrategy;
import strategy.ExpressDelivery;
import strategy.SameDayDelivery;
import strategy.StandardDelivery;
import util.Formatter;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Self-checking tests for the Parcel Delivery System (no libraries needed).
 *
 * Run in VS Code with the "Run Tests" launch configuration, or from a terminal:
 *     javac -d bin -sourcepath src test/ParcelSystemTest.java
 *     java -cp bin ParcelSystemTest
 *
 * Every line prints PASS or FAIL, followed by a summary at the end.
 */
public class ParcelSystemTest {

    private static int passed = 0;
    private static int failed = 0;

    // ------------------------------------------------------------------
    // Tiny test helpers
    // ------------------------------------------------------------------

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static void checkEquals(String name, String expected, String actual) {
        boolean ok = expected.equals(actual);
        check(name + "  (expected \"" + expected + "\", got \"" + actual + "\")", ok);
    }

    /** A piece of code that is expected to throw an exception. */
    private interface Action {
        void run() throws Exception;
    }

    private static void expectThrows(String name, Class<? extends Exception> expected, Action action) {
        try {
            action.run();
            check(name + "  (expected " + expected.getSimpleName() + ", nothing was thrown)", false);
        } catch (Exception e) {
            check(name + "  (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")",
                    expected.isInstance(e));
        }
    }

    private static void expectValid(String name, Action action) {
        try {
            action.run();
            check(name, true);
        } catch (Exception e) {
            check(name + "  (unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")", false);
        }
    }

    private static String fee(Parcel p) {
        return Formatter.money(p.getDeliveryFee());
    }

    // ------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        testAssignmentExample();
        testDocumentFees();
        testBoxChargeableWeight();
        testSameDayRule();
        testWeightLimits();
        testValidatorTrackingNumber();
        testValidatorReceiverName();
        testValidatorWeight();
        testValidatorPagesAndDimensions();
        testValidatorHandlingNoteAndMenu();
        testManagerSingletonAndExceptions();
        testPolymorphism();

        System.out.println();
        System.out.println("========================================");
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        System.out.println("========================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------

    private static void testAssignmentExample() throws Exception {
        System.out.println("\n== Assignment example: FRAGILE, 40 x 20 x 10 cm, 1.8 kg, EXPRESS ==");
        Parcel p = new Fragile("ABC0001", "Juan Dela Cruz", 1.8, 40, 20, 10, "Handle with care");
        Box asBox = (Box) p;
        check("volumetric weight = 8000 / 3500 = 2.29", Math.abs(asBox.getVolumetricWeight() - 2.2857) < 0.001);
        check("chargeable weight = 3 kg", p.getChargeableWeight() == 3.0);
        check("base fee = (40 x 3) + 100 = 220", p.getBaseFee() == 220.0);
        p.setDeliveryStrategy(new ExpressDelivery());
        checkEquals("delivery fee", "Php 330.00", fee(p));
    }

    private static void testDocumentFees() throws Exception {
        System.out.println("\n== Document: base fee Php 50 flat, chargeable = actual weight ==");
        Parcel d = new Document("ABC0101", "Maria Santos", 0.3, 10);
        check("chargeable weight = actual weight (0.3)", d.getChargeableWeight() == 0.3);
        check("base fee = 50", d.getBaseFee() == 50.0);
        checkEquals("Standard fee", "Php 50.00", fee(d));
        d.setDeliveryStrategy(new ExpressDelivery());
        checkEquals("Express fee (50 x 1.5)", "Php 75.00", fee(d));
        d.setDeliveryStrategy(new SameDayDelivery());
        checkEquals("Same Day fee (50 x 2 + 100)", "Php 200.00", fee(d));
    }

    private static void testBoxChargeableWeight() throws Exception {
        System.out.println("\n== Box: chargeable weight rounded UP (2.0 -> 2, 2.1 -> 3) ==");
        Parcel exact = new Box("ABC0201", "Ana", 2.0, 10, 10, 10);
        check("actual 2.0 kg, small volume -> 2 kg", exact.getChargeableWeight() == 2.0);
        Parcel up = new Box("ABC0202", "Ana", 2.1, 10, 10, 10);
        check("actual 2.1 kg -> 3 kg", up.getChargeableWeight() == 3.0);
        Parcel volumeExact = new Box("ABC0203", "Ana", 0.5, 35, 10, 10);
        check("volumetric exactly 3500/3500 = 1.0 -> 1 kg", volumeExact.getChargeableWeight() == 1.0);
        checkEquals("base fee 40 x 1", "Php 40.00", fee(volumeExact));
        Parcel bigger = new Box("ABC0204", "Ana", 5, 30, 30, 30);
        check("volumetric 27000/3500 = 7.71 > 5 kg -> 8 kg", bigger.getChargeableWeight() == 8.0);
        checkEquals("Standard fee 40 x 8", "Php 320.00", fee(bigger));
        bigger.setDeliveryStrategy(new SameDayDelivery());
        checkEquals("Same Day fee (320 x 2 + 100)", "Php 740.00", fee(bigger));
        Parcel heavier = new Box("ABC0205", "Ana", 12.5, 10, 10, 10);
        check("actual 12.5 kg beats volumetric -> 13 kg", heavier.getChargeableWeight() == 13.0);

        Parcel f = new Fragile("ABC0206", "Ana", 2.0, 10, 10, 10, "Glass");
        check("Fragile base fee = Box base fee + 100 (80 + 100)", f.getBaseFee() == 180.0);
        Parcel huge = new Box("ABC0207", "Ana", 1, 2000, 2000, 2000);
        check("very large dimensions do not overflow", huge.getChargeableWeight() == Math.ceil(8.0e9 / 3500));
    }

    private static void testSameDayRule() throws Exception {
        System.out.println("\n== Same Day only for chargeable weight <= 10 kg ==");
        final Parcel tenKg = new Box("ABC0301", "Ana", 1, 50, 35, 20); // 35000/3500 = 10.0
        check("chargeable weight exactly 10 kg", tenKg.getChargeableWeight() == 10.0);
        expectValid("Same Day allowed at exactly 10 kg", new Action() {
            public void run() throws Exception { tenKg.setDeliveryStrategy(new SameDayDelivery()); }
        });
        checkEquals("Same Day fee (400 x 2 + 100)", "Php 900.00", fee(tenKg));

        final Parcel light = new Box("ABC0302", "Ana", 2, 50, 50, 50); // 125000/3500 = 35.71 -> 36
        check("2 kg box but volumetric 35.71 -> 36 kg", light.getChargeableWeight() == 36.0);
        expectThrows("Same Day refused for 36 kg chargeable", SameDayNotAllowedException.class, new Action() {
            public void run() throws Exception { light.setDeliveryStrategy(new SameDayDelivery()); }
        });
        check("refused parcel keeps its old service (Standard)",
                light.getDeliveryStrategy() instanceof StandardDelivery);

        final Parcel justOver = new Box("ABC0303", "Ana", 10.1, 10, 10, 10); // -> 11 kg
        expectThrows("Same Day refused for 10.1 kg (-> 11 kg)", SameDayNotAllowedException.class, new Action() {
            public void run() throws Exception { justOver.setDeliveryStrategy(new SameDayDelivery()); }
        });
        try {
            justOver.setDeliveryStrategy(new SameDayDelivery());
        } catch (SameDayNotAllowedException e) {
            checkEquals("exact Same Day message",
                    "Same Day delivery is only for parcels up to 10 kg!", e.getMessage());
        }
    }

    private static void testWeightLimits() {
        System.out.println("\n== Maximum weight per type ==");
        expectValid("Document 0.5 kg accepted", new Action() {
            public void run() throws Exception { new Document("ABC0401", "Ana", 0.5, 1); }
        });
        expectThrows("Document 0.51 kg rejected", InvalidParcelException.class, new Action() {
            public void run() throws Exception { new Document("ABC0402", "Ana", 0.51, 1); }
        });
        expectValid("Box 50 kg accepted", new Action() {
            public void run() throws Exception { new Box("ABC0403", "Ana", 50, 1, 1, 1); }
        });
        expectThrows("Box 50.01 kg rejected", InvalidParcelException.class, new Action() {
            public void run() throws Exception { new Box("ABC0404", "Ana", 50.01, 1, 1, 1); }
        });
        expectValid("Fragile 30 kg accepted", new Action() {
            public void run() throws Exception { new Fragile("ABC0405", "Ana", 30, 1, 1, 1, "Glass"); }
        });
        expectThrows("Fragile 30.1 kg rejected (max is 30, not Box's 50)", InvalidParcelException.class, new Action() {
            public void run() throws Exception { new Fragile("ABC0406", "Ana", 30.1, 1, 1, 1, "Glass"); }
        });
        expectThrows("weight 0 rejected", InvalidParcelException.class, new Action() {
            public void run() throws Exception { new Box("ABC0407", "Ana", 0, 1, 1, 1); }
        });
        expectThrows("Document with 0 pages rejected", InvalidParcelException.class, new Action() {
            public void run() throws Exception { new Document("ABC0408", "Ana", 0.2, 0); }
        });
        expectThrows("Fragile with blank note rejected", InvalidParcelException.class, new Action() {
            public void run() throws Exception { new Fragile("ABC0409", "Ana", 1, 1, 1, 1, "   "); }
        });
        try {
            new Document("ABC0410", "Ana", 0.6, 1);
        } catch (InvalidParcelException e) {
            check("weight error says the maximum (" + e.getMessage() + ")", e.getMessage().contains("0.5 kg"));
            checkEquals("exception carries the field name", "Weight", e.getFieldName());
        }
        try {
            new Fragile("ABC0411", "Ana", 31, 1, 1, 1, "Glass");
        } catch (InvalidParcelException e) {
            check("Fragile error says 30 kg (" + e.getMessage() + ")", e.getMessage().contains("30 kg"));
        }
    }

    private static void testValidatorTrackingNumber() {
        System.out.println("\n== Tracking number: uppercase ABC + exactly 4 digits ==");
        String[] valid = { "ABC0001", "ABC9999", "ABC1234" };
        for (final String s : valid) {
            expectValid("valid \"" + s + "\"", new Action() {
                public void run() throws Exception { Validator.checkTrackingNumber(s); }
            });
        }
        String[] invalid = { "", "abc0001", "Abc0001", "ABC001", "ABC00001", "ABC000A",
                             " ABC0001", "ABC0001 ", "XYZ0001", "ABC-0001", "ABC" };
        for (final String s : invalid) {
            expectThrows("invalid \"" + s + "\"", InvalidParcelException.class, new Action() {
                public void run() throws Exception { Validator.checkTrackingNumber(s); }
            });
        }
    }

    private static void testValidatorReceiverName() {
        System.out.println("\n== Receiver name: letters only, one space between words ==");
        String[] valid = { "Juan", "Juan Dela Cruz", "Maria Clara", "Peña" };
        for (final String s : valid) {
            expectValid("valid \"" + s + "\"", new Action() {
                public void run() throws Exception { Validator.checkReceiverName(s); }
            });
        }
        String[] invalid = { "", "   ", "Juan  Cruz", " Juan", "Juan ", "Juan2", "Juan-Cruz",
                             "J.Cruz", "Juan\tCruz", "123" };
        for (final String s : invalid) {
            expectThrows("invalid \"" + s.replace("\t", "\\t") + "\"", InvalidParcelException.class, new Action() {
                public void run() throws Exception { Validator.checkReceiverName(s); }
            });
        }
    }

    private static void testValidatorWeight() throws Exception {
        System.out.println("\n== Weight: digits + optional decimal point, > 0, <= max ==");
        String[] valid = { "1.8", "2", "0.5", "50", "10.25", ".5", "5." };
        for (final String s : valid) {
            expectValid("valid \"" + s + "\" for a Box", new Action() {
                public void run() throws Exception { Validator.parseWeight(s, Box.MAX_WEIGHT, "Box"); }
            });
        }
        String[] invalid = { "", " ", "abc", "1.8kg", "-1", "+1", " 1.8", "1.8 ", "1,8", "1.2.3",
                             ".", "0", "0.0", "000", "50.01", "51", "1e3",
                             "99999999999999999999999999999999999999" };
        for (final String s : invalid) {
            expectThrows("invalid \"" + s + "\" for a Box", InvalidParcelException.class, new Action() {
                public void run() throws Exception { Validator.parseWeight(s, Box.MAX_WEIGHT, "Box"); }
            });
        }
        check("parses \"1.8\" to 1.8", Validator.parseWeight("1.8", 50, "Box") == 1.8);
    }

    private static void testValidatorPagesAndDimensions() throws Exception {
        System.out.println("\n== Pages (1-100) and dimensions (> 0, no leading zeros) ==");
        String[] validPages = { "1", "50", "100" };
        for (final String s : validPages) {
            expectValid("valid pages \"" + s + "\"", new Action() {
                public void run() throws Exception { Validator.parsePages(s); }
            });
        }
        String[] invalidPages = { "", "0", "101", "-5", "1.5", "ten", " 5", "5 ", "99999999999" };
        for (final String s : invalidPages) {
            expectThrows("invalid pages \"" + s + "\"", InvalidParcelException.class, new Action() {
                public void run() throws Exception { Validator.parsePages(s); }
            });
        }

        String[] validDims = { "1", "40", "3500", "2147483647" };
        for (final String s : validDims) {
            expectValid("valid length \"" + s + "\"", new Action() {
                public void run() throws Exception { Validator.parseDimension(s, "Length"); }
            });
        }
        String[] invalidDims = { "", "0", "00", "040", "01", "-4", "4.5", "4 ", " 4", "four",
                                 "2147483648", "99999999999" };
        for (final String s : invalidDims) {
            expectThrows("invalid length \"" + s + "\"", InvalidParcelException.class, new Action() {
                public void run() throws Exception { Validator.parseDimension(s, "Length"); }
            });
        }
        try {
            Validator.parseDimension("99999999999", "Height");
        } catch (InvalidParcelException e) {
            check("too-large number keeps the original NumberFormatException as the cause",
                    e.getCause() instanceof NumberFormatException);
        }
    }

    private static void testValidatorHandlingNoteAndMenu() {
        System.out.println("\n== Handling note and exact menu choices ==");
        expectValid("note \"Handle with care\"", new Action() {
            public void run() throws Exception { Validator.checkHandlingNote("Handle with care"); }
        });
        expectThrows("note \"\" rejected", InvalidParcelException.class, new Action() {
            public void run() throws Exception { Validator.checkHandlingNote(""); }
        });
        expectThrows("note \"    \" rejected", InvalidParcelException.class, new Action() {
            public void run() throws Exception { Validator.checkHandlingNote("    "); }
        });

        for (int i = 1; i <= 5; i++) {
            final String s = String.valueOf(i);
            expectValid("menu choice \"" + s + "\"", new Action() {
                public void run() throws Exception { Validator.parseMenuChoice(s, 5); }
            });
        }
        String[] invalid = { "", "0", "6", "01", " 1", "1 ", "1a", "one", "1.0", "+1", "\\\\" };
        for (final String s : invalid) {
            expectThrows("menu choice \"" + s + "\" rejected", InvalidInputException.class, new Action() {
                public void run() throws Exception { Validator.parseMenuChoice(s, 5); }
            });
        }
    }

    private static void testManagerSingletonAndExceptions() throws Exception {
        System.out.println("\n== ParcelManager: Singleton + custom exceptions ==");
        final ParcelManager m1 = ParcelManager.getInstance();
        ParcelManager m2 = ParcelManager.getInstance();
        check("getInstance() always returns the same object", m1 == m2);

        int before = m1.getParcelCount();
        m1.addParcel(new Document("ABC5001", "Ana Cruz", 0.4, 12));
        check("parcel added", m1.getParcelCount() == before + 1);

        expectThrows("duplicate tracking number rejected", DuplicateTrackingNumberException.class, new Action() {
            public void run() throws Exception { m1.addParcel(new Box("ABC5001", "Ben", 1, 1, 1, 1)); }
        });
        expectThrows("checkTrackingNumberAvailable detects duplicate", DuplicateTrackingNumberException.class,
                new Action() {
                    public void run() throws Exception { m1.checkTrackingNumberAvailable("ABC5001"); }
                });
        expectThrows("unknown tracking number -> ParcelNotFoundException", ParcelNotFoundException.class,
                new Action() {
                    public void run() throws Exception { m1.findParcel("ABC9998"); }
                });
        try {
            m1.findParcel("ABC9998");
        } catch (ParcelNotFoundException e) {
            checkEquals("exact not-found message", "Parcel not found!", e.getMessage());
        }

        double newFee = m1.changeDeliveryService("ABC5001", new ExpressDelivery());
        checkEquals("change service -> new fee", "Php 75.00", Formatter.money(newFee));

        m1.addParcel(new Box("ABC5002", "Ben Reyes", 2, 50, 50, 50)); // 36 kg chargeable
        expectThrows("change to Same Day refused for 36 kg", SameDayNotAllowedException.class, new Action() {
            public void run() throws Exception { m1.changeDeliveryService("ABC5002", new SameDayDelivery()); }
        });
        check("refused change keeps the old service",
                m1.findParcel("ABC5002").getDeliveryStrategy() instanceof StandardDelivery);

        m1.markAsDelivered("ABC5001");
        check("status is Delivered", m1.findParcel("ABC5001").getStatus() == ParcelStatus.DELIVERED);
        expectThrows("mark delivered twice -> ParcelAlreadyDeliveredException",
                ParcelAlreadyDeliveredException.class, new Action() {
                    public void run() throws Exception { m1.markAsDelivered("ABC5001"); }
                });
        expectThrows("change service of delivered parcel -> ParcelAlreadyDeliveredException",
                ParcelAlreadyDeliveredException.class, new Action() {
                    public void run() throws Exception { m1.changeDeliveryService("ABC5001", new StandardDelivery()); }
                });
        expectThrows("findPendingParcel on delivered parcel", ParcelAlreadyDeliveredException.class, new Action() {
            public void run() throws Exception { m1.findPendingParcel("ABC5001"); }
        });
        expectThrows("mark unknown parcel -> ParcelNotFoundException", ParcelNotFoundException.class, new Action() {
            public void run() throws Exception { m1.markAsDelivered("ABC9997"); }
        });
        expectThrows("getAllParcels() list is read-only", UnsupportedOperationException.class, new Action() {
            public void run() throws Exception { m1.getAllParcels().clear(); }
        });

        // Every custom exception extends the one base exception.
        check("all custom exceptions extend ParcelException",
                ParcelException.class.isAssignableFrom(InvalidParcelException.class)
                && ParcelException.class.isAssignableFrom(InvalidInputException.class)
                && ParcelException.class.isAssignableFrom(DuplicateTrackingNumberException.class)
                && ParcelException.class.isAssignableFrom(ParcelNotFoundException.class)
                && ParcelException.class.isAssignableFrom(ParcelAlreadyDeliveredException.class)
                && ParcelException.class.isAssignableFrom(SameDayNotAllowedException.class));
    }

    private static void testPolymorphism() throws Exception {
        System.out.println("\n== Polymorphism: one List<Parcel>, each object uses its own methods ==");
        List<Parcel> parcels = new ArrayList<Parcel>();
        parcels.add(new Document("ABC6001", "Ana", 0.2, 5));
        parcels.add(new Box("ABC6002", "Ben", 3, 10, 10, 10));
        parcels.add(new Fragile("ABC6003", "Cy", 3, 10, 10, 10, "Glass"));

        String[] expectedTypes = { "Document", "Box", "Fragile" };
        double[] expectedMax = { 0.5, 50, 30 };
        double[] expectedBase = { 50, 120, 220 };
        for (int i = 0; i < parcels.size(); i++) {
            Parcel p = parcels.get(i);
            checkEquals("getType()", expectedTypes[i], p.getType());
            check(expectedTypes[i] + " getMaxWeight() = " + expectedMax[i], p.getMaxWeight() == expectedMax[i]);
            check(expectedTypes[i] + " getBaseFee() = " + expectedBase[i], p.getBaseFee() == expectedBase[i]);
        }

        DeliveryStrategy[] services = { new StandardDelivery(), new ExpressDelivery(), new SameDayDelivery() };
        String[] expectedNames = { "Standard", "Express", "Same Day" };
        double[] expectedFees = { 220, 330, 540 };
        for (int i = 0; i < services.length; i++) {
            checkEquals("strategy name", expectedNames[i], services[i].getName());
            check(expectedNames[i] + " fee on base 220 = " + expectedFees[i],
                    services[i].calculateFee(220) == expectedFees[i]);
        }
    }
}
