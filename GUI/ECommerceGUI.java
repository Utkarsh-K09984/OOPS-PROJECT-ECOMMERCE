package GUI;

import Models.Admin;
import Models.Customer;
import Models.User;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ECommerceGUI extends JFrame {
    private static List<User> users = new ArrayList<>();
    private static final String ADMIN_FILE = "data/admins.txt";
    private static final String CUSTOMER_FILE = "data/customers.txt";
    
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
            users.add(new Admin("admin", "admin123"));
            saveUsers();
        }
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
    
    public void loginUser(String username, String password, String type) {
        for (User u : users) {
            if (u.getUsername().equals(username) && u.checkPassword(password)) {
                if (type.equals("admin") && u instanceof Admin) {
                    adminPanel.setCurrentUser((Admin) u);
                    showPanel("admin");
                    return;
                } else if (type.equals("customer") && u instanceof Customer) {
                    customerPanel.setCurrentUser((Customer) u);
                    showPanel("customer");
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(this, "Login failed. Please check your credentials.", 
                "Login Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void registerCustomer(String username, String password) {
        // Check if username already exists
        if (users.stream().anyMatch(u -> u.getUsername().equals(username))) {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose another.",
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Customer newCustomer = new Customer(username, password);
        users.add(newCustomer);
        saveUsers();
        JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.",
                "Registration Success", JOptionPane.INFORMATION_MESSAGE);
        showPanel("login");
    }
    
    private void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(ADMIN_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];
                    users.add(new Admin(username, password));
                }
            }
        } catch (IOException e) {
            System.out.println("No existing admin data found.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(CUSTOMER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];
                    users.add(new Customer(username, password));
                }
            }
        } catch (IOException e) {
            System.out.println("No existing customer data found.");
        }
    }

    private void saveUsers() {
        try (BufferedWriter adminWriter = new BufferedWriter(new FileWriter(ADMIN_FILE));
             BufferedWriter customerWriter = new BufferedWriter(new FileWriter(CUSTOMER_FILE))) {

            for (User u : users) {
                if (u instanceof Admin) {
                    adminWriter.write(u.getUsername() + "," + u.getPassword());
                    adminWriter.newLine();
                } else if (u instanceof Customer) {
                    customerWriter.write(u.getUsername() + "," + u.getPassword());
                    customerWriter.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving users.");
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