import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * SeatEditorDialog
 * - Modal dialog to edit passenger info for a seat
 * - Supports two paths:
 *   1) Book seat: requires First, Last, DOB (yyyy-MM-dd) -> autosave
 *   2) Release seat: clears passenger, marks AVAILABLE -> autosave
 */
public class SeatEditorDialog extends JDialog {
    private final DatabaseService db;
    private final String flightId;
    private final String flightNumber;
    private final String seatNumber;

    // UI fields
    private final JTextField firstField = new JTextField(18);
    private final JTextField lastField  = new JTextField(18);
    private final JTextField dobField   = new JTextField(10); // yyyy-MM-dd

    private final JCheckBox releaseCheck = new JCheckBox("Mark seat AVAILABLE (release booking)");

    private final JButton saveBtn   = new JButton("Save");
    private final JButton cancelBtn = new JButton("Cancel");

    public SeatEditorDialog(Frame owner, DatabaseService db, String flightId, String flightNumber, String seatNumber) {
        super(owner, "Edit Seat – " + seatNumber + " / " + flightNumber, true);
        this.db = db;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;

        initComponents();
        loadExistingValues();
        setSize(480, 280);
        setLocationRelativeTo(owner);
        wireKeyShortcuts();
    }

    private void initComponents() {
        // Header
        JLabel header = new JLabel("Flight " + flightNumber + " (" + flightId + ") – Seat " + seatNumber, SwingConstants.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(4, 4, 4, 8); gc.anchor = GridBagConstraints.LINE_END;
        form.add(new JLabel("First Name:"), gc);
        gc.gridy++;
        form.add(new JLabel("Last Name:"), gc);
        gc.gridy++;
        form.add(new JLabel("Date of Birth:"), gc);

        gc.gridx = 1; gc.gridy = 0; gc.anchor = GridBagConstraints.LINE_START;
        form.add(firstField, gc);
        gc.gridy++;
        form.add(lastField, gc);
        gc.gridy++;
        JPanel dobRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dobRow.add(dobField);
        dobRow.add(new JLabel("   (yyyy-MM-dd)"));
        form.add(dobRow, gc);

        // Release checkbox
        JPanel releasePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        releasePanel.add(releaseCheck);
        releaseCheck.addActionListener(e -> toggleReleaseMode());

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> onSave());

        // Layout
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout());
        center.add(form, BorderLayout.CENTER);
        center.add(releasePanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadExistingValues() {
        // Look up current seat to prefill fields if BOOKED
        List<Seat> seats = db.getSeats(flightId);
        for (Seat s : seats) {
            if (seatNumber.equalsIgnoreCase(s.getSeatNumber())) {
                if (s.getPassenger() != null) {
                    firstField.setText(s.getPassenger().getFirstName());
                    lastField.setText(s.getPassenger().getLastName());
                    dobField.setText(s.getPassenger().getDateOfBirth());
                } else {
                    firstField.setText("");
                    lastField.setText("");
                    dobField.setText("");
                }

                // Release Seat always start unchecked (scheduler must intentionally choose to release)
                releaseCheck.setSelected(false);
                toggleReleaseMode();

                // Reflect current status in the dialog title
                setTitle("Edit Seat – " + seatNumber + " / " + flightNumber + " [" + (s.isBooked() ? "BOOKED" : "AVAILABLE") + "]");
                break;
            }
        }
    }

    private void toggleReleaseMode() {
        boolean releasing = releaseCheck.isSelected();
        firstField.setEnabled(!releasing);
        lastField.setEnabled(!releasing);
        dobField.setEnabled(!releasing);
        if (releasing) {
            // dim placeholders when releasing (optional UX)
            firstField.putClientProperty("JComponent.outline", "warning");
            lastField.putClientProperty("JComponent.outline", "warning");
            dobField.putClientProperty("JComponent.outline", "warning");
        } else {
            firstField.putClientProperty("JComponent.outline", null);
            lastField.putClientProperty("JComponent.outline", null);
            dobField.putClientProperty("JComponent.outline", null);
        }
    }

    private void onSave() {
        if (releaseCheck.isSelected()) {
            // RELEASE seat
            boolean ok = db.releaseSeat(flightId, seatNumber);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Failed to release seat. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            return;
        }

        // BOOK seat: validate fields
        String first = firstField.getText().trim();
        String last  = lastField.getText().trim();
        String dob   = dobField.getText().trim();

        if (first.isEmpty() || last.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First and Last name are required when booking a seat.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!isValidIsoDate(dob)) {
            JOptionPane.showMessageDialog(this, "Date of Birth must be a valid date in yyyy-MM-dd format (e.g., 1990-05-04).", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Passenger p = new Passenger(first, last, dob);
        boolean ok = db.bookSeat(flightId, seatNumber, p); // autosaves inside DatabaseService
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to book seat. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
    }

    private boolean isValidIsoDate(String s) {
        if (s == null || !s.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try {
            LocalDate.parse(s); // ensures it's a real date (e.g., not 2025-02-30)
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private void wireKeyShortcuts() {
        // Enter = Save, Esc = Cancel
        getRootPane().setDefaultButton(saveBtn);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void openModal() {
        setVisible(true);
    }
}
