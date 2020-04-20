package org.dominoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Player /*extends Thread*/ {
	
	private int mPlayerPos=-1;
	
	private String mPlayerName=null;
	
	private Connection mConnection=null;
	
	public Player(int playerPos, String playerName) {
		
		mPlayerPos=playerPos;
		
		mPlayerName=playerName;
	}
	
	/*
	public void setSocket(Socket clientSocket) {
		
		mSocket=clientSocket;		
	}
	*/

	public void setConnection(Connection connection) {
		
		mConnection=connection;		
	}
	
	public boolean sendMessage(String msg) {
		
		if (mConnection==null) {
					
			Log.error("Player.sendMessage() mConnection==null");
			
			return false;
		}
		
		return mConnection.sendMessage(msg);
	}
	
	public String getPlayerName() {
		
		return mPlayerName;
		
	}
}
