package de.domisum.lib.compitum.navmesh.json;

import de.domisum.lib.compitum.navmesh.NavMeshPoint;
import de.domisum.lib.compitum.navmesh.NavMeshTriangle;
import de.domisum.lib.auxilium.util.java.annotations.DeserializationNoArgsConstructor;
import de.domisum.lib.auxilium.util.java.annotations.SetByDeserialization;

import java.util.List;

class SerializationNavMeshTriangle
{

	// PROPERTIES
	@SetByDeserialization
	private String id;

	@SetByDeserialization
	private String point1;
	@SetByDeserialization
	private String point2;
	@SetByDeserialization
	private String point3;


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public SerializationNavMeshTriangle()
	{

	}

	SerializationNavMeshTriangle(NavMeshTriangle navMeshTriangle)
	{
		this.id = navMeshTriangle.id;

		this.point1 = navMeshTriangle.point1.getId();
		this.point2 = navMeshTriangle.point2.getId();
		this.point3 = navMeshTriangle.point3.getId();
	}


	// -------
	// GETTERS
	// -------
	NavMeshTriangle getNavMeshTriangle(List<NavMeshPoint> points)
	{
		return new NavMeshTriangle(this.id, getPoint(points, this.point1), getPoint(points, this.point2),
				getPoint(points, this.point3));
	}

	private NavMeshPoint getPoint(List<NavMeshPoint> points, String id)
	{
		for(NavMeshPoint point : points)
			if(point.getId().equals(id))
				return point;

		return null;
	}

}
