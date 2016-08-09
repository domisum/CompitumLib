package de.domisum.compitumapi;

import de.domisum.compitumapi.path.MaterialEvaluator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class CompitumAPI
{

	// REFERENCES
	private static CompitumAPI instance;
	private JavaPlugin plugin;


	// -------
	// CONSTRUCTOR
	// -------
	protected CompitumAPI(JavaPlugin plugin)
	{
		instance = this;
		this.plugin = plugin;

		onEnable();
	}

	public static void enable(JavaPlugin plugin)
	{
		if(instance != null)
			return;

		new CompitumAPI(plugin);
	}

	public static void disable()
	{
		if(instance == null)
			return;

		getInstance().onDisable();
		instance = null;
	}

	protected void onEnable()
	{
		MaterialEvaluator.prepareEvaluation();

		getLogger().info(this.getClass().getSimpleName()+" has been enabled");
	}

	protected void onDisable()
	{
		getLogger().info(this.getClass().getSimpleName()+" has been disabled");
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
