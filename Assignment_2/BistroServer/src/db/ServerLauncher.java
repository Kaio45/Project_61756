package db;
/**
 * A utility class that serves as the entry point for the application JAR.
 * This class acts as a wrapper around the main JavaFX application.
 * It is required to bypass JavaFX runtime module restrictions when running the application
 * as a standalone JAR file without explicit VM arguments.
 
 */
public class ServerLauncher {
    
    /**
     * The main method for execution to the JavaFX application.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        ServerUI.main(args);
    }
}