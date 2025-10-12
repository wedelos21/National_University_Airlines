import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * AddFlightDialog
 * ---------------
 * Validates and creates new flights via DatabaseService.addFlight(...).
 */
public class AddFlightDialog extends JDialog {
    private final DatabaseService db;
    private final JTextField idField = new JTextField(12);
    private final JTextField numberField = new JTextField(12);
    private final JTextField startRowField = new JTextField(6);
    private final JTextField endRowField = new JTextField(6);
    private final JTextField lettersField = new JTextField(12);

    private final JButton saveBtn = new JButton("Save");
    private final JButton cancelBtn = new JButton("Cancel");

    public AddFlightDialog(Frame owner, DatabaseService db) {
        super(owner, "Add Flight", true);
        this.db = db;
        initComponents();
        setSize(460, 340);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JLabel header = new JLabel("Add New Flight", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tooltips to guide the scheduler
        idField.setToolTipText("Flight ID (e.g., F003 or INTL-01; letters, numbers, dashes)");
        numberField.setToolTipText("Flight Number (e.g., NU310)");
        startRowField.setToolTipText("First row number (>= 1)");
        endRowField.setToolTipText("Last row number (>= start row, reasonable size)");
        lettersField.setToolTipText("Seat letters, uppercase, unique, up to 10 (e.g., ABCDEF)");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 6, 8, 6);
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
        String lettersRaw = lettersField.getText().trim();

        // Validate fields
        if (!ValidationUtils.isValidFlightId(id)) {
            warn("Flight ID is required and may only contain letters, numbers, and dashes (e.g., F003, INTL-01).");
            idField.requestFocusInWindow();
            return;
        }
        if (!ValidationUtils.isValidFlightNumber(number)) {
            warn("Flight Number must follow pattern NU### (e.g., NU310).");
            numberField.requestFocusInWindow();
            return;
        }

        int startRow, endRow;
        try {
            startRow = Integer.parseInt(startStr);
            endRow = Integer.parseInt(endStr);
        } catch (NumberFormatException ex) {
            warn("Row numbers must be integers.");
            startRowField.requestFocusInWindow();
            return;
        }
        if (!ValidationUtils.isValidRowRange(startRow, endRow)) {
            warn("Invalid row range. Start row must be ≥ 1, end row ≥ start row, and the total range reasonable (≤ 200 rows).");
            startRowField.requestFocusInWindow();
            return;
        }

        Set<Character> uniqueLetters = ValidationUtils.parseSeatLettersUnique(lettersRaw);
        if (uniqueLetters.isEmpty()) {
            warn("Seat letters must be uppercase A–Z, unique, and up to 10 characters (e.g., ABCDEF).");
            lettersField.requestFocusInWindow();
            return;
        }

        // Convert Set<Character> to char[]
        char[] letters = new char[uniqueLetters.size()];
        int i = 0;
        for (Character c : uniqueLetters) letters[i++] = c;

        // Call backend
        boolean ok = db.addFlight(id, number, startRow, endRow, letters);
        if (!ok) {
            warn("Could not add flight. It may already exist or inputs are invalid.");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Flight created:\n" +
                "ID: " + id + "\n" +
                "Number: " + number + "\n" +
                "Rows: " + startRow + "–" + endRow + "\n" +
                "Seats: " + new String(letters),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    public void openModal() {
        setVisible(true);
    }
}
