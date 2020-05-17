package org.dominoserver;

import org.dominoserver.Game.RoundStatus;
import org.dominoserver.Message.MsgId;

public class CommProtocol {
	
	static public String createMsgPing() {
		
		return "<ping>";
	}
	
	static public String createMsgGameInfo(Game game) {
		
		String message="<game_info";
		
		message+=", status=";
		
		Game.GameStatus gameStatus=game.getGameStatus();
		
		switch (gameStatus) {
		
		case NOT_STARTED:
			message+="notStarted";
			break;
		
		case RUNNING:
			message+="running";
			break;
			
		case FINISHED:
			message+="finished";
			break;
			
		default:
			message+="unknown";
			break;	
		}
		
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			message+=", player"+i+"=";
			
			if (game.mPlayers[i]==null) {
				
				message+="Robot"+i;
			}
			else {
				
				message+=game.mPlayers[i].getPlayerName();
			}
		}
		
		message += ", pair1Points="+game.mPair1Points;
		
		message += ", pair2Points="+game.mPair2Points;
		
		message+=">";
		
		return message;
		
	}
	
	static public String createMsgRoundInfo(Game game) {

		String message="<round_info";
		
		message += ", roundCount=" + game.mRoundCount;
		
		message += ", status=";
		
		Game.RoundStatus roundStatus=game.getRoundStatus();
		
		switch (roundStatus) {
		
		case NOT_STARTED:
			message+="notStarted";
			break;
		
		case RUNNING:
			message+="running";
			break;
			
		case WON:
			message+="won";
			break;
			
		case CLOSED:
			message+="closed";
			break;
			
		default:
			message+="unknown";
			break;	
		}
		
		boolean addPlayerPoints = false;
		
		if (roundStatus == RoundStatus.WON) {
			
			message+=", winnerPlayerPos="+game.mWinnerPlayerPos;
			
			addPlayerPoints = true;
		}
		else if (roundStatus == RoundStatus.CLOSED) {
			
			message+=", closerPlayerPos="+game.mCloserPlayerPos;
			
			addPlayerPoints = true;
		}
		
		if (addPlayerPoints) {
			
			for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
				int points = game.mPlayers[i].getPoints();
				
				message+=", player"+i+"Points="+points;
			}
		}
		
		message+=">";
		
		return message;
	}
	
	
	static public String createMsgGameTileInfo(Game game) {
		
		String message="<game_tile_info";
		
		message+=", handPlayer="+game.mHandPlayer;
				
		message+=", turnPlayer="+game.mTurnPlayer;
		
		for(int i=0; i<Game.MAX_PLAYERS; i++) {
			
			message+=", player"+i+"=";
			
			message+=game.mPlayers[i].getTileCount();
		}
		
		message+=">";
		
		return message;		
	}
	
	static public String createMsgPlayerTileInfo(Player player) {
		
		String message="<player_tile_info";
		
		message+=", playerName="+player.getPlayerName();
		
		message+=", tileCount="+player.getTileCount();
		
		for(int i=0; i<player.mTiles.size(); i++){
			
			DominoTile tile = player.mTiles.get(i);
			
			message+=", tile"+i+"="+tile.mNumber1+"-"+tile.mNumber2;
		}
		
		message+=">";
		
		return message;		
	}
	
	static public String createMsgBoardTilesInfo1(Game game) {

		String message="<board_tile_info1";
		
		int boardTiles1Count = game.mBoardTiles1.size();
		
		message+=", tileCount="+boardTiles1Count;
		
		if (boardTiles1Count == 0) {
			
			message+=", forceDouble6Tile="+(game.mRoundCount==0);
		}
		else {
			
			for(int i=0; i<boardTiles1Count; i++){
			
				DominoTile tile = game.mBoardTiles1.get(i);
			
				message+=", tile"+i+"="+tile.mNumber1+"-"+tile.mNumber2;
			}
		}
		
		message+=">";
		
		return message;
	}
	
	static public String createMsgBoardTilesInfo2(Game game) {

		String message="<board_tile_info2";
		
		int boardTiles2Count = game.mBoardTiles2.size();
		
		message+=", tileCount="+boardTiles2Count;
		
		for(int i=0; i<boardTiles2Count; i++){
			
			DominoTile tile = game.mBoardTiles2.get(i);
			
			message+=", tile"+i+"="+tile.mNumber1+"-"+tile.mNumber2;
		}
		
		message+=">";
		
		return message;
	}
		
	static public Message processLine(String line) {
		
		Message msg=new Message(MsgId.UNKNOWN);
		
		line=line.trim();
		
		if (!line.startsWith("<") ) {
			
			msg.mId=MsgId.UNKNOWN;
			
			msg.mErrorString="Received message '"+line+"' does not start with '<'";
			
			return msg;			
		}
		
		if (!line.endsWith(">") ) {
			
			msg.mId=MsgId.UNKNOWN;
			
			msg.mErrorString="Received message '"+line+"' does not end with '>'";
			
			return msg;			
		}
		
		if (line.length()<5) {
			
			msg.mId=MsgId.UNKNOWN;
			
			msg.mErrorString="Received message '"+line+"' is too short";
			
			return msg;	
		}
		
		line=line.substring(1, line.length()-1);
		
		line.trim();
		
		String command;
		String args;
		
		int commaPos=line.indexOf(",");
		
		if (commaPos<0) {
			
			command=line.trim();
			args=null;
		}
		else {
			
			command=line.substring(0, commaPos).trim();
			args=line.substring(commaPos+1).trim();
		}
		
		if (args!=null) {
			
			// Process arguments...
			
			while(args.length()>0) {
				
				String arg;
				
				commaPos=args.indexOf(",");
				
				if (commaPos<0) {
					
					arg=args;
					
					args="";					
				}
				else {
					
					arg=args.substring(0, commaPos).trim();
					args=args.substring(commaPos+1).trim();
				}
				
				// Analyze message argument
				
				int equalPos=arg.indexOf("=");
				
				if (equalPos<0) {
					
					msg.mId=MsgId.UNKNOWN;
					
					msg.mErrorString="Received message '"+line+"'. Arg '"+arg+"' does no have '=' char";
					
					return msg;	
				}
				else {
					
					String key=arg.substring(0, equalPos).trim();
					String value=arg.substring(equalPos+1).trim();
					
					msg.addArgument(key, value);
				}
			}
		}
		
		if (command.compareTo("login")==0) {
			
			msg.mId=MsgId.LOG_IN;			
		}
		else if (command.compareTo("logout")==0) {
			
			msg.mId=MsgId.LOG_OUT;			
		}
		else if (command.compareTo("move_player")==0) {
			
			msg.mId=MsgId.MOVE_PLAYER;			
		}
		else if (command.compareTo("launch_game")==0) {
			
			msg.mId=MsgId.LAUNCH_GAME;			
		}
		else if (command.compareTo("request_tile_info")==0) {
			
			msg.mId=MsgId.REQUEST_TILE_INFO;			
		}
		else if (command.compareTo("request_game_info")==0) {
			
			msg.mId=MsgId.REQUEST_GAME_INFO;			
		}
		else if (command.compareTo("play_tile")==0) {
			
			msg.mId=MsgId.PLAY_TILE;			
		}
		else {
			
			msg.mErrorString="Received message '"+line+"' with unknown command '"+command+"'";
			
			return msg;
		}
		
		return msg;
	}
}
