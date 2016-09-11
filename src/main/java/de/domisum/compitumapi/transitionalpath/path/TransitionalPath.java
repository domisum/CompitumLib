package de.domisum.compitumapi.transitionalpath.path;

import de.domisum.auxiliumapi.data.container.Duo;
import de.domisum.auxiliumapi.data.container.math.Vector3D;

import java.util.List;

public class TransitionalPath
{

	private List<Duo<Vector3D, Integer>> waypoints; // <location, TransitionType>


	// -------
	// CONSTRUCTOR
	// -------
	public TransitionalPath(List<Duo<Vector3D, Integer>> waypoints)
	{
		this.waypoints = waypoints;
	}


	// -------
	// GETTERS
	// -------
	public int getNumberOfWaypoints()
	{
		return this.waypoints.size();
	}

	public Duo<Vector3D, Integer> getWaypoint(int index)
	{
		if(index >= this.waypoints.size() || index < 0)
			return null;

		return this.waypoints.get(index);
	}

}
