package com.aos.config;

import java.util.Arrays;

public class VectorClock {

	private int[] vc;
	private int myId;
	private int numProc;
	
	
	/**
	 * @param myId
	 * @param numProc
	 */
	public VectorClock(int myId, int numProc) {
		super();
		this.myId = myId;
		this.numProc = numProc;
		
		// Initialize Vector clock
		this.vc = new int[numProc];
		for(int i = 0; i < numProc; i++)
			this.vc[i] = 0;
	}
	
	public synchronized int[] getVectorClock(){
		return this.vc;
	}
	
	public synchronized void sendEvent(){
		this.vc[this.myId]++;
	}
	
	public synchronized void receiveEvent(int[] received){
		for(int k = 0; k < this.numProc; k++)
			this.vc[k] = this.max(this.vc[k], received[k]);
		this.vc[this.myId]++;
	}
	
	private int max(int a, int b){ return (a > b) ? a : b; }

	@Override
	public String toString() {
		return "VectorClock " + Arrays.toString(vc);
	}
	
}
