package de.domisum.lib.compitum.universal;


import de.domisum.lib.auxilium.util.bukkit.LocationUtil;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.compitum.CompitumLib;
import de.domisum.lib.compitum.navmesh.NavMesh;
import de.domisum.lib.compitum.navmesh.NavMeshManager;
import de.domisum.lib.compitum.navmesh.path.NavMeshTrianglePathfinder;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalBlockPath;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPath;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPathSmoother;
import de.domisum.lib.compitum.transitionalpath.pathfinders.TransitionalAStar;
import org.bukkit.Location;

@APIUsage
public class UniversalPathfinder
{

	// INPUT
	private Location start;
	private Location target;

	// PROPERTIES


	// OUTPUT
	private TransitionalPath path;

	private String diagnose;
	private String error;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public UniversalPathfinder(Location start, Location target)
	{
		this.start = fixPathfindingLocation(start);
		this.target = fixPathfindingLocation(target);
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public TransitionalPath getPath()
	{
		return path;
	}

	@APIUsage
	public boolean isPathFound()
	{
		return path != null;
	}


	@APIUsage
	public String getDiagnose()
	{
		return diagnose;
	}

	@APIUsage
	public String getError()
	{
		return error;
	}


	// -------
	// PATHFINDING
	// -------
	@APIUsage
	public void findPath()
	{
		navMeshCheck:
		if(CompitumLib.areNavMeshesEnabled())
		{
			NavMeshManager nmm = CompitumLib.getNavMeshManager();
			NavMesh meshAtStart = nmm.getNavMeshAt(start);
			if(meshAtStart == null)
				break navMeshCheck;

			NavMesh meshAtTarget = nmm.getNavMeshAt(target);
			if(meshAtTarget == null)
				break navMeshCheck;

			if(meshAtStart != meshAtTarget)
				break navMeshCheck;

			useNavMesh(meshAtStart);
			return;
		}

		useWorldAStar();
	}


	private void useWorldAStar()
	{
		TransitionalAStar pathfinder = new TransitionalAStar(start, target);
		pathfinder.findPath();
		diagnose = pathfinder.getDiagnose();

		if(!pathfinder.pathFound())
		{
			this.error = pathfinder.getError();
			return;
		}
		TransitionalBlockPath blockPath = pathfinder.getPath();

		TransitionalPathSmoother smoother = new TransitionalPathSmoother(blockPath);
		smoother.convert();
		path = smoother.getSmoothPath();
	}

	private void useNavMesh(NavMesh navMesh)
	{
		NavMeshTrianglePathfinder pathfinder = new NavMeshTrianglePathfinder(start, target, navMesh);
		pathfinder.findPath();
		diagnose = pathfinder.getDiagnose();
		if(!pathfinder.pathFound())
		{
			this.error = pathfinder.getError();
			return;
		}

		path = pathfinder.getPath();
	}


	@APIUsage
	public static Location fixPathfindingLocation(Location location)
	{
		location = LocationUtil.getFloorCenter(location);

		String materialName = location.getBlock().getType().name();
		if(materialName.contains("SLAB") || materialName.contains("STEP"))
			location = location.add(0, 1, 0);

		return location;
	}

}
