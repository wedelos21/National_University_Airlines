import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

/**
 * Home screen showing all flights. Selecting a flight opens SeatsFrame.
 * Adds: Delete Flight (with confirm). Uses reflection so it compiles before #16.
 */
public class HomeFrame extends JFrame {
    private final DatabaseService db;
    private final DefaultListModel<Flight> listModel = new DefaultListModel<>();
    private final JList<Flight> flightList = new JList<>(listModel);
    private final JButton openBtn = new JButton("Open Flight");
    private final JButton deleteBtn = new JButton("Delete Flight"); // NEW

    public HomeFrame(DatabaseService db) {
        super("National University Airlines");
        this.db = db;
        setJMenuBar(buildMenuBar());
        initComponents();
        loadFlights();
        setSize(680, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic('F');

        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic('E');
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
        exit.addActionListener(e -> confirmAndExit());

        file.add(exit);
        bar.add(file);
        return bar;
    }

    private void confirmAndExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Exit National University Airlines?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void initComponents() {
        // Header
        JLabel header = new JLabel("National University Airlines", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        // Flight list config
        flightList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightList.setVisibleRowCount(12);
        flightList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Flight f) {
                    l.setText(f.getId() + "  —  " + f.getFlightNumber());
                }
                return l;
            }
        });

        // Double-click to open
        flightList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedFlight();
            }
        });

        // Buttons
        openBtn.addActionListener(e -> openSelectedFlight());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> reloadFromDisk());

        deleteBtn.setEnabled(false); // enabled only when a flight is selected
        deleteBtn.addActionListener(e -> deleteSelectedFlight());

        // Enable/disable Delete based on selection
        flightList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                deleteBtn.setEnabled(flightList.getSelectedIndex() >= 0);
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshBtn);
        buttons.add(deleteBtn);
        buttons.add(openBtn);

        // Layout
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        center.add(new JScrollPane(flightList), BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void loadFlights() {
        listModel.clear();
        for (Flight f : db.getFlights()) listModel.addElement(f);

        if (!listModel.isEmpty()) {
            flightList.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(
                this,
                "No flights were loaded.\nA default database will be created when you first save.",
                "No Flights Loaded",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /** Reloads from database.txt */
    private void reloadFromDisk() {
        db.load();
        loadFlights();
    }

    private void openSelectedFlight() {
        Flight selected = flightList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SeatsFrame seats = new SeatsFrame(db, selected.getId(), selected.getFlightNumber());
        seats.setVisible(true);
        this.dispose();
    }

    // -------- Delete Flight flow --------
    private void deleteSelectedFlight() {
        Flight selected = flightList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight to delete.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete flight " + selected.getFlightNumber() + " (" + selected.getId() + ") and ALL its seats?\nThis cannot be undone.",
                "Confirm Delete Flight",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // Runtime check
        try {
            Method m = DatabaseService.class.getMethod("deleteFlight", String.class);
            Object result = m.invoke(db, selected.getId());
            boolean ok = (result instanceof Boolean) ? (Boolean) result : false;
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Delete failed. The flight may not exist or could not be removed.", "Delete Flight", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Reload and update selection
            reloadFromDisk();
            JOptionPane.showMessageDialog(this, "Flight deleted.", "Delete Flight", JOptionPane.INFORMATION_MESSAGE);
        } catch (NoSuchMethodException nsme) {
            // Backend not added yet (Issue #16)
            JOptionPane.showMessageDialog(
                this,
                "Delete will be enabled after backend wiring (Issue #16).\nFor now, this is a placeholder action.",
                "Delete Flight (Pending Backend)",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred while deleting the flight:\n" + ex.getMessage(),
                    "Delete Flight", JOptionPane.ERROR_MESSAGE);
        }
    }
}
