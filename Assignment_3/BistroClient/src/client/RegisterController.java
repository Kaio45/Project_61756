package client;

import common.ActionType;
import common.Message;
import common.Subscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller for the New Subscriber Registration screen.
 * Handles input validation, sending registration requests, and displaying the generated QR code.
 * @author Group-17
 * @version 1.0
 */
public class RegisterController {
    
    @FXML private TextField userTxt;
    @FXML private TextField nameTxt;
    @FXML private TextField surnameTxt;
    @FXML private TextField phoneTxt;
    @FXML private TextField emailTxt;
    @FXML private Label statusLbl;
    @FXML private Button regBtn;
    @FXML private Button closeBtn; 

    /**
     * Validates input fields and sends a registration request to the server.
     */
    @FXML
    public void register() {
        String username = userTxt.getText().trim();
        String name = nameTxt.getText().trim();
        String surname = surnameTxt.getText().trim();
        String phone = phoneTxt.getText().trim();
        String email = emailTxt.getText().trim();

        // Validate mandatory fields
        if (username.isEmpty() || name.isEmpty() || surname.isEmpty() || 
            phone.isEmpty() || email.isEmpty()) {
            
            statusLbl.setText("Error: All fields are required!");
            statusLbl.setStyle("-fx-text-fill: red;"); 
            return;
        }

        // Create new subscriber object (ID is 0 initially)
        Subscriber sub = new Subscriber(0, name, surname, phone, email, null, 0);
        sub.setUsername(username);
        
        // Send to server
        if (ClientUI.chat != null) {
            ClientUI.chat.handleMessageFromClientUI(new Message(ActionType.REGISTER_SUBSCRIBER, sub));
            statusLbl.setText("Generating ID...");
            statusLbl.setStyle("-fx-text-fill: black;"); 
            regBtn.setDisable(true); 
        }
    }

    /**
     * Closes the registration window.
     */
    @FXML
    public void closeWindow() {
        ((Stage) regBtn.getScene().getWindow()).close();
    }
 
    /**
     * Generates a QR code image for the given subscriber ID using an external API.
     *
     * @param id the subscriber ID to encode
     * @return an ImageView containing the QR code, or null if generation fails
     */
    private ImageView createQRImageView(int id) {
        try {
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=" + id;
            ImageView qrImage = new ImageView(new Image(qrUrl));
            qrImage.setFitHeight(120);
            qrImage.setFitWidth(120);
            return qrImage;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Handles the server response regarding the registration status.
     * Displays a success alert with the new ID and QR code, or an error message.
     *
     * @param msg the message received from the server
     */
    public void handleResponse(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Message) {
                Object content = ((Message)msg).getContent();
                
                if (content instanceof String) {
                    String response = (String) content;
                    
                    if (response.toLowerCase().contains("success") && response.contains("ID")) {
                        try {
                            // Extract ID from response string
                            String numberOnly = response.replaceAll("[^0-9]", "");
                            int newId = Integer.parseInt(numberOnly);
                            
                            // Show success alert with QR
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Registration Successful");
                            alert.setHeaderText("Client Added Successfully!");
                            
                            alert.setContentText("The new ID is: " + newId + 
                                                 "\nUsername: " + userTxt.getText() + 
                                                 "\n\n(Scan this QR code at the entrance)");

                            alert.setGraphic(createQRImageView(newId));
                            
                            alert.showAndWait();
                            closeWindow(); 
                            
                        } catch (Exception e) {
                            // Fallback if ID parsing fails
                            statusLbl.setText(response);
                            statusLbl.setStyle("-fx-text-fill: green;");
                        }
                    } else {
                        // Error message
                        statusLbl.setText(response);
                        statusLbl.setStyle("-fx-text-fill: red;");
                    }
                    regBtn.setDisable(false);
                }
                
                // Legacy support for Integer response
                else if (content instanceof Integer) {
                    int newId = (Integer) content;
                    if (newId > 0) {
                         Alert alert = new Alert(AlertType.INFORMATION);
                         alert.setTitle("Registration Successful");
                         alert.setHeaderText("Client Added Successfully!");
                         alert.setContentText("ID: " + newId + "\nScan QR Code:");
                         alert.setGraphic(createQRImageView(newId));
                         alert.showAndWait();
                         closeWindow();
                    }
                }
            }
        });
    }
}