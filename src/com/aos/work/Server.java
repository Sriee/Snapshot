 	package com.aos.work;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.aos.config.Configuration;
import com.aos.log.Logger;
import com.aos.msg.Message;

public class Server implements Runnable{

	private Configuration resource = null;
	private Logger logger;
	private ServerSocket serverSocket;

	/**
	 * @param resource
	 * @param logger
	 * @param serverSocket
	 */
	public Server(Configuration resource, Logger logger, ServerSocket serverSocket) {
		super();
		this.resource = resource;
		this.logger = logger;
		this.serverSocket = serverSocket;
	}


	@Override
	public void run() {
		Socket clientSocket = null;
		int neighborSize = this.resource.getOneHopNeighbhor().size();
		int connection = 0;
		int k;
		this.logger.writeLog("Setup Started");

		// Check for how many connection this node should wait for
		for(k = 0; k < neighborSize; k++){
			if( Integer.parseInt(this.resource.getOneHopNeighbhor().get(k)) > 
					Integer.parseInt(this.resource.getNodeId()))
				connection ++;
		}
		
		this.logger.writeLog("I should wait for " + connection + " connections");
		try {

			k = 0; 
			while ( k < connection ) {
				k++;
				
				// Blocking call for accepting client connection 
				clientSocket = this.serverSocket.accept();
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
				Message msg = (Message) in.readObject();
				
				if( msg == null )
					throw new NullPointerException(this.resource.getNodeId() + " received 'null' message ");

				this.logger.writeLog("Connected to " + msg.getSender() + " @ " + 
						clientSocket.getInetAddress().getHostName());
				
				this.resource.updateSocketMap(msg.getSender(), clientSocket);
				this.resource.hashOs.put(msg.getSender(), new ObjectOutputStream(clientSocket.getOutputStream()));
				new Thread(new Listener(this.resource, this.logger, msg.getSender(), in)).start();
			}
		} catch (IOException e){
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	} 

}