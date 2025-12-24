package common;

import java.io.Serializable;

/**
 * Represents a User entity in the Bistro application.
 * <p>
 * This class contains user details such as username, password, permissions,
 * and name. It implements {@link Serializable} to allow objects of this class
 * to be sent over the network between the Client and the Server.
 * </p>
 */
public class User implements Serializable {
    
    /** Serial Version UID for serialization compatibility. */
    private static final long serialVersionUID = 1L;

    /** The unique ID of the user in the database. */
    private int id;

    /** The unique username used for login. */
    private String username;

    /** The password used for login. */
    private String password;

    /** The type of the user (e.g., "Manager", "Waiter", "Customer"). */
    private String userType; 

    /** The first name of the user. */
    private String firstName;

    /** The last name of the user. */
    private String lastName;

    /** Indicates whether the user is currently logged in. */
    private boolean isLoggedIn;

    /**
     * Constructs a new User object.
     *
     * @param id the user's ID
     * @param username the user's username
     * @param password the user's password
     * @param userType the role/type of the user
     * @param firstName the user's first name
     * @param lastName the user's last name
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

    /**
     * Gets the username.
     * @return the username string
     */
    public String getUsername() { return username; }

    /**
     * Gets the password.
     * @return the password string
     */
    public String getPassword() { return password; }

    /**
     * Gets the user type/role.
     * @return the user type string
     */
    public String getUserType() { return userType; }

    /**
     * Checks if the user is currently logged in.
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() { return isLoggedIn; }

    /**
     * Sets the login status of the user.
     * @param isLoggedIn the new login status
     */
    public void setLoggedIn(boolean isLoggedIn) { this.isLoggedIn = isLoggedIn; }
    
    /**
     * Returns a string representation of the User.
     * @return full name and user type
     */
    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + userType + ")";
    }
}