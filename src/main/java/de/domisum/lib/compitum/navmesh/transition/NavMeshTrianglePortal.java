package de.domisum.lib.compitum.navmesh.transition;

import de.domisum.lib.auxilium.data.container.math.LineSegment3D;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshPoint;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.transitionalpath.node.TransitionType;

import java.util.Collection;
import java.util.Iterator;

public class NavMeshTrianglePortal implements NavMeshTriangleTransition
{

	// REFERENCES
	private final NavMeshTriangle triangle1;
	private final NavMeshTriangle triangle2;

	private final NavMeshPoint point1;
	private final NavMeshPoint point2;

	// STATUS
	private LineSegment3D fullLineSegment;
	private double triangleHeuristicCenterDistance = -1;


	// INIT
	public NavMeshTrianglePortal(NavMeshTriangle triangle1, NavMeshTriangle triangle2, Collection<NavMeshPoint> commonPoints)
	{
		this.triangle1 = triangle1;
		this.triangle2 = triangle2;

		if(commonPoints.size() != 2)
			throw new IllegalArgumentException("The number of current points has to be 2 (was "+commonPoints.size()+")");

		Iterator<NavMeshPoint> iterator = commonPoints.iterator();
		this.point1 = iterator.next();
		this.point2 = iterator.next();
	}


	// GETTERS
	public LineSegment3D getFullLineSegment()
	{
		if(this.fullLineSegment == null)
			this.fullLineSegment = new LineSegment3D(this.point1.getPositionVector(), this.point2.getPositionVector());

		return this.fullLineSegment;
	}

	@Override public int getTransitionType()
	{
		return TransitionType.WALK;
	}

	@Override public double getWeight()
	{
		if(this.triangleHeuristicCenterDistance == -1)
			this.triangleHeuristicCenterDistance = this.triangle2.getHeuristicCenter()
					.subtract(this.triangle1.getHeuristicCenter()).length();

		return this.triangleHeuristicCenterDistance;
	}

}
