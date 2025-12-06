package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main entry point for the Client side of the Restaurant Order Management system.
 * This class extends the JavaFX Application class to launch the GUI and establishes
 * the initial connection to the server.
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
     * The main entry point for JavaFX applications.
     * This method establishes the connection to the server, loads the main GUI frame (OrderFrame),
     * initializes the controller, and displays the primary stage.
     *
     * @param primaryStage the primary stage for this application, onto which the application scene can be set
     * @throws Exception if an error occurs during FXML loading or server connection
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Establishes connection to the specific IP and Port
            chat = new ChatClient("10.121.237.14", 5555);
        } catch (Exception e) {
            System.out.println("Can't connect to server");
        }

        // Loads the FXML layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/OrderFrame.fxml"));
        Parent root = loader.load();
        
        // Retrieves the controller and links it with the client instance
        OrderFrameController controller = loader.getController();
        controller.setClient(chat);
        
        // Saves the controller reference in ChatClient for receiving updates
        ChatClient.orderController = controller; 

        // Sets up the scene and displays the stage
        Scene scene = new Scene(root);
        primaryStage.setTitle("Restaurant Order Management");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}