package com.github.stockRater;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class Tools {
	
	// DD/MM/YYYY
	public static Long convertToEpoch( String dateTime, DateTimeFormatter formatter, ZoneId zoneId ) { 
		
		if( formatter == null ) {
			formatter  = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		}
		
		// as the input string has no timezone information, parse it to a LocalDateTime
		LocalDateTime dt = LocalDateTime.parse( dateTime + " 00:00:00", formatter);

		// convert the LocalDateTime to a timezone
		if( zoneId == null ) {
			zoneId = ZoneId.of("UTC");	
		}

		ZonedDateTime zdt = dt.atZone( zoneId );

		// get the millis value
		// long millis = zdt.toInstant().toEpochMilli(); // 1500460950423		
		Long epoch = zdt.toEpochSecond();		
		return epoch; 
	}
	
	public static void waitMs( int milliseconds ) {
	
		try {
			Thread.sleep( milliseconds );
			
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
	}

	public static String getIniSetting( String path, String section, String keyName, String defaultValue ) {

		String keyValue = null;

		try {

			Ini iniFile = new Ini( new File( path ) );
			org.ini4j.Config.getGlobal().setEscape(false);

			Section iniSection = iniFile.get( section );
			if( iniSection == null ) {
				return defaultValue;
			}

			keyValue = iniSection.get( keyName );
			if( keyValue == null ) {
		
				return defaultValue;
			}

			if( keyValue.length() == 0 ) {

				return defaultValue;
			}

						
		} catch ( java.io.FileNotFoundException e ) {

			// if ini file not found just return the default value specified
			keyValue = defaultValue;

		} catch ( Exception e ) {

			keyValue = defaultValue;
			e.printStackTrace();
		}

		return keyValue;
	}
}
