package de.domisum.lib.compitum.navmesh;

import de.domisum.lib.auxilium.data.container.dir.Direction2D;
import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxilium.util.keys.Base64Key;
import de.domisum.lib.auxiliumspigot.util.LocationUtil;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshPoint;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.navmesh.transition.NavMeshLadder;
import de.domisum.lib.compitum.navmesh.transition.NavMeshTrianglePortal;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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
	private Map<String, NavMeshPoint> points = new HashMap<>(); // <id, point>
	private Map<String, NavMeshTriangle> triangles = new HashMap<>(); // <id, triangle>


	// INIT
	public NavMesh(String id, Vector3D ranceCenter, double range, World world, Collection<NavMeshPoint> points,
			Collection<NavMeshTriangle> triangles)
	{
		this.id = id;
		this.rangeCenter = ranceCenter;
		this.range = range;

		this.world = world;
		for(NavMeshPoint p : points)
			this.points.put(p.getId(), p);
		for(NavMeshTriangle t : triangles)
			this.triangles.put(t.id, t);

		fillInNeighbors();
		determineHeuristicTriangleCenters();
	}


	// GETTERS
	// general
	@APIUsage public String getId()
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

	boolean isInRange(Location location)
	{
		if(location.getWorld() != this.world)
			return false;

		return LocationUtil.toVector3D(location).distanceToSquared(this.rangeCenter) < this.range*this.range;
	}

	public World getWorld()
	{
		return this.world;
	}


	// poin
	public Collection<NavMeshPoint> getPoints()
	{
		return this.points.values();
	}

	private NavMeshPoint getPoint(String id)
	{
		return this.points.get(id);
	}


	// triangle
	public Collection<NavMeshTriangle> getTriangles()
	{
		return this.triangles.values();
	}

	@APIUsage public Set<NavMeshTriangle> getTrianglesUsingPoint(NavMeshPoint point)
	{
		Set<NavMeshTriangle> trianglesUsingPoint = new HashSet<>();

		for(NavMeshTriangle triangle : this.triangles.values())
			if(triangle.isUsingPoint(point))
				trianglesUsingPoint.add(triangle);

		return trianglesUsingPoint;
	}


	public NavMeshTriangle getTriangle(String id)
	{
		return this.triangles.get(id);
	}

	public NavMeshTriangle getTriangleAt(Location location)
	{
		// TODO optimize this, might become bottleneck with thousands of triangles

		for(NavMeshTriangle triangle : this.triangles.values())
			if(triangle.doesContain(location))
				return triangle;

		return null;
	}


	// POINT
	@APIUsage public NavMeshPoint createPoint(double x, double y, double z)
	{
		NavMeshPoint point = new NavMeshPoint(getUnusedId(), x, y, z);

		this.points.put(point.getId(), point);
		return point;
	}

	@APIUsage public void removePoint(NavMeshPoint point)
	{
		for(NavMeshTriangle t : getTrianglesUsingPoint(point))
			deleteTriangle(t);

		this.points.remove(point.getId());
	}


	// TRIANGLE
	@APIUsage public NavMeshTriangle createTriangle(NavMeshPoint point1, NavMeshPoint point2, NavMeshPoint point3)
	{
		NavMeshTriangle triangle = new NavMeshTriangle(getUnusedId(), point1, point2, point3);
		this.triangles.put(triangle.id, triangle);

		fillInNeighborsFor(triangle);

		return triangle;
	}

	@APIUsage public void deleteTriangle(NavMeshTriangle triangle)
	{
		this.triangles.remove(triangle.id);
		triangle.clearNeighbors();
	}


	// LADDER
	@APIUsage public void createLadder(NavMeshTriangle triangle1, Vector3D position1, NavMeshTriangle triangle2,
			Vector3D position2, Direction2D ladderDirection)
	{
		NavMeshLadder ladder;
		if(position1.y < position2.y)
			ladder = new NavMeshLadder(triangle1, position1, triangle2, position2, ladderDirection);
		else
			ladder = new NavMeshLadder(triangle2, position2, triangle1, position1, ladderDirection);

		triangle1.makeNeighbors(triangle2, ladder);
	}

	@APIUsage public void removeLadder(NavMeshLadder ladder)
	{
		ladder.getTriangleBottom().removeNeighbor(ladder.getTriangleTop());
	}


	// PATHFINDING
	private void fillInNeighbors()
	{
		long start = System.nanoTime();

		for(NavMeshTriangle triangle : this.triangles.values())
			fillInNeighborsFor(triangle);
	}

	private void fillInNeighborsFor(NavMeshTriangle triangle)
	{
		for(NavMeshTriangle t : this.triangles.values())
		{
			if(Objects.equals(t, triangle))
				continue;

			Set<NavMeshPoint> commonPoints = getCommonPoints(triangle, t);

			if(commonPoints.size() == 2)
			{
				NavMeshTrianglePortal portal = new NavMeshTrianglePortal(triangle, t, commonPoints);
				triangle.makeNeighbors(t, portal);
			}
		}
	}


	private void determineHeuristicTriangleCenters()
	{
		for(int i = 0; i < 20; i++)
			reduceHeuristicCenterDistances(0.1);
	}

	private void reduceHeuristicCenterDistances(double factor)
	{
		for(NavMeshTriangle triangle : this.triangles.values())
		{
			Set<NavMeshTriangle> neighbors = triangle.neighbors.keySet();
			if(neighbors.size() <= 1)
				continue;

			Vector3D neighborSum = new Vector3D();
			for(NavMeshTriangle n : neighbors)
				neighborSum = neighborSum.add(n.getHeuristicCenter());

			Vector3D neighborAverage = neighborSum.divide(neighbors.size());
			Vector3D currentHeuristicCenter = triangle.getHeuristicCenter();
			Vector3D fromCurrentToAverage = neighborAverage.subtract(currentHeuristicCenter);

			Vector3D newHeuristicCenter = currentHeuristicCenter
					.moveTowards(neighborAverage, fromCurrentToAverage.length()*factor);
			if(triangle.doesContain(newHeuristicCenter))
				triangle.setHeuristicCenter(newHeuristicCenter);
		}
	}


	// UTIL
	private String getUnusedId()
	{
		String id;
		do
			id = Base64Key.generate(KEY_LENGTH);
		while(getPoint(id) != null || getTriangle(id) != null);

		return id;
	}

	@SuppressWarnings("unused") private boolean areTrianglesAdjacent(NavMeshTriangle triangle1, NavMeshTriangle triangle2)
	{
		int same = 0;

		if(triangle1.point1 == triangle2.point1)
			same++;
		if(triangle1.point1 == triangle2.point2)
			same++;
		if(triangle1.point1 == triangle2.point3)
			same++;

		if(triangle1.point2 == triangle2.point1)
			same++;
		if(triangle1.point2 == triangle2.point2)
			same++;
		if(triangle1.point2 == triangle2.point3)
			same++;

		if(triangle1.point3 == triangle2.point1)
			same++;
		if(triangle1.point3 == triangle2.point2)
			same++;
		if(triangle1.point3 == triangle2.point3)
			same++;

		return same == 2;
	}

	private Set<NavMeshPoint> getCommonPoints(NavMeshTriangle triangle1, NavMeshTriangle triangle2)
	{
		NavMeshPoint[] triangle1Points = new NavMeshPoint[] {triangle1.point1, triangle1.point2, triangle1.point3};
		NavMeshPoint[] triangle2Points = new NavMeshPoint[] {triangle2.point1, triangle2.point2, triangle2.point3};

		Set<NavMeshPoint> commonPoints = new HashSet<>();
		for(NavMeshPoint t1p : triangle1Points)
			for(NavMeshPoint t2p : triangle2Points)
				if(Objects.equals(t1p, t2p))
					commonPoints.add(t1p);

		return commonPoints;
	}

}
