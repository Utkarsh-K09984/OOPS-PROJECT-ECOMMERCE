import GUI.ECommerceGUI;
import utils.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Test database connection before launching the application
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            System.out.println("Successfully connected to the database");
            conn.close();
        } catch (SQLException e) {
            String errorMessage = "Failed to connect to the database. Please make sure the MySQL server is running.\n\n";
            errorMessage += "Error: " + e.getMessage() + "\n\n";
            errorMessage += "1. Make sure Docker is running\n";
            errorMessage += "2. Start the MySQL container with: docker-compose up -d\n";
            errorMessage += "3. Try running the application again";
            
            JOptionPane.showMessageDialog(null, errorMessage, 
                    "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            
            // Print instructions to console as well
            System.err.println(errorMessage);
            return;
        }
        
        // Launch the GUI application
        SwingUtilities.invokeLater(() -> {
            ECommerceGUI app = new ECommerceGUI();
            app.setVisible(true);
        });
    }
}
