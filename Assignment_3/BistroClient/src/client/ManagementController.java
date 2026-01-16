package client;

import common.ActionType;
import common.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Restaurant Management screen.
 * Allows adding/removing tables and updating opening hours.
 * @author Group-17
 * @version 1.0
 */
public class ManagementController {

    @FXML private TextField tableIdTxt;
    @FXML private TextField seatsTxt;
    @FXML private TextField dayDateTxt;
    @FXML private TextField hoursTxt;
    @FXML private Label msgLabel;

    /**
     * Sends a request to add a new table to the restaurant.
     * @param event the button click event
     */
    @FXML
    public void addTable(ActionEvent event) {
        try {
            int id = Integer.parseInt(tableIdTxt.getText());
            int seats = Integer.parseInt(seatsTxt.getText());
            // Format: "ID,SEATS"
            String data = id + "," + seats;
            ClientUI.chat.handleMessageFromClientUI(new Message(ActionType.ADD_TABLE, data));
            msgLabel.setText("Request sent...");
        } catch (NumberFormatException e) {
            msgLabel.setText("Error: ID and Seats must be numbers");
        }
    }

    /**
     * Sends a request to delete an existing table.
     * @param event the button click event
     */
    @FXML
    public void deleteTable(ActionEvent event) {
        try {
            int id = Integer.parseInt(tableIdTxt.getText());
            ClientUI.chat.handleMessageFromClientUI(new Message(ActionType.DELETE_TABLE, id));
            msgLabel.setText("Request sent...");
        } catch (NumberFormatException e) {
            msgLabel.setText("Error: Invalid Table ID");
        }
    }

    /**
     * Sends a request to update the opening hours for a specific day or date.
     * @param event the button click event
     */
    @FXML
    public void updateHours(ActionEvent event) {
        String dayDate = dayDateTxt.getText();
        String hours = hoursTxt.getText();
        
        if (dayDate.isEmpty() || hours.isEmpty()) {
            msgLabel.setText("Please fill Day/Date and Hours");
            return;
        }
        
        // Format: "Date::Hours"
        String data = dayDate + "::" + hours;
        ClientUI.chat.handleMessageFromClientUI(new Message(ActionType.UPDATE_OPENING_HOURS, data));
        msgLabel.setText("Updating hours...");
    }

    /**
     * Closes the management window.
     * @param event the button click event
     */
    @FXML
    public void close(ActionEvent event) {
        ((Stage) msgLabel.getScene().getWindow()).close();
    }
    
    /**
     * Sends a request to update the seat capacity of an existing table.
     * @param event the button click event
     */
    @FXML
    public void updateTable(ActionEvent event) {
        try {
            String idStr = tableIdTxt.getText();
            String seatsStr = seatsTxt.getText();

            if (idStr.isEmpty() || seatsStr.isEmpty()) {
                msgLabel.setText("Please enter Table ID AND new Seats count");
                return;
            }

            int id = Integer.parseInt(idStr);
            int seats = Integer.parseInt(seatsStr);
            
            // Format: "ID,SEATS"
            String data = id + "," + seats;
            
            ClientUI.chat.handleMessageFromClientUI(new Message(ActionType.UPDATE_TABLE, data));
            
            msgLabel.setText("Update request sent...");
            
        } catch (NumberFormatException e) {
            msgLabel.setText("Error: ID and Seats must be numbers");
        }
    }
}