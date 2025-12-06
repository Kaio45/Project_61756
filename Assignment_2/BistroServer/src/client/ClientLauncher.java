package client;

/**
 * A utility class that serves as the entry point for the application JAR.
 * This class acts as a wrapper around the main JavaFX application.
 * It is required to bypass JavaFX runtime module restrictions when running the application
 * as a standalone JAR file without explicit VM arguments.
 
 */
public class ClientLauncher {
    
    /**
     * The main method that delegates execution to the JavaFX application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        ClientUI.main(args);
    }
}