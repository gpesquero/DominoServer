package org.dominoserver;

import java.net.Socket;
import java.util.HashMap;

public class Message {
	
	public enum MsgId {
		
		UNKNOWN,
		NEW_CONNECTION,
		LOG_IN,
		LOG_OUT,
		TIMER,
		MOVE_PLAYER,
		LAUNCH_GAME,
		REQUEST_TILE_INFO,
		REQUEST_GAME_INFO,
		PLAY_TILE
		
	};
	
	public MsgId mId=MsgId.UNKNOWN;
	
	public Socket mSocket=null;
	
	public Connection mConnection=null;
	
	public String mErrorString=null;
	
	public HashMap<String, String> mArgs=new HashMap<String, String>();
		
	public Message(MsgId id) {
		
		mId=id;
	}
	
	public void setSocket(Socket socket) {
		
		mSocket=socket;
	}
	
	public void setConnection(Connection connection) {
		
		mConnection=connection;
	}
	
	public void addArgument(String key, String value) {
		
		mArgs.put(key, value);		
	}
	
	public String getArgument(String key) {
		
		return mArgs.get(key);
	}
}
