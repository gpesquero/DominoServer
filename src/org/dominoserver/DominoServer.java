package org.dominoserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DominoServer {

	private static String APP_NAME="DominoServer";
	
	private static final int COMM_PORT=52301;
	
	public static void main(String[] args) {
		
		Log.info("Starting <"+APP_NAME+">...");
		
		boolean mContinue=true;
		
		ServerSocket serverSocket=null;
		
		try {
			
			Log.info("Listening on port "+COMM_PORT+"...");
			
			serverSocket=new ServerSocket(COMM_PORT);
			
			while(mContinue) {
				
				Socket clientSocket=serverSocket.accept();
				
				Log.info("Connection accepted !!!");
				
				
			}			
			
			
		} catch (IOException e) {
			
			Log.error("ServerSocket error: "+e.getMessage());
		}
		
		
		

		
		if (serverSocket!= null) {
			
			try {
				serverSocket.close();
				
				Log.info("ServerSocket closed");
				
			} catch (IOException e) {
				
				Log.error("ServerSocket.close() error: "+e.getMessage());
			}
		}
		
		Log.info("Quitting <"+APP_NAME+">");
	}
}
