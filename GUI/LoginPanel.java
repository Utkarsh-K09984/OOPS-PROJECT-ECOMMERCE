package GUI;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private ECommerceGUI parent;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton adminRadio;
    private JRadioButton customerRadio;
    
    public LoginPanel(ECommerceGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("E-Commerce Store Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);
        
        // Radio buttons for user type
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Login as:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adminRadio = new JRadioButton("Admin");
        customerRadio = new JRadioButton("Customer");
        customerRadio.setSelected(true);
        
        ButtonGroup group = new ButtonGroup();
        group.add(adminRadio);
        group.add(customerRadio);
        
        radioPanel.add(adminRadio);
        radioPanel.add(customerRadio);
        formPanel.add(radioPanel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register New Customer");
        
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String type = adminRadio.isSelected() ? "admin" : "customer";
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Username and password cannot be empty", 
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            parent.loginUser(username, password, type);
        });
        
        registerButton.addActionListener(e -> parent.showPanel("register"));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        // Add all panels to main panel
        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}