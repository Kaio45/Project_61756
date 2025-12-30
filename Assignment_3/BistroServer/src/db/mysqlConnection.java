package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList; // Added for getAllTables
import common.Order;
import common.Table;    // Added for Assignment 3
import common.User; 

/**
 * The Class mysqlConnection.
 * <p>
 * Handles the connection to the MySQL database and provides methods
 * for querying, inserting, and updating data (Orders, Users, and Tables).
 * </p>
 */
public class mysqlConnection {

    /** The database connection instance. */
    private static Connection conn;

    /**
     * Connects to the database.
     * <p>
     * Loads the MySQL JDBC driver and establishes a connection using the defined URL,
     * username, and password.
     * </p>
     */
    public static void connectToDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Connecting to database...");

            // BASED ON YOUR CODE: DB name is 'order_sch'.
            // WARNING: If your MySQL Workbench shows 'order', please change 'order_sch' to 'order' below.
            String url = "jdbc:mysql://127.0.0.1:3306/order_sch?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";
            conn = DriverManager.getConnection(url, "root", "abc123"); // Update password if needed

            System.out.println("SQL connection succeed");

        } catch (SQLException ex) {
            System.out.println("--- Connection Failed ---");
            System.out.println("Message: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("--- General Error ---");
            ex.printStackTrace();
        }
    }

    /**
     * Gets the connection.
     *
     * @return the active database connection
     */
    public static Connection getConnection() {
        return conn;
    }

    /**
     * Gets the max order number from the database.
     * Used to generate the next unique order ID.
     *
     * @return the maximum order number currently stored, or 0 if none found
     */
    public static int getMaxOrderNumber() {
        int max = 0;
        try {
            String query = "SELECT MAX(order_number) FROM `orders`";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                max = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Error finding max ID: " + e.getMessage());
        }
        return max;
    }

    /**
     * Save order to DB.
     * UPDATED: Now saves 'status', 'order_time', and 'table_id' required for Assignment 3.
     *
     * @param order the order object to save
     */
    public static void saveOrderToDB(Order order) {

        if (order.get_order_number() == 0) {
            int nextId = getMaxOrderNumber() + 1;
            order.set_order_number(nextId);
        }
        
        // Default status for safety
        if (order.get_status() == null) order.set_status("ACTIVE");

        System.out.println("Generated new Order ID: " + order.get_order_number());

        // We added the new fields here: order_time, status, table_id
        String query = "INSERT INTO `orders` (order_number, order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order, order_time, status, table_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setInt(1, order.get_order_number());
            ps.setString(2, order.get_order_date());
            ps.setInt(3, order.get_number_of_guests());
            ps.setInt(4, order.get_confirmation_code());
            ps.setInt(5, order.get_subscriber_id());
            ps.setString(6, order.get_date_of_placing_order());
            
            // New fields for Assignment 3
            ps.setString(7, order.get_order_time());
            ps.setString(8, order.get_status());
            
            if (order.get_table_id() > 0) ps.setInt(9, order.get_table_id());
            else ps.setNull(9, java.sql.Types.INTEGER);

            ps.executeUpdate();
            System.out.println("Order saved successfully to DB!");

        } catch (SQLException e) {
            System.out.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets an order by its ID.
     * UPDATED: Retrieves the new fields from the DB.
     *
     * @param orderId the ID of the order to retrieve
     * @return the Order object if found, null otherwise
     */
    public static Order getOrder(int orderId) {
        Order order = null;
        try {
            String query = "SELECT * FROM `orders` WHERE order_number = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Using the full constructor (Make sure Order.java is updated!)
                order = new Order(
                    rs.getInt("order_number"), 
                    rs.getString("order_date"), 
                    rs.getString("order_time"), // new
                    rs.getInt("number_of_guests"),
                    rs.getInt("confirmation_code"), 
                    rs.getInt("subscriber_id"),
                    rs.getString("date_of_placing_order"),
                    rs.getString("status"),     // new
                    rs.getInt("table_id")       // new
                );
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    /**
     * Updates an existing order in the database.
     * UPDATED: Updates status and table allocation as well.
     *
     * @param order the order object containing updated information
     */
    public static void updateOrder(Order order) {
        String query = "UPDATE `orders` SET order_date = ?, number_of_guests = ?, status = ?, table_id = ? WHERE order_number = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, order.get_order_date());
            ps.setInt(2, order.get_number_of_guests());
            
            // New fields update
            ps.setString(3, order.get_status());
            if (order.get_table_id() > 0) ps.setInt(4, order.get_table_id());
            else ps.setNull(4, java.sql.Types.INTEGER);
            
            ps.setInt(5, order.get_order_number());

            ps.executeUpdate();
            System.out.println("Order updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // --- NEW METHOD NEEDED FOR ASSIGNMENT 3 ---
    
    /**
     * Retrieves all physical tables from the database.
     * Required for server logic to assign tables.
     * @return an ArrayList of Table objects
     */
    public static ArrayList<Table> getAllTables() {
        ArrayList<Table> tables = new ArrayList<>();
        try {
            String query = "SELECT * FROM restaurant_tables";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tables.add(new Table(rs.getInt("table_id"), rs.getInt("seats")));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }
    
    /**
     * Authenticates a user against the database and updates their login status.
     * <p>
     * Checks if a user exists with the provided username and password.
     * If found, it updates the 'is_logged_in' field to 1 in the database.
     * </p>
     *
     * @param username the username entered by the client
     * @param password the password entered by the client
     * @return a {@link User} object if authentication succeeds, or null if failed
     */
    public static User loginUser(String username, String password) {
        User user = null;
        try {
            // 1. Check if user exists
            String selectQuery = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(selectQuery);
            ps.setString(1, username);
            ps.setString(2, password);
            
            ResultSet rs = ps.executeQuery();
            
            // If user exists
            if (rs.next()) {
                user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("user_type"),
                    rs.getString("first_name"),
                    rs.getString("last_name")
                );

                // Check the ORIGINAL status from the database
                int currentDbStatus = rs.getInt("is_logged_in");

                if (currentDbStatus == 1) {
                    // User is ALREADY connected in the DB -> Set object to true so Server throws error
                    user.setLoggedIn(true);
                } else {
                    // User is NOT connected (Status 0)
                    
                    // A. Set object to false so Server allows the login process to complete
                    user.setLoggedIn(false); 

                    // B. Update the DB to 1 (So next time it will be locked)
                    String updateQuery = "UPDATE users SET is_logged_in = 1 WHERE id = ?";
                    PreparedStatement updatePs = conn.prepareStatement(updateQuery);
                    updatePs.setInt(1, rs.getInt("id"));
                    updatePs.executeUpdate();
                    
                    System.out.println("User " + username + " login approved and DB updated.");
                }
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Error during login query: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }
    
    /**
     * Updates the user's logged_in status to 0 (offline) in the database.
     */
    public static void updateUserLogout(String username) {
        String query = "UPDATE users SET is_logged_in = 0 WHERE username = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * The main method.
     * Useful for testing the database connection independently.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        connectToDB();
    }
}
