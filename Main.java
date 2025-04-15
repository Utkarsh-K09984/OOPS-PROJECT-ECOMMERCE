import Models.Admin;
import Models.Customer;
import Models.User;
import Store.ECommerceStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static List<User> users = new ArrayList<>();
    static final String USER_FILE = "users.txt";
    public static void main(String[] args) {
        
        // Add default admin
        users.add(new Admin("admin", "admin123"));

        // Add some default products
        ECommerceStore.addProduct("Laptop", 55000, 5);
        ECommerceStore.addProduct("Smartphone", 25000, 10);
        ECommerceStore.addProduct("Headphones", 2000, 15);
        ECommerceStore.addProduct("Keyboard", 1500, 8);
        ECommerceStore.addProduct("Mouse", 800, 12);

        Scanner sc = new Scanner(System.in);
        int choice;
        
        do {
            System.out.println("\n--- Welcome to E-Commerce Store ---");
            System.out.println("1. Admin Login");
            System.out.println("2. Customer Register");
            System.out.println("3. Customer Login");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine(); // clear buffer

            switch (choice) {
                case 1 -> login("admin");
                case 2 -> registerCustomer();
                case 3 -> login("customer");
                case 4 -> System.out.println("Thank you for visiting!");
                default -> System.out.println("Invalid option. Please try again.");
            }

        } while (choice != 4);
    }

    static void registerCustomer() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String uname = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();
        users.add(new Customer(uname, pass));
        System.out.println("Registration successful! You can now log in.");
    }

    static void login(String type) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String uname = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        for (User u : users) {
            if (u.getUsername().equals(uname) && u.checkPassword(pass)) {
                if (type.equals("admin") && u instanceof Admin) {
                    u.showMenu();
                    return;
                } else if (type.equals("customer") && u instanceof Customer) {
                    u.showMenu();
                    return;
                }
            }
        }

        System.out.println("Login failed. Please check your credentials.");
    }
}
