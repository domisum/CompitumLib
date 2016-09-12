package de.domisum.lib.compitum.navmesh;

import de.domisum.lib.auxilium.data.container.math.Vector3D;
import org.bukkit.Location;

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

	@Override
	public String toString()
	{
		return "triangle["+this.point1.getId()+","+this.point2.getId()+","+this.point3.getId()+"]";
	}


	// -------
	// GETTERS
	// -------
	Vector3D getCenter()
	{
		Vector3D sum = this.point1.getPositionVector().add(this.point2.getPositionVector().add(this.point3.getPositionVector()));
		return sum.divide(3);
	}

	boolean isUsingPoint(NavMeshPoint point)
	{
		return this.point1 == point || this.point2 == point || this.point3 == point;
	}

	boolean isNeighbor(NavMeshTriangle other)
	{
		int same = 0;

		if(this.point1 == other.point1)
			same++;
		if(this.point1 == other.point2)
			same++;
		if(this.point1 == other.point3)
			same++;

		if(this.point2 == other.point1)
			same++;
		if(this.point2 == other.point2)
			same++;
		if(this.point2 == other.point3)
			same++;

		if(this.point3 == other.point1)
			same++;
		if(this.point3 == other.point2)
			same++;
		if(this.point3 == other.point3)
			same++;

		return same == 2;
	}


	boolean doesContain(Location location)
	{
		if(Math.abs(getCenter().y-location.getY()) > 3)
			return false;

		Vector3D a = this.point1.getPositionVector();
		Vector3D b = this.point2.getPositionVector();
		Vector3D c = this.point3.getPositionVector();
		Vector3D p = new Vector3D(location);

		boolean b1 = sign(p, a, b) < 0;
		boolean b2 = sign(p, b, c) < 0;
		boolean b3 = sign(p, c, a) < 0;

		return ((b1 == b2) && (b2 == b3));
	}


	// -------
	// UTIL
	// -------
	private double sign(Vector3D p1, Vector3D p2, Vector3D p3)
	{
		return (p1.x-p3.x)*(p2.z-p3.z)-(p2.x-p3.x)*(p1.z-p3.z);
	}

}
