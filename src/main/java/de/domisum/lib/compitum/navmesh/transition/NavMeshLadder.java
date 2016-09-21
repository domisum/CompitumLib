package de.domisum.lib.compitum.navmesh.transition;

import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.transitionalpath.node.TransitionType;

public class NavMeshLadder implements NavMeshTriangleTransition
{

	// PROPERTIES
	private NavMeshTriangle triangleBottom;
	private Vector3D positionBottom;

	private NavMeshTriangle triangleTop;
	private Vector3D positionTop;


	// -------
	// CONSTRUCTOR
	// -------
	public NavMeshLadder(NavMeshTriangle triangleBottom, Vector3D positionBottom, NavMeshTriangle triangleTop,
			Vector3D positionTop)
	{
		this.triangleBottom = triangleBottom;
		this.positionBottom = positionBottom;

		this.triangleTop = triangleTop;
		this.positionTop = positionTop;
	}


	// -------
	// GETTERS
	// -------
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


	@Override
	public int getTransitionType()
	{
		return TransitionType.CLIMB;
	}

	@Override
	public double getWeight()
	{
		// TODO
		return 0;
	}
}
