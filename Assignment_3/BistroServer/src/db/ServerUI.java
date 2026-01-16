package db;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The entry point for the Server's Graphical User Interface (GUI).
 * This class extends Application and is responsible for loading the FXML layout,
 * initializing the main controller, and starting the server logic in a background thread.
 * * @author Group-17
 * @version 1.0
 */
public class ServerUI extends Application {

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the primary stage of the application.
     * Loads the 'ServerPortFrame.fxml' file, sets up the scene, and initializes
     * the server connection logic.
     *
     * @param primaryStage the primary window of the application
     * @throws Exception if an error occurs during FXML loading or server startup
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/db/ServerPortFrame.fxml"));
        Parent root = loader.load();
        
        // Get the controller and link it to EchoServer
        ServerPortFrameController controller = loader.getController();
        EchoServer.serverController = controller;
        
        // Start the server logic in a separate thread to prevent UI freezing
        runServer();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Server Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Initializes and starts the EchoServer instance.
     * Runs on a separate thread to ensure the GUI remains responsive.
     * Listens on the default port (5555).
     */
    private void runServer() {
        Thread serverThread = new Thread(() -> {
            try {
                EchoServer sv = new EchoServer(5555);
                sv.listen();
            } catch (Exception ex) {
                System.out.println("ERROR - Could not listen for clients!");
            }
        });
        serverThread.start();
    }
}
