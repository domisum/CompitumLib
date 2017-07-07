package de.domisum.lib.compitum.path.pathfinders;

import de.domisum.lib.compitum.path.node.Node;
import de.domisum.lib.compitum.path.node.NodeUnderSurface;
import org.bukkit.Location;
import org.bukkit.World;

public class AStarUnderSurface extends AStar
{

	// INIT
	public AStarUnderSurface(Location startLocation, Location endLocation)
	{
		super(startLocation, endLocation);
	}


	// PATHFINDING
	@Override protected void visitNode(Node node, int dX, int dY, int dZ)
	{
		Node newNode = null;

		// # check if node can be reached
		// no pathing to current location wanted
		if((dX == 0) && (dY == 0) && (dZ == 0))
			return;

		// dont allow 3-d diagonal movement
		if((dX != 0) && (dZ != 0) && (dY != 0))
			return;

		// WATCH OUT HERE - DON'T OVEREXCLUDE
		if(!this.ladderClimbingAllowed && (dX == 0) && (dZ == 0))
			return;

		// get node representing current block
		newNode = newNode(this.world, node.x+dX, node.y+dY, node.z+dZ, this.endNode);


		// node has already been visited -> skip
		if(this.visitedNodes.contains(newNode))
			return;

		// node is already on the list to be visited -> skip
		if(this.unvisitedNodes.contains(newNode))
			return;


		// if the node is blocked skip (not even ladders or falling can change that)
		if(!newNode.isUnblocked())
			return;

		// if the node is invalid move on
		if(!newNode.canStandAt())
			return;

		// check scraped blocks on diagonal movement
		if((dX != 0) && (dZ != 0))
		{
			Node scrapedBlock1 = newNode(this.world, node.x+dX, node.y+dY, node.z, this.endNode); // don't
			// modify
			// z
			if(!scrapedBlock1.canStandAt())
				return;

			Node scrapedBlock2 = newNode(this.world, node.x, node.y+dY, node.z+dZ, this.endNode); // don't
			// modify
			// x
			if(!scrapedBlock2.canStandAt())
				return;
		}


		// # node can be reached, update references

		// check if a shorter way to the already known node has been found
		if(!newNode.hasParent())
			newNode.setParent(node);

		// add new node to list of unvisited nodes
		this.unvisitedNodes.add(newNode);

	}


	// UTIL
	@Override protected Node newNode(World world, int x, int y, int z, Node target)
	{
		return new NodeUnderSurface(world, x, y, z, this, target);
	}

}
