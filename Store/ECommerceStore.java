package Store;

import Models.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ECommerceStore {
    private static final String PRODUCT_FILE = "products.txt";
    private static List<Product> products = new ArrayList<>();
    private static int productIdCounter = 1;

    static {
        loadProducts();
    }

    public static void addProduct(String name, double price, int quantity) {
        Product p = new Product(productIdCounter++, name, price, quantity);
        products.add(p);
        saveProducts();
        System.out.println("Product added.");
    }

    public static void removeProduct(int id) {
        products.removeIf(p -> p.getId() == id);
        saveProducts();
        System.out.println("Product removed.");
    }

    public static void listProducts() {
        System.out.println("\n--- Product List ---");
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
