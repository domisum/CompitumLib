package de.domisum.lib.compitum.transitionalpath.special;


import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxilium.util.math.RandomUtil;
import de.domisum.lib.compitum.transitionalpath.node.TransitionalBlockNode;
import de.domisum.lib.compitum.transitionalpath.pathfinders.TransitionalAStar;
import org.bukkit.Location;

@APIUsage
public class TransitionalStrollAStar extends TransitionalAStar
{

	// PROPERTIES
	private double maxDistance;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public TransitionalStrollAStar(Location startLocation, Location strollBaseLocation, double maxDistance)
	{
		super(startLocation, getEndLocation(strollBaseLocation, maxDistance));

		this.maxDistance = maxDistance;
	}

	private static Location getEndLocation(Location strollBaseLocation, double maxDistance)
	{
		double randomAngle = RandomUtil.nextDouble()*2*Math.PI;
		double dX = Math.sin(randomAngle)*maxDistance*1.5;
		double dZ = Math.cos(randomAngle)*maxDistance*1.5;

		return strollBaseLocation.clone().add(Math.round(dX), 0, dZ);
	}


	// -------
	// PATHFINDING
	// -------
	@Override
	protected boolean isTargetReached(TransitionalBlockNode nodeToVisit)
	{
		if(nodeToVisit.equals(this.endNode))
			return true;

		if(nodeToVisit.getGValue() >= this.maxDistance)
			return true;

		if(RandomUtil.nextDouble() < 0.01)
			return true;

		return false;
	}

}
