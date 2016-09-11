package de.domisum.compitumapi;

import de.domisum.auxiliumapi.AuxiliumAPI;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.compitumapi.evaluator.MaterialEvaluator;
import de.domisum.compitumapi.evaluator.StairEvaluator;
import de.domisum.compitumapi.navgraph.NavGraphManager;
import de.domisum.compitumapi.navmesh.NavMeshManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

@APIUsage
public class CompitumAPI
{

	// SETTINGS
	private static boolean navGraphsEnabled = false;
	private static boolean navMeshesEnabled = false;

	// REFERENCES
	private static CompitumAPI instance;
	private Plugin plugin;

	private NavGraphManager navGraphManager;
	private NavMeshManager navMeshManager;


	// -------
	// CONSTRUCTOR
	// -------
	private CompitumAPI(JavaPlugin plugin)
	{
		instance = this;
		this.plugin = plugin;

		onEnable();
	}

	@APIUsage
	public static void enable(JavaPlugin plugin)
	{
		if(instance != null)
			return;

		new CompitumAPI(plugin);
	}

	@APIUsage
	public static void disable()
	{
		if(instance == null)
			return;

		getInstance().onDisable();
		instance = null;
	}

	private void onEnable()
	{
		AuxiliumAPI.enable(this.plugin);

		MaterialEvaluator.prepareEvaluation();
		StairEvaluator.prepareEvaluation();

		if(navGraphsEnabled)
		{
			this.navGraphManager = new NavGraphManager();
			this.navGraphManager.initiialize();
		}
		if(navMeshesEnabled)
		{
			this.navMeshManager = new NavMeshManager();
			this.navMeshManager.initiialize();
		}

		getLogger().info(this.getClass().getSimpleName()+" has been enabled");
	}

	private void onDisable()
	{
		if(this.navGraphManager != null)
			this.navGraphManager.terminate();
		if(this.navMeshManager != null)
			this.navMeshManager.terminate();

		getLogger().info(this.getClass().getSimpleName()+" has been disabled");
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public static CompitumAPI getInstance()
	{
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


	@APIUsage
	public static NavGraphManager getNavGraphManager()
	{
		if(!navGraphsEnabled)
			throw new IllegalStateException("The usage of NavGraphs has to be enabled first!");

		return getInstance().navGraphManager;
	}

	@APIUsage
	public static NavMeshManager getNavMeshManager()
	{
		if(!navMeshesEnabled)
			throw new IllegalStateException("The usage of NavMeshes has to be enabled first!");

		return getInstance().navMeshManager;
	}


	// -------
	// SETTERS
	// -------
	@APIUsage
	public static void enableNavGraphs()
	{
		navGraphsEnabled = true;
	}

	@APIUsage
	public static void enableNavMeshes()
	{
		navMeshesEnabled = true;
	}

}
