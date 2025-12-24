package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import java.io.IOException;

// Importing the new common protocol classes
import common.Order;
import common.Message;
import common.ActionType;

/**
 * Controller class for the Order Management GUI.
 * <p>
 * This class handles the interaction between the user interface and the client logic.
 * It is responsible for sending requests to the server (using the {@link Message} protocol)
 * and updating the view based on the server's responses.
 * </p>
 */
public class OrderFrameController {

    /** The text field for entering the order ID to search. */
    @FXML
    private TextField idTextField;
    
    /** The search button to trigger the order retrieval. */
    @FXML
    private Button searchBtn;
    
    /** The text field for displaying and editing the order date. */
    @FXML
    private TextField dateTextField;
    
    /** The text field for displaying and editing the number of guests. */
    @FXML
    private TextField guestsTextField;
    
    /** The text field for displaying the confirmation code (read-only in this view). */
    @FXML
    private TextField codeTextField;
    
    /** The text field for displaying the subscriber ID (read-only in this view). */
    @FXML
    private TextField subIdTextField;
    
    /** The text field for displaying the date when the order was placed. */
    @FXML
    private TextField placeDateTextField;

    /** The update button to save changes to the order. */
    @FXML
    private Button updateBtn;
    
    /** The label for displaying status messages or errors to the user. */
    @FXML
    private Label messageLabel;

    /** The network client used for communication with the server. */
    private ChatClient client;
    
    /** The currently loaded order object. */
    private Order currentOrder;

    /**
     * Sets the network client instance for this controller.
     * This allows the controller to send messages via the established connection.
     *
     * @param client the active ChatClient instance
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /**
     * Handles the "Search" button click event.
     * <p>
     * Validates the input ID, creates a {@link Message} with {@link ActionType#GET_ORDER},
     * and sends it to the server to fetch the order details.
     * </p>
     *
     * @param event the action event triggered by the button
     */
    @FXML
    public void searchOrder(ActionEvent event) {
        String id = idTextField.getText();
        
        // Basic validation
        if (id.isEmpty()) {
            if (messageLabel != null) messageLabel.setText("Please enter Order ID");
            return;
        }
        
        try {
            // Create a request message encapsulated with ActionType
            Message msg = new Message(ActionType.GET_ORDER, id);
            client.sendToServer(msg); 
            
            if (messageLabel != null) messageLabel.setText("Searching...");
        } catch (Exception e) {
            if (messageLabel != null) messageLabel.setText("Connection Error");
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Update" button click event.
     * <p>
     * Updates the local {@link Order} object with values from the text fields,
     * wraps it in a {@link Message} with {@link ActionType#UPDATE_ORDER},
     * and sends it to the server for persistence.
     * </p>
     *
     * @param event the action event triggered by the button
     */
    @FXML
    public void updateOrder(ActionEvent event) {
        if (currentOrder == null) {
            if (messageLabel != null) messageLabel.setText("No order loaded");
            return;
        }

        // Update local object properties
        currentOrder.set_order_date(dateTextField.getText());
        try {
             currentOrder.set_number_of_guests(Integer.parseInt(guestsTextField.getText()));
        } catch (NumberFormatException e) {
             if (messageLabel != null) messageLabel.setText("Invalid guests number");
             return;
        }
        
        try {
            // Send the updated object to the server
            Message msg = new Message(ActionType.UPDATE_ORDER, currentOrder);
            client.sendToServer(msg);
            
            if (messageLabel != null) messageLabel.setText("Update request sent...");
        } catch (Exception e) {
            if (messageLabel != null) messageLabel.setText("Error sending update");
        }
    }

    /**
     * Updates the UI fields with data received from the server.
     * <p>
     * This method is called by {@link ChatClient} when a message arrives.
     * It runs on the JavaFX Application Thread using {@link Platform#runLater}
     * to ensure thread safety when modifying GUI components.
     * </p>
     *
     * @param msg the message received from the server (expected to be of type {@link Message})
     */
    public void updateFields(Object msg) {
        Platform.runLater(() -> {
            try {
                // Check if the received object is a valid Message protocol object
                if (msg instanceof Message) {
                    Message receivedMsg = (Message) msg;
                    
                    // Case 1: The message contains an Order object (response to GET_ORDER)
                    if (receivedMsg.getContent() instanceof Order) {
                        currentOrder = (Order) receivedMsg.getContent();
                        
                        // Populate UI fields
                        if (dateTextField != null) dateTextField.setText(currentOrder.get_order_date());
                        if (guestsTextField != null) guestsTextField.setText(String.valueOf(currentOrder.get_number_of_guests()));
                        if (codeTextField != null) codeTextField.setText(String.valueOf(currentOrder.get_confirmation_code()));
                        if (subIdTextField != null) subIdTextField.setText(String.valueOf(currentOrder.get_subscriber_id()));
                        if (placeDateTextField != null) placeDateTextField.setText(currentOrder.get_date_of_placing_order());
                        
                        if (messageLabel != null) messageLabel.setText("Order Loaded!");
                        
                    } 
                    // Case 2: The message contains a String (status message like "Action successful")
                    else if (receivedMsg.getContent() instanceof String) {
                        if (messageLabel != null) messageLabel.setText((String) receivedMsg.getContent());
                    }
                } 
                else {
                    // Fallback for legacy string messages or unexpected objects
                    System.out.println("Received unknown object type: " + msg.getClass().getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles the "Disconnect" button click event.
     * <p>
     * Closes the connection to the server gracefully and terminates the client application.
     * </p>
     *
     * @param event the action event triggered by the button
     */
    @FXML
    public void disconnectBtn(ActionEvent event) {
        try {
            if (ClientUI.chat != null && ClientUI.chat.isConnected()) {
                ClientUI.chat.closeConnection();
                System.out.println("Disconnected from server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Closing application...");
        System.exit(0); 
    }
}
