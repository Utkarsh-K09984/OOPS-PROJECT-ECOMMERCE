package GUI;

import Models.Admin;
import Models.Customer;
import Models.User;
import utils.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ECommerceGUI extends JFrame {
    private static List<User> users = new ArrayList<>();
    
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Panels for different screens
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private AdminPanel adminPanel;
    private CustomerPanel customerPanel;
    
    public ECommerceGUI() {
        // Set up the JFrame
        setTitle("E-Commerce Store");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create the card layout and main panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize panels
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        adminPanel = new AdminPanel(this);
        customerPanel = new CustomerPanel(this);
        
        // Add panels to card layout
        mainPanel.add(loginPanel, "login");
        mainPanel.add(registerPanel, "register");
        mainPanel.add(adminPanel, "admin");
        mainPanel.add(customerPanel, "customer");
        
        // Show login panel by default
        cardLayout.show(mainPanel, "login");
        
        // Add main panel to frame
        add(mainPanel);
        
        // Load users
        loadUsers();
        
        // Add default admin if not present
        if (users.stream().noneMatch(u -> u instanceof Admin)) {
            ensureDefaultAdmin();
        }
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    
    public void loginUser(String username, String password, String type) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM users WHERE username = ? AND password = ? AND user_type = ?")) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, type);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (type.equals("admin")) {
                        Admin admin = new Admin(username, password);
                        adminPanel.setCurrentUser(admin);
                        showPanel("admin");
                    } else if (type.equals("customer")) {
                        Customer customer = new Customer(username, password);
                        customerPanel.setCurrentUser(customer);
                        showPanel("customer");
                    }
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error logging in user");
            e.printStackTrace();
        }
        
        JOptionPane.showMessageDialog(this, "Login failed. Please check your credentials.", 
                "Login Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void registerCustomer(String username, String password) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM users WHERE username = ?")) {
            
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Username already exists. Please choose another.",
                            "Registration Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            try (PreparedStatement insertStmt = conn.prepareStatement(
                     "INSERT INTO users (username, password, user_type) VALUES (?, ?, ?)")) {
                
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, "customer");
                
                int affectedRows = insertStmt.executeUpdate();
                
                if (affectedRows > 0) {
                    Customer newCustomer = new Customer(username, password);
                    users.add(newCustomer);
                    JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.",
                            "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                    showPanel("login");
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed. Please try again.",
                            "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error registering customer");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registration failed due to a database error.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadUsers() {
        users.clear();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String userType = rs.getString("user_type");
                
                if (userType.equals("admin")) {
                    users.add(new Admin(username, password));
                } else if (userType.equals("customer")) {
                    users.add(new Customer(username, password));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading users from database");
            e.printStackTrace();
        }
    }
    
    private void ensureDefaultAdmin() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM users WHERE user_type = 'admin'")) {
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                             "INSERT INTO users (username, password, user_type) VALUES (?, ?, ?)")) {
                        
                        insertStmt.setString(1, "admin");
                        insertStmt.setString(2, "admin123");
                        insertStmt.setString(3, "admin");
                        
                        insertStmt.executeUpdate();
                        users.add(new Admin("admin", "admin123"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error ensuring default admin");
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(() -> {
            ECommerceGUI app = new ECommerceGUI();
            app.setVisible(true);
        });
    }
}