package com.aos.work;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.aos.config.Color;
import com.aos.config.ProcessState;
import com.aos.config.Configuration;
import com.aos.config.SpanningTree;
import com.aos.log.Logger;
import com.aos.msg.Message;
import com.aos.msg.StateMessage;

public class Listener implements Runnable {

	private Configuration resource;
	private Logger logger;
	private String remoteId;
	private ObjectInputStream in;

	public Listener() {
		super();
		this.resource = null;
		this.logger = null;
	}

	/**
	 * @param resource
	 * @param logger
	 * @param serverSocket
	 */
	public Listener(Configuration resource, Logger logger, String remoteId) {
		super();
		this.resource = resource;
		this.logger = logger;
		this.remoteId = remoteId;
		this.in = null;
	}

	/**
	 * @param resource
	 * @param logger
	 * @param serverSocket
	 */
	public Listener(Configuration resource, Logger logger, String remoteId, ObjectInputStream in) {
		super();
		this.resource = resource;
		this.logger = logger;
		this.remoteId = remoteId;
		this.in = in;
	}

	@Override
	public void run() {
		int neighborSize = this.resource.getOneHopNeighbhor().size();
		String thisId = this.resource.getNodeId();
		int numNodes = this.resource.getNumOfNodes();
		int trueCount = 0;
		boolean[] duplicateReceivedState = null; 
		int[] duplicateVectorClock = null;
		
		// Wait till the network is setup
		while (this.resource.getSocketMap().keySet().size() != this.resource.getOneHopNeighbhor().size())
			;
	
		try {
			if (this.in == null) {
				this.in = new ObjectInputStream(this.resource.getSocketMap(remoteId).getInputStream());
			}
			while (!this.resource.isTerminate()) {

				Message msg = (Message) this.in.readObject();

				if (msg == null)
					throw new NullPointerException(thisId + " received 'null' message ");

				this.logger.writeLog("Received " + msg);
				synchronized (resource) {
					// Application Message
					if (msg.getMessageType().equalsIgnoreCase("application")) {

						if (!this.resource.isActive() && !this.resource.isLogging()
								&& this.resource.getTotalMsgSent() < this.resource.getMaxNumber()) {

							this.resource.setState(ProcessState.ACTIVE);
							this.logger.writeLog("Spawning a client");
							new Thread(new Client(this.resource, this.logger)).start();

						} else if (!this.resource.isActive() && this.resource.isLogging()) {

							this.resource.setState(ProcessState.ACTIVE);
							this.resource.putChannelMsg(msg.getSender(), msg);
							
						} else if (this.resource.isActive() && this.resource.isLogging()){
							this.resource.putChannelMsg(msg.getSender(), msg);
						}					
					}
					// Marker Message
					else if (msg.getMessageType().equalsIgnoreCase("marker")) {

						// Update received marker
						this.resource.putReceivedMarker(msg.getSender(), true);

						if (this.resource.getColor() == Color.BLUE) {

							this.resource.setColor(Color.RED); // Change color
							Message markerMsg = new Message(thisId, "marker");

							this.resource.setLogging(true); // Enable logging
							
							// Storing the vector clock for the snapshot
							this.resource.getMyVC().sendEvent(); // Update vector clock
							
							duplicateVectorClock = new int[numNodes];
							duplicateVectorClock = Arrays.copyOf(this.resource.getMyVC().getVectorClock(), numNodes);
							this.resource.addOutput(duplicateVectorClock);
							this.logger.writeLog("Snapshot : " + 
									Arrays.toString(duplicateVectorClock));
							this.sendToNeighbors(markerMsg);
							
							/* Special case where there is only one neighbor */
							if( neighborSize == 1 && !thisId.equals("0")){
								this.resource.getMyState().setState(this.resource.getState());
								this.resource.getMyState().setChannelState(this.resource.getChannelMsg());

								this.resource.setColor(Color.BLUE); // reset color
								this.resource.setLogging(false); // Disable logging

								// Send the state message to parent
								this.sendToParent(this.resource.getMyState());
								
								// Re-Initialize snapshot parameters
								this.logger.writeLog("Re-init (special case) snapshot parameters @" + thisId);
								this.resource.initSnapshotProperties();
							}
						} else if (this.resource.getColor() == Color.RED) {

							int rcvMarkerCount = 0;

							while ((rcvMarkerCount < neighborSize) && this.resource
									.getReceivedMarker(this.resource.getOneHopNeighbhor().get(rcvMarkerCount))) {
								rcvMarkerCount++;
							}
							/**
							 * Node 0 has received marker message from all its
							 * neighbors if not node 0 then send the state
							 * message to the parent
							 */
							if (rcvMarkerCount == neighborSize && thisId.equals("0")) {
								// Store the state message
								this.resource.getMyState().setState(this.resource.getState());
								this.resource.getMyState().setChannelState(this.resource.getChannelMsg());
								this.resource.putStateMsg(thisId, this.resource.getMyState());

								this.logger.writeLog("0 received all marker messages.");
								this.resource.setColor(Color.BLUE); // reset color
								this.resource.setLogging(false); // Disable logging

							} else if (rcvMarkerCount == neighborSize && !thisId.equals("0")) {

								this.resource.getMyState().setState(this.resource.getState());
								this.resource.getMyState().setChannelState(this.resource.getChannelMsg());
								this.resource.setColor(Color.BLUE); // reset color
								this.resource.setLogging(false); // Disable logging
								
								// Send the state message to parent
								this.sendToParent(this.resource.getMyState());
								// Re-Initialize snapshot parameters
								this.logger.writeLog("Re-init (after sending state to parent) @" + thisId);
								if( this.resource.getTotalMsgSent() == 0 ){
									// this.resource.setState(ProcessState.PASSIVE);
									new Thread(new Client(this.resource, this.logger)).start();
								}
								this.resource.initSnapshotProperties();
							}
						} else {
							throw new IllegalArgumentException("Received bad marker @" + thisId);
						}
						
						
					}
					// State Message
					else if (msg.getMessageType().equalsIgnoreCase("state")) {
						if (thisId.equals("0")) {
							// update received state message count
							trueCount = 0;					
							this.resource.setReceivedStateMsg(Integer.parseInt(msg.getSender()), true);
							duplicateReceivedState = this.resource.getReceivedStateMsg();
							trueCount = Arrays.toString(duplicateReceivedState).replaceAll("[^t]", "").length();
							
							this.resource.putStateMsg(msg.getSender(), (StateMessage) msg);
							
							// Check whether '0' has received all the state messages
							if( trueCount == numNodes ){
								if( this.processStateMessage((StateMessage) msg)){
									this.resource.initSnapshotProperties();
									new Thread(new ChandyLamport(this.resource, this.logger)).start();
								}
							}
						} else {
							this.sendToParent((StateMessage) msg);
							
							
						}
					}
					// Terminate Message
					else if (msg.getMessageType().equalsIgnoreCase("terminate")) {
						this.resource.putReceivedTerminate(msg.getSender(), true);
						
						// Send Terminate message only once per listener
						if (!this.resource.isSentTerminate()) { 
							this.logger.writeLog(thisId + " Sending terminate message");
							Message terminateMsg = new Message(thisId, "terminate");
							this.sendToNeighbors(terminateMsg);
							this.resource.setSentTerminate(true);

							// Print the output
							this.logger.writeEntry(this.resource.getOutput());
							this.logger.writeLog("Output printed.");
						}
					}
					if (msg.getMessageType().equalsIgnoreCase("application")) {
						this.resource.getMyVC().receiveEvent(msg.getVectorClock());
					}
					if (!this.resource.isActive() && this.isDone())
						this.resource.setTerminate(true);
				}
			}

		} catch (EOFException e) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.logger.writeLog("Listener " + remoteId + " quiting..");
		}

	}

	public synchronized void sendToNeighbors(Message msg) throws IOException {
		Socket to = null;
		for (String toSend : resource.getOneHopNeighbhor()) {
			to = this.resource.getSocketMap(toSend);

			if (to == null) {
				this.logger.writeLog("'null' socket for " + toSend);
				throw new NullPointerException("Error in sendToNeighbors.");
			}

			this.logger.writeLog("Sending " + msg.getMessageType() + " message to " + toSend);
			ObjectOutputStream out = this.resource.hashOs.get(toSend);
			out.reset();
			out.writeObject(msg);
			out.flush();
		}
	}

	public synchronized void sendToParent(StateMessage msg) throws IOException {
		Socket to = null;
		String thisId = this.resource.getNodeId();
		String parent = SpanningTree.getParent(thisId);

		if (msg == null)
			throw new NullPointerException("'null' in SendToParent");

		to = this.resource.getSocketMap(parent);

		if (to == null) {
			this.logger.writeLog("'null' socket for " + parent);
			throw new NullPointerException("Error in SendToParent.");
		}

		this.logger.writeLog("Sending " + msg.getMessageType() + " message to parent " + parent);
		ObjectOutputStream out = this.resource.hashOs.get(parent);
		out.reset();
		out.writeObject(msg);
		out.flush();
	}

	public synchronized boolean processStateMessage(StateMessage stateMessage) throws IOException {
		int i = 0, passiveCount = 0, channelEmptyCount = 0;
		int numNodes = this.resource.getNumOfNodes();
		synchronized (resource) {
			this.logger.writeLog("@ ProcessStateMessage : " + Arrays.toString(this.resource.getReceivedStateMsg()));
			while (i < numNodes && this.resource.getReceivedStateMsg()[i])
				i++;
			if (i != numNodes)
				return true;
			for(passiveCount = 0; passiveCount < this.resource.getStateMsg().size(); passiveCount ++){
				if (this.resource.getStateMsg().get(Integer.toString(passiveCount)).getState() == ProcessState.ACTIVE){
					this.logger.writeLog(passiveCount + " is true.");
					return true;
				}
			}
			
			if (passiveCount != numNodes) return true;
			this.logger.writeLog("passiveCount == numNodes.");
			while (channelEmptyCount < numNodes) {
				HashMap<String, ArrayList<Message>> channelSet = this.resource.getStateMsg()
						.get(Integer.toString(channelEmptyCount)).getChannelState();

				for (String channel : channelSet.keySet()) {
					if (!channel.isEmpty())
						return true;
				}
				channelEmptyCount += 1;
			}
			this.logger.writeLog("channelEmptyCount == numNodes.");
			if( channelEmptyCount == numNodes && passiveCount == numNodes ){
				this.logger.writeLog("Node 0 detected termination.");
				Message terminateMsg = new Message(this.resource.getNodeId(), "terminate");
				this.sendToNeighbors(terminateMsg);
				this.resource.setSentTerminate(true);
				
				// Print the output
				this.logger.writeEntry(this.resource.getOutput());
				this.logger.writeLog("Output printed");
			}
		}
		return false;
	}

	public synchronized boolean isDone(){
		for (String neighbor : this.resource.getOneHopNeighbhor()) {
			if( !this.resource.getReceivedTerminate().get(neighbor) ){
				return false;
			}
		}
		return true;
	}
}
