package de.domisum.compitumapi;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import de.domisum.compitumapi.path.MaterialEvaluator;

public class CompitumAPI
{

	// REFERENCES
	private static CompitumAPI instance;
	private JavaPlugin plugin;


	// -------
	// CONSTRUCTOR
	// -------
	public CompitumAPI(JavaPlugin plugin)
	{
		instance = this;
		this.plugin = plugin;

		onEnable();
	}

	public void onEnable()
	{
		MaterialEvaluator.prepareEvaluation();

		getLogger().info("CompitumAPI has been enabled\n");
	}

	public void onDisable()
	{
		getLogger().info("CompitumAPI has been disabled\n");
	}


	// -------
	// GETTERS
	// -------
	public static CompitumAPI getInstance()
	{
		return instance;
	}

	public Logger getLogger()
	{
		return getInstance().plugin.getLogger();
	}

}
