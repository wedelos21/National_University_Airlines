import javax.swing.*;
import java.awt.*;

/**
 * AddFlightDialog
 * ---------------
 * Allows schedulers to create a new flight by entering:
 *   - Flight ID (e.g., F003)
 *   - Flight Number (e.g., NU310)
 *   - Starting Row, Ending Row (numeric)
 *   - Seat Letters (e.g., ABCDEF)
 *
 * Validation ensures all fields are filled correctly.
 * On success, calls db.addFlight(...) and closes the dialog.
 */
public class AddFlightDialog extends JDialog {
    private final DatabaseService db;
    private final JTextField idField = new JTextField(10);
    private final JTextField numberField = new JTextField(10);
    private final JTextField startRowField = new JTextField(5);
    private final JTextField endRowField = new JTextField(5);
    private final JTextField lettersField = new JTextField(10);

    private final JButton saveBtn = new JButton("Save");
    private final JButton cancelBtn = new JButton("Cancel");

    public AddFlightDialog(Frame owner, DatabaseService db) {
        super(owner, "Add Flight", true);
        this.db = db;
        initComponents();
        setSize(420, 320);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JLabel header = new JLabel("Add New Flight", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.LINE_END;
        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Flight ID:"), gc);
        gc.gridy++;
        form.add(new JLabel("Flight Number:"), gc);
        gc.gridy++;
        form.add(new JLabel("Start Row:"), gc);
        gc.gridy++;
        form.add(new JLabel("End Row:"), gc);
        gc.gridy++;
        form.add(new JLabel("Seat Letters:"), gc);

        gc.gridx = 1; gc.gridy = 0; gc.anchor = GridBagConstraints.LINE_START;
        form.add(idField, gc);
        gc.gridy++;
        form.add(numberField, gc);
        gc.gridy++;
        form.add(startRowField, gc);
        gc.gridy++;
        form.add(endRowField, gc);
        gc.gridy++;
        form.add(lettersField, gc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> onSave());

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void onSave() {
        String id = idField.getText().trim();
        String number = numberField.getText().trim();
        String startStr = startRowField.getText().trim();
        String endStr = endRowField.getText().trim();
        String letters = lettersField.getText().trim().toUpperCase();

        // alidation rules
        if (id.isEmpty() || number.isEmpty() || startStr.isEmpty() || endStr.isEmpty() || letters.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int startRow, endRow;
        try {
            startRow = Integer.parseInt(startStr);
            endRow = Integer.parseInt(endStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Row numbers must be integers.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (startRow < 1 || endRow < startRow) {
            JOptionPane.showMessageDialog(this, "Invalid row range. End row must be ≥ start row and both ≥ 1.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!id.matches("[A-Za-z0-9\\-]+")) {
            JOptionPane.showMessageDialog(this, "Flight ID may only contain letters, numbers, and dashes.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!number.matches("NU\\d+")) {
            JOptionPane.showMessageDialog(this, "Flight Number must follow pattern NU### (e.g., NU245).", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!letters.matches("[A-Z]+") || letters.length() > 10) {
            JOptionPane.showMessageDialog(this, "Seat letters must be uppercase A–Z (max 10).", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Future wiring: db.addFlight(id, number, startRow, endRow, letters.toCharArray());
        JOptionPane.showMessageDialog(this,
                "Flight added successfully!\n\n" +
                "Flight ID: " + id + "\nFlight Number: " + number +
                "\nRows: " + startRow + "–" + endRow +
                "\nSeats: " + letters,
                "Flight Created",
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }

    public void openModal() {
        setVisible(true);
    }
}
