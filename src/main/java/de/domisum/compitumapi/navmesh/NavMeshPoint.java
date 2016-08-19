package de.domisum.compitumapi.navmesh;

import de.domisum.auxiliumapi.data.container.math.Vector3D;
import de.domisum.auxiliumapi.util.java.annotations.DeserializationNoArgsConstructor;

public class NavMeshPoint
{

	// PROPERTIES
	private final String id;

	private double x;
	private double y;
	private double z;


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public NavMeshPoint()
	{
		this.id = "";
	}

	public NavMeshPoint(String id, double x, double y, double z)
	{
		this.id = id;

		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof NavMeshPoint))
			return false;

		NavMeshPoint other = (NavMeshPoint) o;
		return this.id.equals(other.id);
	}

	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}


	// -------
	// GETTERS
	// -------
	public String getId()
	{
		return this.id;
	}


	public double getX()
	{
		return this.x;
	}

	public double getY()
	{
		return this.y;
	}

	public double getZ()
	{
		return this.z;
	}

	public Vector3D getPositionVector()
	{
		return new Vector3D(this.x, this.y, this.z);
	}

}
