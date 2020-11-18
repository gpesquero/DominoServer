package org.dominoserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.dominoserver.Message.MsgId;

public class CommListener extends Thread {
	
	private static final int COMM_PORT=52301;
	
	private ServerSocket mServerSocket=null;
	
	private MessageHandler mMessageHandler=null;
	
	private boolean mContinue=true;
		
	public CommListener(MessageHandler msgHandler) {
		
		mMessageHandler=msgHandler;
		
		try {
			
			mServerSocket=new ServerSocket(COMM_PORT);
			
			Log.info("ServerSocket create() Ok !!");
		}
		catch (IOException e) {
			
			Log.error("CommListener ServerSocket create() error: "+e.getMessage());
			
			mServerSocket=null;
		}
	}
	
	public boolean isOk() {
		
		return mServerSocket!=null;
	}
	
	public void run() {
		
		Log.info("Listening on port "+COMM_PORT+"...");
		
		while (mContinue) {
			
			try {
				Socket clientSocket=mServerSocket.accept();
				
				Log.info("CommListener ServerSocket connection accepted !!!");
				
				Connection conn=new Connection(clientSocket, mMessageHandler);
				conn.start();
				
				Message msg=new Message(MsgId.NEW_CONNECTION);
				
				msg.setConnection(conn);
				
				mMessageHandler.addMessage(msg);
				
			}
			catch (SocketException e) {
				
				Log.warning("ServerSocket accept() SocketException: "+e.getMessage());
				
				mContinue=false;				
			}
			catch (IOException e) {
				
				Log.warning("ServerSocket accept() IOException: "+e.getMessage());
				
				mContinue=false;				
			}
		}
		
		try {
			
			mServerSocket.close();
			
			Log.info("ServerSocket closed !!!");
			
		} catch (IOException e) {
			
			Log.error("ServerSocket close() IOException: "+e.getMessage());
		}
	}
	
	public void close() {
		
		try {
			
			mServerSocket.close();
			
			Log.info("ServerSocket closed !!!");
			
		} catch (IOException e) {
			
			Log.error("ServerSocket close() IOException: "+e.getMessage());
		}
	}
}
