package client;

import ocsf.client.AbstractClient;
import java.io.IOException;
import common.ActionType;
import common.Message;
import javafx.application.Platform;

/**
 * The ChatClient is responsible for handling communication between the client GUI and the server.
 * It routes incoming messages to the appropriate GUI controller (Login, Order, Report, etc.).
 * @author Group-17
 * @version 1.0
 */
public class ChatClient extends AbstractClient {
  
    /** Static reference to the OrderFrameController to update the main UI. */
    public static OrderFrameController orderController;
    
    /** Static reference to the LoginFrameController to update the login UI. */
    public static LoginFrameController loginController;
    
    /** Static reference to the RegisterController. */
    public static RegisterController registerController;
    
    /** Static reference to the ReportController. */
    public static ReportController reportController; 
    
    /** Indicates if we are waiting for a response (optional usage). */
    public static boolean awaitResponse = false;

    /**
     * Constructs an instance of the chat client.
     *
     * @param host the server to connect to
     * @param port the port number to connect on
     * @throws IOException if an I/O error occurs during connection
     */
    public ChatClient(String host, int port) throws IOException {
        super(host, port); 
        openConnection();  
    }

    /**
     * Handles a message received from the server.
     * Routes the message to the correct controller based on the ActionType.
     *
     * @param msg the message received from the server
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        System.out.println("Server says: " + msg);
        awaitResponse = false;
        
        if (msg instanceof Message) {
            Message message = (Message) msg;
            ActionType type = message.getAction();

            // 1. Login and Identification
            if (type == ActionType.LOGIN || type == ActionType.IDENTIFY_SUBSCRIBER || type == ActionType.IDENTIFY_BY_CODE) {
                if (loginController != null) loginController.handleResponse(message);
            }
            
            // 2. Subscriber Registration
            else if (type == ActionType.REGISTER_SUBSCRIBER) {
                if (registerController != null) {
                    registerController.handleResponse(message);
                }
            }
            
            // 3. Reports
            else if (type == ActionType.GET_REPORT) {
                if (reportController != null) {
                    reportController.updateReport((String) message.getContent());
                }
            }
            
            // 4. Recover Subscriber ID
            else if (type == ActionType.RECOVER_SUBSCRIBER_ID) {
                if (ForgotIdController.instance != null) {
                    ForgotIdController.instance.displayResult(message.getContent());
                }
            }
            
            // 5. Default (Order Updates)
            else {
                if (orderController != null) orderController.updateFields(message);
            }
        }
    }
  
    /**
     * Handles messages coming from the client UI to be sent to the server.
     *
     * @param message the message object to send
     */
    public void handleMessageFromClientUI(Object message) {
        try {
            sendToServer(message);
            awaitResponse = true;
        } catch (IOException e) {
            System.out.println("Could not send message to server. Terminating client.");
            quit();
        }
    }
  
    /**
     * Terminates the client connection and exits the application.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {}
        System.exit(0);
    }

    @Override
    protected void connectionClosed() {
        System.out.println("Server connection closed. Exiting...");
        Platform.runLater(() -> System.exit(0));
    }

    @Override
    protected void connectionException(Exception exception) {
        System.out.println("Server connection exception. Exiting...");
        Platform.runLater(() -> System.exit(0));
    }
}
