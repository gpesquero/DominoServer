package org.dominoserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.dominoserver.Message.MsgId;

public class DominoServer {

	private static final String APP_NAME="DominoServer";
	
	public static final int MAX_PLAYERS=4;
	private static final int TIMER_TIMEOUT=5000;
	
	public static void main(String[] args) {
		
		new DominoServer();
		
	}
	
	TimerThread mTimerThread=null;
	
	MessageHandler mMessageHandler=null;
	
	CommListener mCommListener=null;
	
	Player[] mPlayers=null;
	
	ArrayList<Connection> mConnections=null;
	
	private class TimerThread extends Thread {
		
		public boolean mRun=true;
	
		public void terminate() {
			
			mRun=false;
		}

		@Override
		public void run() {
			
			while(mRun) {
				
				try {
					Thread.sleep(TIMER_TIMEOUT);
					
				} catch (InterruptedException e) {
					
					Log.info("Timer thread sleep interrupted!!");
					
					mRun=false;
				}
			
				Message msg=new Message(MsgId.TIMER);
			
				mMessageHandler.addMessage(msg);
			}
		}
	};
	
	public DominoServer() {
		
		Log.info("SERVER: Starting <"+APP_NAME+">...");
		
		mMessageHandler=new MessageHandler();
		
		mCommListener=new CommListener(mMessageHandler);
		
		mPlayers=new Player[MAX_PLAYERS];
		
		for(int i=0; i<MAX_PLAYERS; i++) {
			
			mPlayers[i]=null;
		};
		
		mConnections=new ArrayList<Connection>(); 
		
		// Start timer...
		mTimerThread=new TimerThread();
		mTimerThread.start();
				
		if (mCommListener.isOk()) {
			
			mCommListener.start();
			
			boolean run=true;
			
			while (run) {
				
				Message msg=mMessageHandler.waitForMessage();
				
				run=processMessage(msg);				
			}
			
			mCommListener.close();
			
			mTimerThread.terminate();
		}
		else {
			
			Log.error("SERVER: ServerSocket is not Ok...");
		}
		
		Log.info("Quitting <"+APP_NAME+">");
	}
	
	private boolean processMessage(Message msg) {
		
		boolean run=true;
		
		if (msg==null) {
		
			Log.error("SERVER: Received message==null");
		
			return false;
		}
	
		if (msg.mId==MsgId.NEW_CONNECTION) {
			
			mConnections.add(msg.mConnection);
			
		}
		else if (msg.mId==MsgId.OPEN_SESSION) {
			
			String playerName=msg.getArgument("playerName");
			
			Log.info("Received Msg OPEN_SESSION with playerName=<"+playerName+">");
			
			// First, check if we already have a player with that name...
			
			int playerPos=-1;
			
			for(int i=0; i<MAX_PLAYERS; i++) {
				
				//Log.info("Player["+i+"]="+mPlayers[i]);
				
				if (mPlayers[i]!=null) {
					
					if (mPlayers[i].getPlayerName().compareTo(playerName)==0) {
						
						playerPos=i;
						break;
					}
				}						
			}
			
			Player player=null;
			
			if (playerPos<0) {
				
				// We have not found this player. Create a new one in an available space...
				
				for(int i=0; i<MAX_PLAYERS; i++) {
					
					if (mPlayers[i]==null) {
						
						playerPos=i;
						break;
					}
				}
				
				if (playerPos<0) {
					
					Log.error("SERVER: No room available for more players....");
				
					// Close socket...
					
					try {
						msg.mSocket.close();
						
					} catch (IOException e) {
						
						Log.error("SERVER: Socket.close() error="+e.getMessage());
					}
					
					player=null;
				}
				
				player=new Player(playerPos, playerName);
					
				//newPlayer.setMessageHandler(mMessageHandler);
				//newPlayer.setSocket(msg.mSocket);
				
				player.setConnection(msg.mConnection);
				
				mPlayers[playerPos]=player;
			
				// Start the player thread...
				//newPlayer.start();
				
				Log.info("SERVER: Created new player <"+playerName+">");
			}
			else {
				
				// Connect to an existing Player...
				
				player=mPlayers[playerPos];
				
				player.setConnection(msg.mConnection);
				
				/*
				if (player.isAlive()) {
					
					existingPlayer.interrupt();
				}
				
				existingPlayer.setSocket(msg.mSocket);
				
				existingPlayer.start();
				*/
				
			}
			
			String msgString=CommProtocol.createMsgSessionInfo(mPlayers);
			
			player.sendMessage(msgString);
			
			printPlayers();
		}
		else if (msg.mId==MsgId.TIMER) {
			
			//printPlayers();
			
			
			
			/*
			// Send a <ping> to every active player...
			
			String msgString=CommProtocol.createMsgPing();
			
			for(int i=0; i<MAX_PLAYERS; i++) {
				
				if (mPlayers[i]!=null) {
					
					boolean error=mPlayers[i].sendMessage(msgString);						
				}
			}
			*/
		}
		else {
			
			Log.error("SERVER: Received UNKNOWN Msg (MsgId="+msg.mId+", Error="+msg.mErrorString+")");
		}
		
		return run;
	}
	
	private void printPlayers() {
		
		// List active connections...
		
		Log.info("Number of active connections="+mConnections.size());
		
		// List all players...
		for(int i=0; i<MAX_PLAYERS; i++) {
			
			String playerName;
			
			if (mPlayers[i]==null) {
				
				playerName="-----";
				
			}
			else {
				
				playerName=mPlayers[i].getPlayerName();
			}
			
			Log.info("Player["+i+"] is <"+playerName+">");
		}
	}
}
