package de.domisum.lib.compitum;

import de.domisum.lib.compitum.navmesh.NavMeshManager;
import de.domisum.lib.compitum.evaluator.MaterialEvaluator;
import de.domisum.lib.compitum.evaluator.StairEvaluator;
import de.domisum.lib.compitum.navgraph.NavGraphManager;
import de.domisum.lib.auxilium.AuxiliumLib;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

@APIUsage
public class CompitumLib
{

	// SETTINGS
	private static boolean navGraphsEnabled = false;
	private static boolean navMeshesEnabled = false;

	// REFERENCES
	private static CompitumLib instance;
	private Plugin plugin;

	private NavGraphManager navGraphManager;
	private NavMeshManager navMeshManager;


	// -------
	// CONSTRUCTOR
	// -------
	private CompitumLib(JavaPlugin plugin)
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

		new CompitumLib(plugin);
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
		AuxiliumLib.enable(this.plugin);

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

		AuxiliumLib.disable();

		getLogger().info(this.getClass().getSimpleName()+" has been disabled");
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public static CompitumLib getInstance()
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

	@APIUsage
	public static boolean areNavGraphsEnabled()
	{
		return navGraphsEnabled;
	}

	@APIUsage
	public static boolean areNavMeshesEnabled()
	{
		return navMeshesEnabled;
	}


	// -------
	// SETTERS
	// -------
	@APIUsage
	public static void enableNavGraphs()
	{
		if(getInstance() != null)
			throw new IllegalStateException("NavGraphs have to be enabled before enabling CompitumLib");

		navGraphsEnabled = true;
	}

	@APIUsage
	public static void enableNavMeshes()
	{
		if(getInstance() != null)
			throw new IllegalStateException("NavMeshes have to be enabled before enabling CompitumLib");

		navMeshesEnabled = true;
	}

}
