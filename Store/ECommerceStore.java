package Store;

import Models.Product;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ECommerceStore {
    private static List<Product> products = new ArrayList<>();

    public static void addProduct(String name, double price, int quantity) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO products (name, price, quantity) VALUES (?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, quantity);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    // Add to cache
                    products.add(new Product(id, name, price, quantity));
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding product to database");
            e.printStackTrace();
        }
    }

    public static void removeProduct(int id) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Remove from cache if exists
                products.removeIf(p -> p.getId() == id);
            }
        } catch (SQLException e) {
            System.err.println("Error removing product from database");
            e.printStackTrace();
        }
    }

    public static void listProducts() {
        List<Product> prods = getAllProducts();
        if (prods.isEmpty()) {
            System.out.println("No products available.");
        } else {
            for (Product p : prods) {
                System.out.println(p);
            }
        }
    }

    public static Product getProductById(int id) {
        // First try to find in the cache
        for (Product p : products) {
            if (p.getId() == id) {
                return p;
            }
        }
        
        // If not in cache, try to fetch from database
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE id = ?")) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity");
                    
                    Product product = new Product(id, name, price, quantity);
                    // Add to cache
                    products.add(product);
                    return product;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product from database");
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static List<Product> getAllProducts() {
        products.clear(); // Clear cache to refresh from database
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                
                products.add(new Product(id, name, price, quantity));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching products from database");
            e.printStackTrace();
        }
        
        return new ArrayList<>(products); // Return a copy to protect the original list
    }
    
    public static boolean updateProduct(int id, String name, double price, int quantity) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE products SET name = ?, price = ?, quantity = ? WHERE id = ?")) {
            
            stmt.setString(1, name);
            stmt.setDouble(2, price);
            stmt.setInt(3, quantity);
            stmt.setInt(4, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update in cache if exists
                for (Product p : products) {
                    if (p.getId() == id) {
                        p.setName(name);
                        p.setPrice(price);
                        p.setQuantity(quantity);
                        break;
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating product in database");
            e.printStackTrace();
        }
        
        return false;
    }
    
    public static void saveProducts() {
        // This method is kept for compatibility but does nothing
        // as changes are immediately saved to the database
    }
}
