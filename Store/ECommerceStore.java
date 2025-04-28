package Store;

import Models.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ECommerceStore {
    private static final String PRODUCT_FILE = "data/products.txt";
    private static List<Product> products = new ArrayList<>();
    private static int productIdCounter = 1;

    static {
        loadProducts();
    }

    public static void addProduct(String name, double price, int quantity) {
        products.add(new Product(productIdCounter++, name, price, quantity));
        saveProducts();
    }

    public static void removeProduct(int id) {
        products.removeIf(p -> p.getId() == id);
        saveProducts();
    }

    public static void listProducts() {
        if (products.isEmpty()) {
            System.out.println("No products available.");
        } else {
            for (Product p : products) {
                System.out.println(p);
            }
        }
    }

    public static Product getProductById(int id) {
        for (Product p : products) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
    
    public static List<Product> getAllProducts() {
        return new ArrayList<>(products); // Return a copy to protect the original list
    }
    
    public static boolean updateProduct(int id, String name, double price, int quantity) {
        for (Product p : products) {
            if (p.getId() == id) {
                p.setName(name);
                p.setPrice(price);
                p.setQuantity(quantity);
                saveProducts();
                return true;
            }
        }
        return false;
    }

    private static void loadProducts() {
        try (BufferedReader br = new BufferedReader(new FileReader(PRODUCT_FILE))) {
            String line ; 
            while((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    int quantity = Integer.parseInt(parts[3]);
                    products.add(new Product(id, name, price, quantity));
                    productIdCounter = Math.max(productIdCounter, id + 1); // Update counter
                }
            }
        } catch (IOException e) {
            System.out.println("No existing product data found.");
        }
    }

    private static void saveProducts() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PRODUCT_FILE))) {
            for (Product p : products) {
                bw.write(p.getId() + "," + p.getName() + "," + p.getPrice() + "," + p.getQuantity());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving products.");
        }
    }
}
