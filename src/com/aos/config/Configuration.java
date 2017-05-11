package com.aos.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import com.aos.log.Logger;
import com.aos.msg.Message;
import com.aos.msg.StateMessage;
import com.aos.work.Listener;

public class Configuration {

	private String CONFIG_FILE = "config.txt";

	private int numOfNodes = -1;

	private int minPerActive = -1;

	private int maxPerActive = -1;

	private int minSendDelay = -1;

	private int snapshotDelay = -1;

	private int maxNumber = -1;

	private String nodeId = null;

	private String[] machines = null;

	private int[] ports = null;

	private boolean [][]adjMatrix;
	
	private ArrayList<String> neighbhors = null;

	public static HashMap<String, Socket> nodeSocketMap = null;

	public HashMap<String, ObjectOutputStream> hashOs = new HashMap<>(); 

	public static boolean terminate = false;

	/**
	 * Snapshot properties
	 */
	private ProcessState state = ProcessState.PASSIVE;	// Current state of the process
	
	private Color color = Color.BLUE;	// Default color of the process 
	
	private boolean logging;		// Enable logging to store channel messages 
	
	private boolean sentTerminate = false;
	
	private int totalMsgSent = 0;	// Tracks the #Application message sent
	
	private HashMap<String, ArrayList<Message>> channelMsg; // Stores channel messages
	
	private HashMap<String, Boolean> receivedMarker; // Track #marker messages received
	
	private HashMap<String, Boolean> receivedTerminate; // Tracks #terminate message received
	
	private boolean receivedStateMsg[]; // Tracks receiving of state messages at node 0
	
	private HashMap<String, StateMessage> stateMsg;
	
	private StateMessage myState;
	
	private VectorClock myVC;
	
	private ArrayList<int[]> output;
	
	public Configuration() {
		this.neighbhors = new ArrayList<String>();
		nodeSocketMap = new HashMap<>();
		this.output = new ArrayList<>();
		this.totalMsgSent = 0;
	}

	/**
	 * @return the numOfNodes
	 */
	public int getNumOfNodes() {
		return numOfNodes;
	}

	/**
	 * @param numOfNodes
	 *            the numOfNodes to set
	 */
	public void setNumOfNodes(int numOfNodes) {
		this.numOfNodes = numOfNodes;
	}


	/**
	 * @return the minPerActive
	 */
	public int getMinPerActive() {
		return minPerActive;
	}

	/**
	 * @param minPerActive the minPerActive to set
	 */
	public void setMinPerActive(int minPerActive) {
		this.minPerActive = minPerActive;
	}

	/**
	 * @return the maxPerActive
	 */
	public int getMaxPerActive() {
		return maxPerActive;
	}

	/**
	 * @param maxPerActive the maxPerActive to set
	 */
	public void setMaxPerActive(int maxPerActive) {
		this.maxPerActive = maxPerActive;
	}

	/**
	 * @return the minSendDelay
	 */
	public int getMinSendDelay() {
		return minSendDelay;
	}

	/**
	 * @param minSendDelay the minSendDelay to set
	 */
	public void setMinSendDelay(int minSendDelay) {
		this.minSendDelay = minSendDelay;
	}

	/**
	 * @return the snapshotDelay
	 */
	public int getSnapshotDelay() {
		return snapshotDelay;
	}

	/**
	 * @param snapshotDelay the snapshotDelay to set
	 */
	public void setSnapshotDelay(int snapshotDelay) {
		this.snapshotDelay = snapshotDelay;
	}

	/**
	 * @return the maxNumber
	 */
	public int getMaxNumber() {
		return maxNumber;
	}

	/**
	 * @param maxNumber the maxNumber to set
	 */
	public void setMaxNumber(int maxNumber) {
		this.maxNumber = maxNumber;
	}

	/**
	 * @return the nodeId
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId
	 *            the nodeId to set
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return the machines
	 */
	public String[] getMachines() {
		return machines;
	}

	/**
	 * @param id
	 *            machine id
	 * @param value
	 *            machine name
	 */
	public void setMachine(int id, String value) {
		this.machines[id] = value;
	}


	/**
	 * @return the ports
	 */
	public int[] getPorts() {
		return ports;
	}

