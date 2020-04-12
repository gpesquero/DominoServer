package org.dominoserver;

import java.time.LocalDateTime;

public class Log {
	
	private static void log(String type, String text) {
		
		LocalDateTime time=LocalDateTime.now();
		
		String timeString=String.format("%04d-%02d-%02d %02d:%02d:%02d",
				time.getYear(),
				time.getMonthValue(),
				time.getDayOfMonth(),
				time.getHour(),
				time.getMinute(),
				time.getSecond());
		
		System.out.println(timeString+type+": "+text);
		
	}
	
	public static void info(String text) {
		
		log(" (INFO)", text);
	}

	public static void error(String text) {
		
		log(" ((((ERROR))))", text);		
	}

}
