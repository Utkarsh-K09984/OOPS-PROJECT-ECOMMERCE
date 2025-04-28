package Models;

import Interfaces.AdminOperations;
import Store.ECommerceStore;

import java.util.Scanner;

public class Admin extends User implements AdminOperations {
    public Admin(String username, String password) {
        super(username, password);
    }
    
    @Override
    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. Add Product");
            System.out.println("2. Remove Product");
            System.out.println("3. List Products");
            System.out.println("4. Logout");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            switch (choice) {
                case 1 -> addProduct();
                case 2 -> removeProduct();
                case 3 -> ECommerceStore.listProducts();
                case 4 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid option.");
            }
        } while (choice != 4);
    }

    @Override
    public void addProduct() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter product name: ");
        String name = sc.nextLine();
        System.out.print("Enter price: ");
        double price = sc.nextDouble();
        System.out.print("Enter quantity: ");
        int quantity = sc.nextInt();
        ECommerceStore.addProduct(name, price, quantity);
    }

    @Override
    public void removeProduct() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter product ID to remove: ");
        int id = sc.nextInt();
        ECommerceStore.removeProduct(id);
    }
}
