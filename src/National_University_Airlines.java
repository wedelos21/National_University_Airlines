import javax.swing.*;
import java.awt.*;

public class National_University_Airlines {
    public static void main(String[] args) {
        // Use the event dispatch thread for Swing
        SwingUtilities.invokeLater(() -> {
            // Try a modern look if available (optional)
            try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}

            JFrame frame = new JFrame("National University Airlines");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JLabel title = new JLabel("National University Airlines", SwingConstants.CENTER);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));   // “header style”
            title.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

            frame.getContentPane().add(title, BorderLayout.CENTER);
            frame.setSize(640, 200);          // simple fixed size
            frame.setLocationRelativeTo(null); // center on screen
            frame.setVisible(true);
        });
    }
}
