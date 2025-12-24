package ocsf.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionToClient extends Thread {
  private AbstractServer server;
  private Socket clientSocket;
  private ObjectInputStream input;
  private ObjectOutputStream output;
  private boolean readyToStop;

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

  public final void sendToClient(Object msg) throws IOException {
    if (clientSocket == null || output == null) throw new SocketException("socket does not exist");
    output.writeObject(msg);
  }

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

  public void close() throws IOException {
    readyToStop = true;
    try { closeAll(); } catch (Exception ex) { }
  }

  private void closeAll() throws IOException {
    if (clientSocket != null) clientSocket.close();
    if (output != null) output.close();
    if (input != null) input.close();
  }
  
  public InetAddress getInetAddress() { return clientSocket == null ? null : clientSocket.getInetAddress(); }
}
