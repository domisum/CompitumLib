package de.domisum.lib.compitum.transitionalpath.path;

import de.domisum.lib.auxilium.data.container.DataRegister;
import de.domisum.lib.auxilium.data.container.math.Vector3D;

public class TransitionalWaypoint
{

	// PROPERTIES
	private Vector3D position;
	private int transitionType;

	private DataRegister dataRegister = new DataRegister();


	// INIT
	public TransitionalWaypoint(Vector3D position, int transitionType)
	{
		this.position = position;
		this.transitionType = transitionType;
	}

	public String toString()
	{
		return "waypoint[position="+this.position+";transitionType="+this.transitionType+"]";
	}


	// GETTERS
	public Vector3D getPosition()
	{
		return this.position;
	}

	public int getTransitionType()
	{
		return this.transitionType;
	}

	public Object getData(String key)
	{
		return this.dataRegister.get(key);
	}


	// SETTERS
	public void setData(String key, Object value)
	{
		this.dataRegister.set(key, value);
	}

}
