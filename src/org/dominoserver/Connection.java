package org.dominoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection extends Thread {
	
	private Socket mSocket=null;
	
	private PrintWriter mOutWriter=null;
	
	private MessageHandler mMsgHandler=null;
	
	public Connection(Socket socket, MessageHandler msgHandler) {
		
		mSocket=socket;
		mMsgHandler=msgHandler;
	}
	
	public void setMessageHandler(MessageHandler msgHandler) {
		
		mMsgHandler=msgHandler;
	}
	
	public void run() {
		
        try {
        	
        	mOutWriter=new PrintWriter(mSocket.getOutputStream(), true);
        	
        	BufferedReader in=new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        	
        	String inputLine;
        	
        	Log.debug("Player Thread.run()");
        	
            while((inputLine=in.readLine())!=null) {
        		
        		Log.debug("Received Message: "+inputLine);
        		
        		Message msg = CommProtocol.processLine(inputLine);
        		
        		msg.setConnection(this);
        		        		
        		if (mMsgHandler!=null) {
        			
        			mMsgHandler.addMessage(msg);
        		}
            }
        }
        catch(IOException e) {
        	
        	Log.error("Connection.run() IOException error: "+e.getMessage());
        }
	}
	
	public boolean sendMessage(String msg) {
		
		if (mOutWriter==null) {
			
			Log.error("Connection.sendMessage() mOutWriter==null");
			
			return false;
		}
		
		mOutWriter.println(msg);
		
		if (mOutWriter.checkError()) {
		
			Log.error("Player.sendMessage() mOutWriter.checkError() has error");
		
			return false;
		}
		
		return true;
	}
}
