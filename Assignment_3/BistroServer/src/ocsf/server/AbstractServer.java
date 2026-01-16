package ocsf.server;

import java.net.*;
import java.io.*;

/**
 * The abstract superclass of any server in the system.
 * This class contains the basic logic for listening for connections,
 * accepting clients, and managing the thread pool.
 * <p>
 * Concrete subclasses must implement the {@link #handleMessageFromClient} method.
 * </p>
 * @author Group-17
 * @version 1.0
 */
public abstract class AbstractServer implements Runnable {
  
  /** The server socket used to accept connections. */
  private ServerSocket serverSocket = null;
  
  /** The thread used to listen for new connections. */
  private Thread connectionListener;
  
  /** The port number to listen on. */
  private int port;
  
  /** The timeout duration for the server socket. */
  private int timeout = 500;
  
  /** The maximum queue length for incoming connection requests. */
  private int backlog = 10;
  
  /** The thread group containing all client connection threads. */
  private ThreadGroup clientThreadGroup;
  
  /** Indicates if the server is ready to stop listening. */
  private boolean readyToStop = false;

  /**
   * Constructs a new server.
   *
   * @param port the port number on which to listen.
   */
  public AbstractServer(int port) {
    this.port = port;
    this.clientThreadGroup = new ThreadGroup("ConnectionToClient threads");
  }

  /**
   * Begins the thread that waits for new clients.
   * If the server is already listening, this method does nothing.
   *
   * @throws IOException if an I/O error occurs when creating the server socket.
   */
  public void listen() throws IOException {
    if (!isListening()) {
      if (serverSocket == null) serverSocket = new ServerSocket(port, backlog);
      serverSocket.setSoTimeout(timeout);
      connectionListener = new Thread(this);
      connectionListener.start();
    }
  }

  /**
   * Causes the server to stop accepting new connections.
   */
  public void stopListening() { readyToStop = true; }

  /**
   * Closes the server socket and the connections with all clients.
   * Any exception caused by this method is ignored.
   *
   * @throws IOException if an I/O error occurs when closing the server socket.
   */
  public void close() throws IOException {
    stopListening();
    if (serverSocket != null) serverSocket.close();
    serverSocket = null;
  }

  /**
   * Sends a message to every client connected to the server.
   * This is merely a utility; a subclass may want to do some checks
   * before sending it to all.
   *
   * @param msg the message to be sent.
   */
  public void sendToAllClients(Object msg) {
    Thread[] clientThreadList = new Thread[clientThreadGroup.activeCount()];
    clientThreadGroup.enumerate(clientThreadList);
    for (int i=0; i<clientThreadList.length; i++) {
      try { ((ConnectionToClient)clientThreadList[i]).sendToClient(msg); } catch (Exception ex) {}
    }
  }

  /**
   * Checks if the server is currently listening for new connections.
   *
   * @return true if the server is listening, false otherwise.
   */
  public boolean isListening() { return (connectionListener != null && connectionListener.isAlive()); }

  /**
   * Handles a command sent from one client to the server.
   * This MUST be implemented by subclasses, who should respond to messages.
   *
   * @param msg the message sent.
   * @param client the connection that sent the message.
   */
  protected abstract void handleMessageFromClient(Object msg, ConnectionToClient client);

  /**
   * Hook method called each time a new client connection is accepted.
   * The default implementation does nothing.
   * @param client the connection connected to the client.
   */
  protected void clientConnected(ConnectionToClient client) {}

  /**
   * Hook method called each time a client disconnects.
   * The default implementation does nothing.
   * @param client the connection with the client.
   */
  protected synchronized void clientDisconnected(ConnectionToClient client) {}

  /**
   * Hook method called each time an exception occurs in a client thread.
   * The default implementation does nothing.
   * @param client the client that raised the exception.
   * @param exception the exception raised.
   */
  protected synchronized void clientException(ConnectionToClient client, Throwable exception) {}

  /**
   * Hook method called when the server starts listening for connections.
   */
  protected void serverStarted() {}

  /**
   * Hook method called when the server stops accepting connections.
   */
  protected void serverStopped() {}

  /**
   * Hook method called when an exception occurs while listening.
   * @param exception the exception raised.
   */
  protected void listeningException(Throwable exception) {}

  /**
   * Hook method called when the server is closed.
   */
  protected void serverClosed() {}

  /**
   * Runs the listening thread that allows clients to connect.
   */
  public void run() {
    serverStarted();
    try {
      while (!readyToStop) {
        try {
          Socket clientSocket = serverSocket.accept();
          ConnectionToClient c = new ConnectionToClient(clientThreadGroup, clientSocket, this);
          clientConnected(c);
        } catch (InterruptedIOException exception) {
        }
      }
    } catch (IOException exception) {
      if (!readyToStop) listeningException(exception);
    } finally {
      readyToStop = true;
      connectionListener = null;
      serverStopped();
    }
  }
  
  /**
   * Receives a message from a client.
   * This method calls the abstract method {@link #handleMessageFromClient}.
   *
   * @param msg the message received.
   * @param client the connection that sent the message.
   */
  public final void receiveMessage(Object msg, ConnectionToClient client) {
	  this.handleMessageFromClient(msg, client);
  }
  
  /**
   * Returns the port number.
   * @return the port number.
   */
  public final int getPort() {
	    return port;
	}
}
