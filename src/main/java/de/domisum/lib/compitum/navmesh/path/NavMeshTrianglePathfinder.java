package de.domisum.lib.compitum.navmesh.path;


import de.domisum.lib.auxilium.data.container.Duo;
import de.domisum.lib.auxilium.data.container.math.LineSegment3D;
import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.DebugUtil;
import de.domisum.lib.auxilium.util.TextUtil;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxilium.util.math.MathUtil;
import de.domisum.lib.compitum.navgraph.GraphNode;
import de.domisum.lib.compitum.navgraph.NavGraph;
import de.domisum.lib.compitum.navgraph.pathfinding.NavGraphAStar;
import de.domisum.lib.compitum.navmesh.NavMesh;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTrianglePortal;
import de.domisum.lib.compitum.transitionalpath.node.TransitionType;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPath;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@APIUsage
public class NavMeshTrianglePathfinder
{

	// INPUT
	private Location startLocation;
	private Location targetLocation;

	private NavMesh navMesh;

	private NavMeshTriangle startTriangle;
	private List<NavMeshTriangle> triangleSequence = new ArrayList<>();

	private long pathfindingStartNano;
	private long pathfindingEndNano;

	// OUTPUT
	private TransitionalPath path;
	private String error;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public NavMeshTrianglePathfinder(Location startLocation, Location targetLocation, NavMesh navMesh)
	{
		this.startLocation = startLocation;
		this.targetLocation = targetLocation;

		this.navMesh = navMesh;
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public boolean pathFound()
	{
		return this.path != null;
	}

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


	private long getNanoDuration()
	{
		return this.pathfindingEndNano-this.pathfindingStartNano;
	}

	private double getMsDuration()
	{
		return MathUtil.round(getNanoDuration()/1000d/1000, 2);
	}

	@APIUsage
	public String getDiagnose()
	{
		String diagnose = "";

		diagnose += "found="+pathFound()+", ";
		/*if(pathFound())
			diagnose += "length="+getPath().getLength()+", ";*/

		/*diagnose += "visitedNodes="+this.visitedNodes.size()+", ";
		diagnose += "unvisitedNodes="+this.unvisitedNodes.getSize()+", ";*/
		diagnose += "durationMs="+getMsDuration()+", ";

		return diagnose;
	}


	// -------
	// PATHFINDING
	// -------
	@APIUsage
	public void findPath()
	{
		this.pathfindingStartNano = System.nanoTime();

		// validation
		if(this.startLocation.getWorld() != this.targetLocation.getWorld())
		{
			this.error = "The start and target location are not in the same world";
			return;
		}

		pathfinding:
		{
			findTriangleSequence();
			if(this.triangleSequence.size() == 0)
				break pathfinding;

			findPathThroughTriangles();
		}

		this.pathfindingEndNano = System.nanoTime();
	}

	private void findTriangleSequence()
	{
		NavGraph navGraph = this.navMesh.getNavGraph();
		this.startTriangle = this.navMesh.getTriangleAt(this.startLocation);
		NavMeshTriangle targetTriangle = this.navMesh.getTriangleAt(this.targetLocation);

		if(this.startTriangle == null || targetTriangle == null)
			return;

		GraphNode startNode = navGraph.getNode(this.startTriangle.id);
		GraphNode targetNode = navGraph.getNode(targetTriangle.id);

		NavGraphAStar pathfinder = new NavGraphAStar(startNode, targetNode);
		pathfinder.findPath();

		if(pathfinder.getPath() == null)
			return;

		for(GraphNode node : pathfinder.getPath())
			this.triangleSequence.add(this.navMesh.getTriangle(node.getId()));
	}

	private void findPathThroughTriangles()
	{
		List<Duo<Vector3D, Integer>> waypoints = new ArrayList<>();

		Vector3D targetPosition = new Vector3D(this.targetLocation);
		Vector3D currentPosition = new Vector3D(this.startLocation);

		Vector3D lastVisLeft = null;
		Vector3D lastVisRight = null;

		NavMeshTriangle lastTriangle = null;
		for(NavMeshTriangle t : this.triangleSequence)
		{
			portalProcessing:
			{
				if(t == this.startTriangle)
					break portalProcessing;

				NavMeshTrianglePortal portal = lastTriangle.getPortalTo(t);
				LineSegment3D portalLineSegment = portal.getPlayerLineSegment();

				// find out current left and right
				Vector3D pointLeft = portalLineSegment.a;
				Vector3D pointRight = portalLineSegment.b;

				Vector3D towardsPointLeft = pointLeft.subtract(currentPosition);
				Vector3D towardsPointRight = pointRight.subtract(currentPosition);
				if(isLeftOf(towardsPointRight, towardsPointLeft))
				{
					Vector3D temp = pointLeft;
					pointLeft = pointRight;
					pointRight = temp;

					temp = towardsPointLeft;
					towardsPointLeft = towardsPointRight;
					towardsPointRight = temp;
				}

				// initial iteration, just set the lastVis to the first portal
				if(lastVisLeft == null) // if that is the case, lastVisRight will be null too
				{
					lastVisLeft = pointLeft;
					lastVisRight = pointRight;
					break portalProcessing;
				}

				Vector3D towardsLastVisLeft = lastVisLeft.subtract(currentPosition);
				Vector3D towardsLastVisRight = lastVisRight.subtract(currentPosition);

				// update the last vis variables if they are further inwards
				if(isLeftOf(towardsPointRight, towardsLastVisRight))
				{
					lastVisRight = pointRight;
					towardsLastVisRight = lastVisRight.subtract(currentPosition);
				}

				if(isLeftOf(towardsLastVisLeft, towardsPointLeft))
				{
					lastVisLeft = pointLeft;
					towardsLastVisLeft = lastVisLeft.subtract(currentPosition);
				}

				// check if one point is outside on the other side
				// left turn
				if(isLeftOf(towardsPointRight, towardsLastVisLeft))
				{
					currentPosition = lastVisLeft;
					waypoints.add(new Duo<>(currentPosition, TransitionType.WALK));

					lastVisLeft = pointLeft;
					lastVisRight = pointRight;
				}

				// right turn
				if(isLeftOf(towardsLastVisRight, towardsPointLeft))
				{
					currentPosition = lastVisRight;
					waypoints.add(new Duo<>(currentPosition, TransitionType.WALK));

					lastVisLeft = pointLeft;
					lastVisRight = pointRight;
				}
			}

			lastTriangle = t;
		}

		// now check if the target is visible through the portal left by the lastVis points, if not add another corner to the path
		Vector3D towardsTarget = targetPosition.subtract(currentPosition);
		Vector3D towardsLastVisLeft = lastVisLeft.subtract(currentPosition);
		Vector3D towardsLastVisRight = lastVisRight.subtract(currentPosition);
		if(isLeftOf(towardsTarget, towardsLastVisLeft))
			waypoints.add(new Duo<>(lastVisLeft, TransitionType.WALK));
		else if(isLeftOf(towardsLastVisRight, towardsLastVisLeft))
			waypoints.add(new Duo<>(lastVisLeft, TransitionType.WALK));

		// target
		waypoints.add(new Duo<>(targetPosition, TransitionType.WALK));

		DebugUtil.say(TextUtil.getListAsString(waypoints));

		this.path = new TransitionalPath(waypoints);
	}


	// -------
	// UTIL
	// -------
	private static boolean isLeftOf(Vector3D v1, Vector3D v2)
	{
		return getCrossProductY(v1, v2) >= 0;
	}

	private static double getCrossProductY(Vector3D v1, Vector3D v2)
	{
		return v1.crossProduct(v2).y;
	}

}
