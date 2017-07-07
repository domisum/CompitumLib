package de.domisum.lib.compitum.transitionalpath.path;

import java.util.List;

public class TransitionalPath
{

	private List<TransitionalWaypoint> waypoints;


	// INIT
	public TransitionalPath(List<TransitionalWaypoint> waypoints)
	{
		this.waypoints = waypoints;
	}


	// GETTERS
	public int getNumberOfWaypoints()
	{
		return this.waypoints.size();
	}

	public TransitionalWaypoint getWaypoint(int index)
	{
		if(index >= this.waypoints.size() || index < 0)
			return null;

		return this.waypoints.get(index);
	}

}
