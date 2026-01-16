package client;

import java.io.IOException;
import common.ActionType;
import common.Message;
import common.Subscriber;
import common.User;
import common.Order;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Login screen.
 * Handles authentication for Staff, Subscribers, Casual diners, and access by Confirmation Code.
 * @author Group-17
 * @version 1.0
 */
public class LoginFrameController {

    @FXML private TextField userTxt;
    @FXML private PasswordField passTxt; 
    @FXML private Label errorLabelStaff;

    @FXML private TextField subIdTxt;
    @FXML private Label errorLabelSub;

    @FXML private TextField casualNameTxt;
    @FXML private TextField casualPhoneTxt;
    @FXML private TextField casualEmailTxt; 
    @FXML private Label errorLabelCasual;

    @FXML private Button exitBtn;
    
    @FXML private TextField codeLoginTxt;  
    @FXML private Label errorCodeLbl;
    @FXML private TextField subUsernameTxt;
    private Subscriber currentAuthSubscriber; 

    /**
     * Handles staff login requests.
     * @param event the button click event
     */
    @FXML
    public void loginStaff(ActionEvent event) {
        String username = userTxt.getText();
        String password = passTxt.getText();
        if (username.isEmpty() || password.isEmpty()) {
            errorLabelStaff.setText("Please enter all fields");
            return;
        }
        User user = new User(0, username, password, null, null, null);
        sendToServer(new Message(ActionType.LOGIN, user), errorLabelStaff, "Authenticating...");
    }

    /**
     * Handles subscriber login requests.
     * @param event the button click event
     */
    @FXML
    public void loginSubscriber(ActionEvent event) {
        String idStr = subIdTxt.getText();
        String username = subUsernameTxt.getText();

        if (idStr.isEmpty() || username.isEmpty()) { 
            errorLabelSub.setText("Please enter Username AND ID"); 
            return; 
        }
        
        try {
            int id = Integer.parseInt(idStr);
            // Create a temporary subscriber object for the request
            Subscriber reqSub = new Subscriber(id, "", "", "", "", "", 0);
            reqSub.setUsername(username); 
            
            sendToServer(new Message(ActionType.IDENTIFY_SUBSCRIBER, reqSub), errorLabelSub, "Verifying...");
        } catch (NumberFormatException e) { errorLabelSub.setText("ID must be numbers only"); }
    }

    /**
     * Opens the 'Forgot ID' window.
     * @param event the button click event
     */
    @FXML
    public void openForgotId(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ForgotIdFrame.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Recover ID");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Handles login for casual clients (Guests).
     * @param event the button click event
     */
    @FXML
    public void loginCasual(ActionEvent event) {
        String name = casualNameTxt.getText();
        String phone = casualPhoneTxt.getText();
        String email = casualEmailTxt.getText();
        
        // Check 1: Full name is mandatory
        if (name.isEmpty()) {
            showAlert("Login Error", "Please enter Full Name.");
            return; 
        }

        // Check 2: At least one contact method is required
        if (phone.isEmpty() && email.isEmpty()) {
            showAlert("Login Error", "Please enter Phone Number OR Email address.");
            return; 
        }
        
        // Login valid
        System.out.println("Guest Login: " + name);
        openOrderFrame("Guest", name, phone, email);
    }

    /**
     * Helper method to show alerts.
     * @param title the alert title
     * @param content the alert content
     */
    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Handles login using an order confirmation code.
     * @param event the button click event
     */
    @FXML
    public void loginByCode(ActionEvent event) {
        String codeStr = codeLoginTxt.getText();
        if (codeStr.isEmpty()) return;
        try {
            sendToServer(new Message(ActionType.IDENTIFY_BY_CODE, Integer.parseInt(codeStr)), errorCodeLbl, "Searching...");
        } catch (NumberFormatException e) { errorCodeLbl.setText("Code must be numbers only"); }
    }
    
    private void sendToServer(Message msg, Label errorLbl, String statusText) {
        try {
            if (ClientUI.chat != null) {
                ClientUI.chat.sendToServer(msg);
                errorLbl.setText(statusText);
            } else { errorLbl.setText("No connection"); }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void getExitBtn(ActionEvent event) { System.exit(0); }
    
    /**
     * Handles responses from the server regarding login attempts.
     * @param msg the message received from server
     */
    public void handleResponse(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Message) {
                Message receivedMsg = (Message) msg;
                
                // --- Staff/Manager Login ---
                if (receivedMsg.getAction() == ActionType.LOGIN) {
                    if (receivedMsg.getContent() instanceof User) {
                        User loggedInUser = (User) receivedMsg.getContent();
                        
                        String realRole = loggedInUser.getUserType(); 
                        String realName = loggedInUser.getFirstName() + " " + loggedInUser.getLastName();
                        
                        openOrderFrame(realRole, realName, "", ""); 
                        
                    } else { 
                        errorLabelStaff.setText((String) receivedMsg.getContent()); 
                    }
                }
                // --- Subscriber Identification ---
                else if (receivedMsg.getAction() == ActionType.IDENTIFY_SUBSCRIBER) {
                    if (receivedMsg.getContent() instanceof Subscriber) {
                        Subscriber sub = (Subscriber) receivedMsg.getContent();
                        this.currentAuthSubscriber = sub;
                        openOrderFrame("Subscriber", sub.getFirstName(), sub.getPhone(), sub.getEmail());
                    } else { errorLabelSub.setText("Subscriber not found"); }
                }
                // --- Identification by Code ---
                else if (receivedMsg.getAction() == ActionType.IDENTIFY_BY_CODE) {
                    if (receivedMsg.getContent() instanceof Order) {
                        openOrderFrameForGuestOrder((Order) receivedMsg.getContent());
                    } else { errorCodeLbl.setText("Order not found"); }
                }
            }
        });
    }
    
    private void openOrderFrame(String userType, String name, String phone, String email) {
        try {
            ((Stage) exitBtn.getScene().getWindow()).close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/OrderFrame.fxml"));
            Parent root = loader.load();
            OrderFrameController controller = loader.getController();
            controller.setClient(ClientUI.chat);
            ChatClient.orderController = controller; 
            
            int userId = 0;
            if (userType.equals("Subscriber")) {
                 try { userId = Integer.parseInt(subIdTxt.getText()); } catch(Exception e){}
            }
            
            controller.initPermissions(userType, userId, name, phone, email); 
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private void openOrderFrameForGuestOrder(Order order) {
        try {
            ((Stage) exitBtn.getScene().getWindow()).close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/OrderFrame.fxml"));
            Parent root = loader.load();
            OrderFrameController controller = loader.getController();
            controller.setClient(ClientUI.chat);
            ChatClient.orderController = controller; 
            
            controller.initPermissions("GuestView", order.get_subscriber_id(), "", order.getPhone(), order.getEmail());
            controller.updateFields(new Message(ActionType.GET_ORDER, order));
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
