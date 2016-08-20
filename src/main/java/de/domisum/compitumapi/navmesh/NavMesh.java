package de.domisum.compitumapi.navmesh;

import de.domisum.auxiliumapi.data.container.math.Vector3D;
import de.domisum.auxiliumapi.util.keys.Base64Key;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NavMesh
{

	// CONSTANTS
	private static final int KEY_LENGTH = 5;

	// PROPERTIES
	private String id;
	private Vector3D rangeCenter;
	private double range;

	// REFERENCES
	private World world;
	private Set<NavMeshPoint> points = new HashSet<>();
	private Set<NavMeshTriangle> triangles = new HashSet<>();


	// -------
	// CONSTRUCTOR
	// -------
	public NavMesh(String id, Vector3D ranceCenter, double range, World world, Collection<NavMeshPoint> points,
			Collection<NavMeshTriangle> triangles)
	{
		this.id = id;
		this.rangeCenter = ranceCenter;
		this.range = range;

		this.world = world;
		this.points.addAll(points);
		this.triangles.addAll(triangles);
	}


	// -------
	// GETTERS
	// -------
	public String getId()
	{
		return this.id;
	}

	public Vector3D getRangeCenter()
	{
		return this.rangeCenter;
	}

	public double getRange()
	{
		return this.range;
	}

	public boolean isInRange(Location location)
	{
		if(location.getWorld() != this.world)
			return false;

		return new Vector3D(location).distanceToSquared(this.rangeCenter) < this.range*this.range;
	}

	public World getWorld()
	{
		return this.world;
	}


	public Set<NavMeshPoint> getPoints()
	{
		return this.points;
	}

	public Set<NavMeshTriangle> getTriangles()
	{
		return this.triangles;
	}

	public Set<NavMeshTriangle> getTrianglesUsingPoint(NavMeshPoint point)
	{
		Set<NavMeshTriangle> trianglesUsingPoint = new HashSet<>();

		for(NavMeshTriangle triangle : this.triangles)
			if(triangle.isUsingPoint(point))
				trianglesUsingPoint.add(triangle);

		return trianglesUsingPoint;
	}


	public NavMeshPoint getPoint(String id)
	{
		for(NavMeshPoint point : this.points)
			if(point.getId().equals(id))
				return point;

		return null;
	}

	public NavMeshTriangle getTriangle(String id)
	{
		for(NavMeshTriangle triangle : this.triangles)
			if(triangle.id.equals(id))
				return triangle;

		return null;
	}

	public NavMeshTriangle getTriangleAt(Location location)
	{
		for(NavMeshTriangle triangle : this.triangles)
			if(triangle.doesContain(location))
				return triangle;

		return null;
	}


	// -------
	// CHANGERS
	// -------
	public NavMeshPoint createPoint(double x, double y, double z)
	{
		NavMeshPoint point = new NavMeshPoint(getUnusedId(), x, y, z);

		this.points.add(point);
		return point;
	}

	public void removePoint(NavMeshPoint point)
	{
		this.points.remove(point);

		Iterator<NavMeshTriangle> iterator = this.triangles.iterator();
		while(iterator.hasNext())
		{
			NavMeshTriangle triangle = iterator.next();
			if(triangle.isUsingPoint(point))
				iterator.remove();
		}
	}


	public NavMeshTriangle createTriangle(NavMeshPoint point1, NavMeshPoint point2, NavMeshPoint point3)
	{
		NavMeshTriangle triangle = new NavMeshTriangle(getUnusedId(), point1, point2, point3);

		this.triangles.add(triangle);
		return triangle;
	}

	public void deleteTriangle(NavMeshTriangle triangle)
	{
		this.triangles.remove(triangle);
	}


	// -------
	// UTIL
	// -------
	private String getUnusedId()
	{
		String id;
		do
			id = Base64Key.generate(KEY_LENGTH);
		while(getPoint(id) != null || getTriangle(id) != null);

		return id;
	}

}
