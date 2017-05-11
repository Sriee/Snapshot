package com.aos.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class FileLogger implements Logger {

	private PrintWriter out;
	private String fileName = null; 
	private File logFile = null;
	
	/**
	 * @param out
	 * @throws IOException
	 */
	public FileLogger(String configFileName, String originId) throws IOException {
		super();
		this.fileName = "log_" + originId + ".txt";
		this.logFile = new File(fileName);
		if(configFileName.indexOf('.') != -1)
			configFileName = configFileName.substring(0, configFileName.indexOf('.'));
		this.out = new PrintWriter( 
				new FileWriter(configFileName + "-" + originId + ".out"),
				true);
		this.setup(originId);
	}

	@Override
	public synchronized void writeEntry(ArrayList<int[]> output) {
		Iterator<int[]> iterEntry = output.iterator();
		while (iterEntry.hasNext())
			this.out.println(Arrays.toString(iterEntry.next()));
	}
	
	@Override
	public synchronized void writeEntry(String entry) {
		this.out.println(this.getTimeStamp() + entry);
	}
	
	@Override
	public synchronized void writeLog(String entry){
		if( logFile == null )
			throw new NullPointerException("Log file not initialized.");
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter( new FileWriter(this.logFile.getCanonicalPath(), true));
			bw.write(this.getTimeStamp() + entry + "\n");
			bw.close();
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void writeLog(Collection<String> entry){
		Iterator<String> iter = entry.iterator();
	
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter( new FileWriter(this.logFile.getCanonicalPath(), true));
			while( iter.hasNext() )
				bw.write(this.getTimeStamp() + iter.next() + "\n");
			
			bw.close();
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setup(String id) {
		
		if (id.isEmpty() || id == null)
			throw new IllegalArgumentException("Origin id is null or Empty.");

		this.logFile = new File(this.fileName);

		try {
			
			if( this.logFile.exists() ){
				System.out.println("Existing file deleted.");
				logFile.delete();
			}
			
			if( ! logFile.createNewFile() )
				System.out.println(fileName + " file not created.");
			else{
				System.out.println(fileName + " file created.");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getTimeStamp(){
		String timeStamp = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("M-dd-yyyy hh:mm:ss a");
		Date date = new Date();
		timeStamp = dateFormat.format(date.getTime()) + ": ";
		return timeStamp;
	}
}