	/**
	 * @param id
	 *            machine id
	 * @param value
	 *            machine port number
	 */
	public void setPort(int id, int value) {
		this.ports[id] = value;
	}

	/**
	 * @return the tokenPath
	 */
	public ArrayList<String> getOneHopNeighbhor() {
		return neighbhors;
	}

	/**
	 * @param cONFIG_FILE the cONFIG_FILE to set
	 */
	public void setCONFIG_FILE(String cONFIG_FILE) {
		CONFIG_FILE = cONFIG_FILE;
	}

	/**
	 * @return the CONFIG_FILE
	 */
	public String getCONFIG_FILE() {
		return CONFIG_FILE;
	}

	/**
	 * Initialize machines and ports array
	 */
	private void initMachineAndPort() {
		try {
			this.machines = new String[this.getNumOfNodes()];
			this.ports = new int[this.getNumOfNodes()];
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the adjMatrix
	 */
	public boolean [][] getAdjMatrix() {
		return adjMatrix;
	}

	/**
	 * @return the terminate
	 */
	public synchronized boolean isTerminate() {
		return terminate;
	}

	/**
	 * @param terminate the terminate to set
	 */
	public synchronized void setTerminate(boolean terminate) {
		Configuration.terminate = terminate;
	}

	public synchronized void updateSocketMap(String neighbor, Socket hopSocket){
		nodeSocketMap.put(neighbor, hopSocket);
	}

	public synchronized Socket getSocketMap(String key){
		return nodeSocketMap.get(key);
	}

	public synchronized HashMap<String, Socket> getSocketMap(){
		return nodeSocketMap;
	}
	
	/**
	 * @return the active
	 */
	public synchronized boolean isActive() {
		return ( state == ProcessState.ACTIVE );
	}

	/**
	 * @param active the active to set
	 */
	public synchronized void setState(ProcessState state) {
		this.state = state;
	}

	/**
	 * @return the Process state
	 */
	public synchronized ProcessState getState() {
		return this.state;
	}
	
	/**
	 * @return the color
	 */
	public synchronized Color getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public synchronized void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @return the totalMsgSent
	 */
	public synchronized int getTotalMsgSent() {
		return totalMsgSent;
	}

	/**
	 * @param totalMsgSent the totalMsgSent to set
	 */
	public synchronized void incrementTotalMsgSent() {
		this.totalMsgSent += 1;
	}

	
	/**
	 * @return the myVC
	 */
	public synchronized VectorClock getMyVC() {
		return myVC;
	}

	/**
	 * @param myVC the myVC to set
	 */
	public synchronized void setMyVC(VectorClock myVC) {
		this.myVC = myVC;
	}

	/**
	 * @return the output
	 */
	public synchronized ArrayList<int[]> getOutput() {
		return output;
	}

	/**
	 * @param vc the output to set
	 */
	public synchronized void addOutput(int[] vc) {
		if( this.output.isEmpty() )
			this.output.add(vc);
		else if( this.output.indexOf(vc) == -1 &&
				!Arrays.equals(this.output.get(this.output.size() - 1), vc))
			this.output.add(vc);
	}

	/**
	 * @return the logging
	 */
	public synchronized boolean isLogging() {
		return logging;
	}

	/**
	 * @param logging the logging to set
	 */
	public synchronized void setLogging(boolean logging) {
		this.logging = logging;
	}
	
	/**
	 * @return the sentTerminate
	 */
	public synchronized boolean isSentTerminate() {
		return sentTerminate;
	}

	/**
	 * @param sentTerminate the sentTerminate to set
	 */
	public synchronized void setSentTerminate(boolean sentTerminate) {
		this.sentTerminate = sentTerminate;
	}

	/**
	 * @return the channelMsg
	 */
	public synchronized HashMap<String, ArrayList<Message>> getChannelMsg() {
		return channelMsg;
	}

	
	/**
	 * @return the receivedMarker
	 */
	public synchronized boolean getReceivedMarker(String id) {
		return receivedMarker.get(id);
	}

	/**
	 * @param receivedMarker the receivedMarker to set
	 */
	public synchronized void putReceivedMarker(String key, Boolean value) {
		this.receivedMarker.put(key, value);
	}

	
	/**
	 * @return the receivedTerminate
	 */
	public synchronized HashMap<String, Boolean> getReceivedTerminate() {
		return receivedTerminate;
	}

	/**
	 * @param receivedTerminate the receivedTerminate to set
	 */
	public synchronized void putReceivedTerminate(String key, Boolean value) {
		this.receivedTerminate.put(key, value);
	}

	/**
	 * @return the receivedStateMsg
	 */
	public synchronized boolean[] getReceivedStateMsg() {
		return receivedStateMsg;
	}

	/**
	 * @param receivedStateMsg the receivedStateMsg to set
	 */
	public synchronized void setReceivedStateMsg(int idx, boolean value) {
		this.receivedStateMsg[idx] = value;
	}

	/**
	 * @return the stateMsg
	 */
	public synchronized HashMap<String, StateMessage> getStateMsg() {
		return stateMsg;
	}

	/**
	 * @param stateMsg the stateMsg to set
	 */
	public synchronized void putStateMsg(String key, StateMessage stateMsg) {
		this.stateMsg.put(key, stateMsg);
	}

	/**
	 * @return the myState
	 */
	public synchronized StateMessage getMyState() {
		return myState;
	}

	/**
	 * @param channelMsg the channelMsg to set
	 */
	public synchronized void putChannelMsg(String key, Message channelMsg) {
		if( this.channelMsg.containsKey(key) ){
			this.channelMsg.get(key).add(channelMsg);
		} else {
			ArrayList<Message> newMsg = new ArrayList<>();
			newMsg.add(channelMsg);
			this.channelMsg.put(key, newMsg);
		}
	}

	/**
	 * Parse 'config.txt'
	 * 
	 * Section 1 - <Number of nodes><minPerActive><maxPerActive><minSendDelay><snapshotDelay><maxNumber>
	 * 
	 * Section 2 - <Identifier> <Hostname> <Port>
	 * 
	 * Section 3 - <Neighbor nodes>
	 */
	public void parseConfigFile() {
		String inputLine = null;
		String line = null;
		int section = 0, locationStored = 0, idx = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.getCONFIG_FILE()));

			while ((inputLine = br.readLine()) != null) {

				if (inputLine.startsWith("#"))
					continue;

				if (inputLine.contains("#")) {
					line = inputLine.substring(0, inputLine.indexOf('#'));
					line = line.trim();
				} else {
					line = inputLine.trim();
				}

				if (line.length() == 0)
					continue;

				String[] tokens = line.split("\\s+");

				switch (section) {
				case 0:

					if (tokens[0].isEmpty())
						throw new IllegalArgumentException("Error in getting number of nodes.");

					if (tokens[1].isEmpty())
						throw new IllegalArgumentException("Error in getting minPerActive.");

					if (tokens[2].isEmpty())
						throw new IllegalArgumentException("Error in getting maxPerActive.");

					if (tokens[3].isEmpty())
						throw new IllegalArgumentException("Error in getting minSendDelay.");

					if (tokens[4].isEmpty())
						throw new IllegalArgumentException("Error in getting snapshotDelay.");

					if (tokens[5].isEmpty())
						throw new IllegalArgumentException("Error in getting maxNumber.");


					this.setNumOfNodes(Integer.parseInt(tokens[0]));	// No of nodes 
					this.setMinPerActive(Integer.parseInt(tokens[1]));	// minPerActive
					this.setMaxPerActive(Integer.parseInt(tokens[2]));	// maxPerActive
					this.setMinSendDelay(Integer.parseInt(tokens[3]));	// minSendDelay
					this.setSnapshotDelay(Integer.parseInt(tokens[4])); // snapshotDelay
					this.setMaxNumber(Integer.parseInt(tokens[5])); 	// maxNumber

					this.initMachineAndPort();
					this.adjMatrix = new boolean[this.numOfNodes][this.numOfNodes];
					section += 1;
					break;
				case 1:
					/* Store machine and port array for each node */
					if (tokens.length > 3 || tokens[0].isEmpty() || tokens[1].isEmpty() || tokens[2].isEmpty())
						throw new IllegalArgumentException("Parsing <id><hostname><port>");

					this.setMachine(Integer.parseInt(tokens[0]), tokens[1]);
					this.setPort(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[2]));

					locationStored += 1;

					if (locationStored == this.getNumOfNodes())
						section += 1;
					break;
				case 2:

					/* Store the path to send tokens */
					if( idx == Integer.parseInt(this.getNodeId()) ){
						for (int i = 0; i < tokens.length; i++){
							if( ! this.neighbhors.contains( tokens[i] ) )
								this.neighbhors.add(tokens[i]);
							this.getAdjMatrix()[idx][Integer.parseInt(tokens[i])] = true;
							this.getAdjMatrix()[Integer.parseInt(tokens[i])][idx] = true;
						}
						idx++;
					} else {
						for (int i = 0; i < tokens.length; i++){
							if( tokens[i].equals(this.getNodeId()) && 
								(! this.neighbhors.contains( Integer.toString(idx))) )
								this.neighbhors.add( Integer.toString(idx) );
							this.getAdjMatrix()[idx][Integer.parseInt(tokens[i])] = true;
							this.getAdjMatrix()[Integer.parseInt(tokens[i])][idx] = true;
						}
						idx++;
					}

					break;
				}
			}
			Collections.sort(this.neighbhors);
			
