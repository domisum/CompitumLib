package de.domisum.lib.compitum.navmesh.path.traversal;

import de.domisum.lib.auxilium.data.container.math.LineSegment3D;
import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.TextUtil;
import de.domisum.lib.auxilium.util.java.debug.DebugUtil;
import de.domisum.lib.auxilium.util.java.debug.ProfilerStopWatch;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.navmesh.transition.NavMeshLadder;
import de.domisum.lib.compitum.navmesh.transition.NavMeshTrianglePortal;
import de.domisum.lib.compitum.navmesh.transition.NavMeshTriangleTransition;
import de.domisum.lib.compitum.transitionalpath.node.TransitionType;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPath;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalWaypoint;

import java.util.ArrayList;
import java.util.List;

public class NavMeshTriangleTraverser
{

	// INPUT
	private Vector3D startPosition;
	private Vector3D targetPosition;
	private List<NavMeshTriangle> triangleSequence;

	// STATUS
	private List<TransitionalWaypoint> waypoints = new ArrayList<>();
	// triangle traversal
	private Vector3D currentPosition;
	private Vector3D visLeft;
	private Vector3D visRight;
	private int visLeftTriangleIndex;
	private int visRightTriangleIndex;

	private int triangleFromIndex = 0;
	private NavMeshTriangle triangle;
	private NavMeshTriangle triangleAfter;

	private Vector3D portalEndpointLeft;
	private Vector3D portalEndpointRight;

	private ProfilerStopWatch stopWatch = new ProfilerStopWatch("pathfinding.navMesh.triangleTraversal");

	// OUTPUT
	private TransitionalPath path;


	// -------
	// CONSTRUCTOR
	// -------
	public NavMeshTriangleTraverser(Vector3D startPosition, Vector3D targetPosition, List<NavMeshTriangle> triangleSequence)
	{
		this.startPosition = startPosition;
		this.targetPosition = targetPosition;

		this.triangleSequence = triangleSequence;
	}


	// -------
	// GETTERS
	// -------
	public TransitionalPath getPath()
	{
		return this.path;
	}

	public ProfilerStopWatch getStopWatch()
	{
		return this.stopWatch;
	}


	// -------
	// TRAVERSAL
	// -------
	public void traverseTriangles()
	{
		this.stopWatch.start();

		this.currentPosition = this.startPosition;

		if(this.triangleSequence.size() == 1)
			this.waypoints.add(new TransitionalWaypoint(this.targetPosition, TransitionType.WALK));
		else
		{
			for(this.triangleFromIndex = 0; this.triangleFromIndex < this.triangleSequence.size(); this.triangleFromIndex++)
				processTriangleTransition();
		}

		this.path = new TransitionalPath(this.waypoints);
		this.stopWatch.stop();

		DebugUtil.say(TextUtil.getListAsString(this.waypoints));
	}


	private void processTriangleTransition()
	{
		this.triangle = this.triangleSequence.get(this.triangleFromIndex);
		this.triangleAfter = this.triangleFromIndex+1 < this.triangleSequence.size() ?
				this.triangleSequence.get(this.triangleFromIndex+1) :
				null;

		NavMeshTriangleTransition transition = this.triangle.getTransitionTo(this.triangleAfter);

		if(this.triangleAfter == null)
		{
			traverseTrianglePortal();
			processMovementTowardsTargetPoint(this.targetPosition);
		}
		else if(transition.getTransitionType() == TransitionType.WALK)
			traverseTrianglePortal();
		else if(transition.getTransitionType() == TransitionType.CLIMB)
			useLadder();
	}


