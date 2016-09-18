package de.domisum.lib.compitum.navmesh.geometry;

import de.domisum.lib.auxilium.data.container.math.Vector3D;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class NavMeshTriangle
{

	// PROPERTIES
	public final String id;

	public final NavMeshPoint point1;
	public final NavMeshPoint point2;
	public final NavMeshPoint point3;

	public final Map<NavMeshTriangle, NavMeshTrianglePortal> neighbors = new HashMap<>();

	// STATUS
	private Vector3D heuristicCenter;


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

	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}


	// -------
	// GETTERS
	// -------

	// INTRINSIC
	public Vector3D getCenter()
	{
		Vector3D sum = this.point1.getPositionVector().add(this.point2.getPositionVector().add(this.point3.getPositionVector()));
		return sum.divide(3);
	}

	public Vector3D getHeuristicCenter()
	{
		if(this.heuristicCenter == null)
			return getCenter();

		return this.heuristicCenter;
	}


	// RELATIONAL
	public boolean isUsingPoint(NavMeshPoint point)
	{
		return this.point1 == point || this.point2 == point || this.point3 == point;
	}

	boolean isNeighbor(NavMeshTriangle other)
	{
		return this.neighbors.containsKey(other);
	}

	public NavMeshTrianglePortal getPortalTo(NavMeshTriangle other)
	{
		return this.neighbors.get(other);
	}


	// WORLD
	public boolean doesContain(Location location)
	{
		return doesContain(new Vector3D(location));
	}

	public boolean doesContain(Vector3D point)
	{
		if(Math.abs(getCenter().y-point.y) > 3)
			return false;

		Vector3D a = this.point1.getPositionVector();
		Vector3D b = this.point2.getPositionVector();
		Vector3D c = this.point3.getPositionVector();

		boolean b1 = sign(point, a, b) < 0;
		boolean b2 = sign(point, b, c) < 0;
		boolean b3 = sign(point, c, a) < 0;

		return ((b1 == b2) && (b2 == b3));
	}


	// -------
	// SETTERS
	// -------
	public void setHeuristicCenter(Vector3D heuristicCenter)
	{
		this.heuristicCenter = heuristicCenter;
	}


	// -------
	// CHANGERS
	// -------
	public void makeNeighbors(NavMeshTriangle other, NavMeshTrianglePortal portal)
	{
		this.neighbors.put(other, portal);
		other.neighbors.put(this, portal);
	}

	public void removeNeighbor(NavMeshTriangle other)
	{
		this.neighbors.remove(other);
		other.neighbors.remove(this);
	}

	public void clearNeighbors()
	{
		for(NavMeshTriangle n : new HashSet<>(this.neighbors.keySet()))
			removeNeighbor(n);
	}


	// -------
	// UTIL
	// -------
	private double sign(Vector3D p1, Vector3D p2, Vector3D p3)
	{
		return (p1.x-p3.x)*(p2.z-p3.z)-(p2.x-p3.x)*(p1.z-p3.z);
	}

}
