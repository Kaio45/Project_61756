package db;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * The Class ServerPortFrameController.
 * Controls the server GUI window, displaying logs and providing an exit mechanism.
 */
public class ServerPortFrameController {

    /** The text area for displaying server logs. */
    @FXML
    private TextArea logTextArea;

    /** The exit button. */
    @FXML
    private Button exitBtn;

    /**
     * Handles the exit button action.
     * Stops the server and terminates the application when the button is clicked.
     *
     * @param event the action event triggered by clicking the button
     */
    public void getExitBtn(ActionEvent event) {
        System.out.println("Stopping Server...");
        System.exit(0);
    }

    /**
     * Adds a message to the server log area.
     * This method ensures the UI update is performed on the JavaFX Application Thread
     * to prevent threading exceptions.
     *
     * @param msg the message string to append to the log
     */
    public void addToLog(String msg) {
        Platform.runLater(() -> {
            logTextArea.appendText(msg + "\n");
        });
    }
}