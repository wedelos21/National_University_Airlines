import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * SeatsFrame
 * - Displays seats for the selected flight in a table
 * - Open editor on button or double-click
 * - Back to Home
 */
public class SeatsFrame extends JFrame {
    private final DatabaseService db;
    private final String flightId;
    private final String flightNumber;

    private final JTable table;
    private final DefaultTableModel model;
    private final JButton editBtn = new JButton("Edit Seat");
    private final JButton backBtn = new JButton("Back");
    private final JButton refreshBtn = new JButton("Refresh");

    public SeatsFrame(DatabaseService db, String flightId, String flightNumber) {
        super("Seats – " + flightNumber + " (" + flightId + ")");
        this.db = db;
        this.flightId = flightId;
        this.flightNumber = flightNumber;

        // Table model (non-editable cells)
        model = new DefaultTableModel(new Object[]{"Seat", "Status", "Passenger", "DOB"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.setAutoCreateRowSorter(true);

        initComponents();
        loadSeats();

        setSize(800, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        // Header
        JLabel header = new JLabel("Flight " + flightNumber + " (" + flightId + ")", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Actions
        editBtn.setEnabled(false);
        editBtn.addActionListener(e -> openEditorForSelected());

        backBtn.addActionListener(e -> {
            new HomeFrame(db).setVisible(true);
            dispose();
        });

        refreshBtn.addActionListener(e -> loadSeats());

        // Enable Edit button when a row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                editBtn.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        // Double-click row to open editor
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openEditorForSelected();
                }
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshBtn);
        south.add(backBtn);
        south.add(editBtn);

        setLayout(new BorderLayout(8, 8));
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private void loadSeats() {
        // Clear table
        model.setRowCount(0);

        List<Seat> seats = db.getSeats(flightId);
        for (Seat s : seats) {
            String name = s.getPassenger() == null ? "" : s.getPassenger().getFullName();
            String dob  = s.getPassenger() == null ? "" : s.getPassenger().getDateOfBirth();
            model.addRow(new Object[]{ s.getSeatNumber(), s.getStatus().name(), name, dob });
        }
        editBtn.setEnabled(false);
    }

    private void openEditorForSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int row = table.convertRowIndexToModel(viewRow);
        String seatNumber = (String) model.getValueAt(row, 0);

        // Open the (temporary) editor; Issue #9 will implement full editing & save
        SeatEditorDialog dlg = new SeatEditorDialog(this, db, flightId, flightNumber, seatNumber);
        dlg.openModal();

        // After dialog closes, refresh view (later this will reflect changes)
        loadSeats();
    }
}
