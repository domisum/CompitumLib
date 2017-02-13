package de.domisum.lib.compitum.navmesh.path;

import de.domisum.lib.auxilium.util.java.ProfilerStopWatch;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxiliumspigot.util.LocationUtil;
import de.domisum.lib.compitum.navmesh.NavMesh;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.navmesh.path.traversal.NavMeshTriangleTraverser;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPath;
import org.bukkit.Location;

import java.util.List;

@APIUsage
public class NavMeshPathfinder
{

	// INPUT
	private Location startLocation;
	private Location targetLocation;
	private NavMesh navMesh;

	// STATUS
	private ProfilerStopWatch stopWatch = new ProfilerStopWatch("pathfinding.navMesh");
	private ProfilerStopWatch triangleFindingStopWatch = new ProfilerStopWatch("pathfinding.navMesh.startTargetTriangles");

	// OUTPUT
	private TransitionalPath path;
	private String failure;


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
	public TransitionalPath getPath()
	{
		return this.path;
	}

	@APIUsage
	public String getFailure()
	{
		return this.failure;
	}


	public ProfilerStopWatch getStopWatch()
	{
		return this.stopWatch;
	}


	// -------
	// PATHFINDING
	// -------
	@APIUsage
	public void findPath()
	{
		this.stopWatch.start();
		this.triangleFindingStopWatch.start();
		NavMeshTriangle startTriangle = this.navMesh.getTriangleAt(this.startLocation);
		if(startTriangle == null)
		{
			this.failure = "Start location is not on NavMesh";
			return;
		}

		NavMeshTriangle targetTriangle = this.navMesh.getTriangleAt(this.targetLocation);
		if(targetTriangle == null)
		{
			this.failure = "Target location is not on NavMesh";
			return;
		}

		this.triangleFindingStopWatch.stop();

		NavMeshTrianglePathfinder trianglePathfinder = new NavMeshTrianglePathfinder(startTriangle, targetTriangle);
		trianglePathfinder.findPath();
		List<NavMeshTriangle> triangleSequence = trianglePathfinder.getTriangleSequence();
		if(triangleSequence == null)
		{
			this.failure = trianglePathfinder.getFailure();
			return;
		}

		NavMeshTriangleTraverser triangleTraverser = new NavMeshTriangleTraverser(LocationUtil.toVector3D(this.startLocation),
				LocationUtil.toVector3D(this.targetLocation), triangleSequence);
		triangleTraverser.traverseTriangles();
		this.path = triangleTraverser.getPath();

		this.stopWatch.stop();


		/*DebugUtil.say("");
		DebugUtil.say(getStopWatch());
		DebugUtil.say(this.triangleFindingStopWatch);
		DebugUtil.say(trianglePathfinder.getStopWatch());
		DebugUtil.say(triangleTraverser.getStopWatch());*/
	}

}
