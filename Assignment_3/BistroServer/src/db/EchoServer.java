package db;

import java.io.IOException;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import common.Order;
import common.Message;
import common.ActionType;
import common.User; // <--- Added import for User entity

/**
 * The Class EchoServer.
 * <p>
 * This class extends the {@link AbstractServer} class to handle client-server communication
 * and database operations. It implements the server-side logic of the Bistro application,
 * processing requests sent by clients using the {@link Message} protocol.
 * </p>
 */
public class EchoServer extends AbstractServer {
    
    /** The default port number to listen on. */
    final public static int DEFAULT_PORT = 5555;
    
    /** * The server controller for GUI interactions.
     * This static variable allows the server logic to update the server's user interface (logs).
     */
    public static ServerPortFrameController serverController;

    /**
     * Instantiates a new EchoServer.
     *
     * @param port the port number on which the server will listen for connections
     */
    public EchoServer(int port) {
        super(port);
    }

    /**
     * Handles incoming messages from a client.
     * <p>
     * This method is triggered whenever a message is received from a connected client.
     * It expects the message to be of type {@link Message}. Based on the {@link ActionType}
     * contained in the message, it delegates the request to the appropriate handler
     * (e.g., fetching an order, adding an order, updating an order, or logging in).
     * </p>
     *
     * @param msg    the message received from the client (expected to be a {@link Message} object)
     * @param client the connection instance representing the client that sent the message
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        // Validate that the received object is a valid Message protocol object
        if (msg instanceof Message) {
            Message receivedMsg = (Message) msg;
            
            // Switch based on the action type requested by the client
            switch (receivedMsg.getAction()) {
            
                case LOGIN:
                    // Scenario: Client attempts to log in
                    if (receivedMsg.getContent() instanceof User) {
                        User loginRequest = (User) receivedMsg.getContent();
                        
                        // Check credentials against the database using mysqlConnection
                        User fullUser = mysqlConnection.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
                        
                        try {
                            if (fullUser != null) {
                                if (fullUser.isLoggedIn()) {
                                     // User exists but is already marked as logged in
                                     client.sendToClient(new Message(ActionType.LOGIN, "Already Logged In"));
                                } else {
                                     // Login successful: send the full User object back
                                     client.sendToClient(new Message(ActionType.LOGIN, fullUser));
                                }
                            } else {
                                // User not found or wrong password
                                client.sendToClient(new Message(ActionType.LOGIN, "Wrong username or password"));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case GET_ORDER:
                    // Scenario: Client requests to search for an order by ID
                    if (receivedMsg.getContent() instanceof String) {
                        String idStr = (String) receivedMsg.getContent();
                        try {
                            int id = Integer.parseInt(idStr);
                            
                            // Query the database
                            Order order = mysqlConnection.getOrder(id);
                            
                            if (order != null) {
                                // Order found: send the Order object back to the client
                                client.sendToClient(new Message(ActionType.GET_ORDER, order));
                            } else {
                                // Order not found: send an error message
                                client.sendToClient(new Message(ActionType.GET_ORDER, "Order not found!"));
                            }
                        } catch (NumberFormatException e) {
                            // Handle invalid ID format (non-numeric)
                            try {
                                client.sendToClient(new Message(ActionType.GET_ORDER, "Invalid ID"));
                            } catch (IOException ex) { ex.printStackTrace(); }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case ADD_ORDER:
                    // Scenario: Client sends a new order to be saved
                    if (receivedMsg.getContent() instanceof Order) {
                        Order order = (Order) receivedMsg.getContent();
                        System.out.println("Saving new order...");
                        mysqlConnection.saveOrderToDB(order);
                        try {
                            // Confirm success to the client
                            client.sendToClient(new Message(ActionType.ADD_ORDER, "Order Saved Successfully"));
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                    break;
                    
                case UPDATE_ORDER:
                     // Scenario: Client requests to update an existing order
                    if (receivedMsg.getContent() instanceof Order) {
                        Order order = (Order) receivedMsg.getContent();
                        System.out.println("Updating existing order...");
                        mysqlConnection.updateOrder(order);
                        try {
                            // Confirm success to the client
                            client.sendToClient(new Message(ActionType.UPDATE_ORDER, "Order Updated Successfully"));
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                    break;
                    
                default:
                    System.out.println("Unknown Action: " + receivedMsg.getAction());
                    break;
            }
        } else {
            // Handle unexpected message types (legacy support or errors)
            System.out.println("Received message is not a Message object!");
        }
    }
    
    /**
     * Called when a client disconnects from the server.
     * <p>
     * Logs the disconnection event to the console and updates the server GUI log.
     * </p>
     *
     * @param client the connection instance of the disconnected client
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        String msg = "Client disconnected: " + client.getInetAddress();
        System.out.println(msg);
        
        // Update the Server Log GUI if available
        if (serverController != null) {
            serverController.addToLog(msg);
        }
    }

    /**
     * Called when the server starts listening for connections.
     * <p>
     * Initializes the database connection and updates the server GUI log
     * to indicate that the server is online.
     * </p>
     */
    @Override
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
        try {
            // Attempt to connect to the MySQL database
            mysqlConnection.connectToDB();
            
            if (serverController != null) {
                serverController.addToLog("Server listening on port " + getPort());
                serverController.addToLog("Database connected!");
            }
        } catch (Exception e) {
            System.out.println("DB Connection failed");
            if (serverController != null) {
                serverController.addToLog("Database connection failed!");
            }
        }
    }

    /**
     * Called when the server stops listening for connections.
     * <p>
     * Updates the server GUI log to indicate that the server has stopped.
     * </p>
     */
    @Override
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
        if (serverController != null) {
            serverController.addToLog("Server Stopped.");
        }
    }
    
    /**
     * Called when a new client connects to the server.
     * <p>
     * Logs the new connection details to the console and the server GUI.
     * </p>
     *
     * @param client the connection instance of the newly connected client
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
          String msg = "Client Connected! IP: " + client.getInetAddress();
          System.out.println(msg);
          
          if (serverController != null) {
              serverController.addToLog(msg);
          }
    }
    
    /**
     * The main entry point for the server application.
     * <p>
     * Parses the command line arguments to determine the port number and starts the server.
     * If no port is specified, the default port (5555) is used.
     * </p>
     *
     * @param args command line arguments (optional: port number)
     */
    public static void main(String[] args){
        int port = 0; 

        try{
          port = Integer.parseInt(args[0]); 
        }
        catch(Throwable t){
          port = DEFAULT_PORT; 
        }

        EchoServer sv = new EchoServer(port);

        try {
          sv.listen(); 
        } catch (Exception ex) {
          System.out.println("ERROR - Could not listen for clients!");
        }
    }
}
