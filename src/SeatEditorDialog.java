import javax.swing.*;
import java.awt.*;

/**
 * TEMPORARY for Issue #8.
 * - Shows selected seat info
 * - No editing/saving yet (Issue #9 will implement the form + validation + autosave)
 */
public class SeatEditorDialog extends JDialog {
    private final DatabaseService db;
    private final String flightId;
    private final String flightNumber;
    private final String seatNumber;

    public SeatEditorDialog(Frame owner, DatabaseService db, String flightId, String flightNumber, String seatNumber) {
        super(owner, "Edit Seat – " + seatNumber + " / " + flightNumber, true);
        this.db = db;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;

        initComponents();
        setSize(420, 220);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JLabel info = new JLabel(
            "<html><body style='padding:8px;'>"
            + "<b>Flight:</b> " + flightNumber + " (" + flightId + ")<br>"
            + "<b>Seat:</b> " + seatNumber + "<br>"
            + "<i>(Seat editing form will be implemented in Issue #9.)</i>"
            + "</body></html>"
        );

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(close);

        setLayout(new BorderLayout());
        add(info, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    public void openModal() {
        setVisible(true);
    }
}
