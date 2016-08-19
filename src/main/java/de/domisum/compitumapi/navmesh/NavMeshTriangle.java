package de.domisum.compitumapi.navmesh;

import de.domisum.auxiliumapi.data.container.math.Vector3D;

public class NavMeshTriangle
{

	// PROPERTIES
	public final String id;

	// REFERENCES
	public final NavMeshPoint point1;
	public final NavMeshPoint point2;
	public final NavMeshPoint point3;


	// -------
	// CONSTRUCTOR
	// -------
	public NavMeshTriangle(String id, NavMeshPoint point1, NavMeshPoint point2, NavMeshPoint point3)
	{
		this.id = id;

		this.point1 = point1;
		this.point2 = point2;
		this.point3 = point3;
	}


	// -------
	// GETTERS
	// -------
	public boolean isUsingPoint(NavMeshPoint point)
	{
		return this.point1 == point || this.point2 == point || this.point3 == point;
	}

	public Vector3D getCenter()
	{
		Vector3D sum = this.point1.getPositionVector().add(this.point2.getPositionVector().add(this.point3.getPositionVector()));
		return sum.divide(3);
	}

}