			// For node 0 initialize state to Active 
			if( this.getNodeId().equals("0") ) 
				this.setState(ProcessState.ACTIVE);
			else
				this.setState(ProcessState.PASSIVE);
			// Initialize vector clock 
			this.myVC = new VectorClock(Integer.parseInt(this.getNodeId()), this.getNumOfNodes());
			
			// Initialize Snapshot Properties
			this.initSnapshotProperties();
			
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable to find " + this.getCONFIG_FILE());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException i) {
			i.printStackTrace();
		}
	}

	public void setUpNetwork(Logger logger) {
		int idx = -1;
		String neighbor = null;
		int neighborSize = this.getOneHopNeighbhor().size();
		try {

			for(int k = 0; k  < neighborSize; k++){

				neighbor = this.getOneHopNeighbhor().get(k);
				idx = Integer.parseInt(neighbor);

				if( Integer.parseInt(neighbor) < 
						Integer.parseInt(this.getNodeId())){

					String machine = this.getMachines()[idx];
					int port = this.getPorts()[idx];
					System.out.println(this.getTimeStamp() + 
							"Connecting to " + neighbor + " " + machine + " " + port);

					InetAddress ipAddress = InetAddress.getByName(machine);
			
					Socket hopSocket = new Socket(ipAddress, port);
					// Persist the client socket connection
					updateSocketMap(neighbor, hopSocket);
					new Thread(new Listener(this, logger, neighbor)).start();

					// Set only the origin
					Message first = new Message();
					first.setSender(this.getNodeId());

					ObjectOutputStream out = new ObjectOutputStream( hopSocket.getOutputStream() );
					out.writeObject(first);
					out.flush();

					this.hashOs.put(neighbor, out);
				}
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(ConnectException c){
			System.out.println("Exception at " + neighbor);
			c.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void initSnapshotProperties(){

		this.channelMsg = new HashMap<>();
		this.receivedMarker = new HashMap<>();
		this.receivedTerminate = new HashMap<>();
		this.receivedStateMsg = new boolean[this.getNumOfNodes()];
		this.stateMsg = new HashMap<>();
		this.myState = new StateMessage(this.getNodeId(), "state", this.getMyVC());
		
		// Initialize logging to be false
		this.setLogging(false);
		
		// Initialize color
		this.setColor(Color.BLUE);
		
		// Received State message for o is always true 
		this.setReceivedStateMsg(0, true);
		
		// Initialize Received Marker and Terminate count
		for(String neighbor : this.getOneHopNeighbhor()) {
			this.putReceivedMarker(neighbor, false);
			this.putReceivedTerminate(neighbor, false);
		}
		
		
	}
	
	private String getTimeStamp(){
		String timeStamp = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("M-dd-yyyy hh:mm:ss a");
		Date date = new Date();
		timeStamp = dateFormat.format(date.getTime()) + ": ";
		return timeStamp;
	}

	@Override
	public String toString() {
		int id = Integer.parseInt(nodeId);
		String currentMachine = machines[id];
		currentMachine = currentMachine.replaceAll(".utdallas.edu", "");
		return "SharedResource [numOfNodes=" + numOfNodes + ", nodeId=" + nodeId + ", machines="
				+ currentMachine + ", ports=" + ports[id] + ", neighbhors=" + neighbhors + "]";
	}

}
