package GUI;

import Models.Admin;
import Models.Product;
import Store.ECommerceStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

public class AdminPanel extends JPanel {
    private ECommerceGUI parent;
    private Admin currentUser;
    private JTable productTable;
    private DefaultTableModel tableModel;
    
    // Product form components
    private JTextField nameField;
    private JTextField priceField;
    private JTextField quantityField;
    private JTextField idField;
    
    public AdminPanel(ECommerceGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Create main content panel with split
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        
        // Product table on the left
        JPanel tablePanel = createProductTablePanel();
        splitPane.setLeftComponent(tablePanel);
        
        // Product form on the right
        JPanel formPanel = createProductFormPanel();
        splitPane.setRightComponent(formPanel);
        
        // Button panel at the bottom
        JPanel buttonPanel = new JPanel();
        JButton logoutButton = new JButton("Logout");
        buttonPanel.add(logoutButton);
        
        logoutButton.addActionListener(e -> {
            parent.showPanel("login");
        });
        
        // Add all panels to main panel
        add(titlePanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createProductTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Product List"));
        
        // Create table model with column names
        String[] columnNames = {"ID", "Name", "Price", "Quantity"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        // Create table and scroll pane
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        
        // Add selection listener to populate form when row is selected
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                int row = productTable.getSelectedRow();
                idField.setText(tableModel.getValueAt(row, 0).toString());
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                priceField.setText(tableModel.getValueAt(row, 2).toString().replace("$", ""));
                quantityField.setText(tableModel.getValueAt(row, 3).toString());
            }
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Product List");
        refreshButton.addActionListener(e -> refreshProductTable());
        
        panel.add(refreshButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createProductFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Product Management"));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ID field (non-editable)
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Product ID:"), gbc);
        
        gbc.gridx = 1;
        idField = new JTextField(10);
        idField.setEditable(false);
        formPanel.add(idField, gbc);
        
        // Name field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Product Name:"), gbc);
        
        gbc.gridx = 1;
        nameField = new JTextField(10);
        formPanel.add(nameField, gbc);
        
        // Price field
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Price:"), gbc);
        
        gbc.gridx = 1;
        priceField = new JTextField(10);
        formPanel.add(priceField, gbc);
        
        // Quantity field
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Quantity:"), gbc);
        
        gbc.gridx = 1;
        quantityField = new JTextField(10);
        formPanel.add(quantityField, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add New Product");
        JButton updateButton = new JButton("Update Product");
        JButton removeButton = new JButton("Remove Product");
        JButton clearButton = new JButton("Clear Form");
        
        // Add button action
        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "Product name cannot be empty", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (price <= 0 || quantity < 0) {
                    JOptionPane.showMessageDialog(parent, "Price must be greater than 0 and quantity must be positive", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                ECommerceStore.addProduct(name, price, quantity);
                JOptionPane.showMessageDialog(parent, "Product added successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshProductTable();
                clearForm();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent, "Price and quantity must be numbers", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Update button action
        updateButton.addActionListener(e -> {
            if (idField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Please select a product to update", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(parent, "Product name cannot be empty", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (price <= 0 || quantity < 0) {
                    JOptionPane.showMessageDialog(parent, "Price must be greater than 0 and quantity must be positive", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = ECommerceStore.updateProduct(id, name, price, quantity);
                if (success) {
                    JOptionPane.showMessageDialog(parent, "Product updated successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshProductTable();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(parent, "Product not found", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent, "Price and quantity must be numbers", 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Remove button action
        removeButton.addActionListener(e -> {
            if (idField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Please select a product to remove", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int id = Integer.parseInt(idField.getText());
            int confirm = JOptionPane.showConfirmDialog(parent, 
                    "Are you sure you want to remove this product?", 
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                ECommerceStore.removeProduct(id);
                JOptionPane.showMessageDialog(parent, "Product removed successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshProductTable();
                clearForm();
            }
        });
        
        // Clear button action
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        priceField.setText("");
        quantityField.setText("");
        productTable.clearSelection();
    }
    
    public void refreshProductTable() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Get product list from store
        java.util.List<Product> products = ECommerceStore.getAllProducts();
        
        if (products != null) {
            for (Product product : products) {
                Vector<Object> row = new Vector<>();
                row.add(product.getId());
                row.add(product.getName());
                row.add("$" + product.getPrice());
                row.add(product.getQuantity());
                tableModel.addRow(row);
            }
        }
    }
    
    public void setCurrentUser(Admin user) {
        this.currentUser = user;
        refreshProductTable();
    }
}