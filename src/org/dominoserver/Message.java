package org.dominoserver;

import java.net.Socket;

public class Message {
	
	public enum MsgId {
		
		UNKNOWN,
		NEW_CONNECTION
	};
	
	public MsgId mId=MsgId.UNKNOWN;
	
	public Socket mSocket=null;
	
	public Message(MsgId id) {
		
		mId=id;
	}
	
	public void setSocket(Socket socket) {
		
		mSocket=socket;
	}

}
