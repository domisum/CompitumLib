package de.domisum.compitumapi.util;

import java.util.logging.Level;

import de.domisum.compitumapi.CompitumAPI;

public class Logger
{

	// -------
	// LOGGING
	// -------
	public static void info(String message)
	{
		getLogger().log(Level.INFO, message);
	}

	public static void warn(String message)
	{
		getLogger().log(Level.WARNING, message);
	}

	public static void err(String message)
	{
		getLogger().log(Level.SEVERE, message);
	}

	public static void err(String message, Exception exception)
	{
		getLogger().log(Level.SEVERE, message, exception);
	}


	private static java.util.logging.Logger getLogger()
	{
		return CompitumAPI.getInstance().getLogger();
	}

}