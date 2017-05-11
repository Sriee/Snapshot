package com.aos.work;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

import com.aos.config.ProcessState;
import com.aos.config.Configuration;
import com.aos.log.Logger;
import com.aos.msg.Message;

public class Client implements Runnable {

	private Configuration resource;
	private Logger logger;

	/**
	 * @param config
	 * @param logger
	 * @param vectorClock
	 */
	public Client(Configuration config, Logger logger) {
		super();
		this.resource = config;
		this.logger = logger;
	}

	@Override
	public void run() {
		Socket to = null;
		int idx = 0;
		String toSend;
		int maxNumber = this.resource.getMaxNumber();
		int applicationMsgCount = ThreadLocalRandom.current().nextInt(this.resource.getMinPerActive(),
				this.resource.getMaxPerActive() + 1);

		// Wait till the network is setup
		while (this.resource.getSocketMap().keySet().size() != this.resource.getOneHopNeighbhor().size());
		
		this.logger.writeLog("Client has started.");
		applicationMsgCount = applicationMsgCount < (maxNumber - this.resource.getTotalMsgSent())
				? (applicationMsgCount) : (maxNumber - this.resource.getTotalMsgSent());
	
		this.logger.writeLog("I will send " + applicationMsgCount + " Application Msg.");
		Message appMsg = null;
		try {

			appMsg = new Message(resource.getNodeId(), "application");

			for (int i = 0; i < applicationMsgCount && this.resource.isActive() &&	
					this.resource.getTotalMsgSent() <= this.resource.getMaxNumber(); i++) {
				synchronized (resource) {
					toSend = this.resource.getOneHopNeighbhor().get(idx); 
					this.resource.getMyVC().sendEvent(); // Update vector clock
					appMsg.setVectorClock(this.resource.getMyVC());

					to = this.resource.getSocketMap(toSend);

					if (to == null) {
						this.logger.writeLog("'null' socket for " + idx);
						throw new NullPointerException("Error in connection setup.");
					}

					this.logger.writeLog("Sending " + appMsg.getMessageType() + " message to " + toSend + " : "
							+ this.resource.getMyVC());

					ObjectOutputStream out = this.resource.hashOs.get(toSend);
					out.reset();
					out.writeObject(appMsg);
					out.flush();

					// Calculate index in circular token
					idx = (idx + 1) % this.resource.getOneHopNeighbhor().size();  

					this.resource.incrementTotalMsgSent(); // Increment total # of msg sent
				}

				Thread.sleep(resource.getMinSendDelay());
			}

			this.resource.setState(ProcessState.PASSIVE); // Process becomes passive
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void sendToNeighbors(Message msg) throws IOException{
		Socket to = null;
		for (String toSend : resource.getOneHopNeighbhor()) {
			to = this.resource.getSocketMap(toSend);

			if (to == null) {
				this.logger.writeLog("'null' socket for " + toSend);
				throw new NullPointerException("Error in connection setup.");
			}

			this.logger.writeLog("Sending " + msg.getMessageType() + " message to " +
					toSend);
			ObjectOutputStream out = this.resource.hashOs.get(toSend);
			out.reset();
			out.writeObject(msg);
			out.flush();
		}
	}
}
