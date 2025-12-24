package db;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The Class ServerUI.
 * This class serves as the main entry point for the Server application.
 * It launches the JavaFX user interface and starts the server listening thread.
 */
public class ServerUI extends Application {

    /**
     * The main method.
     * Launches the JavaFX application.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start.
     * Initializes the primary stage, loads the FXML interface, links the
     * controller to the server logic, and starts the server connection listener.
     *
     * @param primaryStage the primary stage for this application
     * @throws Exception if an error occurs during FXML loading
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/db/ServerPortFrame.fxml"));
        Parent root = loader.load();
        
        // Get the controller and set it in EchoServer static reference
        ServerPortFrameController controller = loader.getController();
        EchoServer.serverController = controller;
        
        // Start the server in a background thread to avoid freezing the UI
        runServer();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Server Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Starts the EchoServer.
     * Creates a new thread to run the server listening loop, ensuring the
     * JavaFX Application Thread remains responsive.
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