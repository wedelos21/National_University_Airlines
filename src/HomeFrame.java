import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

public class HomeFrame extends JFrame {
    private final DatabaseService db;
    private final DefaultListModel<Flight> listModel = new DefaultListModel<>();
    private final JList<Flight> flightList = new JList<>(listModel);
    private final JButton openBtn = new JButton("Open Flight");

    public HomeFrame(DatabaseService db) {
        super("National University Airlines");
        this.db = db;
        setJMenuBar(buildMenuBar());                // <-- Menu bar
        initComponents();
        loadFlights();
        setSize(640, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic('F');

        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic('E');
        // Ctrl+Q Shortcut Quit Key
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
        JLabel header = new JLabel("National University Airlines", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 24f));
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

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

        flightList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedFlight();
            }
        });

        openBtn.addActionListener(e -> openSelectedFlight());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> reloadFromDisk());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshBtn);
        buttons.add(openBtn);

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
}
