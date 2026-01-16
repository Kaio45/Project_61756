package db;

/**
 * A launcher class designed to bypass JavaFX runtime module restrictions.
 * This class serves as the entry point when running the application as a standalone JAR file.
 * It delegates execution to the main JavaFX class, ServerUI.
 * * @author Group-17
 * @version 1.0
 */
public class ServerLauncher {
    
    /**
     * The main entry point for the JAR execution.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        ServerUI.main(args);
    }
}
