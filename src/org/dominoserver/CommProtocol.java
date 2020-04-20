package org.dominoserver;

import org.dominoserver.Message.MsgId;

public class CommProtocol {
	
	static public String createMsgPing() {
		
		return "<ping>";
	}
	
	static public String createMsgSessionInfo(Player[] players) {
		
		String message="<session_info";
		
		for(int i=0; i<DominoServer.MAX_PLAYERS; i++) {
			
			message+=", player"+i+"=";
			
			if (players[i]==null) {
				
				message+=null;
			}
			else {
				
				message+=players[i].getPlayerName();
			}
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
		
		if (command.compareTo("open_session")==0) {
			
			msg.mId=MsgId.OPEN_SESSION;			
		}
		else {
			
			msg.mErrorString="Received message '"+line+"' with unknown command '"+command+"'";
			
			return msg;
		}
		
		return msg;
	}
}
