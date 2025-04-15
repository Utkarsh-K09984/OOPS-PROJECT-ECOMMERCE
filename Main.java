import Models.Admin;
import Models.Customer;
import Models.User;
import Store.ECommerceStore;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static List<User> users = new ArrayList<>();
    static final String USER_FILE = "users.txt";

    public static void main(String[] args) {
        loadUsersFromFile(); // 🔁 Load users from file on app start

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
            sc.nextLine();

            switch (choice) {
                case 1 -> login("admin");
                case 2 -> registerCustomer();
                case 3 -> login("customer");
                case 4 -> System.out.println("Thank you for visiting!");
                default -> System.out.println("Invalid option. Try again.");
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
        saveUsersToFile(); // 💾 Save new user to file
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

    // ✅ Add this function inside Main class
    static void saveUsersToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User u : users) {
                String type = (u instanceof Admin) ? "admin" : "customer";
                writer.write(type + "," + u.getUsername() + "," + u.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    // ✅ Add this function inside Main class
    static void loadUsersFromFile() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            // If file not found, create default admin
            users.add(new Admin("admin", "admin123"));
            saveUsersToFile();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            users.clear();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String type = parts[0];
                    String uname = parts[1];
                    String pass = parts[2];

                    if (type.equals("admin")) {
                        users.add(new Admin(uname, pass));
                    } else if (type.equals("customer")) {
                        users.add(new Customer(uname, pass));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }
}