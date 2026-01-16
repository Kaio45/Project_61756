package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main entry point for the Client side of the Bistro application.
 * This class extends the JavaFX Application class. It establishes the initial
 * connection logic and loads the Connection Screen.
 * @author Group-17
 * @version 1.0
 */
public class ClientUI extends Application {

    /** * The static instance of the ChatClient used to communicate with the server. 
     * It is static to allow access from various parts of the client application.
     */
    public static ChatClient chat; 

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args); 
    }

    /**
     * Starts the primary stage of the client application.
     * Loads the 'ConnectFrame.fxml' file to allow the user to enter server details.
     *
     * @param primaryStage the primary stage for this application
     * @throws Exception if an error occurs during FXML loading
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Step 1: The ChatClient creation is handled in ConnectFrameController.
        
        // Step 2: Load the Connect Frame FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ConnectFrame.fxml"));
        Parent root = loader.load();

        // Step 3: Set the title and display the stage
        Scene scene = new Scene(root);
        primaryStage.setTitle("Bistro - Connect"); 
        primaryStage.setScene(scene);
        
        // Handle clean application exit
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Closing client...");
            if (chat != null) {
                try {
                    chat.quit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.exit(0);
        });

        primaryStage.show();
    }
}
