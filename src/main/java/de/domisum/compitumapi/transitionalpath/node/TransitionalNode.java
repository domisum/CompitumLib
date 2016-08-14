package de.domisum.compitumapi.transitionalpath.node;

import org.bukkit.Location;
import org.bukkit.World;

public class TransitionalNode
{

	// PROPERTIES
	public final int x;
	public final int y;
	public final int z;

	// SUCCESSOR
	private TransitionalNode parent;
	private int transitionType = 0;

	private double weightFromParent;
	private double heuristicWeight;


	// -------
	// CONSTRUCTOR
	// -------
	public TransitionalNode(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public boolean equals(Object other)
	{
		if(!(other instanceof TransitionalNode))
			return false;

		TransitionalNode o = (TransitionalNode) other;

		return o.x == this.x && o.y == this.y && o.z == this.z;
	}

	public int hashCode()
	{
		int hash = this.x;
		hash |= (this.x%4096)<<20; // 12 bits long, in [0;11]
		hash |= this.y<<12; // 8 bits long, in [12;19]
		hash |= (this.z%4096); // 12 bits long, in [20;31]

		return hash;
	}

	@Override
	public String toString()
	{
		return "transitionalNode[x="+this.x+",y="+this.y+",z="+this.z+"]";
	}


	// -------
	// GETTERS
	// -------
	public TransitionalNode getParent()
	{
		return this.parent;
	}

	public int getTransitionType()
	{
		return this.transitionType;
	}


	public double getWeight()
	{
		if(this.parent == null)
			return this.weightFromParent;

		return this.parent.getWeight()+this.weightFromParent;
	}

	public double getEstimatedCombinedWeight()
	{
		return getWeight()+this.heuristicWeight;
	}


	public Location getLocation(World world)
	{
		return new Location(world, this.x, this.y, this.z);
	}


	// -------
	// SETTERS
	// -------
	public void setParent(TransitionalNode parent, int transitionType, double additionalWeight)
	{
		this.parent = parent;
		this.transitionType = transitionType;
		this.weightFromParent = additionalWeight;
	}

	public void setHeuristicWeight(double heuristicWeight)
	{
		this.heuristicWeight = heuristicWeight;
	}


	// -------
	// WEIGHT
	// -------


}
