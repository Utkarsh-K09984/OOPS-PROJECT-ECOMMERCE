package Models;

public abstract class User {
    protected String username;
    protected String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public abstract void showMenu();
    public String getUsername() { return username; }
    public boolean checkPassword(String pass) { return password.equals(pass); }
    public String getPassword() {
        return password;
    }
}