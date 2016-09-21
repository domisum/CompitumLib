package de.domisum.lib.compitum.navmesh.json;

import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.java.annotations.DeserializationNoArgsConstructor;
import de.domisum.lib.auxilium.util.java.annotations.SetByDeserialization;
import de.domisum.lib.compitum.navmesh.NavMesh;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshPoint;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.navmesh.transition.NavMeshLadder;
import de.domisum.lib.compitum.navmesh.transition.NavMeshTriangleTransition;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class SerializationNavMesh
{

	// PROPERTIES
	@SetByDeserialization
	private String worldName;
	@SetByDeserialization
	private Vector3D rangeCenter;
	@SetByDeserialization
	private double range;

	// REFERENCES
	@SetByDeserialization
	private Set<NavMeshPoint> points = new HashSet<>();
	@SetByDeserialization
	private Set<SerializationNavMeshTriangle> triangles = new HashSet<>();
	@SetByDeserialization
	private Set<SerializationNavMeshLadder> ladders = new HashSet<>();


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public SerializationNavMesh()
	{

	}


	// -------
	// CONVERSION
	// -------
	public SerializationNavMesh(NavMesh mesh)
	{
		this.worldName = mesh.getWorld().getName();
		this.rangeCenter = mesh.getRangeCenter();
		this.range = mesh.getRange();

		this.points.addAll(mesh.getPoints());
		for(NavMeshTriangle triangle : mesh.getTriangles())
		{
			this.triangles.add(new SerializationNavMeshTriangle(triangle));

			for(NavMeshTriangleTransition transition : triangle.neighbors.values())
			{
				if(transition instanceof NavMeshLadder)
					this.ladders.add(new SerializationNavMeshLadder((NavMeshLadder) transition));
			}
		}
	}

	public NavMesh convertToNavMesh(String id)
	{
		Set<NavMeshTriangle> triangles = new HashSet<>();
		for(SerializationNavMeshTriangle serializationTriangle : this.triangles)
			triangles.add(serializationTriangle.getNavMeshTriangle(this.points));

		NavMesh navMesh = new NavMesh(id, this.rangeCenter, this.range, Bukkit.getWorld(this.worldName), this.points, triangles);

		for(SerializationNavMeshLadder serializationLadder : this.ladders)
		{
			NavMeshLadder ladder = serializationLadder.getNavMeshLadder(navMesh);
			ladder.getTriangleBottom().makeNeighbors(ladder.getTriangleTop(), ladder);
		}

		return navMesh;
	}

}
