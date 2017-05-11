package com.aos.work;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import com.aos.config.Configuration;
import com.aos.config.SpanningTree;
import com.aos.log.FileLogger;
import com.aos.log.Logger;

import Convergecast.MainMog;

public class AOS {

	public static void main(String[] args) throws UnknownHostException {

		if (args.length == 0)
			throw new IllegalArgumentException("Argument missing.");

		if (args[0].isEmpty())
			throw new IllegalArgumentException("Config file missing.");

		if (args[1].isEmpty())
			throw new ArrayIndexOutOfBoundsException("Id missing.");

		// Object Instance
		Logger logger = null;
		Configuration resource = new Configuration();
		resource.setCONFIG_FILE(args[0]);
		resource.setNodeId(args[1]);
	
		/* Parse config.txt file and store data */
		resource.parseConfigFile();
		
		/* Build the spanning tree */
		SpanningTree.buildSpanningTree(resource.getAdjMatrix());
		
/*		// Testing Tree
		for(int i = 0; i < resource.getNumOfNodes(); i++)
			System.out.println("Parent of " + i + " is " + SpanningTree.getParent(i));
*/
		ServerSocket serverSocket = null;

		try {
			serverSocket = new
					ServerSocket(resource.getPorts()[Integer.parseInt(resource.getNodeId())]);

			logger = new FileLogger(resource.getCONFIG_FILE(), resource.getNodeId());
			logger.writeEntry("Outputs\n");

			// Test for Configuration
			logger.writeLog(resource.toString());

			// Start Server Execution 
			new Thread(new Server(resource, logger, serverSocket)).start();

			// Send initialize token to its one hop neighbors
			resource.setUpNetwork(logger);

			// Node 0 is the one assumed to be active and starts the Snapshot protocol
			if( resource.getNodeId().equals("0") ){
				logger.writeLog(args[1] + " Sending active msg.");

				new Thread(new Client(resource, logger)).start(); // Start Client Execution

				new Thread(new ChandyLamport(resource, logger)).start(); // Start Chandy Lamport Execution
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}		 

