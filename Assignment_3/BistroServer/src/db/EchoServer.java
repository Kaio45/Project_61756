package db;

import java.io.IOException;
import java.util.ArrayList;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import common.Order;
import common.Table;
import common.Message;
import common.ActionType;
import common.User;

/**
 * The Class EchoServer.
 * Implements the main server logic for the Bistro application (Assignment 3).
 * Handles connections, database interactions, and business logic (Table Allocation).
 */
public class EchoServer extends AbstractServer {
    
    final public static int DEFAULT_PORT = 5555;
    public static ServerPortFrameController serverController;

    public EchoServer(int port) {
        super(port);
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        if (msg instanceof Message) {
            Message receivedMsg = (Message) msg;
            
            switch (receivedMsg.getAction()) {
            
                case LOGIN:
                    handleLogin(receivedMsg, client);
                    break;
                    
                case LOGOUT:
                    break;

                case GET_ORDER:
                    handleGetOrder(receivedMsg, client);
                    break;

                case ADD_ORDER:
                    handleAddOrderSmart(receivedMsg, client);
                    break;
                    
                case UPDATE_ORDER:
                    handleUpdateOrder(receivedMsg, client);
                    break;
                    
                case GET_ALL_TABLES:
                    try {
                        ArrayList<Table> tables = mysqlConnection.getAllTables();
                        client.sendToClient(new Message(ActionType.GET_ALL_TABLES, tables));
                    } catch (IOException e) { e.printStackTrace(); }
                    break;
                    
                default:
                    System.out.println("Unknown Action");
                    break;
            }
        }
    }
    
    /**
     * Handles the Smart Order logic (Assignment 3).
     * 1. Checks if a suitable table is free.
     * 2. If free -> Assigns Table ID, Sets Status ACTIVE.
     * 3. If full -> Sets Status WAITING.
     * 4. Saves to DB and notifies client.
     */
    private void handleAddOrderSmart(Message msg, ConnectionToClient client) {
        Order newOrder = (Order) msg.getContent();
        
        // Algorithm: Check for available table
        int assignedTableId = findFreeTable(newOrder.get_order_date(), newOrder.get_order_time(), newOrder.get_number_of_guests());
        
        if (assignedTableId > 0) {
            // SUCCESS: Table found
            newOrder.set_table_id(assignedTableId);
            newOrder.set_status("ACTIVE");
            mysqlConnection.saveOrderToDB(newOrder);
            
            try {
                // Send success message + the updated order object
                client.sendToClient(new Message(ActionType.ADD_ORDER, "Order Approved! Table: " + assignedTableId));
            } catch (IOException e) { e.printStackTrace(); }
            
        } else {
            // FAILURE: No table -> Waiting List
            newOrder.set_table_id(0);
            newOrder.set_status("WAITING");
            mysqlConnection.saveOrderToDB(newOrder);
            
            try {
                client.sendToClient(new Message(ActionType.ADD_ORDER, "No table available. Entered Waiting List."));
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    /**
     * Core Algorithm: Find a free table.
     * @return Table ID if found, -1 if not.
     */
    private int findFreeTable(String date, String time, int guests) {
        // Get all physical tables
        ArrayList<Table> allTables = mysqlConnection.getAllTables();
        
        // Get currently active orders for that time
        ArrayList<Order> activeOrders = mysqlConnection.getActiveOrders(date, time);
        
        // Create a list of occupied table IDs
        ArrayList<Integer> occupiedIds = new ArrayList<>();
        
        int reqTime = Integer.parseInt(time.split(":")[0]); // Extract hour

        for (Order o : activeOrders) {
            if (o.get_table_id() > 0) {
                // Assume each reservation takes 2 hours.
                // If existing order is at 18:00, it blocks 18:00-20:00.
                // If request is 19:00, it overlaps.
                try {
                    int existingTime = Integer.parseInt(o.get_order_time().split(":")[0]);
                    if (Math.abs(existingTime - reqTime) < 2) {
                        occupiedIds.add(o.get_table_id());
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
        }
        
        // Find the best fit (First Fit)
        for (Table t : allTables) {
            // Condition: Table has enough seats AND is not occupied
            if (t.getSeats() >= guests && !occupiedIds.contains(t.getTableId())) {
                return t.getTableId();
            }
        }
        
        return -1; // No table found
    }

    private void handleLogin(Message msg, ConnectionToClient client) {
        User loginRequest = (User) msg.getContent();
        User fullUser = mysqlConnection.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
        try {
            if (fullUser != null) {
                if (fullUser.isLoggedIn()) {
                     client.sendToClient(new Message(ActionType.LOGIN, "Already Logged In"));
                } else {
                     client.sendToClient(new Message(ActionType.LOGIN, fullUser));
                }
            } else {
                client.sendToClient(new Message(ActionType.LOGIN, "Wrong username or password"));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleGetOrder(Message msg, ConnectionToClient client) {
        String idStr = (String) msg.getContent();
        try {
            int id = Integer.parseInt(idStr);
            Order order = mysqlConnection.getOrder(id);
            if (order != null) {
                client.sendToClient(new Message(ActionType.GET_ORDER, order));
            } else {
                client.sendToClient(new Message(ActionType.GET_ORDER, "Order not found!"));
            }
        } catch (Exception e) {
            try { client.sendToClient(new Message(ActionType.GET_ORDER, "Invalid ID")); } 
            catch (IOException ex) {}
        }
    }
    
    private void handleUpdateOrder(Message msg, ConnectionToClient client) {
        Order order = (Order) msg.getContent();
        mysqlConnection.updateOrder(order);
        try {
            client.sendToClient(new Message(ActionType.UPDATE_ORDER, "Order Updated"));
        } catch (IOException e) {}
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        String msg = "Client disconnected: " + client.getInetAddress();
        System.out.println(msg);
        if (serverController != null) serverController.addToLog(msg);
    }

    @Override
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
        try {
            mysqlConnection.connectToDB();
            if (serverController != null) {
                serverController.addToLog("Server listening on port " + getPort());
                serverController.addToLog("Database connected!");
            }
        } catch (Exception e) {
            if (serverController != null) serverController.addToLog("Database connection failed!");
        }
    }

    @Override
    protected void serverStopped() {
        if (serverController != null) serverController.addToLog("Server Stopped.");
    }
    
    @Override
    protected void clientConnected(ConnectionToClient client) {
          String msg = "Client Connected! IP: " + client.getInetAddress();
          if (serverController != null) serverController.addToLog(msg);
    }
    
    public static void main(String[] args){
        int port = 0; 
        try{ port = Integer.parseInt(args[0]); }
        catch(Throwable t){ port = DEFAULT_PORT; }
        EchoServer sv = new EchoServer(port);
        try { sv.listen(); } 
        catch (Exception ex) { System.out.println("ERROR - Could not listen for clients!"); }
    }
}
