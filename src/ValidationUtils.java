import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class ValidationUtils {
    private ValidationUtils() {}

    /** Non-empty, trims, allows letters, spaces, hyphens, apostrophes. */
    public static boolean isValidName(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (t.isEmpty()) return false;
        // Allow letters, spaces, hyphens, apostrophes (e.g., O'Neil, Anne-Marie)
        return t.matches("[A-Za-z\\p{L}][A-Za-z\\p{L}'\\- ]*");
    }

    /** yyyy-MM-dd, real calendar date, not in the future. */
    public static boolean isValidDobIso(String s) {
        if (s == null || !s.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try {
            LocalDate d = LocalDate.parse(s);
            return !d.isAfter(LocalDate.now());
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
