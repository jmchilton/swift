package edu.mayo.mprc.messaging.rmi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

class AnonymousRMISocketFactory extends RMISocketFactory {

	private int lastUsedPort;

	public AnonymousRMISocketFactory() {
		super();
	}

	public Socket createSocket(String host, int port) throws IOException {
		Socket socket = new Socket(host, port);
		lastUsedPort = socket.getLocalPort();
		return socket;
	}

	public ServerSocket createServerSocket(int port) throws IOException {
		ServerSocket serverSocket = new ServerSocket(port);
		lastUsedPort = serverSocket.getLocalPort();
		return serverSocket;
	}

	public int getLastUsedPort() {
		return lastUsedPort;
	}
}