	// WALKING
	private void traverseTrianglePortal()
	{
		DebugUtil.say("from: "+this.triangle+" to: "+this.triangleAfter);

		// last triangle
		if(this.triangleAfter == null)
		{
			// simulate portal at end point, so the boundaries are breached if the target is not in the cone
			this.portalEndpointLeft = this.targetPosition;
			this.portalEndpointRight = this.targetPosition;
		}

		// either first triangle processing or after new corner
		if(this.visLeft == null) // if visLeft is null, then visRight is also null
		{
			if(this.triangleAfter != null) // check if last triangle
				findPortalEndpoints(this.triangle, this.triangleAfter);
			this.visLeft = this.portalEndpointLeft;
			this.visRight = this.portalEndpointRight;
			this.visLeftTriangleIndex = this.triangleFromIndex;
			this.visRightTriangleIndex = this.triangleFromIndex;

			return;
		}
		else if(this.triangleAfter != null) // only if not last triangle
			findPortalEndpoints(this.triangle, this.triangleAfter);


		Vector3D towardsVisLeft = this.visLeft.subtract(this.currentPosition);
		Vector3D towardsVisRight = this.visRight.subtract(this.currentPosition);

		Vector3D towardsPortalEndpointLeft = this.portalEndpointLeft.subtract(this.currentPosition);
		Vector3D towardsPortalEndpointRight = this.portalEndpointRight.subtract(this.currentPosition);

		boolean leftSame = isSame(this.visLeft, this.currentPosition);
		boolean rightSame = isSame(this.visRight, this.currentPosition);

		// check if portal is out on one side
		if(isLeftOf(towardsVisRight, towardsPortalEndpointLeft, true) && !leftSame && !rightSame) // right turn
		{
			DebugUtil.say("rightTurn: "+this.visRight);
			newWaypoint(this.visRight, TransitionType.WALK);

			this.triangleFromIndex = this.visRightTriangleIndex;
			return;
		}
		else if(isLeftOf(towardsPortalEndpointRight, towardsVisLeft, true) && !leftSame && !rightSame) // left turn
		{
			DebugUtil.say("leftTurn: "+this.visLeft);
			newWaypoint(this.visLeft, TransitionType.WALK);

			this.triangleFromIndex = this.visLeftTriangleIndex;
			return;
		}

		// confine movement cone
		if(isLeftOf(towardsVisLeft, towardsPortalEndpointLeft, true)) // left
		{
			DebugUtil.say("confine left");
			this.visLeft = this.portalEndpointLeft;
			this.visLeftTriangleIndex = this.triangleFromIndex;
		}
		if(isLeftOf(towardsPortalEndpointRight, towardsVisRight, true)) // right
		{
			DebugUtil.say("confine right");
			this.visRight = this.portalEndpointRight;
			this.visRightTriangleIndex = this.triangleFromIndex;
		}
	}

	private void findPortalEndpoints(NavMeshTriangle from, NavMeshTriangle to)
	{
		NavMeshTriangleTransition transition = from.getTransitionTo(to);
		LineSegment3D portalLineSegment = ((NavMeshTrianglePortal) transition).getFullLineSegment();

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

	private void processMovementTowardsTargetPoint(Vector3D targetPoint)
	{
		// the vis points can be null if the transition into the previous triangle was a turn
		if(this.visLeft != null)
		{
			Vector3D towardsVisLeft = this.visLeft.subtract(this.currentPosition);
			Vector3D towardsVisRight = this.visRight.subtract(this.currentPosition);

			Vector3D towardsTargetPoint = targetPoint.subtract(this.currentPosition);

			if(isLeftOf(towardsVisRight, towardsTargetPoint, false)) // right turn
			{
				DebugUtil.say("final rightTurn: "+this.visRight);
				newWaypoint(this.visRight, TransitionType.WALK);
			}
			else if(isLeftOf(towardsTargetPoint, towardsVisLeft, false)) // left turn
			{
				DebugUtil.say("final leftTurn: "+this.visLeft);
				newWaypoint(this.visLeft, TransitionType.WALK);
			}
		}

		this.waypoints.add(new TransitionalWaypoint(targetPoint, TransitionType.WALK));
	}


	// LADDER CLIMBING
	private void useLadder()
	{
		NavMeshTriangleTransition transition = this.triangle.getTransitionTo(this.triangleAfter);
		NavMeshLadder ladder = (NavMeshLadder) transition;

		boolean upwards = ladder.getTriangleBottom() == this.triangle;
		if(upwards)
		{
			processMovementTowardsTargetPoint(ladder.getPositionBottom());
			Vector3D climbingEndPosition = new Vector3D(ladder.getPositionBottom().x, ladder.getPositionTop().y,
					ladder.getPositionBottom().z);

			TransitionalWaypoint climbWaypoint = newWaypoint(climbingEndPosition, TransitionType.CLIMB);
			climbWaypoint.setData("ladderDirection", ladder.getLadderDirection());
			newWaypoint(ladder.getPositionTop(), TransitionType.WALK);
		}
		else
		{
			Vector3D climbingStartPosition = new Vector3D(ladder.getPositionBottom().x, ladder.getPositionTop().y,
					ladder.getPositionBottom().z);

			processMovementTowardsTargetPoint(climbingStartPosition);
			TransitionalWaypoint climbWaypoint = newWaypoint(ladder.getPositionBottom(), TransitionType.CLIMB);
			climbWaypoint.setData("ladderDirection", ladder.getLadderDirection());
		}
	}


	private TransitionalWaypoint newWaypoint(Vector3D position, int transitionType)
	{
		TransitionalWaypoint waypoint = new TransitionalWaypoint(position, transitionType);
		this.waypoints.add(waypoint);

		this.currentPosition = position;
		this.visLeft = null;
		this.visRight = null;

		return waypoint;
	}


	// -------
	// UTIL
	// -------
	private static boolean isLeftOf(Vector3D v1, Vector3D v2, boolean onZero)
	{
		double crossY = v1.crossProduct(v2).y;

		if(Math.abs(crossY) < 0.000001)
			return onZero;

		return crossY < 0;
	}

	private static boolean isSame(Vector3D a, Vector3D b)
	{
		if(a == null)
			return b == null;

		return a.equals(b);
	}

}
