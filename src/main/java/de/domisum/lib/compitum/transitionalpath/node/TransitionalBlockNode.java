package de.domisum.lib.compitum.transitionalpath.node;

import de.domisum.lib.compitum.universal.WeightedNode;
import org.bukkit.Location;
import org.bukkit.World;

public class TransitionalBlockNode implements WeightedNode
{

	// PROPERTIES
	public final int x;
	public final int y;
	public final int z;

	// SUCCESSOR
	private TransitionalBlockNode parent;
	private int transitionType = 0;

	private double weightFromParent;
	private double heuristicWeight;


	// INIT
	public TransitionalBlockNode(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override public boolean equals(Object other)
	{
		if(!(other instanceof TransitionalBlockNode))
			return false;

		TransitionalBlockNode o = (TransitionalBlockNode) other;

		return o.x == this.x && o.y == this.y && o.z == this.z;
	}

	@Override public int hashCode()
	{
		int hash = 0;
		hash |= (this.x%4096)<<20; // 12 bits long, in [0;11]
		hash |= this.y<<12; // 8 bits (2^8 = 256) long, in [12;19]
		hash |= (this.z%4096); // 12 bits long, in [20;31]

		return hash;
	}

	@Override public String toString()
	{
		return "transitionalNode[x="+this.x+",y="+this.y+",z="+this.z+"]";
	}


	// GETTERS
	public TransitionalBlockNode getParent()
	{
		return this.parent;
	}

	public int getTransitionType()
	{
		return this.transitionType;
	}


	@Override public double getGValue()
	{
		if(this.parent == null)
			return this.weightFromParent;

		return this.parent.getGValue()+this.weightFromParent;
	}

	@Override public double getHValue()
	{
		return this.heuristicWeight;
	}

	@Override public double getFValue()
	{
		return getGValue()+getHValue();
	}


	public Location getLocation(World world)
	{
		return new Location(world, this.x, this.y, this.z);
	}


	// SETTERS
	public void setParent(TransitionalBlockNode parent, int transitionType, double additionalWeight)
	{
		this.parent = parent;
		this.transitionType = transitionType;
		this.weightFromParent = additionalWeight;
	}

	public void setHeuristicWeight(double heuristicWeight)
	{
		this.heuristicWeight = heuristicWeight;
	}

}
