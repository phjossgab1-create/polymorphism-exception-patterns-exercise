package service;

import exception.DuplicateTrackingNumberException;
import exception.ParcelAlreadyDeliveredException;
import exception.ParcelNotFoundException;
import exception.SameDayNotAllowedException;
import model.Parcel;
import strategy.DeliveryStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores and manages all parcels.
 *
 * SINGLETON PATTERN:
 * - Exactly ONE ParcelManager exists in the whole program, so every part of
 *   the program sees the same list of parcels.
 * - private constructor: nobody outside this class can write "new ParcelManager()".
 * - static getInstance(): the single global access point.
 * - Thread-safe version using double-checked locking with a volatile field.
 *
 * POLYMORPHISM: the manager only knows the abstract type Parcel. It stores
 * Documents, Boxes and Fragiles in the same Map<String, Parcel>.
 *
 * ENCAPSULATION: the map is private; outside code gets a read-only copy
 * from getAllParcels(), so it cannot add or remove parcels behind our back.
 *
 * EXCEPTION HANDLING: methods declare the checked exceptions they may throw
 * with "throws", and the menu handles them with try/catch.
 */
public class ParcelManager {

    // SINGLETON: the one and only instance.
    // volatile makes sure every thread sees the fully created object.
    private static volatile ParcelManager instance;

    // LinkedHashMap keeps parcels in the order they were added
    // and finds a tracking number quickly.
    private final Map<String, Parcel> parcels;

    // SINGLETON: private constructor - no "new ParcelManager()" from outside.
    private ParcelManager() {
        parcels = new LinkedHashMap<String, Parcel>();
    }

    /**
     * SINGLETON: global access point (double-checked locking).
     * The first call creates the instance; every later call returns the same one.
     */
    public static ParcelManager getInstance() {
        if (instance == null) {
            synchronized (ParcelManager.class) {
                if (instance == null) {
                    instance = new ParcelManager();
                }
            }
        }
        return instance;
    }

    /** True if a parcel with this tracking number already exists. */
    public boolean isTrackingNumberTaken(String trackingNumber) {
        return parcels.containsKey(trackingNumber);
    }

    /**
     * Used while the user is typing a new tracking number.
     *
     * @throws DuplicateTrackingNumberException if the number is already used
     */
    public void checkTrackingNumberAvailable(String trackingNumber)
            throws DuplicateTrackingNumberException {
        if (isTrackingNumberTaken(trackingNumber)) {
            throw new DuplicateTrackingNumberException(trackingNumber);
        }
    }

    /**
     * Adds a new parcel.
     *
     * @throws DuplicateTrackingNumberException if the tracking number is already used
     */
    public void addParcel(Parcel parcel) throws DuplicateTrackingNumberException {
        if (parcel == null) {
            throw new IllegalArgumentException("Parcel cannot be null.");
        }
        checkTrackingNumberAvailable(parcel.getTrackingNumber());
        parcels.put(parcel.getTrackingNumber(), parcel);
    }

    /**
     * Finds a parcel by tracking number.
     *
     * @throws ParcelNotFoundException if no parcel has this tracking number
     */
    public Parcel findParcel(String trackingNumber) throws ParcelNotFoundException {
        Parcel parcel = parcels.get(trackingNumber);
        if (parcel == null) {
            throw new ParcelNotFoundException(trackingNumber);
        }
        return parcel;
    }

    /**
     * Finds a parcel that can still be changed (status Pending).
     *
     * @throws ParcelNotFoundException         if no parcel has this tracking number
     * @throws ParcelAlreadyDeliveredException if the parcel is already delivered
     */
    public Parcel findPendingParcel(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        if (parcel.isDelivered()) {
            throw new ParcelAlreadyDeliveredException(trackingNumber);
        }
        return parcel;
    }

    /** A read-only copy of all parcels, in the order they were added. */
    public List<Parcel> getAllParcels() {
        return Collections.unmodifiableList(new ArrayList<Parcel>(parcels.values()));
    }

    public int getParcelCount() {
        return parcels.size();
    }

    /**
     * STRATEGY PATTERN: swaps the parcel's delivery service at runtime.
     * If the new service is not allowed, the parcel keeps its old service.
     *
     * @return the new delivery fee
     */
    public double changeDeliveryService(String trackingNumber, DeliveryStrategy newService)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException,
                   SameDayNotAllowedException {
        Parcel parcel = findParcel(trackingNumber);
        parcel.setDeliveryStrategy(newService);
        return parcel.getDeliveryFee();
    }

    /**
     * Marks a parcel as delivered.
     *
     * @throws ParcelNotFoundException         if no parcel has this tracking number
     * @throws ParcelAlreadyDeliveredException if it was already delivered
     */
    public void markAsDelivered(String trackingNumber)
            throws ParcelNotFoundException, ParcelAlreadyDeliveredException {
        Parcel parcel = findParcel(trackingNumber);
        parcel.markDelivered();
    }
}
