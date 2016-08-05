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

	public static void initialize(JavaPlugin plugin)
	{
		if(instance != null)
			return;

		new CompitumAPI(plugin);
	}

	public void onEnable()
	{
		MaterialEvaluator.prepareEvaluation();

		getLogger().info(this.getClass().getSimpleName() + " has been enabled");
	}

	public void onDisable()
	{
		getLogger().info(this.getClass().getSimpleName() + " has been disabled");
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
