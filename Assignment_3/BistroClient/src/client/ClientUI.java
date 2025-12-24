package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * The main entry point for the Client side of the Bistro Restaurant System.
 * This class extends the JavaFX Application class to launch the GUI.
 * <p>
 * Instead of connecting immediately, it loads a connection frame (ConnectFrame)
 * where the user can input the server IP and Port.
 * </p>
 */
public class ClientUI extends Application {

    /**
     * The static instance of the ChatClient used to communicate with the server.
     * It is initialized only after the user clicks "Connect" in the ConnectFrame.
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
     * The main entry point for JavaFX applications.
     * <p>
     * This method loads the initial Connection Frame (ConnectFrame.fxml).
     * It allows the user to define connection parameters before the main application starts.
     * </p>
     *
     * @param primaryStage the primary stage for this application
     * @throws Exception if an error occurs during FXML loading
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load the FXML for the Connection Screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ConnectFrame.fxml"));
            Parent root = loader.load();
            
            // Set up the stage and scene
            Scene scene = new Scene(root);
            primaryStage.setTitle("Bistro System - Connection");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            // Handle FXML loading errors
            System.out.println("ERROR: Could not load ConnectFrame.fxml");
            e.printStackTrace();
        }
    }
}
