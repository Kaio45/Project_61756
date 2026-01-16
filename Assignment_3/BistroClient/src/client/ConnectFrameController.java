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
 * Handles the initial connection logic where the user inputs the Server IP and Port.
 * It validates the input, establishes the connection, and then transitions to the Login window.
 * @author Group-17
 * @version 1.0
 */
public class ConnectFrameController {

    @FXML private TextField ipTxt;
    @FXML private TextField portTxt;
    @FXML private Button connectBtn;
    @FXML private Button exitBtn;
    @FXML private Label errorLabel;

    /**
     * Handles the "Connect" button click event.
     * Reads the IP and Port from the text fields, validates them, and attempts
     * to create a new instance of ChatClient.
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
            
            // Load and show the Login Frame
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
     * Called only after a successful connection to the server.
     *
     * @throws Exception if the FXML file cannot be loaded
     */
    private void loadLoginFrame() throws Exception {
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
