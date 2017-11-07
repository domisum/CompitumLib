package de.domisum.lib.compitum;

import de.domisum.lib.auxilium.util.java.annotations.API;
import de.domisum.lib.auxiliumspigot.AuxiliumSpigotLib;
import de.domisum.lib.compitum.block.evaluator.MaterialEvaluator;
import de.domisum.lib.compitum.block.evaluator.StairEvaluator;
import de.domisum.lib.compitum.navmesh.NavMeshManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

@API
public class CompitumLib
{

	// SETTINGS
	private static boolean navMeshesEnabled = false;

	// REFERENCES
	private static CompitumLib instance;
	private Plugin plugin;

	private NavMeshManager navMeshManager;


	// INIT
	private CompitumLib(JavaPlugin plugin)
	{
		this.plugin = plugin;

		onEnable();
	}

	@API public static void enable(JavaPlugin plugin)
	{
		if(instance != null)
			return;

		instance = new CompitumLib(plugin);
	}

	@API public static void disable()
	{
		if(instance == null)
			return;

		getInstance().onDisable();
		instance = null;
	}

	private void onEnable()
	{
		AuxiliumSpigotLib.enable(this.plugin);

		MaterialEvaluator.prepareEvaluation();
		StairEvaluator.prepareEvaluation();

		if(navMeshesEnabled)
		{
			this.navMeshManager = new NavMeshManager();
			this.navMeshManager.initiialize();
		}

		getLogger().info(this.getClass().getSimpleName()+" has been enabled");
	}

	private void onDisable()
	{
		if(this.navMeshManager != null)
			this.navMeshManager.terminate();

		AuxiliumSpigotLib.disable();

		getLogger().info(this.getClass().getSimpleName()+" has been disabled");
	}


	// GETTERS
	@API public static CompitumLib getInstance()
	{
		if(instance == null)
			throw new IllegalArgumentException(CompitumLib.class.getSimpleName()+" has to be initialized before usage");

		return instance;
	}

	public static Plugin getPlugin()
	{
		return getInstance().plugin;
	}

	public static Logger getLogger()
	{
		return getInstance().plugin.getLogger();
	}


	@API public static NavMeshManager getNavMeshManager()
	{
		if(!navMeshesEnabled)
			throw new IllegalStateException("The usage of NavMeshes has to be enabled first!");

		return getInstance().navMeshManager;
	}

	@API public static boolean areNavMeshesEnabled()
	{
		return navMeshesEnabled;
	}


	// SETTERS
	@API public static void enableNavMeshes()
	{
		if(instance != null)
			throw new IllegalStateException("NavMeshes have to be enabled before enabling CompitumLib");

		navMeshesEnabled = true;
	}

}
