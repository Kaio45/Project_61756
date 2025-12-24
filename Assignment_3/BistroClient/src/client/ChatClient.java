package client;

import ocsf.client.AbstractClient;
import java.io.IOException;
import common.ActionType;
import common.Message;
import javafx.application.Platform;

/**
 * The ChatClient is responsible for handling communication between the client GUI and the server.
 * <p>
 * It extends {@link AbstractClient} and overrides methods to handle incoming messages
 * and connection events. It routes messages to the appropriate GUI controller.
 * </p>
 */
public class ChatClient extends AbstractClient {
  
    /** Static reference to the OrderFrameController to update the main UI. */
    public static OrderFrameController orderController;
    
    /** Static reference to the LoginFrameController to update the login UI. */
    public static LoginFrameController loginController;
    
    /**
     * Constructs an instance of the chat client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     * @throws IOException if an I/O error occurs when opening the connection
     */
    public ChatClient(String host, int port) throws IOException {
        super(host, port); // Call the superclass constructor
        openConnection();  // Open the connection immediately
    }

    /**
     * Handles incoming messages from the server.
     * <p>
     * Checks the {@link ActionType} of the message and forwards it to the
     * relevant controller (Login or Order).
     * </p>
     *
     * @param msg The message received from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        System.out.println("Server says: " + msg);
        
        if (msg instanceof Message) {
            Message message = (Message) msg;
            
            // Route message based on ActionType
            if (message.getAction() == ActionType.LOGIN) {
                if (loginController != null) {
                    loginController.handleLoginResponse(message);
                }
            } else {
                // Default handling for order-related actions
                if (orderController != null) {
                    orderController.updateFields(message);
                }
            }
        }
    }
  
    /**
     * Handles messages coming from the UI to be sent to the server.
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(Object message) {
        try {
            sendToServer(message);
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

    /**
     * Called when the connection to the server is closed.
     */
    @Override
    protected void connectionClosed() {
        System.out.println("Server connection closed. Exiting...");
        Platform.runLater(() -> System.exit(0));
    }

    /**
     * Called when a connection exception occurs.
     *
     * @param exception the exception that occurred
     */
    @Override
    protected void connectionException(Exception exception) {
        System.out.println("Server connection exception. Exiting...");
        Platform.runLater(() -> System.exit(0));
    }
}
