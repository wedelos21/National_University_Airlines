import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DatabaseService
 * - Owns in-memory flights list
 * - Loads from local CSV on startup
 * - Provides read APIs for UI
 * - Persists changes (autosave) after updates
 *
 * Usage:
 *   DatabaseService db = new DatabaseService("database.txt");
 *   List<Flight> flights = db.getFlights();
 *   db.updateSeat("F001", "12A", new Passenger("Alex","Kim","1990-05-04")); // autosaves
 */
public class DatabaseService {

    private final String dbPath;
    private final List<Flight> flights = new ArrayList<>();

    /** Loads data from dbPath; auto-creates file if missing (via FileStorage.read). */
    public DatabaseService(String dbPath) {
        this.dbPath = dbPath == null ? "database.txt" : dbPath;
        load(); // autoload on construction
    }

    /** Re-load from disk, replacing in-memory flights. */
    public final void load() {
        flights.clear();
        flights.addAll(FileStorage.read(dbPath));
    }

    /** Expose read-only list for UI binding. */
    public List<Flight> getFlights() {
        return Collections.unmodifiableList(flights);
    }

    /** Seats for a specific flight (read-only list); empty list if not found. */
    public List<Seat> getSeats(String flightId) {
        Flight f = findFlight(flightId);
        return f == null ? List.of() : f.getSeats(); // Flight#getSeats already unmodifiable
    }

    /**
     * Update (book or change) a seat's passenger.
     * - passenger == null -> releases the seat (AVAILABLE)
     * - otherwise -> BOOKED with provided passenger
     * Autosaves after successful update.
     * @return true if flight+seat found and updated
     */
    public boolean updateSeat(String flightId, String seatNumber, Passenger passenger) {
        Flight f = findFlight(flightId);
        if (f == null) return false;
        Seat seat = f.getSeat(seatNumber);
        if (seat == null) return false;

        if (passenger == null) {
            seat.clearPassenger();           // AVAILABLE
        } else {
            seat.setPassenger(passenger);    // BOOKED
        }
        save(); // AUTOSAVE
        return true;
    }

    /** Convenience wrappers */
    public boolean bookSeat(String flightId, String seatNumber, Passenger passenger) {
        return updateSeat(flightId, seatNumber, passenger);
    }
    public boolean releaseSeat(String flightId, String seatNumber) {
        return updateSeat(flightId, seatNumber, null);
    }

    /** Persist current flights to disk. */
    public boolean save() {
        try {
            FileStorage.write(dbPath, flights);
            return true;
        } catch (IOException e) {
            System.out.println("[DatabaseService] Save failed: " + e.getMessage());
            return false;
        }
    }

    // ---------- helpers ----------

    private Flight findFlight(String flightId) {
        if (flightId == null) return null;
        for (Flight f : flights) {
            if (flightId.equalsIgnoreCase(f.getId())) return f;
        }
        return null;
        }
}
