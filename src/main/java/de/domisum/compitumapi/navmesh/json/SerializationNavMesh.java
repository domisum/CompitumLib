package de.domisum.compitumapi.navmesh.json;

import de.domisum.auxiliumapi.data.container.math.Vector3D;
import de.domisum.auxiliumapi.util.java.annotations.DeserializationNoArgsConstructor;
import de.domisum.auxiliumapi.util.java.annotations.SetByDeserialization;
import de.domisum.compitumapi.navmesh.NavMesh;
import de.domisum.compitumapi.navmesh.NavMeshPoint;
import de.domisum.compitumapi.navmesh.NavMeshTriangle;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	private List<NavMeshPoint> points = new ArrayList<>();
	private List<SerializationNavMeshTriangle> triangles = new ArrayList<>();


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
			this.triangles.add(new SerializationNavMeshTriangle(triangle));
	}

	public NavMesh convertToNavMesh(String id)
	{
		Set<NavMeshTriangle> triangles = new HashSet<>();
		for(SerializationNavMeshTriangle serializationTriangle : this.triangles)
			triangles.add(serializationTriangle.getNavMeshTriangle(points));

		return new NavMesh(id, this.rangeCenter, this.range, Bukkit.getWorld(this.worldName), points, triangles);
	}

}
