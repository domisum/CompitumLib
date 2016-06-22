package de.domisum.compitumapi;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import de.domisum.compitumapi.path.MaterialEvaluator;

public class CompitumAPI extends JavaPlugin
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

	@Override
	public void onEnable()
	{
		MaterialEvaluator.prepareEvaluation();

		getPluginLogger().info("CompitumAPI has been enabled\n");
	}

	@Override
	public void onDisable()
	{
		getPluginLogger().info("CompitumAPI has been disabled\n");
	}


	// -------
	// GETTERS
	// -------
	public static CompitumAPI getInstance()
	{
		return instance;
	}

	public Logger getPluginLogger()
	{
		return getInstance().plugin.getLogger();
	}

}
