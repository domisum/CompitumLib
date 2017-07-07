package de.domisum.lib.compitum.path;

import java.util.List;

public class Path
{

	private List<PathWaypoint> pathWaypoints;


	// INIT
	public Path(List<PathWaypoint> pathWaypoints)
	{
		this.pathWaypoints = pathWaypoints;
	}


	// GETTERS
	public int getNumberOfWaypoints()
	{
		return this.pathWaypoints.size();
	}

	public PathWaypoint getWaypoint(int index)
	{
		if(index >= this.pathWaypoints.size() || index < 0)
			return null;

		return this.pathWaypoints.get(index);
	}

}
