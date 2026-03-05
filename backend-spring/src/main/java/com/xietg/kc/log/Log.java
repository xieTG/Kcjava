package com.xietg.kc.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Log {

	private static final Logger log = LoggerFactory.getLogger(Log.class);
	
	
	static public void info(String input)
	{
		log.info(input);
	}
	static public void debug(String input)
	{
		log.debug(input);
	}
	static public void error(String input)
	{
		log.error(input);
	}
	
}
