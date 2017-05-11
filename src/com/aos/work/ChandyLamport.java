/**
 * 
 */
package com.aos.work;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

import com.aos.config.Color;
import com.aos.config.Configuration;
import com.aos.log.Logger;
import com.aos.msg.Message;

/**
 * @author sriee
 *
 */
public class ChandyLamport implements Runnable {

	private Configuration resource;
	private Logger logger;

	/**
	 * @param resource
	 * @param logger
	 */
	public ChandyLamport(Configuration resource, Logger logger) {
		super();
		this.resource = resource;
		this.logger = logger;
	}

	@Override
	public void run() {
		Socket to = null;
		String thisId = this.resource.getNodeId();
		int numNodes = this.resource.getNumOfNodes();
		int[] duplicateVectorClock = null;
		
		// Wait till the network is setup
		while (this.resource.getSocketMap().keySet().size() != this.resource.getOneHopNeighbhor().size())
			;
		try {
			Thread.sleep(this.resource.getSnapshotDelay());
			
			this.logger.writeLog("Taking snapshot.." + 
					Arrays.toString(this.resource.getMyVC().getVectorClock()));
			
			synchronized(resource){
				if (this.resource.getColor() == Color.BLUE) {

					this.resource.setColor(Color.RED); // Change color
					Message markerMsg = new Message(thisId, "marker");

					this.resource.setLogging(true); // Enable logging

					// Storing the vector clock for the snapshot
					this.resource.getMyVC().sendEvent(); // Update vector clock
					duplicateVectorClock = new int[numNodes];
					duplicateVectorClock = Arrays.copyOf(this.resource.getMyVC().getVectorClock(), numNodes);
					this.resource.addOutput(duplicateVectorClock);

					for (String toSend : resource.getOneHopNeighbhor()) {
						to = this.resource.getSocketMap(toSend);

						if (to == null) {
							this.logger.writeLog("'null' socket for " + toSend);
							throw new NullPointerException("Error in ChandyLanport.");
						}

						this.logger.writeLog("Sending " + markerMsg.getMessageType() + " message to " + toSend);
						ObjectOutputStream out = this.resource.hashOs.get(toSend);
						out.reset();
						out.writeObject(markerMsg);
						out.flush();
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
