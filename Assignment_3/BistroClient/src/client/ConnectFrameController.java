package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller class for the Connection Frame GUI.
 * <p>
 * This class handles the initial connection logic where the user inputs
 * the Server IP and Port. It validates the input, establishes the connection,
 * and then transitions to the Login window.
 * </p>
 */
public class ConnectFrameController {

    /** The text field for entering the server IP address. */
    @FXML
    private TextField ipTxt;

    /** The text field for entering the server port number. */
    @FXML
    private TextField portTxt;

    /** The button to initiate the connection attempt. */
    @FXML
    private Button connectBtn;

    /** The button to exit the application. */
    @FXML
    private Button exitBtn;

    /** The label for displaying error messages to the user (e.g., connection failed). */
    @FXML
    private Label errorLabel;

    /**
     * Handles the "Connect" button click event.
     * <p>
     * Reads the IP and Port from the text fields, validates them, and attempts
     * to create a new instance of {@link ChatClient}. If successful, it hides
     * the connection window and opens the Login window.
     * </p>
     *
     * @param event the action event triggered by the Connect button
     */
    @FXML
    public void connectToServer(ActionEvent event) {
        String ip = ipTxt.getText();
        String portStr = portTxt.getText();

        if (ip.isEmpty() || portStr.isEmpty()) {
            errorLabel.setText("Please enter IP and Port");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            
            // Attempt to connect and initialize the static ChatClient instance
            ClientUI.chat = new ChatClient(ip, port);
            
            // If we reached here, connection was successful. Hide the connect frame.
            ((Node)event.getSource()).getScene().getWindow().hide(); 
            
            // Load and show the Login Frame (Correction: Go to Login, not Order directly)
            loadLoginFrame();
            
        } catch (NumberFormatException e) {
            errorLabel.setText("Port must be a number");
        } catch (Exception e) {
            errorLabel.setText("Connection Failed!");
            e.printStackTrace();
        }
    }
    
    /**
     * Loads and displays the Login Frame.
     * <p>
     * This method is called only after a successful connection to the server.
     * It initializes the LoginFrameController and links it with the ChatClient static reference.
     * </p>
     *
     * @throws Exception if the FXML file cannot be loaded
     */
    private void loadLoginFrame() throws Exception {
        // Updated path to load LoginFrame instead of OrderFrame
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/LoginFrame.fxml"));
        Parent root = loader.load();
        
        // Get the controller of the login frame
        LoginFrameController controller = loader.getController();
        
        // Update the static reference in ChatClient so it can handle login responses
        ChatClient.loginController = controller; 
        
        Stage primaryStage = new Stage();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Bistro - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Handle window close request to ensure clean disconnection
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Closing login window...");
            if (ClientUI.chat != null) {
                try {
                    ClientUI.chat.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.exit(0);
        });
    }

    /**
     * Handles the "Exit" button click event.
     * Terminates the application immediately.
     *
     * @param event the action event triggered by the Exit button
     */
    @FXML
    public void getExitBtn(ActionEvent event) {
        System.out.println("Exiting application...");
        System.exit(0);
    }
}
