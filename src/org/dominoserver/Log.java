package org.dominoserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Log {
	
	private static PrintWriter mLogFileWriter = null;
	
	private static String mLogDateString = null;
	
	private static void log(String typeString, String text) {
		
		LocalDateTime time=LocalDateTime.now();
		
		String dateString=String.format("%04d-%02d-%02d",
				time.getYear(),
				time.getMonthValue(),
				time.getDayOfMonth());
		
		String timeString=String.format("%02d:%02d:%02d",
				time.getHour(),
				time.getMinute(),
				time.getSecond());
		
		String logLine = dateString + " " + timeString + " " + typeString + ": " + text;
		
		System.out.println(logLine);
		
		if (mLogDateString != null) {
			
			// Check if dateString has changed...
			
			if (mLogDateString.compareTo(dateString) != 0) {
				
				// DateString has changed
				// Close existing logfile to force the open of a new one...
				
				mLogFileWriter.close();
				
				mLogFileWriter = null;
			}
		}
		
		if (mLogFileWriter == null) {
			
			// Create or open log file...
			
			FileWriter logFile;
			
			try {
				
				String logFileName = DominoServer.APP_NAME + "_" + dateString + ".log";
				
				String logDirName = "./logs";
				
				File logDir = new File(logDirName);
				
				if (!logDir.exists()) {
					
					if (!logDir.mkdirs()) {
						
						System.out.println(dateString + " " + timeString + " " + "((((ERROR)))): " +
								"Error in logDir.mkdirs()");
					}
				}
				
				// Create FileWriter. 'true' argument means append to existing file
				
				logFile = new FileWriter(logDirName + "/" + logFileName, true);
				
			} catch (IOException e) {
				
				System.out.println(dateString + " " + timeString + " " + "((((ERROR)))): " +
						e.getMessage());
				
				return;
			}
			
			mLogDateString = dateString;
			
			BufferedWriter bw = new BufferedWriter(logFile);
				
			mLogFileWriter = new PrintWriter(bw);
		}
		
		if (mLogFileWriter != null) {
			
			mLogFileWriter.println(logLine);
			
			mLogFileWriter.flush();
		}
	}
	
	public static void info(String text) {
		
		log("(INFO)", text);
	}

	public static void debug(String text) {
		
		//log("(DEBUG)", text);
	}

	public static void error(String text) {
		
		log("((((ERROR))))", text);		
	}
}
