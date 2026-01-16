package client;

import common.ActionType;
import common.Message;
import common.Subscriber;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for editing subscriber profile details.
 * Allows subscribers to update their phone number and email address.
 * @author Group-17
 * @version 1.0
 */
public class EditProfileController {

    @FXML private TextField phoneTxt;
    @FXML private TextField emailTxt;
    @FXML private Label msgLabel;

    private int subscriberId;
    private String firstName; 

    /**
     * Initializes the form with the subscriber's current data.
     *
     * @param id the subscriber ID
     * @param name the subscriber's name
     * @param currentPhone current phone number
     * @param currentEmail current email address
     */
    public void initData(int id, String name, String currentPhone, String currentEmail) {
        this.subscriberId = id;
        this.firstName = name;
        phoneTxt.setText(currentPhone);
        emailTxt.setText(currentEmail);
    }

    /**
     * Sends a request to save the updated profile details.
     * @param event the button click event
     */
    @FXML
    public void saveChanges(ActionEvent event) {
        String newPhone = phoneTxt.getText();
        String newEmail = emailTxt.getText();

        if (newPhone.isEmpty() || newEmail.isEmpty()) {
            msgLabel.setText("Fields cannot be empty!");
            return;
        }

        // Create temporary subscriber object with updated details
        Subscriber updateSub = new Subscriber(subscriberId, firstName, "", newPhone, newEmail, "", 0);
        
        // Send request to server
        ClientUI.chat.handleMessageFromClientUI(new Message(ActionType.UPDATE_SUBSCRIBER_DETAILS, updateSub));
        
        msgLabel.setText("Request sent! You can close.");
    }

    /**
     * Closes the edit profile window.
     * @param event the button click event
     */
    @FXML
    public void closeWindow(ActionEvent event) {
        ((Stage) phoneTxt.getScene().getWindow()).close();
    }
}