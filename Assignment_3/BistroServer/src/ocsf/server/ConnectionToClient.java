package ocsf.server;

import java.io.*;
import java.net.*;

/**
 * An instance of this class is created for each connection to a client.
 * It handles the input and output streams for a specific client connection.
 * @author Group-17
 * @version 1.0
 */
public class ConnectionToClient extends Thread {
  
  /** The server instance to which this connection belongs. */
  private AbstractServer server;
  
  /** The socket connecting to the client. */
  private Socket clientSocket;
  
  /** The stream used to read from the client. */
  private ObjectInputStream input;
  
  /** The stream used to write to the client. */
  private ObjectOutputStream output;
  
  /** Indicates if the connection loop should stop. */
  private boolean readyToStop;

  /**
   * Constructs a new connection to a client.
   *
   * @param group the thread group that contains the connections.
   * @param clientSocket the socket contains the client's connection.
   * @param server a reference to the server that created this instance.
   * @throws IOException if an I/O error occur when creating the streams.
   */
  ConnectionToClient(ThreadGroup group, Socket clientSocket, AbstractServer server) throws IOException {
    super(group, (Runnable)null);
    this.clientSocket = clientSocket;
    this.server = server;
    clientSocket.setSoTimeout(0);
    try {
      input = new ObjectInputStream(clientSocket.getInputStream());
      output = new ObjectOutputStream(clientSocket.getOutputStream());
    } catch (IOException ex) {
      try { closeAll(); } catch (Exception exc) { }
      throw ex;
    }
    this.start();
  }

  /**
   * Sends an object to the client.
   *
   * @param msg the message to be sent.
   * @throws IOException if an I/O error occur when sending the message.
   */
  public final void sendToClient(Object msg) throws IOException {
    if (clientSocket == null || output == null) throw new SocketException("socket does not exist");
    output.writeObject(msg);
  }

  /**
   * Constantly reads the client's input stream.
   * Receives messages and forwards them to the server's message handler.
   */
  public void run() {
    Object msg;
    try {
      while (!readyToStop) {
        try {
          msg = input.readObject();
          server.receiveMessage(msg, this);
        } catch (ClassNotFoundException ex) {
          server.clientException(this, ex);
        } catch (RuntimeException ex) {
          server.clientException(this, ex);
        }
      }
    } catch (Exception exception) {
      if (!readyToStop) {
        try { closeAll(); } catch (Exception ex) { }
        server.clientDisconnected(this);
      }
    }
  }

  /**
   * Closes the connection to the client.
   *
   * @throws IOException if an error occurs when closing the socket.
   */
  public void close() throws IOException {
    readyToStop = true;
    try { closeAll(); } catch (Exception ex) { }
  }

  /**
   * Closes all streams and the socket.
   *
   * @throws IOException if an I/O error occurs.
   */
  private void closeAll() throws IOException {
    if (clientSocket != null) clientSocket.close();
    if (output != null) output.close();
    if (input != null) input.close();
  }
  
  /**
   * Returns the address of the client.
   *
   * @return the InetAddress of the client.
   */
  public InetAddress getInetAddress() { return clientSocket == null ? null : clientSocket.getInetAddress(); }
}
