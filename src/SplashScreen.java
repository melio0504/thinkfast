package src;
import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
    public SplashScreen(int duration) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        
        JPanel redPanel = new JPanel();
        redPanel.setBackground(new Color(150, 40, 40));
        redPanel.setLayout(new BorderLayout());
        redPanel.setBorder(BorderFactory.createEmptyBorder(50, 80, 50, 80));
        
        JLabel label = new JLabel("ThinkFast", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 72));
        label.setForeground(Color.WHITE);
        redPanel.add(label, BorderLayout.CENTER);
        
        content.add(redPanel, BorderLayout.CENTER);
        getContentPane().add(content);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dispose();
    }
}