package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import common.Order;
import common.User;
import common.Subscriber;

/**
 * Handles all database connectivity and operations for the Bistro application.
 * This class uses JDBC to connect to a MySQL database and provides static methods
 * for querying and updating data related to orders, users, subscribers, and tables.
 * * @author Group-17
 * * @version 1.0
 */
public class mysqlConnection {

    /** The connection instance to the database. */
    private static Connection conn;

    /**
     * Establishes a connection to the MySQL database.
     * Loads the JDBC driver and attempts to connect using the configured URL and credentials.
     */
    public static void connectToDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Connecting to database...");
            String url = "jdbc:mysql://127.0.0.1:3306/order_sch?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";
            conn = DriverManager.getConnection(url, "root", "abc123"); // Update password if needed

            System.out.println("SQL connection succeed");
        } catch (Exception ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }
    }

    /**
     * Saves a newly created order into the database.
     * If the order ID is not set, it generates a new one automatically.
     *
     * @param order the order object containing order details
     * @return the order number if successful, or -1 if an error occurred
     */
    public static int saveOrderToDB(Order order) {
        if (order.get_order_number() == 0)
            order.set_order_number(getMaxOrderNumber() + 1);
        if (order.get_status() == null)
            order.set_status("ACTIVE");

        String query = "INSERT INTO orders (order_number, order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order, order_time, status, table_id, client_phone, client_email, client_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, order.get_order_number());
            ps.setString(2, order.get_order_date());
            ps.setInt(3, order.get_number_of_guests());
            ps.setInt(4, order.get_confirmation_code());
            ps.setInt(5, order.get_subscriber_id());
            ps.setString(6, order.get_date_of_placing_order());
            ps.setString(7, order.get_order_time());
            ps.setString(8, order.get_status());

            if (order.get_table_id() <= 0)
                ps.setNull(9, java.sql.Types.INTEGER);
            else
                ps.setInt(9, order.get_table_id());

            ps.setString(10, order.getPhone());
            ps.setString(11, order.getEmail());
            ps.setString(12, order.get_name());
            ps.executeUpdate();
            return order.get_order_number();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Retrieves the maximum order number currently in the database.
     * Used for generating new order IDs.
     *
     * @return the maximum order number, or 0 if no orders exist
     */
    private static int getMaxOrderNumber() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(order_number) FROM orders");
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
        }
        return 0;
    }

    /**
     * Authenticates a user (staff/manager) against the database.
     *
     * @param user the username
     * @param pass the password
     * @return a User object if authentication is successful, null otherwise
     */
    public static User loginUser(String user, String pass) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                        rs.getString(6));
        } catch (SQLException e) {
        }
        return null;
    }

    /**
     * Updates the user's status to 'Logged Out' in the database.
     *
     * @param username the username of the user to log out
     */
    public static void updateUserLogout(String username) {
        /* Implementation handled in login logic or separate method if required */ 
    }

    /**
     * Retrieves an order by its unique order number.
     *
     * @param id the order number
     * @return the Order object if found, null otherwise
     */
    public static Order getOrder(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE order_number = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order o = new Order(rs.getInt(1), rs.getString(2), rs.getString(7), rs.getInt(3), rs.getInt(4),
                        rs.getInt(5), rs.getString(6), rs.getString(8), rs.getInt(9), "", rs.getString(10));
                o.setEmail(rs.getString("client_email"));
                return o;
            }
        } catch (SQLException e) {
        }
        return null;
    }

    /**
     * Marks an order as 'Cancelled' in the database.
     * This acts as a soft delete operation.
     *
     * @param id the order number to cancel
     * @return true if the operation was successful, false otherwise
     */
    public static boolean deleteOrder(int id) {
        try {
            PreparedStatement ps = conn
                    .prepareStatement("UPDATE orders SET status = 'Cancelled' WHERE order_number = ?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates details of an existing order.
     *
     * @param order the order object containing updated details
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateOrder(Order order) {
        String query = "UPDATE orders SET order_date = ?, order_time = ?, number_of_guests = ? WHERE order_number = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, order.get_order_date());
            ps.setString(2, order.get_order_time());
            ps.setInt(3, order.get_number_of_guests());
            ps.setInt(4, order.get_order_number());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error updating order: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a subscriber by their unique ID.
     *
     * @param id the subscriber ID
     * @return the Subscriber object if found, null otherwise
     */
    public static Subscriber getSubscriber(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM subscribers WHERE subscriber_id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int subId = rs.getInt("subscriber_id");
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                String phone = rs.getString("phone_number");
                String email = rs.getString("email");
                String username = rs.getString("username");

                Subscriber s = new Subscriber(subId, name, surname, phone, email, null, 0);
                s.setUsername(username);

                return s;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subscriber: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a new subscriber to the database.
     *
     * @param sub the subscriber object to add
     * @return the new subscriber ID if successful, or -1 on failure
     */
    public static int addSubscriber(Subscriber sub) {
        int newId = 1;

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(subscriber_id) FROM subscribers");
            if (rs.next()) {
                newId = rs.getInt(1) + 1;
            }

            String query = "INSERT INTO subscribers (subscriber_id, username, name, surname, phone_number, email) VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(query);

            ps.setInt(1, newId);
            ps.setString(2, sub.getUsername());
            ps.setString(3, sub.getFirstName());
            ps.setString(4, sub.getLastName());
            ps.setString(5, sub.getPhoneNumber());
            ps.setString(6, sub.getEmail());

            ps.executeUpdate();

            System.out.println("New subscriber added: " + sub.getUsername() + " (ID: " + newId + ")");
            return newId;

        } catch (SQLException e) {
            System.out.println("Error adding subscriber: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Retrieves an order based on its confirmation code.
     *
     * @param code the confirmation code
     * @return the Order object if found, null otherwise
     */
    public static Order getOrderByConfirmationCode(int code) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE confirmation_code = ?");
            ps.setInt(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order o = new Order(rs.getInt(1), rs.getString(2), rs.getString(7), rs.getInt(3), rs.getInt(4),
                        rs.getInt(5), rs.getString(6), rs.getString(8), rs.getInt(9), "", rs.getString(10));
                o.setEmail(rs.getString("client_email"));
                return o;
            }
        } catch (SQLException e) {
        }
        return null;
    }

    /**
     * Retrieves the last order placed by a specific subscriber.
     *
     * @param id the subscriber ID
     * @return the last Order object, or null if none found
     */
    public static Order getLastOrderForSubscriber(int id) {
        return null; // Placeholder implementation
    }

    /**
     * Retrieves all tables currently defined in the restaurant.
     *
     * @return a list of strings representing tables
     */
    public static ArrayList<String> getAllTables() {
        return null; // Placeholder implementation
    }

    // --- SMART LOGIC ---

    /**
     * Helper class for simulating table allocation.
     */
    private static class TempTable implements Comparable<TempTable> {
        int id, seats;

        public TempTable(int id, int seats) {
            this.id = id;
            this.seats = seats;
        }

        @Override
        public int compareTo(TempTable o) {
            return Integer.compare(this.seats, o.seats);
        }
    }

    /**
     * Smart algorithm to check table availability for a specific date and time.
     * Considers opening hours, existing orders, and table capacities.
     *
     * @param date the requested date
     * @param timeStr the requested time
     * @param newGuests the number of guests
     * @return true if a table is available, false otherwise
     */
    public static boolean checkAvailabilitySmart(String date, String timeStr, int newGuests) {
        try {
            if (!isRestaurantOpen(date, timeStr)) {
                System.out.println("Debug: Restaurant is closed at " + timeStr + " on " + date);
                return false;
            }

            java.time.LocalTime newTime = java.time.LocalTime.parse(timeStr);
            ArrayList<TempTable> allTables = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rsT = stmt.executeQuery("SELECT table_id, number_of_seats FROM restaurant_tables");
            while (rsT.next())
                allTables.add(new TempTable(rsT.getInt("table_id"), rsT.getInt("number_of_seats")));
            rsT.close();
            Collections.sort(allTables);

            ArrayList<Integer> existingOrdersGuests = new ArrayList<>();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT number_of_guests, order_time FROM orders WHERE order_date = ? AND status != 'CANCELLED' AND status != 'WAITING'");
            ps.setString(1, date);
            ResultSet rsO = ps.executeQuery();
            while (rsO.next()) {
                String tStr = rsO.getString("order_time");
                if (tStr.length() > 5)
                    tStr = tStr.substring(0, 5);
                java.time.LocalTime existingTime = java.time.LocalTime.parse(tStr);
                if (Math.abs(java.time.Duration.between(existingTime, newTime).toMinutes()) < 120) {
                    existingOrdersGuests.add(rsO.getInt("number_of_guests"));
                }
            }
            rsO.close();
            existingOrdersGuests.add(newGuests);
            existingOrdersGuests.sort(Collections.reverseOrder());

            ArrayList<TempTable> freeTablesSim = new ArrayList<>(allTables);
            for (int guestsAmount : existingOrdersGuests) {
                boolean seated = false;
                for (int i = 0; i < freeTablesSim.size(); i++) {
                    if (freeTablesSim.get(i).seats >= guestsAmount) {
                        freeTablesSim.remove(i);
                        seated = true;
                        break;
                    }
                }
                if (!seated)
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cancels orders marked as APPROVED if the client has not arrived within 15 minutes.
     * Used by the auto-cancellation service.
     *
     * @return the number of cancelled orders
     */
    public static int cancelNoShows() {
        int count = 0;
        try {
            String query = "UPDATE orders SET status = 'NO_SHOW' " + "WHERE status = 'APPROVED' "
                    + "AND CONCAT(order_date, ' ', order_time) < DATE_SUB(NOW(), INTERVAL 15 MINUTE) "
                    + "AND order_date = CURDATE()";

            Statement stmt = conn.createStatement();
            count = stmt.executeUpdate(query);

        } catch (SQLException e) {
            System.out.println("Error in cancelNoShows: " + e.getMessage());
        }
        return count;
    }

    /**
     * Updates the status of a specific order.
     *
     * @param orderId the order number
     * @param newStatus the new status to set
     * @return true if updated successfully, false otherwise
     */
    public static boolean updateOrderStatus(int orderId, String newStatus) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = ? WHERE order_number = ?");
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Checks the waiting list for a specific date and promotes orders if space becomes available.
     * Promotes orders to APPROVED and notifies the client via simulation (console print).
     *
     * @param date the date to check
     * @return a log string of promoted orders
     */
    public static String checkWaitingList(String date) {
        StringBuilder promotedOrders = new StringBuilder();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM orders WHERE order_date = ? AND status = 'WAITING' ORDER BY order_number ASC");
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            ArrayList<Order> waitingOrders = new ArrayList<>();
            while (rs.next()) {
                Order o = new Order(rs.getInt(1), rs.getString(2), rs.getString(7), rs.getInt(3), rs.getInt(4),
                        rs.getInt(5), rs.getString(6), rs.getString(8), rs.getInt(9), "", rs.getString(10));
                o.setEmail(rs.getString("client_email"));
                waitingOrders.add(o);
            }
            rs.close();

            for (Order waitOrder : waitingOrders) {
                if (checkAvailabilitySmart(waitOrder.get_order_date(), waitOrder.get_order_time(),
                        waitOrder.get_number_of_guests())) {
                    PreparedStatement psUpdate = conn
                            .prepareStatement("UPDATE orders SET status = 'APPROVED' WHERE order_number = ?");
                    psUpdate.setInt(1, waitOrder.get_order_number());
                    psUpdate.executeUpdate();

                    String clientEmail = waitOrder.getEmail();
                    if (clientEmail != null && !clientEmail.isEmpty()) {
                        System.out.println("\n[SIMULATION] Email sent to: " + clientEmail + " (Table Ready!)");
                        promotedOrders.append("Order #").append(waitOrder.get_order_number())
                                .append(" -> APPROVED (Email sent)\n");
                    } else {
                        promotedOrders.append("Order #").append(waitOrder.get_order_number())
                                .append(" -> APPROVED (No Email)\n");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return promotedOrders.toString();
    }

    /**
     * Generates a monthly report string containing stats for the current month.
     * Includes total orders, cancellations, late arrivals, and hourly distribution.
     *
     * @return a comma-separated string containing report data
     */
    public static String generateMonthlyReport() {
        int total = 0, cancelled = 0, noShow = 0, waiting = 0;
        
        int lateArrivals = 0;
        int actualArrivals = 0;
        int[] arrivals = new int[24];
        int[] departures = new int[24];
        
        try {
            String query = "SELECT status, actual_arrival_time, actual_leave_time, order_time FROM orders " +
                           "WHERE MONTH(order_date) = MONTH(CURRENT_DATE()) " +
                           "AND YEAR(order_date) = YEAR(CURRENT_DATE())";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                total++;
                String status = rs.getString("status");
                String arrivalTime = rs.getString("actual_arrival_time");
                String leaveTime = rs.getString("actual_leave_time");
                String orderTime = rs.getString("order_time");

                if (status.equalsIgnoreCase("Cancelled")) cancelled++;
                else if (status.equalsIgnoreCase("No_Show") || status.equalsIgnoreCase("No-Show")) noShow++;
                else if (status.equalsIgnoreCase("Waiting")) waiting++;

                if (arrivalTime != null && !arrivalTime.isEmpty() && orderTime != null && !orderTime.isEmpty()) {
                     actualArrivals++; 
                     try {
                         String t1 = orderTime.contains(":") ? orderTime : orderTime.substring(0, 2) + ":" + orderTime.substring(2);
                         String t2 = arrivalTime.contains(":") ? arrivalTime : arrivalTime.substring(0, 2) + ":" + arrivalTime.substring(2);
                         
                         java.time.LocalTime ordered = java.time.LocalTime.parse(t1);
                         java.time.LocalTime actual = java.time.LocalTime.parse(t2);
                         
                         if (java.time.temporal.ChronoUnit.MINUTES.between(ordered, actual) > 10) {
                             lateArrivals++;
                         }
                     } catch (Exception e) {}
                }

                if (arrivalTime != null && arrivalTime.contains(":")) {
                    try { int h = Integer.parseInt(arrivalTime.split(":")[0]); if (h >= 0 && h < 24) arrivals[h]++; } catch (Exception e) {}
                }
                if (leaveTime != null && leaveTime.contains(":")) {
                    try { int h = Integer.parseInt(leaveTime.split(":")[0]); if (h >= 0 && h < 24) departures[h]++; } catch (Exception e) {}
                }
            }
            rs.close();

        } catch (SQLException e) { e.printStackTrace(); }

        StringBuilder sb = new StringBuilder();
        
        sb.append(total).append(",")
          .append(cancelled).append(",")
          .append(noShow).append(",")
          .append(waiting);

        for (int i = 12; i <= 23; i++) sb.append(",").append(arrivals[i]);
        for (int i = 12; i <= 23; i++) sb.append(",").append(departures[i]);

        sb.append(",").append(lateArrivals).append(",").append(actualArrivals);

        return sb.toString();
    }

    /**
     * Retrieves all orders for a specific subscriber.
     *
     * @param subscriberId the subscriber's ID
     * @return a list of Order objects belonging to the subscriber
     */
    public static ArrayList<Order> getSubscriberOrders(int subscriberId) {
        ArrayList<Order> history = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE subscriber_id = ? ORDER BY order_date DESC, order_time DESC";

        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, subscriberId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order o = new Order(rs.getInt("order_number"), 
                        rs.getString("order_date"), 
                        rs.getString("order_time"), 
                        rs.getInt("number_of_guests"), 
                        rs.getInt("confirmation_code"), 
                        rs.getInt("subscriber_id"), 
                        rs.getString("date_of_placing_order"), 
                        rs.getString("status"), 
                        rs.getInt("table_id"), 
                        "", 
                        rs.getString("client_phone") 
                );

                o.setEmail(rs.getString("client_email"));

                history.add(o);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching history: " + e.getMessage());
        }
        return history;
    }

    /**
     * Updates the contact details of a subscriber.
     *
     * @param subId the subscriber ID
     * @param newPhone the new phone number
     * @param newEmail the new email address
     * @return true if updated successfully, false otherwise
     */
    public static boolean updateSubscriberDetails(int subId, String newPhone, String newEmail) {
        String query = "UPDATE subscribers SET phone_number = ?, email = ? WHERE subscriber_id = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, newPhone);
            ps.setString(2, newEmail);
            ps.setInt(3, subId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a new table to the restaurant.
     *
     * @param tableId the table ID
     * @param seats the number of seats
     * @return a message string indicating success or failure
     */
    public static String addTable(int tableId, int seats) {
        try {
            PreparedStatement ps = conn
                    .prepareStatement("INSERT INTO restaurant_tables (table_id, number_of_seats) VALUES (?, ?)");
            ps.setInt(1, tableId);
            ps.setInt(2, seats);
            ps.executeUpdate();
            return "Table " + tableId + " Added Successfully!";

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                return "Error: Table ID " + tableId + " already exists!";
            }
            e.printStackTrace();
            return "Error: Could not add table.";
        }
    }

    /**
     * Deletes a table from the restaurant configuration.
     *
     * @param tableId the ID of the table to delete
     * @return true if deleted successfully, false otherwise
     */
    public static boolean deleteTable(int tableId) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM restaurant_tables WHERE table_id = ?");
            ps.setInt(1, tableId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Updates the opening hours for a specific day or date.
     *
     * @param dayOrDate the day of week or specific date
     * @param hours the opening hours string (format: open-close)
     * @return true if updated successfully, false otherwise
     */
    public static boolean updateOpeningHour(String dayOrDate, String hours) {
        String[] times = hours.split("-");
        if (times.length < 2)
            return false;

        String openTime = times[0].trim();
        String closeTime = times[1].trim();

        try {
            String updateQuery = "UPDATE restaurant_settings SET open_time = ?, close_time = ? WHERE day_of_week = ?";
            PreparedStatement ps = conn.prepareStatement(updateQuery);
            ps.setString(1, openTime);
            ps.setString(2, closeTime);
            ps.setString(3, dayOrDate);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                String insertQuery = "INSERT INTO restaurant_settings (day_of_week, open_time, close_time) VALUES (?, ?, ?)";
                PreparedStatement psInsert = conn.prepareStatement(insertQuery);
                psInsert.setString(1, dayOrDate);
                psInsert.setString(2, openTime);
                psInsert.setString(3, closeTime);
                psInsert.executeUpdate();
                System.out.println("New special date added: " + dayOrDate);
            } else {
                System.out.println("Existing day updated: " + dayOrDate);
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Strictly authenticates a subscriber using both ID and username.
     *
     * @param id the subscriber ID
     * @param username the username
     * @return the Subscriber object if found, null otherwise
     */
    public static Subscriber loginSubscriberStrict(int id, String username) {
        try {
            String query = "SELECT * FROM subscribers WHERE subscriber_id = ? AND username = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            ps.setString(2, username);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Subscriber s = new Subscriber(rs.getInt("subscriber_id"), rs.getString("name"), rs.getString("surname"),
                        rs.getString("phone_number"), rs.getString("email"), null, 0);
                s.setUsername(rs.getString("username"));
                return s;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Recovers a subscriber's ID using their username and phone number.
     *
     * @param username the username
     * @param phone the phone number
     * @return the subscriber ID if found, or -1 otherwise
     */
    public static int recoverSubscriberId(String username, String phone) {
        try {
            String query = "SELECT subscriber_id FROM subscribers WHERE username = ? AND phone_number = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, phone);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("subscriber_id"); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; 
    }

    /**
     * Retrieves all orders for the staff view, sorted by date and time.
     *
     * @return a list of all orders
     */
    public static ArrayList<Order> getAllOrders() {
        ArrayList<Order> list = new ArrayList<>();
        String query = "SELECT * FROM orders ORDER BY order_date DESC, order_time DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Order o = new Order(rs.getInt("order_number"), rs.getString("order_date"), rs.getString("order_time"),
                        rs.getInt("number_of_guests"), rs.getInt("subscriber_id"), rs.getInt("table_id"),
                        rs.getString("status"), "Active", 0, "", rs.getString("client_email"));
                try {
                    o.set_confirmation_code(rs.getInt("confirmation_code"));
                } catch (Exception e) {
                }
                list.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Marks an order as 'Arrived' and assigns a table number automatically.
     * Updates the actual arrival time.
     *
     * @param orderId the order ID
     * @return the assigned table number, or -1 on failure
     */
    public static int markOrderAsArrived(int orderId) {
        String query = "UPDATE orders SET status = 'ACTIVE', actual_arrival_time = ?, table_id = ? WHERE order_number = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(query);

            String currentTime = java.time.LocalTime.now().toString();
            if (currentTime.length() > 5)
                currentTime = currentTime.substring(0, 5);
            ps.setString(1, currentTime);

            int assignedTable = (orderId % 10) + 1;
            ps.setInt(2, assignedTable);

            ps.setInt(3, orderId);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                return assignedTable;
            } else {
                return -1;
            }

        } catch (SQLException e) {
            System.out.println("Error marking arrived: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Generates a daily report of orders for the current date.
     * Includes details about active and approved orders.
     *
     * @return a formatted string report
     */
    public static String getDailyReport() {
        StringBuilder report = new StringBuilder();
        String today = java.time.LocalDate.now().toString();

        report.append("=== Daily Orders Report (" + today + ") ===\n\n");

        String query = "SELECT o.*, s.name, s.surname " + "FROM orders o "
                + "LEFT JOIN subscribers s ON o.subscriber_id = s.subscriber_id "
                + "WHERE o.order_date = ? AND o.status IN ('APPROVED', 'ACTIVE') "
                + "ORDER BY FIELD(o.status, 'ACTIVE', 'APPROVED'), o.order_time ASC";

        try {
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, today);
            java.sql.ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;

                int subId = rs.getInt("subscriber_id");
                String displayName = "Unknown";

                if (subId > 1 && subId != 999) {
                    try {
                        String fName = rs.getString("name"); 
                        String lName = rs.getString("surname"); 

                        if (fName != null)
                            displayName = fName + " " + (lName != null ? lName : "");
                        else
                            displayName = "Subscriber " + subId;
                    } catch (java.sql.SQLException ex) {
                        displayName = "Subscriber " + subId;
                    }
                } else {
                    try {
                        String savedName = rs.getString("client_name");
                        if (savedName != null && !savedName.isEmpty()) {
                            displayName = savedName;
                        } else {
                            displayName = "Casual Client";
                        }
                    } catch (Exception e) {
                        displayName = "Casual Client";
                    }
                }

                String time = rs.getString("order_time");
                if (time != null && time.length() > 5)
                    time = time.substring(0, 5);

                report.append(String.format("ID: %d | Time: %s | Name: %s | Guests: %d | Status: %s | Table: %s\n",
                        rs.getInt("order_number"), time, displayName, rs.getInt("number_of_guests"),
                        rs.getString("status"), (rs.getString("table_id") == null ? "-" : rs.getString("table_id"))));
                report.append("----------------------------------------------------\n");
            }

            if (!found)
                report.append("No active or approved orders for today.");

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return "Error generating report: " + e.getMessage();
        }

        return report.toString();
    }

    /**
     * Generates a report of the waiting list for the current date.
     *
     * @return a formatted string report
     */
    public static String getWaitingListReport() {
        StringBuilder report = new StringBuilder();
        String today = java.time.LocalDate.now().toString();

        report.append("=== Waiting List (" + today + ") ===\n\n");

        String query = "SELECT * FROM orders WHERE order_date = ? AND status = 'WAITING' ORDER BY order_time ASC";

        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                report.append(String.format("ID: %d | Time: %s | Guests: %d | Client: %s\n", rs.getInt("order_number"),
                        rs.getString("order_time"), rs.getInt("number_of_guests"),
                        (rs.getString("client_phone") != null ? rs.getString("client_phone") : "Subscriber")));
            }

            if (!found)
                report.append("Waiting list is empty.");

        } catch (SQLException e) {
            return "Error fetching waiting list: " + e.getMessage();
        }

        return report.toString();
    }

    /**
     * Marks an order as 'FINISHED' and records the leave time.
     *
     * @param orderId the order ID
     * @return true if updated successfully, false otherwise
     */
    public static boolean markOrderAsFinished(int orderId) {
        String query = "UPDATE orders SET status = 'FINISHED', actual_leave_time = ? WHERE order_number = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(query);

            String currentTime = java.time.LocalTime.now().toString();
            if (currentTime.length() > 5)
                currentTime = currentTime.substring(0, 5);

            ps.setString(1, currentTime);
            ps.setInt(2, orderId);

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error marking finished: " + e.getMessage());
            return false;
        }
    }

    /**
     * Suggests alternative times for a booking if the requested time is full.
     * Checks +/- 30 and 60 minutes from the requested time.
     *
     * @param date the requested date
     * @param requestedTime the requested time
     * @param guests the number of guests
     * @return a suggestion string or a waiting list prompt
     */
    public static String checkAlternativeTimes(String date, String requestedTime, int guests) {
        try {
            String[] parts = requestedTime.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);

            int[] offsets = { -30, 30, -60, 60 };
            StringBuilder suggestions = new StringBuilder();

            for (int offset : offsets) {
                int newH = h;
                int newM = m + offset;

                while (newM >= 60) {
                    newM -= 60;
                    newH++;
                }
                while (newM < 0) {
                    newM += 60;
                    newH--;
                }

                if (newH >= 0 && newH < 24) { 
                    String altTime = String.format("%02d:%02d", newH, newM);
                    if (checkAvailabilitySmart(date, altTime, guests)) {
                        if (suggestions.length() > 0)
                            suggestions.append(" or ");
                        suggestions.append(altTime);
                    }
                }
            }

            if (suggestions.length() > 0) {
                return "Full. Try: " + suggestions.toString();
            } else {
                return "No available table. Join Waiting List?";
            }

        } catch (Exception e) {
            return "No available table";
        }
    }

    /**
     * Updates the seat capacity of a specific table.
     *
     * @param tableId the table ID
     * @param newSeats the new number of seats
     * @return true if updated successfully, false otherwise
     */
    public static boolean updateTableSeats(int tableId, int newSeats) {
        String query = "UPDATE restaurant_tables SET number_of_seats = ? WHERE table_id = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, newSeats); 
            ps.setInt(2, tableId); 

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; 
        } catch (SQLException e) {
            System.out.println("Error updating table: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks active orders against a 2-hour time limit.
     * Auto-closes orders that have exceeded the limit.
     *
     * @return a list of strings describing closed orders
     */
    public static ArrayList<String> checkTimeLimit() {
        ArrayList<String> closedOrdersInfo = new ArrayList<>();
        String query = "SELECT order_number, actual_arrival_time FROM orders WHERE status = 'ACTIVE'";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            java.time.LocalTime now = java.time.LocalTime.now();

            while (rs.next()) {
                String arrivalStr = rs.getString("actual_arrival_time");
                if (arrivalStr != null && arrivalStr.contains(":")) {
                    try {
                        java.time.LocalTime arrivalTime = java.time.LocalTime.parse(arrivalStr);

                        if (now.isAfter(arrivalTime.plusHours(2))) {
                            int orderId = rs.getInt("order_number");

                            markOrderAsFinished(orderId);

                            closedOrdersInfo.add("Order #" + orderId);
                        }
                    } catch (Exception e) {
                         }
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return closedOrdersInfo;
    }

    /**
     * Retrieves all orders associated with a specific client ID.
     *
     * @param clientId the client ID (subscriber ID)
     * @return a list of Order objects
     */
    public static java.util.ArrayList<common.Order> getOrdersByClientId(int clientId) {
        java.util.ArrayList<common.Order> orders = new java.util.ArrayList<>();

        String query = "SELECT * FROM orders WHERE subscriber_id = ?";

        try {
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, clientId);
            java.sql.ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                common.Order order = new common.Order(rs.getInt("order_number"), rs.getString("order_date"),
                        rs.getString("order_time"), rs.getInt("number_of_guests"), rs.getInt("confirmation_code"),
                        rs.getInt("subscriber_id"),
                        null, 
                        rs.getString("status"), rs.getInt("table_id"), null, null);
                orders.add(order);
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error fetching history:");
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Retrieves formatted subscriber details for display.
     *
     * @param id the subscriber ID
     * @return a string containing formatted subscriber details, or null if not found
     */
    public static String getSubscriberDetails(int id) {
        String details = null;
        String query = "SELECT * FROM subscribers WHERE subscriber_id = ?";

        try {
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("name");
                String lastName = rs.getString("surname");
                String phone = rs.getString("phone_number");
                String email = rs.getString("email");
                String username = rs.getString("username");

                details = "Subscriber Details:\n" + "------------------\n" + "ID: " + id + "\n" + "Name: " + firstName
                        + " " + lastName + "\n" + "Phone: " + phone + "\n" + "Email: " + email + "\n" + "Username: "
                        + username;
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    /**
     * Retrieves the subscriber ID associated with a specific order.
     *
     * @param orderId the order number
     * @return the subscriber ID found, or 0 if not found
     */
    public static int getSubscriberIdByOrder(int orderId) {
        int subId = 0;
        String query = "SELECT subscriber_id FROM orders WHERE order_number = ?";
        try {
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, orderId);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                subId = rs.getInt("subscriber_id");
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return subId;
    }

    /**
     * Updates a single cell value in the orders table.
     * Useful for dynamic updates like status or time.
     *
     * @param orderId the order ID
     * @param columnName the column name to update
     * @param newValue the new value
     * @return true if updated successfully, false otherwise
     */
    public static boolean updateCell(int orderId, String columnName, String newValue) {
        String query = "UPDATE orders SET " + columnName + " = ? WHERE order_number = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, newValue);
            ps.setInt(2, orderId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating cell " + columnName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a list of approved orders scheduled for the current day.
     *
     * @return a list of formatted strings describing the orders
     */
    public static ArrayList<String> getApprovedOrdersForToday() {
        ArrayList<String> list = new ArrayList<>();
        String query = "SELECT order_number, order_time, number_of_guests, subscriber_id " + "FROM orders "
                + "WHERE order_date = CURDATE() AND status = 'APPROVED' " + "ORDER BY order_time ASC";
        try {
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int id = rs.getInt("order_number");
                String time = rs.getString("order_time");
                if (time.length() > 5)
                    time = time.substring(0, 5); 

                String type = (rs.getInt("subscriber_id") > 0 && rs.getInt("subscriber_id") != 999) ? " (Subscriber)"
                        : "";

                String display = "Order #" + id + " | " + time + " | " + rs.getInt("number_of_guests") + " ppl" + type;
                list.add(display);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Checks if the restaurant is open at a specific date and time.
     * Verifies against special dates and regular weekly schedules.
     *
     * @param dateStr the date string (YYYY-MM-DD)
     * @param timeStr the time string (HH:MM)
     * @return true if open, false if closed
     */
    public static boolean isRestaurantOpen(String dateStr, String timeStr) {
        try {
            String openStr = null;
            String closeStr = null;

            String queryDate = "SELECT open_time, close_time FROM restaurant_settings WHERE day_of_week = ?";
            PreparedStatement psDate = conn.prepareStatement(queryDate);
            psDate.setString(1, dateStr); 
            ResultSet rsDate = psDate.executeQuery();

            if (rsDate.next()) {
                openStr = rsDate.getString("open_time");
                closeStr = rsDate.getString("close_time");
            } else {
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                String dayOfWeek = date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL,
                        java.util.Locale.ENGLISH);

                PreparedStatement psDay = conn.prepareStatement(queryDate);
                psDay.setString(1, dayOfWeek);
                ResultSet rsDay = psDay.executeQuery();

                if (rsDay.next()) {
                    openStr = rsDay.getString("open_time");
                    closeStr = rsDay.getString("close_time");
                }
            }

            if (openStr == null || closeStr == null)
                return false;

            openStr = openStr.trim();
            closeStr = closeStr.trim();
            if (openStr.length() == 4)
                openStr = "0" + openStr;
            if (closeStr.length() == 4)
                closeStr = "0" + closeStr;
            if (openStr.length() == 5)
                openStr += ":00";
            if (closeStr.length() == 5)
                closeStr += ":00";

            String checkTime = timeStr;
            if (checkTime.length() == 5)
                checkTime += ":00";

            java.time.LocalTime requestedTime = java.time.LocalTime.parse(checkTime);
            java.time.LocalTime openTime = java.time.LocalTime.parse(openStr);
            java.time.LocalTime closeTime = java.time.LocalTime.parse(closeStr);

            return !requestedTime.isBefore(openTime) && requestedTime.isBefore(closeTime);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
