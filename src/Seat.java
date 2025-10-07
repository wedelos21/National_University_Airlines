/**
 * TEMPORARY STUB for Issue #2 compilation.
 * Replace with full implementation in Issue #3.
 */
public class Seat {
    private final String seatNumber;

    public Seat(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    @Override
    public String toString() {
        return "Seat{" + seatNumber + '}';
    }
}
