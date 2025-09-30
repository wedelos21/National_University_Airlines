import javax.swing.*;
import java.awt.*;

public class National_University_Airlines {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignored) {}

            
            JFrame frame = new JFrame("National University Airlines");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JLabel title = new JLabel("National University Airlines", SwingConstants.CENTER);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
            title.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

            frame.getContentPane().add(title, BorderLayout.CENTER);
            frame.setSize(640, 200);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
