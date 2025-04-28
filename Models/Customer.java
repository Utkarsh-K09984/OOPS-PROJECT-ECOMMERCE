package Models;

import Store.ECommerceStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Customer extends User {
    // Store product IDs and quantities in the cart
    private Map<Integer, Integer> cart = new HashMap<>();

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
                case 2 -> addToCart(sc);
                case 3 -> viewCart();
                case 4 -> checkout();
                case 5 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }
        } while (choice != 5);
        
        // Only close scanner when exiting the menu
        sc.close();
    }

    private void addToCart(Scanner sc) {
        System.out.print("Enter Product ID to add to cart: ");
        int id = sc.nextInt();
        Product product = ECommerceStore.getProductById(id);
        
        if (product != null && product.getQuantity() > 0) {
            // Get current quantity in cart (if any)
            int cartQuantity = cart.getOrDefault(id, 0);
            
            // Don't allow adding more than what's in stock
            if (cartQuantity + 1 <= product.getQuantity()) {
                // Update database to decrease the product quantity
                int newQuantity = product.getQuantity() - 1;
                boolean updated = ECommerceStore.updateProduct(
                    product.getId(), product.getName(), product.getPrice(), newQuantity);
                
                if (updated) {
                    // Update cart with new quantity
                    cart.put(id, cartQuantity + 1);
                    System.out.println("Added to cart.");
                } else {
                    System.out.println("Error updating inventory. Please try again.");
                }
            } else {
                System.out.println("Cannot add more. Insufficient stock available.");
            }
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
        
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            
            // Get the product details from the store
            Product p = ECommerceStore.getProductById(productId);
            if (p != null) {
                double itemTotal = p.getPrice() * quantity;
                System.out.println(p.getId() + " | " + p.getName() + " | $" + p.getPrice() + 
                                  " | Quantity: " + quantity + " | Subtotal: $" + itemTotal);
                total += itemTotal;
            }
        }
        
        System.out.println("Total: $" + total);
    }

    private void checkout() {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }
        
        System.out.println("Order placed successfully! 🎉");
        // Clear the cart after successful checkout
        cart.clear();
    }
}
