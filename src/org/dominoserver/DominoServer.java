package org.dominoserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.dominoserver.Message.MsgId;

public class DominoServer {

	private static String APP_NAME="DominoServer";
	
	public static void main(String[] args) {
		
		Log.info("Starting <"+APP_NAME+">...");
		
		boolean mContinue=true;
		
		MessageHandler messageHandler=new MessageHandler();
		
		CommListener commListener=new CommListener(messageHandler);
		
		if (commListener.isOk()) {
			
			commListener.start();
		
			while (mContinue) {
				
				Message msg=messageHandler.waitForMessage();
				
				if (msg==null) {
					
					Log.error("Received message==null");
					
					mContinue=false;
					
					continue;
				}
				
				if (msg.mId==MsgId.NEW_CONNECTION) {
					
					Player newPlayer=new Player(msg.mSocket);
					
					// Start the player thread...
					newPlayer.start();
				}
				else {
					
					Log.error("Received unknown Msg with id="+msg.mId);
					
					mContinue=false;
				}
			}
			
			commListener.close();
		}
		else {
			
			Log.error("ServerSocket is not Ok...");
		}
		
		Log.info("Quitting <"+APP_NAME+">");
	}
}
