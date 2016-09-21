package de.domisum.lib.compitum.navmesh.json;

import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.java.annotations.DeserializationNoArgsConstructor;
import de.domisum.lib.auxilium.util.java.annotations.SetByDeserialization;
import de.domisum.lib.compitum.navmesh.NavMesh;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.navmesh.transition.NavMeshLadder;

class SerializationNavMeshLadder
{

	// PROPERTIES
	@SetByDeserialization
	private String triangleBottom;
	@SetByDeserialization
	private Vector3D positionBottom;

	@SetByDeserialization
	private String triangleTop;
	@SetByDeserialization
	private Vector3D positionTop;


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public SerializationNavMeshLadder()
	{

	}

	SerializationNavMeshLadder(NavMeshLadder navMeshLadder)
	{
		this.triangleBottom = navMeshLadder.getTriangleBottom().id;
		this.positionBottom = navMeshLadder.getPositionBottom();

		this.triangleTop = navMeshLadder.getTriangleTop().id;
		this.positionTop = navMeshLadder.getPositionTop();
	}


	// -------
	// GETTERS
	// -------
	NavMeshLadder getNavMeshLadder(NavMesh navMesh)
	{
		NavMeshTriangle triangleBottom_ = navMesh.getTriangle(this.triangleBottom);
		NavMeshTriangle triangleTop_ = navMesh.getTriangle(this.triangleTop);

		return new NavMeshLadder(triangleBottom_, this.positionBottom, triangleTop_, this.positionTop);
	}

}
