package ocsf.client;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The AbstractClient class contains all the methods necessary to set up
 * the client side of a client-server architecture. When a client is thus
 * connected to the server, the two programs can identify each other and
 * exchange messages.
 */
public abstract class AbstractClient implements Runnable {
  
  // Instance variables
  private Socket clientSocket;
  private ObjectOutputStream output;
  private ObjectInputStream input;
  private Thread clientReader;
  private boolean readyToStop = false;
  private String host;
  private int port;

  /**
   * Constructs the client.
   *
   * @param host the server's host name.
   * @param port the port number.
   */
  public AbstractClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Opens the connection with the server.
   * If the connection is already opened, this method does nothing.
   *
   * @throws IOException if an I/O error occurs when opening.
   */
  public void openConnection() throws IOException {
    if (isConnected()) return;
    try {
      clientSocket = new Socket(host, port);
      output = new ObjectOutputStream(clientSocket.getOutputStream());
      input = new ObjectInputStream(clientSocket.getInputStream());
      clientReader = new Thread(this);
      readyToStop = false;
      clientReader.start(); // Starts the thread that waits for messages
    } catch (IOException exception) {
      connectionException(exception);
      throw exception;
    }
  }

  /**
   * Sends an object to the server. This is the only way that
   * methods should communicate with the server.
   *
   * @param msg The message to be sent.
   * @throws IOException if an I/O error occurs when sending.
   */
  public void sendToServer(Object msg) throws IOException {
    if (clientSocket == null || output == null) 
    	throw new SocketException("socket does not exist");
    
    output.writeObject(msg);
    output.reset();
  }

  /**
   * Closes the connection to the server.
   *
   * @throws IOException if an I/O error occurs when closing.
   */
  public void closeConnection() throws IOException {
    readyToStop = true;
    closeAll();
  }

  // Accessors
  public boolean isConnected() { return clientReader != null && clientReader.isAlive(); }
  public int getPort() { return port; }
  public void setPort(int port) { this.port = port; }
  public String getHost() { return host; }
  public void setHost(String host) { this.host = host; }
  public InetAddress getInetAddress() { return clientSocket == null ? null : clientSocket.getInetAddress(); }

  /**
   * Waits for messages from the server. When each message arrives,
   * a method is called to handle it.
   */
  public void run() {
    connectionEstablished();
    Object msg;
    try {
      while (!readyToStop) {
        try {
          msg = input.readObject(); // Waits for a message
          handleMessageFromServer(msg);
        } catch (ClassNotFoundException ex) {
          connectionException(ex);
        } catch (RuntimeException ex) {
          connectionException(ex);
        }
      }
    } catch (Exception exception) {
      if (!readyToStop) {
        try { closeAll(); } catch (Exception ex) { }
        connectionClosed();
      }
    } finally {
      clientReader = null;
    }
  }

  // Methods to be overridden by the concrete client
  protected void connectionClosed() {}
  protected void connectionException(Exception exception) {}
  protected void connectionEstablished() {}
  
  /**
   * Handles a message sent from the server to this client.
   * This MUST be implemented by the concrete client class.
   *
   * @param msg the message sent.
   */
  protected abstract void handleMessageFromServer(Object msg);

  /**
   * Closes all aspects of the connection to the server.
   */
  private void closeAll() throws IOException {
    try {
      if (clientSocket != null) clientSocket.close();
      if (output != null) output.close();
      if (input != null) input.close();
    } finally {
      output = null;
      input = null;
      clientSocket = null;
    }
  }
}