package de.domisum.lib.compitum.navmesh.geometry;

import de.domisum.lib.auxilium.data.container.math.LineSegment3D;

import java.util.Collection;
import java.util.Iterator;

public class NavMeshTrianglePortal
{

	// REFERENCES
	private final NavMeshTriangle triangle1;
	private final NavMeshTriangle triangle2;

	private final NavMeshPoint point1;
	private final NavMeshPoint point2;

	// STATUS
	private LineSegment3D fullLineSegment;
	private LineSegment3D playerLineSegment;


	// -------
	// CONSTRUCTOR
	// -------
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


	// -------
	// GETTERS
	// -------
	private LineSegment3D getFullLineSegment()
	{
		if(this.fullLineSegment == null)
			this.fullLineSegment = new LineSegment3D(this.point1.getPositionVector(), this.point2.getPositionVector());

		return this.fullLineSegment;
	}

	public LineSegment3D getPlayerLineSegment()
	{
		if(this.playerLineSegment == null)
			this.playerLineSegment = getFullLineSegment().getShortenedBothEnds(0.49);

		return this.playerLineSegment;
	}

}
