package de.domisum.compitumapi.path.node;

import org.bukkit.World;

import de.domisum.compitumapi.path.MaterialEvaluator;
import de.domisum.compitumapi.path.pathfinders.AStar;

public class NodeUnderSurface extends Node
{

	// -------
	// CONSTRUCTOR
	// -------
	public NodeUnderSurface(World world, int x, int y, int z, AStar aStar, Node target)
	{
		super(world, x, y, z, aStar, target);
	}


	// -------
	// VALIDITATION
	// -------
	@Override
	@SuppressWarnings("deprecation")
	public boolean isUnblocked()
	{
		if(this.isUnblockedChecked)
			return this.isUnblocked;

		this.bottomBlockMaterialID = this.world.getBlockAt(this.x, this.y, this.z).getTypeId();

		this.isUnblocked = MaterialEvaluator.canStandOn(this.bottomBlockMaterialID);

		this.isUnblockedChecked = true;
		return this.isUnblocked;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean canStandAt()
	{
		if(this.canStandAtChecked)
			return this.canStandAt;

		this.topBlockMaterialID = this.world.getBlockAt(this.x, this.y + 1, this.z).getTypeId();

		if(!isUnblocked())
			this.canStandAt = false;
		else
			this.canStandAt = MaterialEvaluator.canStandIn(this.topBlockMaterialID);

		this.canStandAtChecked = true;
		return this.canStandAt;
	}

}
