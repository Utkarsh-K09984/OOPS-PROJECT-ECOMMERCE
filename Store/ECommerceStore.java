package Store;


import Models.Product;

import java.util.ArrayList;
import java.util.List;

public class ECommerceStore {
    private static List<Product> products = new ArrayList<>();
    private static int productIdCounter = 1;

    public static void addProduct(String name, double price, int quantity) {
        Product p = new Product(productIdCounter++, name, price, quantity);
        products.add(p);
        System.out.println("Product added.");
    }

    public static void removeProduct(int id) {
        products.removeIf(p -> p.getId() == id);
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
}
