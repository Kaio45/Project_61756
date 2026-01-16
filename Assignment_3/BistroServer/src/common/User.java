package common;

import java.io.Serializable;

/**
 * Represents a User entity (Staff/Manager) in the Bistro application.
 * Contains user credentials and role information.
 * Implements Serializable for network transmission.
 * @author Group-17
 * @version 1.0
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** The unique user ID. */
    private int id;

    /** The username for login. */
    private String username;

    /** The password for login. */
    private String password;

    /** The role of the user (e.g., "Manager", "Waiter"). */
    private String userType; 

    /** The first name. */
    private String firstName;

    /** The last name. */
    private String lastName;

    /** Login status flag. */
    private boolean isLoggedIn;

    /**
     * Constructs a new User object.
     *
     * @param id the user ID
     * @param username the username
     * @param password the password
     * @param userType the role
     * @param firstName the first name
     * @param lastName the last name
     */
    public User(int id, String username, String password, String userType, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isLoggedIn = false;
    }
    
    // --- Getters and Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public boolean isLoggedIn() { return isLoggedIn; }
    public void setLoggedIn(boolean isLoggedIn) { this.isLoggedIn = isLoggedIn; }
    
    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + userType + ")";
    }
}
