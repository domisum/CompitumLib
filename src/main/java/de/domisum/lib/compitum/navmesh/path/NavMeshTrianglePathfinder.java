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

	private List<NavMeshTriangle> triangleSequence = new ArrayList<>();

	private List<Duo<Vector3D, Integer>> waypoints = new ArrayList<>();
	// triangle traversal
	private Vector3D currentPosition;
	private Vector3D targetPosition;
	private Vector3D visLeft;
	private Vector3D visRight;
	private int visLeftTriangleIndex;
	private int visRightTriangleIndex;

	private int currentTriangleIndex = 0;

	private Vector3D portalEndpointLeft;
	private Vector3D portalEndpointRight;

	// benchmarking
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
		NavMeshTriangle startTriangle = this.navMesh.getTriangleAt(this.startLocation);
		NavMeshTriangle targetTriangle = this.navMesh.getTriangleAt(this.targetLocation);

		if(startTriangle == null || targetTriangle == null)
			return;

		GraphNode startNode = navGraph.getNode(startTriangle.id);
		GraphNode targetNode = navGraph.getNode(targetTriangle.id);
		if(startNode == targetNode)
		{
			this.triangleSequence.add(startTriangle);
			return;
		}

		NavGraphAStar pathfinder = new NavGraphAStar(startNode, targetNode);
		pathfinder.findPath();

		if(pathfinder.getPath() == null)
			return;

		for(GraphNode node : pathfinder.getPath())
			this.triangleSequence.add(this.navMesh.getTriangle(node.getId()));
	}

	// TRIANGLE TRAVERSAL
	private void findPathThroughTriangles()
	{
		DebugUtil.say("--------------------------------------------START--------------------------------------------");

		triangleTraversal:
		{
			this.currentPosition = new Vector3D(this.startLocation);
			this.targetPosition = new Vector3D(this.targetLocation);

			if(this.triangleSequence.size() == 1)
			{
				this.waypoints.add(new Duo<>(this.targetPosition, TransitionType.WALK));
				break triangleTraversal;
			}

			for(this.currentTriangleIndex = 0;
			    this.currentTriangleIndex < this.triangleSequence.size(); this.currentTriangleIndex++)
				traverseTriangle();
		}

		DebugUtil.say("WAYPOINTS:");
		for(Duo<Vector3D, Integer> wp : this.waypoints)
			DebugUtil.say(wp.a);

		this.path = new TransitionalPath(this.waypoints);
	}

	private void traverseTriangle()
	{
		NavMeshTriangle triangle = this.triangleSequence.get(this.currentTriangleIndex);
		NavMeshTriangle triangleAfter = this.currentTriangleIndex+1 < this.triangleSequence.size() ?
				this.triangleSequence.get(this.currentTriangleIndex+1) :
				null;

		DebugUtil.say("");
		DebugUtil.say("tri: "+triangle.id);
		DebugUtil.say("triAfter: "+(triangleAfter != null ? triangleAfter.id : null));
		DebugUtil.say("currentPos: "+this.currentPosition);
		DebugUtil.say("visLeft: "+this.visLeft);
		DebugUtil.say("visRight: "+this.visRight);
		DebugUtil.say("portalLeft: "+this.portalEndpointLeft);
		DebugUtil.say("portalRight: "+this.portalEndpointRight);

		if(triangleAfter == null) // last triangle
		{
			DebugUtil.say("#endTriangle");
			// visLeft can be null if the transition into the last triangle was a turn
			if(this.visLeft != null)
			{
				Vector3D towardsVisLeft = this.visLeft.subtract(this.currentPosition);
				Vector3D towardsVisRight = this.visRight.subtract(this.currentPosition);

				Vector3D towardsTarget = this.targetPosition.subtract(this.currentPosition);

				if(isLeftOf(towardsVisRight, towardsTarget, false)) // right curve
				{
					newWaypoint(this.visRight, TransitionType.WALK);
				}
				else if(isLeftOf(towardsTarget, towardsVisLeft, false)) // left curve
				{
					newWaypoint(this.visLeft, TransitionType.WALK);
				}
			}

			this.waypoints.add(new Duo<>(this.targetPosition, TransitionType.WALK));
		}
		// either first triangle processing or after new corner
		else if(this.visLeft == null) // if visLeft is null, then visRight is also null
		{
			DebugUtil.say("#vision points null");
			findPortalEndpoints(triangle, triangleAfter);
			this.visLeft = this.portalEndpointLeft;
			this.visRight = this.portalEndpointRight;
			this.visLeftTriangleIndex = this.currentTriangleIndex;
			this.visRightTriangleIndex = this.currentTriangleIndex;
		}
		else
		{
			DebugUtil.say("#default");

			findPortalEndpoints(triangle, triangleAfter);

			Vector3D towardsVisLeft = this.visLeft.subtract(this.currentPosition);
			Vector3D towardsVisRight = this.visRight.subtract(this.currentPosition);

			Vector3D towardsPortalEndpointLeft = this.portalEndpointLeft.subtract(this.currentPosition);
			Vector3D towardsPortalEndpointRight = this.portalEndpointRight.subtract(this.currentPosition);

			boolean leftSame = isSame(this.visLeft, this.currentPosition);
			boolean rightSame = isSame(this.visRight, this.currentPosition);

			// check if portal is out on one side
			if(isLeftOf(towardsVisRight, towardsPortalEndpointLeft, true) && !leftSame && !rightSame) // right turn
			{
				DebugUtil.say("---rightTurn");
				newWaypoint(this.visRight, TransitionType.WALK);

				this.currentTriangleIndex = this.visRightTriangleIndex;
				return;
			}
			else if(isLeftOf(towardsPortalEndpointRight, towardsVisLeft, true) && !leftSame && !rightSame) // left turn
			{
				DebugUtil.say("---leftTurn");
				newWaypoint(this.visLeft, TransitionType.WALK);

				this.currentTriangleIndex = this.visLeftTriangleIndex;
				return;
			}

			// confine movement cone
			if(isLeftOf(towardsVisLeft, towardsPortalEndpointLeft, true)) // left
			{
				DebugUtil.say("---visConeLeft");
				this.visLeft = this.portalEndpointLeft;
				this.visLeftTriangleIndex = this.currentTriangleIndex;
			}
			if(isLeftOf(towardsPortalEndpointRight, towardsVisRight, true)) // right
			{
				DebugUtil.say("---visConeRight");
				this.visRight = this.portalEndpointRight;
				this.visRightTriangleIndex = this.currentTriangleIndex;
			}
		}
	}

	private void findPortalEndpoints(NavMeshTriangle from, NavMeshTriangle to)
	{
		LineSegment3D portalLineSegment = from.getPortalTo(to).getFullLineSegment();

		this.portalEndpointLeft = portalLineSegment.a;
		this.portalEndpointRight = portalLineSegment.b;

		Vector3D fromCenter = from.getCenter();
		if(isLeftOf(this.portalEndpointRight.subtract(fromCenter), this.portalEndpointLeft.subtract(fromCenter), false))
		{
			Vector3D temp = this.portalEndpointLeft;
			this.portalEndpointLeft = this.portalEndpointRight;
			this.portalEndpointRight = temp;
		}
	}

	private void newWaypoint(Vector3D position, int transitionType)
	{
		this.waypoints.add(new Duo<>(position, transitionType));
		this.currentPosition = position;

		this.visLeft = null;
		this.visRight = null;
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


	private static boolean isSame(Vector3D a, Vector3D b)
	{
		if(a == null)
			return b == null;

		return a.equals(b);
	}

}
