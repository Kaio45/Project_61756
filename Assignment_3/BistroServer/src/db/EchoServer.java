package db;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

import common.Order;
import common.Message;
import common.ActionType;
import common.User;
import common.Subscriber;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The main server class for the Bistro application.
 * Extends the AbstractServer class to handle client connections and messages.
 * This class serves as the central controller for processing client requests,
 * interacting with the database, and managing connected users.
 * * @author Group-17
 * @version 1.0
 */
public class EchoServer extends AbstractServer {

    /** The default port to listen on. */
    final public static int DEFAULT_PORT = 5555;
    
    /** Reference to the server GUI controller for logging purposes. */
    public static ServerPortFrameController serverController;

    /** Map of connected clients to their usernames. */
    private Map<ConnectionToClient, String> connectedUsers = new HashMap<>();

    /**
     * Constructs an instance of the echo server.
     *
     * @param port the port number to connect on
     */
    public EchoServer(int port) {
        super(port);
    }

    /**
     * Handles any message received from the client.
     * Routes the message to the appropriate handler based on the ActionType.
     *
     * @param msg the message received
     * @param client the connection from which the message originated
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (msg instanceof Message) {
            Message receivedMsg = (Message) msg;

            switch (receivedMsg.getAction()) {
            case LOGIN:
                handleLogin(receivedMsg, client);
                break;

            case GET_ORDER:
                Object content = receivedMsg.getContent();

                // Case 1: Search for a specific order (Staff searching by Order ID)
                if (content instanceof String) {
                    try {
                        int orderId = Integer.parseInt((String) content);
                        Order order = mysqlConnection.getOrder(orderId);

                        if (order != null) {
                            client.sendToClient(new Message(ActionType.GET_ORDER, order));
                        } else {
                            client.sendToClient(new Message(ActionType.GET_ORDER, "Order not found."));
                        }
                    } catch (NumberFormatException e) {
                        // Handle non-numeric input silently
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Case 2: Request history (Subscriber clicking My Order)
                else if (content instanceof Integer) {
                    int subId = (Integer) content;
                    ArrayList<Order> history = mysqlConnection.getSubscriberOrders(subId);

                    try {
                        if (!history.isEmpty()) {
                            client.sendToClient(new Message(ActionType.GET_ORDER, history));
                        } else {
                            client.sendToClient(new Message(ActionType.GET_ORDER, "No orders found in history."));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case ADD_TABLE:
                String[] tableData = ((String) receivedMsg.getContent()).split(",");
                int tId = Integer.parseInt(tableData[0]);
                int tSeats = Integer.parseInt(tableData[1]);
                
                String resultMsg = mysqlConnection.addTable(tId, tSeats);
                
                try {
                    client.sendToClient(new Message(ActionType.UPDATE_ORDER, resultMsg));
                } catch (IOException e) {
                }
                break;
                
            case DELETE_TABLE:
                int delTId = (int) receivedMsg.getContent();
                boolean delSuccess = mysqlConnection.deleteTable(delTId);
                try {
                    client.sendToClient(new Message(ActionType.UPDATE_ORDER,
                            delSuccess ? "Table Deleted!" : "Error Deleting Table"));
                } catch (IOException e) {
                }
                break;
                
            case UPDATE_TABLE:
                try {
                    String data = (String) receivedMsg.getContent();
                    String[] parts = data.split(",");
                    int id = Integer.parseInt(parts[0]);
                    int seats = Integer.parseInt(parts[1]);
                    
                    boolean success = mysqlConnection.updateTableSeats(id, seats);
                    if (success) {
                        client.sendToClient(new Message(ActionType.UPDATE_TABLE, "Table " + id + " updated successfully!"));
                    } else {
                        client.sendToClient(new Message(ActionType.UPDATE_TABLE, "Failed to update table (ID not found)."));
                    }
                } catch (Exception e) {
                    try { client.sendToClient(new Message(ActionType.UPDATE_TABLE, "Error updating table.")); } catch (Exception ex) {}
                }
                break;
                
            case GET_HISTORY_BY_USER_ID:
                try {
                    int targetClientId = (Integer) receivedMsg.getContent();
                    java.util.ArrayList<common.Order> history = mysqlConnection.getOrdersByClientId(targetClientId);
                    client.sendToClient(new common.Message(common.ActionType.GET_HISTORY_BY_USER_ID, history));
                } catch (java.io.IOException e) {
                    System.out.println("Error sending history: " + e.getMessage());
                }
                break;
                
            case UPDATE_OPENING_HOURS:
                String[] hoursData = ((String) receivedMsg.getContent()).split("::");
                // hoursData[0] = day or date, hoursData[1] = hours range
                mysqlConnection.updateOpeningHour(hoursData[0], hoursData[1]);
                try {
                    client.sendToClient(new Message(ActionType.UPDATE_ORDER, "Hours Updated for " + hoursData[0]));
                } catch (IOException e) {
                }
                break;

            case ADD_ORDER:
                Order newOrder = (Order) receivedMsg.getContent();
                handleAddOrderSmart(newOrder, client);
                break;
                
            case GET_ALL_ORDERS:
                ArrayList<Order> allOrders = mysqlConnection.getAllOrders();
                try {
                    client.sendToClient(new Message(ActionType.GET_ORDER, allOrders)); 
                } catch (IOException e) {
                }
                break;
                
            case UPDATE_ORDER:
                Order orderToUpdate = (Order) receivedMsg.getContent();
                boolean updateSuccess = mysqlConnection.updateOrder(orderToUpdate);
                try {
                    if (updateSuccess)
                        client.sendToClient(new Message(ActionType.UPDATE_ORDER, "Order Updated Successfully"));
                    else
                        client.sendToClient(new Message(ActionType.UPDATE_ORDER, "Update Failed"));
                } catch (IOException e) {
                }
                break;
                
            case UPDATE_SUBSCRIBER_DETAILS:
                Subscriber subToUpdate = (Subscriber) receivedMsg.getContent();
                boolean subUpdSuccess = mysqlConnection.updateSubscriberDetails(subToUpdate.getId(),
                        subToUpdate.getPhoneNumber(), subToUpdate.getEmail());
                try {
                    client.sendToClient(
                            new Message(ActionType.UPDATE_ORDER, subUpdSuccess ? "Details Updated" : "Update Failed"));
                } catch (IOException e) {
                }
                break;

            case DELETE_ORDER:
                handleDeleteOrder(receivedMsg, client);
                break;

            case IDENTIFY_SUBSCRIBER:
                Subscriber subReq = (Subscriber) receivedMsg.getContent();
                Subscriber foundSub = mysqlConnection.loginSubscriberStrict(subReq.getId(), subReq.getUsername());
                try {
                    if (foundSub != null) {
                        client.sendToClient(new Message(ActionType.IDENTIFY_SUBSCRIBER, foundSub));
                    } else {
                        client.sendToClient(new Message(ActionType.IDENTIFY_SUBSCRIBER, "Wrong ID or Username"));
                    }
                } catch (IOException e) {
                }
                break;

            case REGISTER_SUBSCRIBER:
                common.Subscriber newSub = (common.Subscriber) receivedMsg.getContent();
                int newSubId = mysqlConnection.addSubscriber(newSub);
                try {
                    if (newSubId != -1) {
                        client.sendToClient(new Message(ActionType.REGISTER_SUBSCRIBER, "Success, Subscriber ID: " + newSubId));
                    } else {
                        client.sendToClient(
                                new Message(ActionType.REGISTER_SUBSCRIBER, "Error: Database Save Failed."));
                    }
                } catch (IOException e) {
                }
                break;

            case IDENTIFY_BY_CODE:
                int code = (int) receivedMsg.getContent();
                Order foundOrder = mysqlConnection.getOrderByConfirmationCode(code);
                try {
                    if (foundOrder != null) {
                        client.sendToClient(new Message(ActionType.IDENTIFY_BY_CODE, foundOrder));
                    } else {
                        client.sendToClient(new Message(ActionType.IDENTIFY_BY_CODE, "Invalid Confirmation Code"));
                    }
                } catch (IOException e) {
                }
                break;

            case GET_SUBSCRIBER_LAST_ORDER:
                int subId = (int) receivedMsg.getContent();
                Order lastOrder = mysqlConnection.getLastOrderForSubscriber(subId);
                try {
                    if (lastOrder != null) {
                        client.sendToClient(new Message(ActionType.GET_ORDER, lastOrder));
                    } else {
                        client.sendToClient(new Message(ActionType.GET_ORDER, "No active orders found for you."));
                    }
                } catch (IOException e) {
                }
                break;
                
            case GET_APPROVED_ORDERS_FOR_TODAY:
                try {
                    ArrayList<String> list = mysqlConnection.getApprovedOrdersForToday();
                    client.sendToClient(new common.Message(common.ActionType.GET_APPROVED_ORDERS_FOR_TODAY, list));
                } catch (Exception e) { e.printStackTrace(); }
                break;
                
            case MARK_ARRIVED:
                try {
                    int id = (Integer) receivedMsg.getContent();
                    int assignedTable = mysqlConnection.markOrderAsArrived(id);
                    
                    if (assignedTable != -1) {
                        String msgq = "Client Arrived marked successfully. Table: " + assignedTable;
                        client.sendToClient(new Message(ActionType.MARK_ARRIVED, msgq));
                    } else {
                        client.sendToClient(new Message(ActionType.MARK_ARRIVED, "Failed to mark arrived."));
                    }
                } catch (Exception e) { 
                    e.printStackTrace(); 
                }
                break;
                
            case MARK_FINISHED:
                try {
                    int orderId = (Integer) receivedMsg.getContent();
                    
                    // 1. Check client type (for discount calculation)
                    int subIdt = mysqlConnection.getSubscriberIdByOrder(orderId);
                    
                    // 2. Mark as finished in DB and set leave time
                    mysqlConnection.markOrderAsFinished(orderId);
                    
                    // 3. Prepare simulation messages
                    String simulationMsg = ">>> SIMULATION [SMS]: Order #" + orderId + " Bill sent via SMS";
                    String clientResponse = "Order #" + orderId + " Finished Successfully.";
                    
                    // Apply discount logic if subscriber
                    if (subIdt > 1 && subIdt != 999) {
                        simulationMsg += " [*** 10% DISCOUNT APPLIED ***]";
                        clientResponse += " (Includes 10% Subscriber Discount!)";
                    } else {
                        simulationMsg += " [Standard Bill]";
                    }
                    
                    System.out.println(simulationMsg);
                    client.sendToClient(new common.Message(common.ActionType.MARK_FINISHED, clientResponse));
                    
                } catch (Exception e) {
                    System.out.println("SERVER ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
                
            case GET_DAILY_REPORT:
                try {
                    String dailyRep = mysqlConnection.getDailyReport();
                    client.sendToClient(new Message(ActionType.GET_DAILY_REPORT, dailyRep));
                } catch (IOException e) {
                    System.out.println("Error sending daily report: " + e.getMessage());
                }
                break;
                
            case GET_SUBSCRIBER_DETAILS:
                try {
                    int idToSearch = (Integer) receivedMsg.getContent();
                    String details = mysqlConnection.getSubscriberDetails(idToSearch);
                    
                    if (details != null) {
                        client.sendToClient(new common.Message(common.ActionType.GET_SUBSCRIBER_DETAILS, details));
                    } else {
                        client.sendToClient(new common.Message(common.ActionType.GET_SUBSCRIBER_DETAILS, "Client not found."));
                    }
                } catch (java.io.IOException e) {
                    System.out.println("Error sending subscriber details to client.");
                    e.printStackTrace();
                }
                break;
                
            case GET_WAITING_LIST:
                try {
                    String waitingRep = mysqlConnection.getWaitingListReport();
                    client.sendToClient(new Message(ActionType.GET_WAITING_LIST, waitingRep));
                } catch (IOException e) {
                    System.out.println("Error sending waiting list: " + e.getMessage());
                }
                break;
                
            case GET_REPORT:
                try {
                    String reportData = mysqlConnection.generateMonthlyReport();
                    client.sendToClient(new Message(ActionType.GET_REPORT, reportData));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
                
            case RECOVER_SUBSCRIBER_ID:
                String[] recoveryData = ((String) receivedMsg.getContent()).split("::");
                int recoveredId = mysqlConnection.recoverSubscriberId(recoveryData[0], recoveryData[1]);
                try {
                    if (recoveredId != -1)
                        client.sendToClient(new Message(ActionType.RECOVER_SUBSCRIBER_ID, recoveredId));
                    else
                        client.sendToClient(new Message(ActionType.RECOVER_SUBSCRIBER_ID, "Details not match"));
                } catch (IOException e) {
                }
                break;

            case GET_ALL_TABLES:
                try {
                    client.sendToClient(new Message(ActionType.GET_ALL_TABLES, mysqlConnection.getAllTables()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /**
     * Handles the processing of a new order with smart validation logic.
     * Checks opening hours, table availability, and time constraints.
     *
     * @param order the order object to be processed
     * @param client the client connection
     */
    private void handleAddOrderSmart(common.Order order, ConnectionToClient client) {
        try {
            // Normalize time format
            String timeStr = order.get_order_time();
            if (timeStr.length() == 4) timeStr = "0" + timeStr;
            String timeForCheck = (timeStr.length() == 5) ? timeStr + ":00" : timeStr;

            System.out.println("SERVER: Checking Opening Hours for: " + order.get_order_date() + " " + timeForCheck);

            // Check if restaurant is physically open
            if (!mysqlConnection.isRestaurantOpen(order.get_order_date(), timeForCheck)) {
                System.out.println("SERVER: RESTAURANT CLOSED! Sending refusal message.");
                client.sendToClient(new common.Message(common.ActionType.ADD_ORDER, 
                        "The restaurant is closed at this time."));
                return; 
            }

            java.time.LocalDateTime orderDateTime = java.time.LocalDateTime.parse(order.get_order_date() + "T" + timeStr);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();

            // Constraint: Orders cannot be made more than 1 month in advance
            if (orderDateTime.isAfter(now.plusMonths(1))) {
                client.sendToClient(new common.Message(common.ActionType.ADD_ORDER,
                        "Error: Orders can be made up to 1 month in advance."));
                return;
            }

            // Check table availability
            boolean isAvailable = false;
            if (!order.get_status().equals("WAITING")) {
                isAvailable = mysqlConnection.checkAvailabilitySmart(order.get_order_date(), order.get_order_time(),
                        order.get_number_of_guests());
                
                if (!isAvailable) {
                    // Suggest alternative times if full
                    String suggestion = mysqlConnection.checkAlternativeTimes(
                            order.get_order_date(), order.get_order_time(), order.get_number_of_guests());
                    client.sendToClient(new common.Message(common.ActionType.ADD_ORDER, suggestion));
                    return;
                }
            } else {
                isAvailable = true;
            }

            // Save order if available
            if (isAvailable && !order.get_status().equals("WAITING")) {
                order.set_status("APPROVED");
                order.set_table_id(-1);
            }

            int orderNum = mysqlConnection.saveOrderToDB(order);
            if (orderNum != -1) {
                order.set_order_number(orderNum);
                String msg = "Order Created Successfully! #" + orderNum;
                client.sendToClient(new common.Message(common.ActionType.ADD_ORDER, order));
                client.sendToClient(new common.Message(common.ActionType.ADD_ORDER, msg));
            } else {
                client.sendToClient(new common.Message(common.ActionType.ADD_ORDER, "Error: Database Save Failed."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient(new common.Message(common.ActionType.ADD_ORDER, "Error: processing order."));
            } catch (Exception ex) {}
        }
    }

    /**
     * Handles the login process for a user (staff/manager).
     *
     * @param msg the login message containing user credentials
     * @param client the client connection
     */
    private void handleLogin(Message msg, ConnectionToClient client) {
        User u = (User) msg.getContent();
        User full = mysqlConnection.loginUser(u.getUsername(), u.getPassword());
        try {
            if (full != null) {
                if (full.isLoggedIn()) {
                    client.sendToClient(new Message(ActionType.LOGIN, "Already Logged In"));
                } else {
                    connectedUsers.put(client, full.getUsername());
                    client.sendToClient(new Message(ActionType.LOGIN, full));
                }
            } else {
                client.sendToClient(new Message(ActionType.LOGIN, "Wrong username or password"));
            }
        } catch (IOException e) {
        }
    }

    /**
     * Handles the update of an existing order.
     *
     * @param msg the update message
     * @param client the client connection
     */
    private void handleUpdateOrder(Message msg, ConnectionToClient client) {
        Order order = (Order) msg.getContent();
        boolean isUpdated = mysqlConnection.updateOrder(order);
        try {
            if (isUpdated)
                client.sendToClient(new Message(ActionType.UPDATE_ORDER, "Order Updated Successfully"));
            else
                client.sendToClient(new Message(ActionType.UPDATE_ORDER, "Error: Order ID not found in DB"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the deletion (cancellation) of an order.
     * Also checks the waiting list for potential promotions.
     *
     * @param msg the delete message
     * @param client the client connection
     */
    private void handleDeleteOrder(Message msg, ConnectionToClient client) {
        int orderId = (int) msg.getContent();
        Order orderToDelete = mysqlConnection.getOrder(orderId);
        String dateToCheck = (orderToDelete != null) ? orderToDelete.get_order_date() : null;
        boolean isDeleted = mysqlConnection.deleteOrder(orderId);

        try {
            if (isDeleted) {
                client.sendToClient(new Message(ActionType.DELETE_ORDER, "Order Deleted Successfully"));
                if (dateToCheck != null) {
                    System.out.println("Spot opened on " + dateToCheck + "! Checking waiting list...");
                    String promotions = mysqlConnection.checkWaitingList(dateToCheck);
                    if (!promotions.isEmpty()) {
                        System.out.println("Promotions made:\n" + promotions);
                    }
                }
            } else {
                client.sendToClient(new Message(ActionType.DELETE_ORDER, "Error: Could not delete order."));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Identifies a subscriber by ID.
     *
     * @param msg the identification message
     * @param client the client connection
     */
    private void handleIdentifySubscriber(Message msg, ConnectionToClient client) {
        int subId = (int) msg.getContent();
        common.Subscriber sub = mysqlConnection.getSubscriber(subId);
        try {
            if (sub != null) {
                client.sendToClient(new Message(ActionType.IDENTIFY_SUBSCRIBER, sub));
            } else {
                client.sendToClient(new Message(ActionType.IDENTIFY_SUBSCRIBER, "Subscriber not found"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles client disconnection events.
     * Logs the disconnection and updates the user's login status in the database.
     *
     * @param client the disconnected client
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        String msg = "Client disconnected: " + client.getInetAddress();
        System.out.println(msg);
        if (serverController != null)
            serverController.addToLog(msg);
        String username = connectedUsers.get(client);
        if (username != null) {
            System.out.println("Logging out user: " + username);
            mysqlConnection.updateUserLogout(username);
            connectedUsers.remove(client);
        }
    }

    /**
     * Invoked when the server successfully starts listening for connections.
     * Initializes database connection and background services.
     */
    @Override
    protected void serverStarted() {
        if (serverController != null) {
            serverController.addToLog("Server is starting...");
            serverController.addToLog("Listening on port " + getPort());
        }
        try {
            mysqlConnection.connectToDB();
            if (serverController != null)
                serverController.addToLog("Database connected successfully!");
            startAutoCancelService();
        } catch (Exception e) {
            if (serverController != null)
                serverController.addToLog("DB Connection Failed!");
        }
    }

    /**
     * Starts a background service to automatically manage orders.
     * Performs two main tasks every minute:
     * 1. Cancels 'No-Show' orders (15 min delay).
     * 2. Auto-closes active orders after 2 hours.
     */
    private void startAutoCancelService() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable cancelTask = () -> {
            try {
                // Task 1: Cancel No-Shows
                int cancelledCount = mysqlConnection.cancelNoShows();
                if (cancelledCount > 0) {
                    System.out.println("[AUTO-CANCEL] Cancelled " + cancelledCount + " orders due to 15 min delay.");
                    String today = java.time.LocalDate.now().toString();
                    String promotions = mysqlConnection.checkWaitingList(today);
                    if (!promotions.isEmpty()) {
                        System.out.println("[AUTO-CANCEL] Waiting List Update:\n" + promotions);
                    }
                }

                // Task 2: Auto-close after 2 hours
                ArrayList<String> timeLimitList = mysqlConnection.checkTimeLimit();
                
                for (String info : timeLimitList) {
                    try {
                        String idStr = info.replace("Order #", "").trim();
                        int orderId = Integer.parseInt(idStr);
                        
                        int subId = mysqlConnection.getSubscriberIdByOrder(orderId);
                        
                        String simulationMsg = ">>> SIMULATION [SMS]: " + info + " Bill sent via SMS";
                        
                        if (subId > 0 && subId != 999) {
                             simulationMsg += " [Includes 10% Subscriber Discount!]";
                        } else {
                             simulationMsg += " [Standard Bill]";
                        }
                        
                        System.out.println(simulationMsg);
                        
                        if (serverController != null) {
                             serverController.addToLog("Auto-Closed: " + info + " (2 Hours Limit)");
                        }
                        
                    } catch (NumberFormatException e) {
                        System.out.println(">>> SIMULATION [SMS]: " + info + " Bill sent via SMS");
                    }
                }

            } catch (Exception e) {
                System.out.println("Auto-Service Error: " + e.getMessage());
            }
        };
        // Schedule to run every 60 seconds
        scheduler.scheduleAtFixedRate(cancelTask, 0, 60, TimeUnit.SECONDS);
    }
    
    /**
     * Invoked when the server stops listening for connections.
     */
    @Override
    protected void serverStopped() {
    }

    /**
     * Invoked when a client connects to the server.
     *
     * @param client the connected client
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        if (serverController != null)
            serverController.addToLog("Client connected: " + client.getInetAddress());
    }

    /**
     * The entry point for running the server as a console application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        EchoServer sv = new EchoServer(DEFAULT_PORT);
        try {
            sv.listen();
        } catch (Exception ex) {
        }
    }
}
