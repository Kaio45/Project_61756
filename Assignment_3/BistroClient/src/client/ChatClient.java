package client;

import ocsf.client.AbstractClient;
import java.io.IOException;
import javafx.application.Platform; // <--- הוספתי את זה (חשוב לסגירה)

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 */
public class ChatClient extends AbstractClient {
  
    public static OrderFrameController orderController;
    
    /**
     * Constructs an instance of the chat client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     */
    public ChatClient(String host, int port) throws IOException {
        super(host, port); // Call the superclass constructor
        openConnection();  // Open the connection immediately
    }

    /**
     * This method handles all data that comes in from the server.
     *
     * @param msg The message from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        System.out.println("Server says: " + msg);
        
        if (orderController != null) {
            orderController.updateFields(msg);
        }
    }
  
    /**
     * This method handles all data coming from the UI (User Interface).
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(Object message) {
        try {
            sendToServer(message);
        } catch (IOException e) {
            System.out.println("Could not send message to server. Terminating client.");
          System.out.println("123");
            quit();
        }
    }
  
    /**
     * This method terminates the client.
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
