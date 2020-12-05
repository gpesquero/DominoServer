package org.dominoserver;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.utilslibrary.Log;

public class MessageHandler {
	
	private Semaphore mSemaphore=null;
	
	private ArrayList<Message> mMsgList=null;
	
	public MessageHandler() {
		
		mSemaphore=new Semaphore(0);
		
		mMsgList=new ArrayList<Message>();		
	}
	
	synchronized public void addMessage(Message msg) {
		
		mMsgList.add(msg);
		
		mSemaphore.release();		
	}
	
	synchronized public Message getMessage() {
		
		Message msg;
		
		if (mMsgList.size()==0) {
			
			msg=null;
		}
		else {
			
			msg=mMsgList.remove(0);
		}
		
		return msg;
	}
	
	public Message waitForMessage() {
		
		Message msg;
		
		try {
			mSemaphore.acquire();
			
			msg=getMessage();
			
		} catch (InterruptedException e) {
			
			Log.error("MessageHandler InterruptedException: "+e.getMessage());
			
			msg=null;
		}
		
		return msg;
	}

}
