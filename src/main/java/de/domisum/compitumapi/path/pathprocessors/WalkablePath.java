package de.domisum.compitumapi.path.pathprocessors;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;

import de.domisum.compitumapi.path.RawPath;
import de.domisum.compitumapi.path.node.Node;
import de.domisum.compitumapi.util.MathUtil;

public class WalkablePath
{

	// INPUT
	private RawPath rawPath;

	// OUTPUT
	private ArrayList<Location> locations = new ArrayList<Location>();
	private Iterator<Location> iterator;

	// STATUS
	private Location currentLocation;


	// -------
	// CONSTRUCTOR
	// -------
	public WalkablePath(RawPath rawPath)
	{
		this.rawPath = rawPath;

		create();
	}

	private void create()
	{
		for(Node node : this.rawPath.getNodes())
		{
			Location location = node.getCentralFloorLocation(this.rawPath.getWorld());
			this.locations.add(location);
		}

		for(int i = 0; i < this.locations.size(); i++)
		{
			Location location = this.locations.get(i);
			Location before = i >= 1 ? this.locations.get(i - 1) : null;
			Location after = i <= (this.locations.size() - 2) ? this.locations.get(i + 1) : null;

			// jump before going one block higher
			// if(after != null)
			// if(after.getY() == location.getY() + 1)
			// {
			// location.setY(location.getY() + 0.2);
			// after.setY(after.getY() - 0.05);
			// }

			// look towards next point
			if(after != null)
				location = MathUtil.lookAt(location, after);
			else if(before != null)
			{
				Location direction = MathUtil.lookAt(before, location);
				location.setYaw(direction.getYaw());
				location.setPitch(direction.getPitch());
			}

			this.locations.set(i, location);
		}
	}


	// -------
	// GETTERS
	// -------
	public boolean hasNext()
	{
		if((this.iterator == null) && (this.locations != null))
			this.iterator = this.locations.iterator();

		return this.iterator.hasNext();
	}

	public Location getNext()
	{
		if((this.iterator == null) && (this.locations != null))
			this.iterator = this.locations.iterator();

		this.currentLocation = this.iterator.next();

		return this.currentLocation.clone();
	}

	public Location getCurrent()
	{
		if(this.currentLocation == null)
			return null;

		return this.currentLocation.clone();
	}

}