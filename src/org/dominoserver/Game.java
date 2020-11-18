package org.dominoserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class Game {
	
	public static final int MAX_PLAYERS = 4;
	
	public static final int TILES_PER_PLAYER = 7;
	
	public static final int WINNING_POINTS = 30;
		
	public static final String ROBOT_PLAYER_NAME = "Robot";
	
	private static final int SHUFFLE_TIMES = 5;
	
	public enum GameStatus {
		
		NOT_STARTED,
		RUNNING,
		FINISHED,
		CANCELLED		
	};
	
	public enum RoundStatus {
		
		NOT_STARTED,
		RUNNING,
		WON,
		CLOSED		
	};
	
	public GameStatus mGameStatus = GameStatus.NOT_STARTED;
	
	public RoundStatus mRoundStatus = RoundStatus.NOT_STARTED;
	
	public Player[] mPlayers=null;
	
	public int mHandPlayer = -1;
	public int mTurnPlayer = -1;
	
	public int mRoundCount = 0;
	
	public ArrayList<DominoTile> mBoardTiles1 = null;
	public ArrayList<DominoTile> mBoardTiles2 = null;
	
	public int mWinnerPlayerPos = -1;
	public int mCloserPlayerPos = -1;
	
	public int mPair1Points = 0;
	public int mPair2Points = 0;
	
	public Game() {		
		
	}
	
	public GameStatus getGameStatus() {
		
		return mGameStatus;
	}
	
	public void setGameStatus(GameStatus gameStatus) {
		
		mGameStatus = gameStatus;
	}
	
	public RoundStatus getRoundStatus() {
		
		return mRoundStatus;
	}
	
	public void setRoundStatus(RoundStatus roundStatus) {
		
		mRoundStatus = roundStatus;
	}
	
	public void initPlayers() {
		
		mPlayers=new Player[MAX_PLAYERS];
	
		for(int i=0; i<MAX_PLAYERS; i++) {
			
			mPlayers[i]=new Player(i, ROBOT_PLAYER_NAME+(i+1));
			
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
	
	public void cancelGame() {
		
		mGameStatus = GameStatus.CANCELLED;	
	}
	
	public void launchGame() {
		
		mGameStatus = GameStatus.RUNNING;
		
		mPair1Points = 0;
		mPair2Points = 0;		
	}
	
	public void launchNewRound(int roundCount, MessageHandler msgHandler) {
		
		mRoundStatus = RoundStatus.RUNNING;
		
		mRoundCount = roundCount;
		
		// Generate domino tiles...
		
		ArrayList<DominoTile> allTiles = DominoTile.createAllTiles();
		
		// Shuffle domino tiles...
		
		Log.info("Shuffling domino tiles "+SHUFFLE_TIMES+" times...");
		
		for (int i=0; i<SHUFFLE_TIMES; i++) {
			
			long seed = System.currentTimeMillis();
		
			Random random = new Random(seed);
		
			Collections.shuffle(allTiles, random);
		}
		
		Log.info("Shuffled all tiles...");
		
		// Assign tiles to each player
		
		for(int i=0; i<MAX_PLAYERS; i++) {
			
			// First, remove any existing tile from player
			mPlayers[i].mTiles.clear();
			
			for(int j=0; j<TILES_PER_PLAYER; j++) {
				
				mPlayers[i].addTile(allTiles.remove(0));
			}
		}
		
		// Assign the first turn
		
		if (mRoundCount == 0) {
			
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
	
	void sendGameInfoToAllPlayers() {
		
		String msgString=CommProtocol.createMsgGameInfo(this);
		
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			if (!mPlayers[i].isRobot()) {
				
				// Send message only to "real" players...
		
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
			
			int endNumber;
			
			if (boardSide == 1) {
				
				DominoTile endTile = mBoardTiles1.get(mBoardTiles1.size()-1);
				
				endNumber = endTile.mNumber2;
				
				if (tile.mNumber1 == endNumber) {
					
					mBoardTiles1.add(tile);
				}
				else if (tile.mNumber2 == endNumber) {
					
					tile.swapNumbers();
					
					mBoardTiles1.add(tile);
				}
				else {
					
					Log.error("addPlayedTile(): Played tile <"+tile.mNumber1+"-"+tile.mNumber2+
							"> cannot be placed in the board");
					
					return false;
				}
			}
			else if (boardSide == 2) {
				
				if (mBoardTiles2.size() == 0) {
					
					// Board side 2 is empty.
					
					// Get the end number from board 1
					
					endNumber = mBoardTiles1.get(0).mNumber1;
				}
				else {
					
					DominoTile endTile = mBoardTiles2.get(mBoardTiles2.size()-1);
					
					endNumber = endTile.mNumber2;
				}
				
				if (tile.mNumber1 == endNumber) {
					
					mBoardTiles2.add(tile);
				}
				else if (tile.mNumber2 == endNumber) {
					
					tile.swapNumbers();
					
					mBoardTiles2.add(tile);
				}
				else {
					
					Log.error("addPlayedTile(): Played tile <"+tile.mNumber1+"-"+tile.mNumber2+
							"> cannot be placed in the board");
					
					return false;
				}
			}
			else {
				
				Log.error("addPlayedTile(): Incorrect boardSide="+boardSide);
				
				return false;
			}
		}
		
		Log.debug("addPlayedTile(): Tile <"+tile.mNumber1+"-"+tile.mNumber2+"> added in boardSide="+boardSide);
		
		return true;
	}
	
	public void increaseTurnPlayer(MessageHandler msgHandler) {
		
		mTurnPlayer++;
		
		if (mTurnPlayer == MAX_PLAYERS) {
			
			mTurnPlayer = 0;
		}
		
		if (mPlayers[mTurnPlayer].isRobot()) {
			
			Log.debug("increaseTurnPlayer(): Next player is a robot...");
			
			// It's the turn of this robot
			
			mPlayers[mTurnPlayer].playTurn(this, msgHandler);
		}
	}
	
	public int getEndNumber1() {
		
		int endNumber1;
		
		if (mBoardTiles1.size() == 0) {
			
			Log.error("getEndNumber1() mBoardTiles1.size() == 0");
			
			endNumber1 = -1;
		}
		else {
			
			DominoTile endTile1 = mBoardTiles1.get(mBoardTiles1.size()-1);
		
			endNumber1 = endTile1.mNumber2;
		}
		
		return endNumber1;
	}
	
	public int getEndNumber2() {
		
		int endNumber2;
	
		if (mBoardTiles2.size() == 0) {
		
			// There are no tiles in board2. Get the first number of board 1
			
			if (mBoardTiles1.size() == 0) {
				
				Log.error("getEndNumber2() mBoardTiles1.size() == 0");
				
				endNumber2 = -1;
			}
			else {
				
				endNumber2 = mBoardTiles1.get(0).mNumber1;				
			}
		}
		else {
			
			DominoTile endTile2 = mBoardTiles2.get(mBoardTiles2.size()-1);
			
			endNumber2 = endTile2.mNumber2;
		}
		
		return endNumber2;
	}
	
	public boolean isClosed() {
		
		int endNumber1 = getEndNumber1();
		int endNumber2 = getEndNumber2();
		
		if ((endNumber1 < 0) || (endNumber2 < 0)) {
			
			return false;
		}
		
		if (endNumber1 != endNumber2) {
			
			return false;
		}
		
		for (int i=0; i<MAX_PLAYERS; i++) {
			
			if (mPlayers[i].hasTileWithNumber(endNumber1)) {
				
				return false;
			}
		}
		
		return true;
	}
	
	public int[] getPlayerPoints() {
		
		int points[] = new int[MAX_PLAYERS];
		
		for (int i=0; i<MAX_PLAYERS; i++) {
			
			Player player = mPlayers[i]; 
			
			points[i]=player.getPoints();	
			
			Log.info("Player #"+player.mPlayerPos+" <"+player.getPlayerName()+"> has "+points[i]+" points");
		}		
		
		return points;
	}
	
	public void setWinnerPlayer(int winnerPlayerPos) {
		
		mRoundStatus = RoundStatus.WON;
		
		Log.debug("setWinnerPlayer() winner is player in pos="+winnerPlayerPos+" with name <"+
					mPlayers[winnerPlayerPos].getPlayerName());
		
		mWinnerPlayerPos = winnerPlayerPos;
		
		// Calculate pair points....
		
		int playerPoints[] = getPlayerPoints();
		
		
		int totalPoints =   playerPoints[0] + playerPoints[1] +
                            playerPoints[2] + playerPoints[3];
		
		int addingPoints = (totalPoints-1) / 10 +1;
		
		Log.info("Total points: "+totalPoints+" (+"+addingPoints+")");

        int winningPair = (winnerPlayerPos % 2) +1;
        
        Log.info("Winner is pair #"+winningPair);
        
        String pair1Text = "Pair1: ("+mPlayers[0].getPlayerName()+" + "+mPlayers[2].getPlayerName()+") ";
        String pair2Text = "Pair2: ("+mPlayers[1].getPlayerName()+" + "+mPlayers[3].getPlayerName()+") ";
        
        if (winningPair == 1) {
        	
        	int finalPair1Points = mPair1Points + addingPoints;
        	
        	Log.info(pair1Text+mPair1Points+" + "+addingPoints+" = "+finalPair1Points+" points");
        	
        	Log.info(pair2Text+mPair2Points+" points");
        	
        	mPair1Points += addingPoints;
        }
        else if (winningPair == 2) {
        	
        	int finalPair2Points = mPair2Points + addingPoints;
        	
        	Log.info(pair1Text+mPair1Points+" points");
        	
        	Log.info(pair2Text+mPair2Points+" + "+addingPoints+" = "+finalPair2Points+" points");
        	
        	mPair2Points += addingPoints;
        }
        else {

            Log.error("Incorrect winningPair="+winningPair);
        }      
	}
	
	public void setCloserPlayer(int closerPlayerPos) {
		
		mRoundStatus = RoundStatus.CLOSED;
		
		mCloserPlayerPos = closerPlayerPos;		
		
		// Calculate pair points....
		
		int playerPoints[] = getPlayerPoints();
		
		int pair1Points = playerPoints[0] + playerPoints[2];

        int pair2Points = playerPoints[1] + playerPoints[3];
        
        Log.info("Pair1: "+pair1Points+" points");
        Log.info("Pair2: "+pair2Points+" points");

        int totalPoints = pair1Points + pair2Points;
        
        int addingPoints = (totalPoints-1) / 10 +1;
        
        Log.info("Total: "+totalPoints+" points (+"+addingPoints+")");

        int winningPair;

        if (pair1Points < pair2Points) {

            winningPair = 1;
        }
        else if (pair2Points < pair1Points) {

            winningPair = 2;
        }
        else {

            // pair1Points == pair2Points
        	
        	Log.info("This is a tied game");

            // The winner is the hand...

            if ((mHandPlayer == 0) || (mHandPlayer == 2)) {

                // The winner is pair 1...

                winningPair = 1;
                
                Log.info("Hand belongs to pair #1");
            }
            else {

                winningPair = 2;
                
                Log.info("Hand belongs to pair #2");
            }
        }
        
        Log.info("Winner is pair #"+winningPair);
        
        String pair1Text = "Pair1: ("+mPlayers[0].getPlayerName()+" + "+mPlayers[2].getPlayerName()+") ";
        String pair2Text = "Pair2: ("+mPlayers[1].getPlayerName()+" + "+mPlayers[3].getPlayerName()+") ";
        
        if (winningPair == 1) {
        	
        	int finalPair1Points = mPair1Points + addingPoints;
        	
        	Log.info(pair1Text+mPair1Points+" + "+addingPoints+" = "+finalPair1Points+" points");
        	
        	Log.info(pair2Text+mPair2Points+" points");
        	
        	mPair1Points += addingPoints;
        }
        else if (winningPair == 2) {
        	
        	int finalPair2Points = mPair2Points + addingPoints;
        	
        	Log.info(pair1Text+mPair1Points+" points");
        	
        	Log.info(pair2Text+mPair2Points+" + "+addingPoints+" = "+finalPair2Points+" points");
        	
        	mPair2Points += addingPoints;
        }
        else {

            Log.error("Incorrect winningPair="+winningPair);
        }  
		
	}
	
	public void sendRoundInfoToAllPlayers() {
		
		String msgString = CommProtocol.createMsgRoundInfo(this);
		
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			if (!mPlayers[i].isRobot()) {
				
				// Send messages to "real" player...
				
				mPlayers[i].sendMessage(msgString);			
			}
		}
	}
	
	public boolean hasFinished() {
		
		boolean gameHasFinished = false;
		
		int winningPair;
		
		if (mPair1Points >= WINNING_POINTS) {
			
			winningPair = 1;
		}
		else if (mPair2Points >= WINNING_POINTS) {
			
			winningPair = 2;
		}
		else {
			
			winningPair = -1;
		}
		
		if (winningPair >0 ) {
			
			gameHasFinished = true;
			
			mGameStatus = GameStatus.FINISHED;
			
			Log.info("Game has finished!! Winning pair is Pair #" + winningPair);
		}
		
		return gameHasFinished;
	}
	
	public String findAvailableRobotName() {
		
		for(int i=0; i<MAX_PLAYERS; i++) {
			
			String robotName = ROBOT_PLAYER_NAME+(i+1);
			
			boolean found = false;
			
			for(int j=0; j<MAX_PLAYERS; j++) {
				
				if (mPlayers[j].getPlayerName().compareTo(robotName)==0) {
					
					found = true;
					
					break;
				}				
			}
			
			if (found == false) {
				
				return robotName;
			}					
		}
		
		return ROBOT_PLAYER_NAME+7;
	}
	
	public void sendPlayedTileInfoToAllPlayers(Player player, DominoTile tile) {
		
		String msgString = CommProtocol.createMsgPlayedTile(player, tile);
		
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			if (!mPlayers[i].isRobot()) {
				
				// Send messages to "real" player...
				
				mPlayers[i].sendMessage(msgString);			
			}
		}		
	}
}
