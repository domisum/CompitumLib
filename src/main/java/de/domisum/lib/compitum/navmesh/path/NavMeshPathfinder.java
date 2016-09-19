package de.domisum.lib.compitum.navmesh.path;


import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.compitum.navmesh.NavMesh;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPath;
import org.bukkit.Location;

@APIUsage
public class NavMeshPathfinder
{

	// INPUT
	private Location startLocation;
	private Location targetLocation;
	private NavMesh navMesh;

	// STATUS


	// OUTPUT
	private TransitionalPath path;
	private String error;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public NavMeshPathfinder(Location startLocation, Location targetLocation, NavMesh navMesh)
	{
		this.startLocation = startLocation;
		this.targetLocation = targetLocation;

		this.navMesh = navMesh;
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public TransitionalPath getPath()
	{
		return this.path;
	}

	@APIUsage
	public String getError()
	{
		return this.error;
	}


	// -------
	// PATHFINDING
	// -------
	@APIUsage
	public void findPath()
	{

	}

}
