package db;

import java.io.IOException;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import common.Order;

/**
 * The Class EchoServer.
 * Extends the AbstractServer class to handle client-server communication
 * and database operations.
 */
public class EchoServer extends AbstractServer {
	
	/** The Constant DEFAULT_PORT. */
	final public static int DEFAULT_PORT = 5555;
	
	/** The server controller. */
	public static ServerPortFrameController serverController;

	/**
	 * Instantiates a new echo server.
	 *
	 * @param port the port to listen on
	 */
	public EchoServer(int port) {
		super(port);
	}

	/**
	 * Handle message from client.
	 * Processes incoming messages which can be either a String request for data
	 * or an Order object for database updates.
	 *
	 * @param msg the message received from the client
	 * @param client the connection to the client
	 */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
	    System.out.println("Message received: " + msg + " from " + client);

	    if (msg instanceof String) {
	        String message = (String) msg;
	        if (message.startsWith("GET")) {
	            int id = Integer.parseInt(message.split(" ")[1]);
	            Order order = mysqlConnection.getOrder(id);
	            try {
	                client.sendToClient(order);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    else if (msg instanceof Order) {
	        Order order = (Order) msg;
	        if (order.get_order_number() == 0) {
	            System.out.println("Saving new order...");
	            mysqlConnection.saveOrderToDB(order);
	        } else {
	            System.out.println("Updating existing order...");
	            mysqlConnection.updateOrder(order);
	        }
	        
	        try {
	            client.sendToClient("Action successful");
	        } catch (IOException e) { e.printStackTrace(); }
	    }
	}
	
	/**
	 * Server started.
	 * Called when the server starts listening. Connects to the database
	 * and logs the status to the server controller.
	 */
	@Override
	protected void serverStarted() {
	    System.out.println("Server listening for connections on port " + getPort());
	    mysqlConnection.connectToDB();
	    
	    if (serverController != null) {
	        serverController.addToLog("Server listening on port " + getPort());
	        serverController.addToLog("Database connected!");
	    }
	}

	/**
	 * Server stopped.
	 * Called when the server stops listening for connections.
	 */
	@Override
	protected void serverStopped() {
		System.out.println("Server has stopped listening for connections.");
	}
	
	/**
	 * Client connected.
	 * Called when a client connects to the server.
	 *
	 * @param client the client connection
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
	      String msg = "Client Connected! IP: " + client.getInetAddress() + " Host: " + client.getInetAddress().getHostName();
	      System.out.println(msg);
	      
	      if (serverController != null) {
	          serverController.addToLog(msg);
	      }
	}
	
	/**
	 * The main method.
	 * Responsible for initializing the server instance.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args){
	    int port = 0; 

	    try{
	      port = Integer.parseInt(args[0]); 
	    }
	    catch(Throwable t){
	      port = DEFAULT_PORT; 
	    }

	    EchoServer sv = new EchoServer(port);

	    try {
	      sv.listen(); 
	    } catch (Exception ex) {
	      System.out.println("ERROR - Could not listen for clients!");
	    }
	}
}