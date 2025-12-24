package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import common.Order;
import common.User; 

/**
 * The Class mysqlConnection.
 * <p>
 * Handles the connection to the MySQL database and provides methods
 * for querying, inserting, and updating data (Orders and Users).
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

            // Ensure the schema name (order_sch) matches your database configuration
            String url = "jdbc:mysql://127.0.0.1:3306/order_sch?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";
            conn = DriverManager.getConnection(url, "root", "abc123");

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
     * Generates a new ID based on the current maximum and inserts the order details 
     * into the 'orders' table.
     *
     * @param order the order object to save
     */
    public static void saveOrderToDB(Order order) {

        int nextId = getMaxOrderNumber() + 1;
        order.set_order_number(nextId);

        System.out.println("Generated new Order ID: " + nextId);

        String query = "INSERT INTO `orders` (order_number, order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setInt(1, order.get_order_number());
            ps.setString(2, order.get_order_date());
            ps.setInt(3, order.get_number_of_guests());
            ps.setInt(4, order.get_confirmation_code());
            ps.setInt(5, order.get_subscriber_id());
            ps.setString(6, order.get_date_of_placing_order());

            ps.executeUpdate();
            System.out.println("Order saved successfully to DB!");

        } catch (SQLException e) {
            System.out.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets an order by its ID.
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
                order = new Order(rs.getInt("order_number"), rs.getString("order_date"), rs.getInt("number_of_guests"),
                        rs.getInt("confirmation_code"), rs.getInt("subscriber_id"),
                        rs.getString("date_of_placing_order"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    /**
     * Updates an existing order in the database.
     * Specifically updates the order date and the number of guests.
     *
     * @param order the order object containing updated information
     */
    public static void updateOrder(Order order) {
        String query = "UPDATE `orders` SET order_date = ?, number_of_guests = ? WHERE order_number = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, order.get_order_date());
            ps.setInt(2, order.get_number_of_guests());
            ps.setInt(3, order.get_order_number());

            ps.executeUpdate();
            System.out.println("Order updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Authenticates a user against the database.
     * <p>
     * Checks if a user exists with the provided username and password.
     * If found, returns a User object populated with the data from the DB.
     * </p>
     *
     * @param username the username entered by the client
     * @param password the password entered by the client
     * @return a {@link User} object if authentication succeeds, or null if failed
     */
    public static User loginUser(String username, String password) {
        User user = null;
        try {
            // Query to find the user matching the username and password
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            
            ResultSet rs = ps.executeQuery();
            
            // If user exists, create the User object
            if (rs.next()) {
                user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("user_type"),
                    rs.getString("first_name"),
                    rs.getString("last_name")
                );
                user.setLoggedIn(true);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("Error during login query: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
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
