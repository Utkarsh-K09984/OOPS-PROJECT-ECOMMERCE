import GUI.ECommerceGUI;
import Models.Admin;
import Models.Customer;
import Models.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    static List<User> users = new ArrayList<>();
    private static final String ADMIN_FILE = "data/admins.txt";
    private static final String CUSTOMER_FILE = "data/customers.txt";

    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the GUI application
        SwingUtilities.invokeLater(() -> {
            ECommerceGUI app = new ECommerceGUI();
            app.setVisible(true);
        });
    }

    // The following methods are kept for reference but are no longer used directly
    // as the GUI now handles these operations
    
    /*
    static void registerCustomer() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String uname = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();
        users.add(new Customer(uname, pass));
        saveUsers();
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

    private static void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader(ADMIN_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String password = parts[1];
                users.add(new Admin(username, password));
            }
        } catch (IOException e) {
            System.out.println("No existing admin data found.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(CUSTOMER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String username = parts[0];
                String password = parts[1];
                users.add(new Customer(username, password));
            }
        } catch (IOException e) {
            System.out.println("No existing customer data found.");
        }
    }

    private static void saveUsers() {
        try (BufferedWriter adminWriter = new BufferedWriter(new FileWriter(ADMIN_FILE));
             BufferedWriter customerWriter = new BufferedWriter(new FileWriter(CUSTOMER_FILE))) {

            for (User u : users) {
                if (u instanceof Admin) {
                    adminWriter.write(u.getUsername() + "," + u.getPassword());
                    adminWriter.newLine();
                } else if (u instanceof Customer) {
                    customerWriter.write(u.getUsername() + "," + u.getPassword());
                    customerWriter.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving users.");
        }
    }
    */
}
