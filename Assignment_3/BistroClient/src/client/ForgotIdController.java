package client;

import common.ActionType;
import common.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for recovering a lost Subscriber ID.
 * @author Group-17
 * @version 1.0
 */
public class ForgotIdController {

    @FXML private TextField userTxt;
    @FXML private TextField phoneTxt;
    @FXML private Label resultLabel;
    
    /** Static instance to allow external access. */
    public static ForgotIdController instance; 

    public void initialize() {
        instance = this;
    }

    /**
     * Sends a recovery request to the server.
     * @param event the button click event
     */
    @FXML
    public void sendRecovery(ActionEvent event) {
        String user = userTxt.getText();
        String phone = phoneTxt.getText();
        
        if(user.isEmpty() || phone.isEmpty()) {
            resultLabel.setText("Fill all fields!");
            return;
        }
        
        // Sending request: "user::phone"
        String data = user + "::" + phone;
        if(ClientUI.chat != null) {
            ClientUI.chat.handleMessageFromClientUI(new Message(ActionType.RECOVER_SUBSCRIBER_ID, data));
            resultLabel.setText("Searching...");
        }
    }
    
    /**
     * Displays the result of the recovery attempt.
     * @param msg the message object from the server
     */
    public void displayResult(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Integer) {
                resultLabel.setText("Your ID is: " + msg);
            } else {
                resultLabel.setText("User not found.");
            }
        });
    }
}