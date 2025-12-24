package client;

import java.io.IOException;
import common.ActionType;
import common.Message;
import common.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller class for the Login Screen.
 * <p>
 * This class handles the user authentication process. It captures the username
 * and password input, sends a login request to the server, and processes
 * the server's response.
 * </p>
 */
public class LoginFrameController {

    /** The text field for the username input. */
    @FXML
    private TextField userTxt;

    /** The text field for the password input. */
    @FXML
    private TextField passTxt;

    /** The button to trigger the login action. */
    @FXML
    private Button loginBtn;

    /** The button to exit the application. */
    @FXML
    private Button exitBtn;

    /** The label to display error messages or status updates. */
    @FXML
    private Label errorLabel;

    /**
     * Handles the login button click event.
     * <p>
     * Validates that the fields are not empty, creates a {@link User} object
     * with the credentials, wraps it in a {@link Message} of type LOGIN,
     * and sends it to the server.
     * </p>
     *
     * @param event the action event triggered by the login button
     */
    @FXML
    public void login(ActionEvent event) {
        String username = userTxt.getText();
        String password = passTxt.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter all fields");
            return;
        }

        // Create a temporary User object for the login request
        User user = new User(0, username, password, null, null, null);
        
        // Wrap the User object in a Message protocol
        Message msg = new Message(ActionType.LOGIN, user);
        
        try {
            ClientUI.chat.sendToServer(msg);
            errorLabel.setText("Authenticating...");
        } catch (IOException e) {
            errorLabel.setText("Connection Error");
            e.printStackTrace();
        }
    }

    /**
     * Handles the exit button click event.
     * Terminates the application.
     *
     * @param event the action event triggered by the exit button
     */
    @FXML
    public void getExitBtn(ActionEvent event) {
        System.exit(0);
    }
    
    /**
     * Processes the login response received from the server.
     * <p>
     * This method is called by {@link ChatClient} when a message arrives.
     * It runs on the JavaFX Application Thread to update the UI safely.
     * </p>
     *
     * @param msg the message received from the server
     */
    public void handleLoginResponse(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof Message) {
                Message receivedMsg = (Message) msg;
                
                if (receivedMsg.getAction() == ActionType.LOGIN) {
                    if (receivedMsg.getContent() instanceof User) {
                        // Login successful
                        User loggedInUser = (User) receivedMsg.getContent();
                        System.out.println("User Logged In: " + loggedInUser.getUsername());
                        
                        // Transition to the main application screen
                        openOrderFrame();
                        
                    } else if (receivedMsg.getContent() instanceof String) {
                        // Login failed (display error message from server)
                        errorLabel.setText((String) receivedMsg.getContent());
                    }
                }
            }
        });
    }

    /**
     * Closes the login window and opens the main Order Management Frame.
     * <p>
     * This method is called only after a successful login.
     * </p>
     */
    private void openOrderFrame() {
        try {
            // Close the current login window
            Stage currentStage = (Stage) loginBtn.getScene().getWindow();
            currentStage.close();
            
            // Load the main OrderFrame FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/OrderFrame.fxml"));
            Parent root = loader.load();
            
            // Link the OrderFrameController with the ChatClient
            OrderFrameController controller = loader.getController();
            controller.setClient(ClientUI.chat);
            ChatClient.orderController = controller; // Update the static reference
            
            // Show the main window
            Stage primaryStage = new Stage();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Bistro Main Menu");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}