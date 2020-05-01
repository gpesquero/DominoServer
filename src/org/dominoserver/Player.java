package org.dominoserver;

import java.util.ArrayList;

import org.dominoserver.Message.MsgId;

public class Player /*extends Thread*/ {
	
	public int mPlayerPos = -1;
	
	private String mPlayerName = null;
	
	private Connection mConnection = null;
	
	private boolean mIsRobot = false;
	
	public ArrayList<DominoTile> mTiles = new ArrayList<DominoTile>(); 
	
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
		
		Log.info("Player sendMessage(): "+msg);
		
		return mConnection.sendMessage(msg);
	}
	
	public String getPlayerName() {
		
		return mPlayerName;
		
	}
	
	public void setAsRobot(boolean isRobot) {
		
		mIsRobot=isRobot;
	}
	
	public boolean isRobot() {
		
		return mIsRobot;
	}
	
	public void addTile(DominoTile tile) {
		
		mTiles.add(tile);
	}
	
	public void sendGameTileInfo(Game game) {
		
		String msg = CommProtocol.createMsgGameTileInfo(game);
				
		sendMessage(msg);
		
		msg = CommProtocol.createMsgPlayerTileInfo(this);
		
		sendMessage(msg);
	}
	
	public int getTileCount() {
		
		return mTiles.size();
	}
	
	public void playTurn(Game game, MessageHandler msgHandler) {
		
		// Play the turn of a robot...
		
		if (game.mBoardTiles1.size() == 0) {
			
			// There are no tiles in the board...
			
			if (game.mRoundCount == 0) {
				
				// This is the first round. We have to play the double 6 tile...
				
				DominoTile tile = removeTile(6, 6);
				
				if (tile == null) {
					
					Log.error("Robot has to play double 6 tile, but cannot find it!!!");
					
					return;
				}
				
				// Launch "play_tile" message...
				
				Message msg = new Message(MsgId.PLAY_TILE);
				
				msg.addArgument("playerName", mPlayerName);
				msg.addArgument("playerPos", String.valueOf(mPlayerPos));
				msg.addArgument("tile", String.valueOf(tile.mNumber1)+"-"+String.valueOf(tile.mNumber2));
				msg.addArgument("boardSide", String.valueOf(1));
				
				new Thread() {
					
					public void run() {
						
						try {
							sleep(3000);
							
						} catch (InterruptedException e) {
							
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						msgHandler.addMessage(msg);
					}
					
					
				}.start();
				
				
			}
		}
		else {
			
			Log.error("Robot playTurn() pending...");
		}
		
	}
	
	private DominoTile removeTile(int number1, int number2) {
		
		for(int i=0; i<mTiles.size(); i++) {
			
			DominoTile tile = mTiles.get(i);
			
			if ((tile.mNumber1 == number1) && (tile.mNumber2 == number2)) {
				
				mTiles.remove(i);
				
				return tile;
			}
		}
		
		return null;
	}
	
	void sendBoardTileInfo(Game game) {		
		
		String msgString1=CommProtocol.createMsgBoardTilesInfo1(game);
		
		sendMessage(msgString1);
		
		String msgString2=CommProtocol.createMsgBoardTilesInfo2(game);
		
		sendMessage(msgString2);
	}
}
