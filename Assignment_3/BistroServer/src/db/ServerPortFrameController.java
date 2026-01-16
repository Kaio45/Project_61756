package db;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * Controller class for the Server's GUI window (ServerPortFrame).
 * Handles user interactions such as clicking the exit button and updates
 * the log area with messages from the server.
 * * @author Group-17
 * @version 1.0
 */
public class ServerPortFrameController {

    /** The text area component for displaying server logs. */
    @FXML
    private TextArea logTextArea;

    /** The button component used to exit the server application. */
    @FXML
    private Button exitBtn;

    /**
     * Handles the 'Exit' button click event.
     * Prints a termination message to the console and shuts down the application.
     *
     * @param event the ActionEvent triggered by the button click
     */
    public void getExitBtn(ActionEvent event) {
        System.out.println("Stopping Server...");
        System.exit(0);
    }

    /**
     * Appends a new message to the server log display.
     * Uses Platform.runLater to ensure that UI updates are performed safely
     * on the JavaFX Application Thread.
     *
     * @param msg the message string to add to the log
     */
    public void addToLog(String msg) {
        Platform.runLater(() -> {
            logTextArea.appendText(msg + "\n");
        });
    }
}
