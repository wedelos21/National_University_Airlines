import javax.swing.*;
import java.awt.*;

/**
 * TEMP STUB for Issue #7 so the project compiles.
 * Replace with full implementation in Issue #8.
 */
public class SeatsFrame extends JFrame {
    private final DatabaseService db;
    private final String flightId;
    private final String flightNumber;

    public SeatsFrame(DatabaseService db, String flightId, String flightNumber) {
        super("Seats – " + flightNumber + " (" + flightId + ")");
        this.db = db;
        this.flightId = flightId;
        this.flightNumber = flightNumber;

        initComponents();
        setSize(720, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JLabel msg = new JLabel("Seats screen placeholder for " + flightNumber + " (" + flightId + ")", SwingConstants.CENTER);
        msg.setFont(msg.getFont().deriveFont(Font.PLAIN, 18f));
        msg.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            new HomeFrame(db).setVisible(true);
            dispose();
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(back);

        setLayout(new BorderLayout());
        add(msg, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }
}
