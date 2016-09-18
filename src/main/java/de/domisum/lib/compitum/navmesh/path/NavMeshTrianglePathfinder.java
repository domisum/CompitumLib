package de.domisum.lib.compitum.navmesh.path;


import de.domisum.lib.auxilium.data.container.Duo;
import de.domisum.lib.auxilium.data.container.math.LineSegment3D;
import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.DebugUtil;
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
		if(startNode == targetNode)
		{
			this.triangleSequence.add(this.startTriangle);
			return;
		}

		NavGraphAStar pathfinder = new NavGraphAStar(startNode, targetNode);
		pathfinder.findPath();

		if(pathfinder.getPath() == null)
			return;

		for(GraphNode node : pathfinder.getPath())
			this.triangleSequence.add(this.navMesh.getTriangle(node.getId()));
	}

	private void findPathThroughTriangles()
	{
		DebugUtil.say("-------------------------------------------------START-------------------------------------------------");

		List<Duo<Vector3D, Integer>> waypoints = new ArrayList<>();

		if(this.triangleSequence.size() == 1)
		{
			waypoints.add(new Duo<>(new Vector3D(this.targetLocation), TransitionType.WALK));
			this.path = new TransitionalPath(waypoints);
			return;
		}

		Vector3D targetPosition = new Vector3D(this.targetLocation);
		Vector3D currentPosition = new Vector3D(this.startLocation);

		Vector3D lastVisLeft = null;
		Vector3D lastVisRight = null;
		Vector3D towardsLastVisLeft;
		Vector3D towardsLastVisRight;

		int lastVisRightTriangleIndex = -1;
		int lastVisLeftTriangleIndex = -1;

		NavMeshTriangle lastTriangle = null;
		for(int i = 0; i < this.triangleSequence.size(); i++)
		{
			NavMeshTriangle t = this.triangleSequence.get(i);
			for(int j = 0; j < 3; j++)
				DebugUtil.say("");
			DebugUtil.say("triangle: "+t.id);
			DebugUtil.say("lastTriangle: "+(lastTriangle == null ? null : lastTriangle.id));
			DebugUtil.say("currentPos: "+currentPosition);
			DebugUtil.say("lastVisLeft: "+lastVisLeft);
			DebugUtil.say("lastVisRight: "+lastVisRight);

			portalProcessing:
			{
				if(t == this.startTriangle)
					break portalProcessing;

				NavMeshTrianglePortal portal = lastTriangle.getPortalTo(t);
				LineSegment3D portalLineSegment = portal.getPlayerLineSegment();

				// find out current left and right
				Vector3D pointLeft = portalLineSegment.a;
				Vector3D pointRight = portalLineSegment.b;

				Vector3D lastTriangleCenter = lastTriangle.getCenter();

				Vector3D towardsPointLeft = pointLeft.subtract(lastTriangleCenter);
				Vector3D towardsPointRight = pointRight.subtract(lastTriangleCenter);
				if(isLeftOf(towardsPointRight, towardsPointLeft, false))
				{
					Vector3D temp = pointLeft;
					pointLeft = pointRight;
					pointRight = temp;
				}

				towardsPointLeft = pointLeft.subtract(currentPosition);
				towardsPointRight = pointRight.subtract(currentPosition);

				DebugUtil.say("pointLeft: "+pointLeft);
				DebugUtil.say("pointRight: "+pointRight);

				// initial iteration or last iteration found a new base point
				if(lastVisLeft == null) // if that is the case, lastVisRight will be null too
				{
					lastVisLeft = pointLeft;
					lastVisRight = pointRight;
					lastVisLeftTriangleIndex = i;
					lastVisRightTriangleIndex = i;

					break portalProcessing;
				}

				towardsLastVisLeft = lastVisLeft.subtract(currentPosition);
				towardsLastVisRight = lastVisRight.subtract(currentPosition);


				// check if one point is outside on the other side
				// left turn
				if(isLeftOf(towardsPointRight, towardsLastVisLeft, false))
				{
					DebugUtil.say("leftTurn");

					currentPosition = lastVisLeft;
					waypoints.add(new Duo<>(currentPosition, TransitionType.WALK));
					int iCurrent = i;
					i = lastVisLeftTriangleIndex+1; // no need for +1 because the loop automatically increases by one
					lastTriangle = this.triangleSequence.get(lastVisLeftTriangleIndex+1);

					lastVisLeft = pointLeft;
					lastVisRight = pointRight;
					lastVisLeftTriangleIndex = iCurrent;
					lastVisRightTriangleIndex = iCurrent;

					// breaks the updating because the left and right last vis should only be updated if no turn occurs
					// continue instead of break so the custom lastTriangle doesn't get updated
					continue;
				}
				// right turn
				else if(isLeftOf(towardsLastVisRight, towardsPointLeft, false))
				{
					DebugUtil.say("rightTurn");

					currentPosition = lastVisRight;
					waypoints.add(new Duo<>(currentPosition, TransitionType.WALK));
					int iCurrent = i;
					i = lastVisRightTriangleIndex+1; // no need for +1 because the loop automatically increases by one
					lastTriangle = this.triangleSequence.get(lastVisRightTriangleIndex+1);

					lastVisLeft = pointLeft;
					lastVisRight = pointRight;
					lastVisLeftTriangleIndex = iCurrent;
					lastVisRightTriangleIndex = iCurrent;

					// breaks the updating because the left and right last vis should only be updated if no turn occurs
					// continue instead of break so the custom lastTriangle doesn't get updated
					continue;
				}


				// update the last vis variables if they are further inwards
				if(isLeftOf(towardsPointRight, towardsLastVisRight, true))
				{
					DebugUtil.say("update lastVisRight");
					lastVisRight = pointRight;

					lastVisRightTriangleIndex = i;
				}

				if(isLeftOf(towardsLastVisLeft, towardsPointLeft, true))
				{
					DebugUtil.say("update lastVisLeft");
					lastVisLeft = pointLeft;

					lastVisLeftTriangleIndex = i;
				}
			}

			lastTriangle = t;
		}

		DebugUtil.say("endCurrentPos: "+currentPosition);
		DebugUtil.say("endLastVisLeft: "+lastVisLeft);
		DebugUtil.say("endLastVisRight: "+lastVisRight);

		// now check if the target is visible through the portal left by the lastVis points, if not add another corner to the path
		Vector3D towardsTarget = targetPosition.subtract(currentPosition);
		towardsLastVisLeft = lastVisLeft.subtract(currentPosition);
		towardsLastVisRight = lastVisRight.subtract(currentPosition);
		// switch points if needed for last
		if(isLeftOf(towardsLastVisRight, towardsLastVisLeft, false))
		{
			Vector3D temp = lastVisLeft;
			lastVisLeft = lastVisRight;
			lastVisRight = temp;

			temp = towardsLastVisLeft;
			towardsLastVisLeft = towardsLastVisRight;
			towardsLastVisRight = temp;
		}

		// left turn
		if(isLeftOf(towardsTarget, towardsLastVisLeft, false))
		{
			DebugUtil.say("endLeftTurn");
			waypoints.add(new Duo<>(lastVisLeft, TransitionType.WALK));
		}
		// right turn
		else if(isLeftOf(towardsLastVisRight, towardsTarget, false))
		{
			waypoints.add(new Duo<>(lastVisRight, TransitionType.WALK));
			DebugUtil.say("endRightTurn");
		}

		// target
		waypoints.add(new Duo<>(targetPosition, TransitionType.WALK));

		for(int i = 0; i < 3; i++)
			DebugUtil.say("");
		DebugUtil.say("waypoints:");
		for(Duo<Vector3D, Integer> w : waypoints)
			DebugUtil.say(w.a);

		this.path = new TransitionalPath(waypoints);
	}


	// -------
	// UTIL
	// -------
	private static boolean isLeftOf(Vector3D v1, Vector3D v2, boolean onZero)
	{
		double y = getCrossProductY(v1, v2);

		if(y == 0)
			return onZero;

		return y < 0;
	}

	private static double getCrossProductY(Vector3D v1, Vector3D v2)
	{
		return v1.crossProduct(v2).y;
	}

}
