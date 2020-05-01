package org.dominoserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Game {
	
	public static final int MAX_PLAYERS = 4;
	
	public static final String ROBOT_PLAYER_NAME = "Robot";
	
	public enum Status {
		
		NOT_STARTED,
		RUNNING
		
	};
	
	public Status mStatus = Status.NOT_STARTED;
	
	public Player[] mPlayers=null;
	
	public int mHandPlayer = -1;
	public int mTurnPlayer = -1;
	
	public int mRoundCount = 0;
	
	public ArrayList<DominoTile> mBoardTiles1 = null;
	public ArrayList<DominoTile> mBoardTiles2 = null;
	
	public Game() {		
		
	}
	
	public Status getStatus() {
		
		return mStatus;
	}
	
	public void setStatus(Status status) {
		
		mStatus = status;
	}
	
	public void initPlayers() {
		
		mPlayers=new Player[MAX_PLAYERS];
	
		for(int i=0; i<MAX_PLAYERS; i++) {
			
			mPlayers[i]=new Player(i, ROBOT_PLAYER_NAME);
			
			mPlayers[i].setAsRobot(true);
		};
	}
	
	public Player getPlayer(String playerName) {
		
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			if (mPlayers[i].getPlayerName().compareTo(playerName)==0) {
				
				return mPlayers[i];
			}				
		}
		
		return null;
	}
	
	public int findPlayerPos(String playerName) {
		
		int playerPos = -1;
	
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			if (mPlayers[i].getPlayerName().compareTo(playerName)==0) {
				
				playerPos = i;
				break;					
			}				
		}
		
		return playerPos;
	}
	
	
	
	public void printPlayers() {
		
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
	
	public void launchGame(MessageHandler msgHandler) {
		
		mStatus = Status.RUNNING;
		
		// Generate domino tiles...
		
		ArrayList<DominoTile> allTiles = DominoTile.createAllTiles();
		
		Collections.shuffle(allTiles);
		
		Log.info("Shuffled all tiles...");
		
		// Assign tiles to each player
		
		for(int i=0; i<MAX_PLAYERS; i++) {
			
			for(int j=0; j<DominoServer.TILES_PER_PLAYER; j++) {
				
				mPlayers[i].addTile(allTiles.remove(0));
			}
		}
		
		// Assign the first turn
		
		if (mRoundCount==0) {
			
			Log.info("Search for player with Double Six tile...");
			
			mHandPlayer = getPlayerWithDoubleSixTile();
			
			if (mHandPlayer < 0) {
				
				Log.error("DoubleSix has not been found!!");
				
				mHandPlayer = 0;
			}
		}
		else {
			
			mHandPlayer++;
			
			if (mHandPlayer == MAX_PLAYERS) {
				
				mHandPlayer = 0;
			}
		}
		
		Log.info("Hand player set to player"+mHandPlayer+" ("+mPlayers[mHandPlayer].getPlayerName()+")");
		
		mTurnPlayer = mHandPlayer;
		
		// Clear board tiles...
		
		mBoardTiles1 = new ArrayList<DominoTile>();
		mBoardTiles2 = new ArrayList<DominoTile>();
		

		// Check if Robot has to play in the first turn...
		
		if (mPlayers[mTurnPlayer].isRobot()) {
			
			Log.info("First turn player is a robot...");
			
			// It's the turn of this robot
			
			mPlayers[mTurnPlayer].playTurn(this, msgHandler);
		}
					
		
		//Log.info("Remaining tiles: "+allTiles.size());
	}
	
	private int getPlayerWithDoubleSixTile() {
		
		for(int i=0; i<MAX_PLAYERS; i++) {
			
			Iterator<DominoTile> iter = mPlayers[i].mTiles.iterator();
			
			while(iter.hasNext()) {
				
				DominoTile tile = iter.next();
				
				if ((tile.mNumber1 == 6) && (tile.mNumber2 == 6)) {
					
					return i;
				}
			}
		}
		
		return -1;
	}
	
	void sendGameInfoToAllPlayers(/*MessageHandler msgHandler*/) {
		
		String msgString=CommProtocol.createMsgGameInfo(this);
		
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			if (!mPlayers[i].isRobot()) {
				
				// Send message to "real" player...
		
				mPlayers[i].sendMessage(msgString);
			}
		}
	}
	
	void sendGameTileInfoToAllPlayers(/*MessageHandler msgHandler*/) {
		
		String msgString=CommProtocol.createMsgGameTileInfo(this);
		
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			if (!mPlayers[i].isRobot()) {
				
				// Send message to "real" player...
		
				mPlayers[i].sendMessage(msgString);
			}
		}
	}
	
	void sendBoardTilesInfoToAllPlayers() {	
		
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			if (!mPlayers[i].isRobot()) {
				
				// Send messages to "real" player...
				
				mPlayers[i].sendBoardTileInfo(this);
			}
		}
	}
	
	boolean addPlayedTile(DominoTile tile, int boardSide) {
		
		// Check if tile can be placed in the board...
		
		if (mBoardTiles1.size() == 0) {
			
			// This is the first tile of the board

			// Check if we're in round 0
			
			if (mRoundCount == 0) {
				
				// The first tile has to be a double 6 tile in the first round
				
				if ((tile.mNumber1 != 6) || (tile.mNumber2 != 6)) {
					
					Log.error("addPlayedTile(): First tile in the first round is not a double-6");
					
					return false;
				}
			}
			
			// Add tile in the first position
			mBoardTiles1.add(tile);
		}
		else {
			
			DominoTile endTile;
			
			if (boardSide == 1) {
				
				endTile = mBoardTiles1.get(mBoardTiles1.size()-1);				
			}
			else if (boardSide == 2) {
				
				endTile = mBoardTiles2.get(mBoardTiles1.size()-1);
			}
			else {
				
				Log.error("addPlayedTile(): Incorrect boardSide="+boardSide);
				
				return false;
			}
			
			int endNumber = endTile.mNumber2;
			
			// Check if the played tile has the "endNumber"
			
			if (tile.mNumber1 == endNumber) {
				
				if (boardSide == 1) {
					
					mBoardTiles1.add(tile);
				}
				else {
					
					mBoardTiles2.add(tile);
				}
			}
			else if (tile.mNumber2 == endNumber) {
				
				tile.swapNumbers();
				
				if (boardSide == 1) {
					
					mBoardTiles1.add(tile);
				}
				else {
					
					mBoardTiles2.add(tile);
				}
			}
			else {
				
				Log.error("addPlayedTile(): Played tile cannot be placed in the board");
				
				return false;
			}
		}
		
		Log.info("addPlayedTile(): Tile added in board");
		
		return true;
	}
	
	public void increaseTurnPlayer(MessageHandler msgHandler) {
		
		mTurnPlayer++;
		
		if (mTurnPlayer == MAX_PLAYERS) {
			
			mTurnPlayer = 0;
		}
		
		if (mPlayers[mTurnPlayer].isRobot()) {
			
			Log.info("increaseTurnPlayer(): Next player is a robot...");
			
			// It's the turn of this robot
			
			mPlayers[mTurnPlayer].playTurn(this, msgHandler);
		}
	}
}
