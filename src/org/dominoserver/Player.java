package org.dominoserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.dominoserver.Message.MsgId;
import org.utilslibrary.Log;

public class Player {
	
	private final static long ROBOT_PLAY_TILE_TIMEOUT = 3000;
	
	public int mPlayerPos = -1;
	
	private String mPlayerName = null;
	
	private Connection mConnection = null;
	
	private boolean mIsRobot = false;
	
	public ArrayList<DominoTile> mTiles = new ArrayList<DominoTile>(); 
	
	public Player(int playerPos, String playerName) {
		
		mPlayerPos=playerPos;
		
		mPlayerName=playerName;
	}

	public void setConnection(Connection connection) {
		
		mConnection=connection;		
	}
	
	public void closeConnection() {
		
		if (mConnection != null) {
			
			mConnection.close();
		}
	}
	
	public boolean sendMessage(String msg) {
		
		if (mConnection==null) {
					
			Log.error("Player.sendMessage() mConnection==null");
			
			return false;
		}
		
		Log.debug("Player sendMessage(): "+msg);
		
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
	
	public void sendGameInfo(Game game) {
		
		String msg = CommProtocol.createMsgGameInfo(game);
				
		sendMessage(msg);
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
				
				if (!hasTile(6, 6)) {
					
					Log.error("Robot has to play double 6 tile, but cannot find it!!!");
					
					return;
				}
				
				DominoTile tile = new DominoTile(6, 6);
				
				playTile(msgHandler, tile, 1);
			}
			else {
				
				// This is not the first round. Play any of the tiles...
				
				// First, shuffle the tiles
				
				long seed = System.currentTimeMillis();
				
				Random random = new Random(seed);
				
				Collections.shuffle(mTiles, random);
				
				// Play tile on board side 1
				
				playTile(msgHandler, mTiles.get(0), 1);
			}
		}
		else {
			
			int endNumber1 = game.getEndNumber1();
			
			int endNumber2 = game.getEndNumber2();
			
			
			ArrayList<MyPair<DominoTile, Integer>> playableTiles = getPlayableTiles(endNumber1, endNumber2);
			
			if (playableTiles.size() == 0) {
				
				Log.debug("Robot.playTurn() No playableTiles. Robot has to pass...");
				
				playTile(msgHandler, null, 0);
			}
			else {
				
				// Force a robot to play in one side of the board
				// If forceSide == 0, the play on any side
				
				int forceSide = 0;
				
				boolean tilePlayed = false;
				
				if (forceSide > 0) {
					
					// Force one side...
					
					for(int i=0; i<playableTiles.size(); i++) {
						
						if (playableTiles.get(i).getValue() == forceSide) {
							
							DominoTile tile = playableTiles.get(i).getKey();
							Integer boardSide = playableTiles.get(i).getValue();
							
							Log.debug("Robot.playTurn() Play tile="+tile.mNumber1+"-"+tile.mNumber2+
									", boardSide="+boardSide);
							
							playTile(msgHandler, tile, boardSide);
							
							tilePlayed = true;
							
							break;
						}
					}
				}
				
				if (!tilePlayed) {
					
					// Play a random tile...
					
					long seed = System.currentTimeMillis();
					
					Random random = new Random(seed);
					
					Collections.shuffle(playableTiles, random);
					
					DominoTile tile = playableTiles.get(0).getKey();
					Integer boardSide = playableTiles.get(0).getValue();
					
					Log.debug("Robot.playTurn() Play tile="+tile.mNumber1+"-"+tile.mNumber2+
							", boardSide="+boardSide);
					
					playTile(msgHandler, tile, boardSide);
				}
			}
		}
		
	}
	
	public boolean hasTile(int number1, int number2) {
		
		Iterator<DominoTile> iter = mTiles.iterator();
		
		while (iter.hasNext()) {
			
			DominoTile tile = iter.next();
			
			if ((tile.mNumber1 == number1) && (tile.mNumber2 == number2)) {
				
				return true;
			}
			
			if ((tile.mNumber1 == number2) && (tile.mNumber2 == number1)) {
				
				return true;
			}
		}
		
		return false;			
	}
	
	private void playTile(MessageHandler msgHandler, DominoTile tile, int boardSide) {
		
		// Launch "play_tile" message...
		
		Message msg = new Message(MsgId.PLAY_TILE);
		
		msg.addArgument("playerName", mPlayerName);
		msg.addArgument("playerPos", String.valueOf(mPlayerPos));
		msg.addArgument("boardSide", String.valueOf(boardSide));
		
		String tileString;
		
		if (tile == null) {
			
			tileString = "null";
		}
		else {
			
			tileString = String.valueOf(tile.mNumber1)+"-"+String.valueOf(tile.mNumber2);
		}
		
		msg.addArgument("tile", tileString);
		
		new Thread() {
			
			public void run() {
				
				try {
					sleep(ROBOT_PLAY_TILE_TIMEOUT);
					
				} catch (InterruptedException e) {
					
					Log.error("Sleep InterruptedException");
				}
				
				msgHandler.addMessage(msg);
			}			
			
		}.start();
	}
	
	public boolean removeTile(int number1, int number2) {
		
		for(int i=0; i<mTiles.size(); i++) {
			
			DominoTile tile = mTiles.get(i);
			
			if ((tile.mNumber1 == number1) && (tile.mNumber2 == number2)) {
				
				mTiles.remove(i);
				
				return true;
			}
			
			if ((tile.mNumber1 == number2) && (tile.mNumber2 == number1)) {
				
				mTiles.remove(i);
				
				return true;
			}
		}
		
		return false;
	}
	
	void sendBoardTileInfo(Game game) {		
		
		String msgString1=CommProtocol.createMsgBoardTilesInfo1(game);
		
		sendMessage(msgString1);
		
		String msgString2=CommProtocol.createMsgBoardTilesInfo2(game);
		
		sendMessage(msgString2);
	}
	
	private ArrayList<MyPair<DominoTile, Integer>> getPlayableTiles(int endNumber1, int endNumber2) {
		
		ArrayList<MyPair<DominoTile, Integer>> playableTiles = new ArrayList<MyPair<DominoTile, Integer>>();
		
		Iterator<DominoTile> iter = mTiles.iterator();
		
		while (iter.hasNext()) {
			
			DominoTile tile = iter.next();
			
			if ((tile.mNumber1 == endNumber1) || (tile.mNumber2 == endNumber1)) {
				
				MyPair<DominoTile, Integer> pair = new MyPair<DominoTile, Integer> (tile, 1);
				
				playableTiles.add(pair);				
			}
			
			if ((tile.mNumber1 == endNumber2) || (tile.mNumber2 == endNumber2)) {
				
				MyPair<DominoTile, Integer> pair = new MyPair<DominoTile, Integer> (tile, 2);
				
				playableTiles.add(pair);				
			}
		}
		
		return playableTiles;
	}
	
	public boolean hasTileWithNumber(int number) {
		
		Iterator<DominoTile> iter = mTiles.iterator();
		
		while (iter.hasNext()) {
			
			DominoTile tile = iter.next();
			
			if ((tile.mNumber1 == number) || (tile.mNumber2 == number)) {
				
				return true;
			}		
		}
		
		return false;
	}
	
	public int getPoints() {
		
		int points = 0;
		
		Iterator<DominoTile> iter = mTiles.iterator();
		
		while (iter.hasNext()) {
			
			DominoTile tile = iter.next();
			
			points += tile.mNumber1;
			points += tile.mNumber2;
		}
		
		return points;
	}
	
	public String getTilesString() {
		
		String tiles = "";
		
		Iterator<DominoTile> iter = mTiles.iterator();
		
		while (iter.hasNext()) {
			
			DominoTile tile = iter.next();
			
			tiles += tile.mNumber1;
			tiles += tile.mNumber2;
		}
		
		return tiles;
	}
}
