package org.dominoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Player extends Thread {
	
	private Socket mSocket=null;
	
	public Player(Socket clientSocket) {
		
		mSocket=clientSocket;		
	}
	
	public void run() {
		
        try {
        	
        	PrintWriter out=new PrintWriter(mSocket.getOutputStream(), true);
        	
        	BufferedReader in=new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        	
        	String inputLine, outputLine;
        	
            /*
            KnockKnockProtocol kkp = new KnockKnockProtocol();
            
                outputLine = kkp.processInput(null);
                out.println(outputLine);
                
                */
        	
        	while((inputLine=in.readLine()) != null) {
        		
        		Log.info("Received Message: "+inputLine);
        		
        		/*
        		Message cmd=CommProtocol.processLine(inputLine);
                    
                out.println(outputLine);
                
                if (outputLine.equals("Bye"))
                        break;
                }
                socket.close();
                */
                
            }
        }
        catch(IOException e) {
        	
        	Log.error("IOException error: "+e.getMessage());
        }
	}
}
