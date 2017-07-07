package de.domisum.lib.compitum.navmesh.geometry;

import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxiliumspigot.util.LocationUtil;
import de.domisum.lib.compitum.CompitumLib;
import de.domisum.lib.compitum.navmesh.transition.NavMeshTriangleTransition;
import lombok.Setter;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class NavMeshTriangle
{

	// CONSTANTS
	private static final double CONTAINS_TOLERANCE = 0.00001;

	// PROPERTIES
	public final String id;

	public final NavMeshPoint point1;
	public final NavMeshPoint point2;
	public final NavMeshPoint point3;

	public final Map<NavMeshTriangle, NavMeshTriangleTransition> neighbors = new HashMap<>();

	// STATUS
	@Setter private Vector3D heuristicCenter;


	// INIT
	public NavMeshTriangle(String id, NavMeshPoint point1, NavMeshPoint point2, NavMeshPoint point3)
	{
		this.id = id;

		this.point1 = point1;
		this.point2 = point2;
		this.point3 = point3;
	}

	@Override public String toString()
	{
		return "triangle["+this.point1.getId()+","+this.point2.getId()+","+this.point3.getId()+"]";
	}

	@Override public int hashCode()
	{
		return this.id.hashCode();
	}

	@Override public boolean equals(Object o)
	{
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;

		NavMeshTriangle that = (NavMeshTriangle) o;

		if(!this.id.equals(that.id))
			return false;

		return true;
	}


	// GETTERS
	// intrinsic
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


	// relational
	public boolean isUsingPoint(NavMeshPoint point)
	{
		return this.point1 == point || this.point2 == point || this.point3 == point;
	}

	public NavMeshTriangleTransition getTransitionTo(NavMeshTriangle other)
	{
		return this.neighbors.get(other);
	}


	// world
	public boolean doesContain(Location location)
	{
		return doesContain(LocationUtil.toVector3D(location));
	}

	public boolean doesContain(Vector3D point)
	{
		if(Math.abs(getCenter().y-point.y) >= 2)
			return false;

		Vector3D a = this.point1.getPositionVector();
		Vector3D b = this.point2.getPositionVector();
		Vector3D c = this.point3.getPositionVector();

		double ab = sign(point, a, b);
		double bc = sign(point, b, c);
		double ca = sign(point, c, a);

		if(Math.abs(ab) < CONTAINS_TOLERANCE)
		{
			if(Math.abs(bc) < CONTAINS_TOLERANCE)
				return true;
			else if(Math.abs(ca) < CONTAINS_TOLERANCE)
				return true;

			return bc < 0 == ca < 0;
		}
		else if(Math.abs(bc) < CONTAINS_TOLERANCE)
		{
			if(Math.abs(ab) < CONTAINS_TOLERANCE)
				return true;
			else if(Math.abs(ca) < CONTAINS_TOLERANCE)
				return true;

			return ab < 0 == ca < 0;
		}
		else if(Math.abs(ca) < CONTAINS_TOLERANCE)
		{
			if(Math.abs(ab) < CONTAINS_TOLERANCE)
				return true;
			else if(Math.abs(bc) < CONTAINS_TOLERANCE)
				return true;

			return ab < 0 == bc < 0;
		}

		return (ab < 0 == bc < 0) && (bc < 0 == ca < 0);
	}


	// NEIGHBORS
	public void makeNeighbors(NavMeshTriangle other, NavMeshTriangleTransition transition)
	{
		if(this.neighbors.containsKey(other))
		{
			NavMeshTriangleTransition currentTransition = this.neighbors.get(other);
			if(currentTransition == transition)
				CompitumLib.getLogger()
						.warning("Overridden NavMesh neighbor: "+currentTransition+"' overridden by '"+transition+"'");
		}

		this.neighbors.put(other, transition);
		other.neighbors.put(this, transition);
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


	// UTIL
	private double sign(Vector3D p1, Vector3D p2, Vector3D p3)
	{
		return (p1.x-p3.x)*(p2.z-p3.z)-(p2.x-p3.x)*(p1.z-p3.z);
	}

}
