package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import common.Order;
import javafx.application.Platform;

/**
 * Controller class for the Order management GUI.
 * Handles the interaction between the user interface and the client logic
 * for searching and updating order details.
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
    
    /** The text field for displaying the confirmation code. */
    @FXML
    private TextField codeTextField;
    
    /** The text field for displaying the subscriber ID. */
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
     * Sets the network client instance.
     *
     * @param client the new chat client instance
     */
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /**
     * Handles the search button click event.
     * Validates the input and sends a request to the server to fetch an order by ID.
     *
     * @param event the action event triggered by the button
     */
    @FXML
    public void searchOrder(ActionEvent event) {
        String id = idTextField.getText();
        if (id.isEmpty()) {
            if (messageLabel != null) messageLabel.setText("Please enter Order ID");
            return;
        }
        
        try {
            client.sendToServer("GET " + id); 
            if (messageLabel != null) messageLabel.setText("Searching...");
        } catch (Exception e) {
            if (messageLabel != null) messageLabel.setText("Connection Error");
        }
    }

    /**
     * Handles the update button click event.
     * Updates the current order object with values from the text fields 
     * and sends the updated object to the server.
     *
     * @param event the action event triggered by the button
     */
    @FXML
    public void updateOrder(ActionEvent event) {
        if (currentOrder == null) return;

        currentOrder.set_order_date(dateTextField.getText());
        try {
             currentOrder.set_number_of_guests(Integer.parseInt(guestsTextField.getText()));
        } catch (NumberFormatException e) {
             if (messageLabel != null) messageLabel.setText("Invalid guests number");
             return;
        }
        
        try {
            client.sendToServer(currentOrder);
            if (messageLabel != null) messageLabel.setText("Update request sent...");
        } catch (Exception e) {
            if (messageLabel != null) messageLabel.setText("Error sending update");
        }
    }

    /**
     * Updates the UI fields with data received from the server.
     * This method runs on the JavaFX Application Thread to ensure thread safety.
     *
     * @param msg the message received from the server (either an Order object or a String)
     */
    public void updateFields(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Order) {
                currentOrder = (Order) msg;
                if (dateTextField != null) dateTextField.setText(currentOrder.get_order_date());
                if (guestsTextField != null) guestsTextField.setText(String.valueOf(currentOrder.get_number_of_guests()));
                
                // Update additional fields if they are initialized in the scene
                if (codeTextField != null) codeTextField.setText(String.valueOf(currentOrder.get_confirmation_code()));
                if (subIdTextField != null) subIdTextField.setText(String.valueOf(currentOrder.get_subscriber_id()));
                if (placeDateTextField != null) placeDateTextField.setText(currentOrder.get_date_of_placing_order());
                
                if (messageLabel != null) messageLabel.setText("Order Loaded!");
            } else if (msg instanceof String) {
                if (messageLabel != null) messageLabel.setText((String)msg);
            }
        });
    }
}