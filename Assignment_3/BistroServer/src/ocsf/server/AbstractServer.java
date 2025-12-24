package ocsf.server;

import java.net.*;
import java.util.*;
import java.io.*;

public abstract class AbstractServer implements Runnable {
  private ServerSocket serverSocket = null;
  private Thread connectionListener;
  private int port;
  private int timeout = 500;
  private int backlog = 10;
  private ThreadGroup clientThreadGroup;
  private boolean readyToStop = false;

  public AbstractServer(int port) {
    this.port = port;
    this.clientThreadGroup = new ThreadGroup("ConnectionToClient threads");
  }

  public void listen() throws IOException {
    if (!isListening()) {
      if (serverSocket == null) serverSocket = new ServerSocket(port, backlog);
      serverSocket.setSoTimeout(timeout);
      connectionListener = new Thread(this);
      connectionListener.start();
    }
  }

  public void stopListening() { readyToStop = true; }

  public void close() throws IOException {
    stopListening();
    if (serverSocket != null) serverSocket.close();
    serverSocket = null;
  }

  public void sendToAllClients(Object msg) {
    Thread[] clientThreadList = new Thread[clientThreadGroup.activeCount()];
    clientThreadGroup.enumerate(clientThreadList);
    for (int i=0; i<clientThreadList.length; i++) {
      try { ((ConnectionToClient)clientThreadList[i]).sendToClient(msg); } catch (Exception ex) {}
    }
  }

  public boolean isListening() { return (connectionListener != null && connectionListener.isAlive()); }

  protected abstract void handleMessageFromClient(Object msg, ConnectionToClient client);

  protected void clientConnected(ConnectionToClient client) {}
  protected synchronized void clientDisconnected(ConnectionToClient client) {}
  protected synchronized void clientException(ConnectionToClient client, Throwable exception) {}
  protected void serverStarted() {}
  protected void serverStopped() {}
  protected void listeningException(Throwable exception) {}
  protected void serverClosed() {}

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
  
  public final void receiveMessage(Object msg, ConnectionToClient client) {
	  this.handleMessageFromClient(msg, client);
  }
  
  public final int getPort() {
	    return port;
	}
}