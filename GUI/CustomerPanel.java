package GUI;

import Models.Customer;
import Models.Product;
import Store.ECommerceStore;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CustomerPanel extends JPanel {
    private ECommerceGUI parent;
    private Customer currentUser;
    private List<Product> cart = new ArrayList<>();
    // Map to track original quantities of products in cart
    private Map<Integer, Integer> originalQuantities = new HashMap<>();
    
    private JTable productTable;
    private DefaultTableModel productTableModel;
    
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    
    private JLabel totalLabel;
    
    public CustomerPanel(ECommerceGUI parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Customer Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Create tabs for different views
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Products tab
        JPanel productsPanel = createProductsPanel();
        tabbedPane.addTab("Browse Products", productsPanel);
        
        // Cart tab
        JPanel cartPanel = createCartPanel();
        tabbedPane.addTab("Shopping Cart", cartPanel);
        
        // Button panel at the bottom
        JPanel buttonPanel = new JPanel();
        JButton logoutButton = new JButton("Logout");
        buttonPanel.add(logoutButton);
        
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(parent, 
                "Are you sure you want to logout? Your cart will be cleared.", 
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                cart.clear();
                updateCartTable();
                parent.showPanel("login");
            }
        });
        
        // Add components to main panel
        add(titlePanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table model with column names
        String[] columnNames = {"ID", "Name", "Price", "Stock"};
        productTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        // Create table and scroll pane
        productTable = new JTable(productTableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        JButton addToCartButton = new JButton("Add to Cart");
        JButton refreshButton = new JButton("Refresh Products");
        
        addToCartButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(parent, "Please select a product to add to cart", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int id = Integer.parseInt(productTableModel.getValueAt(selectedRow, 0).toString());
            int stock = Integer.parseInt(productTableModel.getValueAt(selectedRow, 3).toString());
            
            if (stock <= 0) {
                JOptionPane.showMessageDialog(parent, "This product is out of stock", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Product product = ECommerceStore.getProductById(id);
            if (product != null) {
                // Store original quantity if this is the first time adding this product
                if (!originalQuantities.containsKey(product.getId())) {
                    originalQuantities.put(product.getId(), product.getQuantity());
                }
                
                // Decrease product quantity by 1
                product.setQuantity(product.getQuantity() - 1);
                
                // Add product to cart
                cart.add(product);
                
                // Update tables
                updateCartTable();
                refreshProductTable();
                
                JOptionPane.showMessageDialog(parent, "Product added to cart!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        refreshButton.addActionListener(e -> refreshProductTable());
        
        buttonPanel.add(addToCartButton);
        buttonPanel.add(refreshButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create table model with column names
        String[] columnNames = {"ID", "Name", "Price"};
        cartTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        // Create table and scroll pane
        cartTable = new JTable(cartTableModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create bottom panel with total and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Total label
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(totalLabel);
        bottomPanel.add(totalPanel, BorderLayout.NORTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton removeButton = new JButton("Remove from Cart");
        JButton clearButton = new JButton("Clear Cart");
        JButton checkoutButton = new JButton("Checkout");
        
        removeButton.addActionListener(e -> {
            int selectedRow = cartTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(parent, "Please select a product to remove", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get the product being removed
            Product removedProduct = cart.get(selectedRow);
            
            // Remove from cart
            cart.remove(selectedRow);
            
            // Restore one quantity to the product in inventory
            Product inventoryProduct = ECommerceStore.getProductById(removedProduct.getId());
            if (inventoryProduct != null) {
                inventoryProduct.setQuantity(inventoryProduct.getQuantity() + 1);
            }
            
            // Update tables
            updateCartTable();
            refreshProductTable();
        });
        
        clearButton.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Cart is already empty", 
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(parent, 
                    "Are you sure you want to clear your cart?", 
                    "Confirm Clear", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Restore all original quantities
                restoreOriginalQuantities();
                
                // Clear cart
                cart.clear();
                originalQuantities.clear();
                
                // Update tables
                updateCartTable();
                refreshProductTable();
            }
        });
        
        checkoutButton.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Cart is empty, nothing to checkout", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(parent, 
                    "Proceed to checkout? This will complete your order.", 
                    "Confirm Checkout", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Calculate total
                double total = cart.stream().mapToDouble(Product::getPrice).sum();
                
                // Save the updated quantities to file
                ECommerceStore.saveProducts();
                
                JOptionPane.showMessageDialog(parent, 
                        "Order placed successfully!\nTotal: $" + String.format("%.2f", total), 
                        "Checkout Complete", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear cart and original quantities map
                cart.clear();
                originalQuantities.clear();
                updateCartTable();
            }
        });
        
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(checkoutButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    public void refreshProductTable() {
        // Clear existing data
        productTableModel.setRowCount(0);
        
        // Get product list from store
        List<Product> products = ECommerceStore.getAllProducts();
        
        if (products != null) {
            for (Product product : products) {
                Vector<Object> row = new Vector<>();
                row.add(product.getId());
                row.add(product.getName());
                row.add("$" + product.getPrice());
                row.add(product.getQuantity());
                productTableModel.addRow(row);
            }
        }
    }
    
    private void updateCartTable() {
        // Clear existing data
        cartTableModel.setRowCount(0);
        
        // Add cart items to table
        for (Product product : cart) {
            Vector<Object> row = new Vector<>();
            row.add(product.getId());
            row.add(product.getName());
            row.add("$" + product.getPrice());
            cartTableModel.addRow(row);
        }
        
        // Update total
        double total = cart.stream().mapToDouble(Product::getPrice).sum();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }
    
    // Helper method to restore original quantities
    private void restoreOriginalQuantities() {
        for (Map.Entry<Integer, Integer> entry : originalQuantities.entrySet()) {
            Product product = ECommerceStore.getProductById(entry.getKey());
            if (product != null) {
                product.setQuantity(entry.getValue());
            }
        }
    }
    
    public void setCurrentUser(Customer user) {
        this.currentUser = user;
        refreshProductTable();
        cart.clear();
        originalQuantities.clear();
        updateCartTable();
    }
}