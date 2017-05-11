package com.aos.msg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.aos.config.ProcessState;
import com.aos.config.VectorClock;

public class StateMessage extends Message{

	private static final long serialVersionUID = -1537838435817226432L;
	
	ProcessState state;
	HashMap<String, ArrayList<Message>> channelState;
	
	
	/**
	 * @param sender
	 * @param messageType
	 * @param vectorClock
	 */
	public StateMessage(String sender, String messageType, VectorClock vectorClock) {
		super(sender, messageType, vectorClock);
	}

	/**
	 * @param sender
	 * @param messageType
	 * @param vectorClock
	 * @param state
	 * @param channelState
	 */
	public StateMessage(String sender, String messageType, VectorClock vectorClock, ProcessState state,
			HashMap<String, ArrayList<Message>> channelState) {
		super(sender, messageType, vectorClock);
		this.state = state;
		this.channelState = channelState;
	}

	/**
	 * @return the state
	 */
	public ProcessState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(ProcessState state) {
		this.state = state;
	}

	/**
	 * @return the channelState
	 */
	public HashMap<String, ArrayList<Message>> getChannelState() {
		return channelState;
	}

	/**
	 * @param channelState the channelState to set
	 */
	public void setChannelState(HashMap<String, ArrayList<Message>> channelState) {
		this.channelState = channelState;
	}

	@Override
	public String toString() {
		return "StateMessage [state=" + state + ", channelState=" + channelState + ", Sender=" + getSender()
				+ ", MessageType=" + getMessageType() + ", VectorClock=" + Arrays.toString(getVectorClock())
				+ "]";
	}
	
	
}
