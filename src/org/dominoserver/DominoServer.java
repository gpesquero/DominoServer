package org.dominoserver;

import java.io.IOException;
import java.util.ArrayList;

import org.dominoserver.Game.GameStatus;
import org.dominoserver.Message.MsgId;

public class DominoServer {

	private static final String APP_NAME = "DominoServer";
	
	private static final String VERSION_NAME = "0.01";
	
	public static final int TILES_PER_PLAYER = 7;
	
	private static final int TIMER_TIMEOUT = 5000;
	
	public static void main(String[] args) {
		
		new DominoServer();
		
	}
	
	TimerThread mTimerThread=null;
	
	MessageHandler mMessageHandler=null;
	
	CommListener mCommListener=null;
	
	Game mGame = null;
	
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
					
					Log.error("Timer thread sleep interrupted!!");
					
					mRun=false;
				}
			
				Message msg=new Message(MsgId.TIMER);
			
				mMessageHandler.addMessage(msg);
			}
		}
	};
	
	public DominoServer() {
		
		Log.info("SERVER: Starting '"+APP_NAME+"' (version "+VERSION_NAME+")...");
		
		mMessageHandler=new MessageHandler();
		
		mCommListener=new CommListener(mMessageHandler);
		
		mConnections=new ArrayList<Connection>();
		
		mGame = new Game();
		
		mGame.initPlayers();
		
		printConnections();
		
		mGame.printPlayers();
		
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
		else if (msg.mId==MsgId.LOG_IN) {
			
			String playerName=msg.getArgument("playerName");
			
			Log.info("Received Msg LOG_IN with playerName=<"+playerName+">");			
			
			// First, check if we already have a player with that name...
			
			int playerPos = mGame.findPlayerPos(playerName);
			
			Player player=null;
			
			if (playerPos<0) {
				
				// We have not found this player. Create a new one in an available space...
				
				for(int i=0; i<Game.MAX_PLAYERS; i++) {
					
					if (mGame.mPlayers[i].isRobot()) {
						
						playerPos=i;
						break;
					}
				}
				
				if (playerPos<0) {
					
					Log.error("SERVER: No room available for more players....");
				
					// Close socket...
					
					try {
						
						if (msg.mSocket!=null) {
							
							msg.mSocket.close();
						}
						
					} catch (IOException e) {
						
						Log.error("SERVER: Socket.close() error="+e.getMessage());
					}
					
					player=null;
				}
				else {
					
					player=new Player(playerPos, playerName);
					
					//newPlayer.setMessageHandler(mMessageHandler);
					//newPlayer.setSocket(msg.mSocket);
					
					player.setConnection(msg.mConnection);
					
					mGame.mPlayers[playerPos]=player;
				
					// Start the player thread...
					//newPlayer.start();
					
					Log.info("SERVER: Created new player <"+playerName+">");
				}
			}
			else {
				
				// Connect to an existing Player...
				
				player=mGame.mPlayers[playerPos];
				
				player.setConnection(msg.mConnection);
				
				/*
				if (player.isAlive()) {
					
					existingPlayer.interrupt();
				}
				
				existingPlayer.setSocket(msg.mSocket);
				
				existingPlayer.start();
				*/
			}
			
			if (player!=null) {
				
				// Send info to all active players
				
				if (mGame.mGameStatus == GameStatus.RUNNING) {
					
					mGame.sendBoardTilesInfoToAllPlayers();
					mGame.sendGameTileInfoToAllPlayers();
				}
				
				mGame.sendGameInfoToAllPlayers();
				
				mGame.sendRoundInfoToAllPlayers();
			}
			
			mGame.printPlayers();
		}
		else if (msg.mId==MsgId.LOG_OUT) {
			
			String playerName=msg.getArgument("playerName");
			
			Log.info("Received Msg LOG_OUT with playerName=<"+playerName+">");
			
			// First, find playerName...
			
			int playerPos = mGame.findPlayerPos(playerName);
			
			if (playerPos < 0) {
				
				Log.error("Player <"+playerName+"> not found. Cannot log out");				
			}
			else {
				
				Player oldPlayer = mGame.mPlayers[playerPos];
				
				String newRobotName = mGame.findAvailableRobotName();
				
				Player robotPlayer = new Player(playerPos, newRobotName);
				
				robotPlayer.setAsRobot(true);
				
				mGame.mPlayers[playerPos] = robotPlayer;
				
				String msgString = CommProtocol.createMsgGameInfo(mGame);
				
				oldPlayer.sendMessage(msgString);
			}
			
			mGame.sendBoardTilesInfoToAllPlayers();
			
			mGame.sendGameInfoToAllPlayers();
			
			mGame.printPlayers();
			
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
		else if (msg.mId == MsgId.MOVE_PLAYER) {
			
			String playerName=msg.getArgument("playerName");
			int newPos=Integer.parseInt(msg.getArgument("newPos"));
			
			Log.info("Received Msg MOVE_PLAYER with playerName=<"+playerName+">, newPos="+newPos);
			
			// First, find playerName...
			
			int playerPos = mGame.findPlayerPos(playerName);
			
			if (playerPos < 0) {
				
				Log.error("Move Player. playerName not found!!!");				
			}
			else {
				
				// Exchange players....
				
				Player aux=mGame.mPlayers[newPos];
				
				mGame.mPlayers[newPos]=mGame.mPlayers[playerPos];
				mGame.mPlayers[newPos].mPlayerPos = newPos;
				
				mGame.mPlayers[playerPos]=aux;
				mGame.mPlayers[playerPos].mPlayerPos = playerPos;
				
				mGame.sendGameInfoToAllPlayers();
				
				mGame.printPlayers();
			}	
			
		}
		else if (msg.mId == MsgId.LAUNCH_GAME) {
			
			String playerName = msg.getArgument("playerName");
			
			Log.info("Received Msg LAUNCH_GAME with playerName=<"+playerName+">");
			
			if (mGame.getGameStatus() == Game.GameStatus.RUNNING) {
				
				Log.error("Cannot launch game!! Game is already running...");
			}
			else {
				
				mGame.launchGame();
				
				mGame.launchNewRound(0, mMessageHandler);
			}
			
			mGame.sendBoardTilesInfoToAllPlayers();
			mGame.sendGameTileInfoToAllPlayers();
			mGame.sendRoundInfoToAllPlayers();
			mGame.sendGameInfoToAllPlayers();
		}
		else if (msg.mId==MsgId.CANCEL_GAME) {
			
			String playerName=msg.getArgument("playerName");
			
			Log.info("Received Msg CANCEL_GAME with playerName=<"+playerName+">");
			
			if (mGame.getGameStatus() != Game.GameStatus.RUNNING) {
				
				Log.error("Cannot cancel game!! Game is not running...");
			}
			else {
				
				mGame.cancelGame();
			}
			
			mGame.sendBoardTilesInfoToAllPlayers();
			mGame.sendGameTileInfoToAllPlayers();
			mGame.sendRoundInfoToAllPlayers();
			mGame.sendGameInfoToAllPlayers();
		}
		else if (msg.mId==MsgId.REQUEST_TILE_INFO) {
			
			String playerName=msg.getArgument("playerName");
			
			Log.debug("Received Msg REQUEST_TILE_INFO with playerName=<"+playerName+">");
			
			if (mGame.getGameStatus() == Game.GameStatus.NOT_STARTED) {
				
				Log.error("Game is not started!! Cannot send tile info");
			}
			else {
				
				Player player = mGame.getPlayer(playerName);
				
				if (player == null) {
					
					Log.error("Player not found!!");
				}
				else {
					
					player.sendBoardTileInfo(mGame);
					player.sendGameTileInfo(mGame);
				}
			}
		}
		else if (msg.mId==MsgId.REQUEST_GAME_INFO) {
			
			String playerName=msg.getArgument("playerName");
			
			Log.debug("Received Msg REQUEST_GAME_INFO with playerName=<"+playerName+">");
			
			Player player = mGame.getPlayer(playerName);
				
			if (player == null) {
				
				Log.error("Player not found!!");
			}
			else {
				
				player.sendGameInfo(mGame);
			}
		}
		else if (msg.mId==MsgId.PLAY_TILE) {
					
			String playerName=msg.getArgument("playerName");
			
			int playerPos=Integer.parseInt(msg.getArgument("playerPos"));
			
			Log.debug("Received Msg PLAY_TILE with playerName=<"+playerName+"> and playerPos="+playerPos);
			
			// Check if it's the turn of the player...
			
			if (mGame.mTurnPlayer != playerPos) {
				
				
				Log.error("It's not the turn of player #"+playerPos);
				
				return run;
			}
			
			int boardSide = Integer.parseInt(msg.getArgument("boardSide"));
			
			String tileText = msg.getArgument("tile");
			
			Player player = mGame.mPlayers[playerPos];
			
			DominoTile tile;
			
			if (tileText.compareTo("null") == 0) {
				
				tile = null;
				
				Log.info("Player #"+playerPos+" <"+playerName+"> has passed");
				
				mGame.sendPlayedTileInfoToAllPlayers(player, tile);
			}
			else {
				
				String n1 = tileText.substring(0, 1);

	            int number1 = Integer.parseInt(n1);
	
	            String n2 = tileText.substring(2);
	
	            int number2 = Integer.parseInt(n2);
			
	            tile = new DominoTile(number1, number2);
	            
	            Log.info("Player #"+playerPos+" <"+playerName+"> has played tile=["+tile.mNumber1+"-"+tile.mNumber2+"] on boardSide="+boardSide);
	            
	            mGame.sendPlayedTileInfoToAllPlayers(player, tile);
	            
	            if (!player.removeTile(tile.mNumber1, tile.mNumber2)) {
	            	
	            	Log.error("Player.removeTile() tile="+tile.mNumber1+"-"+tile.mNumber2+" not found");
	            }
	            
	            mGame.addPlayedTile(tile, boardSide);
			}
			
			if (player.mTiles.size() == 0) {
				
				// Player has won the round...
				
				Log.info("Player #"+playerPos+" <"+player.getPlayerName()+"> has won the round");
				
				mGame.setWinnerPlayer(playerPos);
				
				mGame.sendRoundInfoToAllPlayers();
				
				if (!mGame.hasFinished()) {
					
					// Game is still running. Launch new round...
				
					mGame.launchNewRound(mGame.mRoundCount+1, mMessageHandler);
				}
				
				/*
				int playerPoints[] = mGame.getPlayerPoints();
				
				String msgString = CommProtocol.createMsgRoundInfo(mGame, playerPos);
				
				Log.info("MsgRoundWon: "+msgString);
				*/				
				
			}
			else if (mGame.isClosed()) {
				
				// Game has been closed...
				
				Log.info("Player"+playerPos+" <"+player.getPlayerName()+"> has closed the round");
				
				mGame.setCloserPlayer(playerPos);
				
				mGame.sendRoundInfoToAllPlayers();
				
				if (!mGame.hasFinished()) {
					
					// Game is still running. Launch new round...
					
					mGame.launchNewRound(mGame.mRoundCount+1, mMessageHandler);
				}
				
				/*
				int playerPoints[] = mGame.getPlayerPoints();
				
				String msgString = CommProtocol.createMsgGameClosed(mGame, playerPos);
				
				Log.info("MsgGameClosed: "+msgString);
				*/			
				
			}
			else {				
				
				mGame.increaseTurnPlayer(mMessageHandler);
				
			}
			
			mGame.sendBoardTilesInfoToAllPlayers();
			
			mGame.sendGameTileInfoToAllPlayers();
			
			mGame.sendRoundInfoToAllPlayers();
			
			mGame.sendGameInfoToAllPlayers();
		}
		else {
			
			Log.error("SERVER: Received UNKNOWN Msg (MsgId="+msg.mId+", Error="+msg.mErrorString+")");
		}
		
		return run;
	}
	
	void printConnections() {
		
		// List active connections...
		
		Log.info("Number of active connections="+mConnections.size());
	}
	
}
