package com.github.stockRater;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Tools {
	
	// DD/MM/YYYY
	public static Long convertToEpoch( String dateTime ) { 
		
		DateTimeFormatter formatter  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		
		// as the input string has no timezone information, parse it to a LocalDateTime
		LocalDateTime dt = LocalDateTime.parse( dateTime + " 00:00:00", formatter);

		// convert the LocalDateTime to a timezone
		ZoneId zoneId = ZoneId.of("Europe/Paris");
		
		ZonedDateTime zdt = dt.atZone( zoneId );

		// get the millis value
		// long millis = zdt.toInstant().toEpochMilli(); // 1500460950423		
		Long epoch = zdt.toEpochSecond();		
		return epoch; 
	}

}
