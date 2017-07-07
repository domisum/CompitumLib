package de.domisum.lib.compitum.navmesh.json;

import de.domisum.lib.auxilium.util.java.annotations.DeserializationNoArgsConstructor;
import de.domisum.lib.auxilium.util.java.annotations.SetByDeserialization;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshPoint;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import lombok.Getter;

import java.util.Collection;

class SerializationNavMeshTriangle
{

	// PROPERTIES
	@SetByDeserialization @Getter private String id;

	@SetByDeserialization private String point1;
	@SetByDeserialization private String point2;
	@SetByDeserialization private String point3;


	// INIT
	@DeserializationNoArgsConstructor public SerializationNavMeshTriangle()
	{

	}

	SerializationNavMeshTriangle(NavMeshTriangle navMeshTriangle)
	{
		this.id = navMeshTriangle.id;

		this.point1 = navMeshTriangle.point1.getId();
		this.point2 = navMeshTriangle.point2.getId();
		this.point3 = navMeshTriangle.point3.getId();
	}


	// GETTERS
	NavMeshTriangle getNavMeshTriangle(Collection<NavMeshPoint> points)
	{
		return new NavMeshTriangle(this.id, getPoint(points, this.point1), getPoint(points, this.point2),
				getPoint(points, this.point3));
	}

	private NavMeshPoint getPoint(Collection<NavMeshPoint> points, String id)
	{
		for(NavMeshPoint point : points)
			if(point.getId().equals(id))
				return point;

		return null;
	}

}
