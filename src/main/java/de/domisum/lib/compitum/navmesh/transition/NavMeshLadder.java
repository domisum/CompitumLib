package de.domisum.lib.compitum.navmesh.transition;

import de.domisum.lib.auxilium.data.container.dir.Direction2D;
import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.transitionalpath.node.TransitionType;

public class NavMeshLadder implements NavMeshTriangleTransition
{

	// CONSTANTS
	private static final double CLIMBING_EXPENSE = 2;

	// PROPERTIES
	private NavMeshTriangle triangleBottom;
	private Vector3D positionBottom;

	private NavMeshTriangle triangleTop;
	private Vector3D positionTop;

	private Direction2D ladderDirection;


	// INIT
	public NavMeshLadder(NavMeshTriangle triangleBottom, Vector3D positionBottom, NavMeshTriangle triangleTop,
			Vector3D positionTop, Direction2D ladderDirection)
	{
		this.triangleBottom = triangleBottom;
		this.positionBottom = positionBottom;

		this.triangleTop = triangleTop;
		this.positionTop = positionTop;

		this.ladderDirection = ladderDirection;
	}


	// GETTERS
	public NavMeshTriangle getTriangleBottom()
	{
		return this.triangleBottom;
	}

	public Vector3D getPositionBottom()
	{
		return this.positionBottom;
	}


	public NavMeshTriangle getTriangleTop()
	{
		return this.triangleTop;
	}

	public Vector3D getPositionTop()
	{
		return this.positionTop;
	}


	public Direction2D getLadderDirection()
	{
		return this.ladderDirection;
	}


	// TRANSITION
	@Override public int getTransitionType()
	{
		return TransitionType.CLIMB;
	}

	@Override public double getWeight()
	{
		double dY = Math.abs(this.positionTop.y-this.positionBottom.y);
		return dY*CLIMBING_EXPENSE;
	}

}
