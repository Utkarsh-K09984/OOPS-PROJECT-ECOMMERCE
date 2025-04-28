package Models;

import Store.ECommerceStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer extends User {
    private List<Product> cart = new ArrayList<>();

    public Customer(String username, String password) {
        super(username, password);
    }

    @Override
    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View Products");
            System.out.println("2. Add to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Checkout");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1 -> ECommerceStore.listProducts();
                case 2 -> addToCart();
                case 3 -> viewCart();
                case 4 -> checkout();
                case 5 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }
        } while (choice != 5);
    }

    private void addToCart() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Product ID to add to cart: ");
        int id = sc.nextInt();
        Product product = ECommerceStore.getProductById(id);
        if (product != null && product.getQuantity() > 0) {
            cart.add(product);
            System.out.println("Added to cart.");
        } else {
            System.out.println("Invalid product or out of stock.");
        }
    }

    private void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        System.out.println("\n--- Your Cart ---");
        double total = 0;
        for (Product p : cart) {
            System.out.println(p);
            total += p.getPrice();
        }
        System.out.println("Total: $" + total);
    }

    private void checkout() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        System.out.println("Order placed successfully! 🎉");
        
        cart.clear();
    }
}
