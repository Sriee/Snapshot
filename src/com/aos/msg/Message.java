package com.aos.msg;

import java.io.Serializable;
import java.util.Arrays;

import com.aos.config.VectorClock;

/**
 * @author sriee
 *
 */
public class Message implements Serializable{
	
	private static final long serialVersionUID = 8442183641921041945L;
	
	private String sender;
	private String messageType;
	private int[] vectorClock;
	
	public Message() {
		super();
		this.sender = null;
		this.messageType = null;
		this.vectorClock = null;
	}

	
	/**
	 * @param sender
	 * @param messageType
	 */
	public Message(String sender, String messageType) {
		super();
		this.sender = sender;
		this.messageType = messageType;
	}


	/**
	 * @param sender
	 * @param msgTag
	 * @param messageType
	 * @param vectorClock
	 */
	public Message(String sender, String messageType, VectorClock vectorClock) {
		super();
		this.sender = sender;
		this.messageType = messageType;
		this.vectorClock = vectorClock.getVectorClock();
	}

	/**
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @param sender the sender to set
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * @param messageType the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	/**
	 * @return the vectorClock
	 */
	public int[] getVectorClock() {
		return vectorClock;
	}

	/**
	 * @param vectorClock the vectorClock to set
	 */
	public void setVectorClock(VectorClock vectorClock) {
		this.vectorClock = vectorClock.getVectorClock();
	}

	@Override
	public String toString() {
		return "Message [sender=" + sender + ", Type=" + messageType + ", vectorClock="
				+ Arrays.toString(vectorClock) + "]";
	}
	
}
