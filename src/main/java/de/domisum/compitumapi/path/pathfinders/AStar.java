package de.domisum.compitumapi.path.pathfinders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import de.domisum.compitumapi.path.PathfindingStatus;
import de.domisum.compitumapi.path.RawPath;
import de.domisum.compitumapi.path.node.Node;

public class AStar
{

	// OPTIONS
	protected final Location startLocation;
	protected final Location endLocation;

	protected int maximumNodeVisits = 500;
	protected double heuristicWeight = 1.5;
	protected boolean ladderClimbingAllowed = false;

	// STATUS
	protected PathfindingStatus pathfindingStatus = PathfindingStatus.NOT_STARTED;

	protected World world;

	protected Node startNode;
	protected Node endNode;

	protected List<Node> unvisitedNodes = new ArrayList<Node>(this.maximumNodeVisits * 2);
	protected Set<Node> visitedNodes = new HashSet<Node>(this.maximumNodeVisits);

	protected RawPath path;

	// BENCHMARK
	protected long startNano;
	protected long endNano;


	// -------
	// CONSTRUCTOR
	// -------
	public AStar(Location startLocation, Location endLocation)
	{
		this.startLocation = startLocation;
		this.endLocation = endLocation;

		if((startLocation == null) || (endLocation == null))
			throw new IllegalArgumentException("The locations can't be null");

		if(startLocation.getWorld() != endLocation.getWorld())
			throw new IllegalArgumentException("The locations are in two different worlds");

		this.world = startLocation.getWorld();
	}


	// -------
	// GETTERS
	// -------
	public World getWorld()
	{
		return this.world;
	}


	public int getMaximumNodeVisits()
	{
		return this.maximumNodeVisits;
	}

	public double getHeuristicWeight()
	{
		return this.heuristicWeight;
	}

	public boolean isLadderClimbingAllowed()
	{
		return this.ladderClimbingAllowed;
	}


	public boolean isPathFound()
	{
		return this.pathfindingStatus == PathfindingStatus.FOUND;
	}

	public RawPath getPath()
	{
		if(this.pathfindingStatus == PathfindingStatus.NOT_STARTED)
			throw new IllegalStateException("The pathfinding has not been started yet");

		if(this.pathfindingStatus == PathfindingStatus.SEARCHING)
			throw new IllegalStateException("The pathfinding is not done yet");

		return this.path;
	}


	public long getNanoDuration()
	{
		if((this.pathfindingStatus == PathfindingStatus.NOT_STARTED) || (this.pathfindingStatus == PathfindingStatus.SEARCHING))
			throw new IllegalStateException("The pathfinding is not done yet");

		return this.endNano - this.startNano;
	}

	public double getMilliDuration()
	{
		double duration = getNanoDuration() / 1000d / 1000d;
		double roundedDuration = Math.round(duration * 1000) / 1000d;

		return roundedDuration;
	}


	public String getDiagnose()
	{
		String diagnose = "";

		diagnose += "found=" + isPathFound() + ",";
		if(isPathFound())
			diagnose += "length=" + getPath().getLength() + ",";

		diagnose += "visitedNodes=" + this.visitedNodes.size() + ",";
		diagnose += "unvisitedNodes=" + this.unvisitedNodes.size() + ",";
		diagnose += "durationMs=" + getMilliDuration();

		return diagnose;
	}

	public Collection<Node> getUnvisitedNodes()
	{
		return new ArrayList<Node>(this.unvisitedNodes);
	}

	public Collection<Node> getVisitedNodes()
	{
		return new HashSet<Node>(this.visitedNodes);
	}


	// -------
	// SETTERS
	// -------
	public void setMaximumNodeVisits(int maximumNodeVisits)
	{
		if(this.pathfindingStatus != PathfindingStatus.NOT_STARTED)
			throw new IllegalStateException("This option can only be modified before the pathfinding has been started!");

		this.maximumNodeVisits = maximumNodeVisits;
	}

	public void setHeuristicWeight(double heuristicWeight)
	{
		this.heuristicWeight = heuristicWeight;
	}

	public void setLadderClimbingAllowed(boolean ladderClimbingAllowed)
	{
		this.ladderClimbingAllowed = ladderClimbingAllowed;
	}


	// -------
	// PATHING
	// -------
	public void findPath() throws IllegalArgumentException
	{
		this.pathfindingStatus = PathfindingStatus.SEARCHING;
		this.startNano = System.nanoTime();

		pathfinding:
		{
			this.endNode = newNode(this.world, this.endLocation.getBlockX(), this.endLocation.getBlockY(),
					this.endLocation.getBlockZ(), null);
			this.startNode = newNode(this.world, this.startLocation.getBlockX(), this.startLocation.getBlockY(),
					this.startLocation.getBlockZ(), this.endNode);

			// check if start and end nodes are accessible
			if(!this.startNode.canStandAt())
				throw new IllegalArgumentException("The start location is invalid! (" + this.startLocation + ")");

			if(!this.endNode.canStandAt())
				throw new IllegalArgumentException("The end location is invalid! (" + this.endLocation + ")");


			// check if startPosition is end position -> path found
			if(this.startNode.equals(this.endNode))
			{
				this.path = new RawPath(this.world, Arrays.asList(new Node[] { this.startNode }));
				this.pathfindingStatus = PathfindingStatus.FOUND;

				break pathfinding;
			}

			// start loop by adding startNode to unvisited nodes
			this.unvisitedNodes.add(this.startNode);
			while(this.visitedNodes.size() < this.maximumNodeVisits)
			{
				// no more open nodes, no path could be found
				if(this.unvisitedNodes.isEmpty())
					break;

				// get most promising node to check
				Node nodeToVisit = getAndRemoveNextNodeToCheck();

				// is nodeToCheck end node? -> path found
				if(nodeToVisit.equals(this.endNode))
				{
					this.endNode.setParent(nodeToVisit.getParent());
					this.visitedNodes.add(this.endNode);
					break;
				}

				// visit nodes to find other unvisited nodes around it
				visitNode(nodeToVisit);
			}

			// has a way to the last node been found? -> create path from connected nodes
			if(this.endNode.getParent() != null)
			{
				createPathFromConnectedNodes();
				this.pathfindingStatus = PathfindingStatus.FOUND;
			}
			else
				this.pathfindingStatus = PathfindingStatus.NOT_FOUND;

		}

		this.endNano = System.nanoTime();
	}

	protected Node getAndRemoveNextNodeToCheck()
	{
		if(this.unvisitedNodes.isEmpty())
			return null;

		Collections.sort(this.unvisitedNodes);

		return this.unvisitedNodes.remove(0);
	}

	protected void visitNode(Node node)
	{
		// loop through close blocks
		for(int dZ = -1; dZ <= 1; dZ++)
			for(int dY = -1; dY <= 1; dY++)
				for(int dX = -1; dX <= 1; dX++)
					visitNode(node, dX, dY, dZ);

		// no removal from unvisitedNodes neeed since the method getting the most promising missing node from the list already
		// does that
		this.visitedNodes.add(node);
	}

	protected void visitNode(Node node, int dX, int dY, int dZ)
	{
		Node newNode = null;

		// # check if node can be reached
		nodeCheck:
		{
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
			newNode = newNode(this.world, node.x + dX, node.y + dY, node.z + dZ, this.endNode);


			// node has already been visited -> skip
			if(this.visitedNodes.contains(newNode))
				return;

			// node is already on the list to be visited -> skip
			if(this.unvisitedNodes.contains(newNode))
				return;


			// if the node is blocked skip (not even ladders or falling can change that)
			if(!newNode.isUnblocked())
				return;


			if(this.ladderClimbingAllowed)
			{
				if((dX == 0) && (dY == 1) && (dZ == 0)) // climb upwards
				{
					if(newNode.getBottomBlockMaterialID() == 65) // ladder
						break nodeCheck; // skip all other node checks -> allow
					else if(newNode.getBeneathBlockMaterialID() == 65)
						break nodeCheck;
				}
				else if(dY == -1) // climb downwards
					if(newNode.getBeneathBlockMaterialID() == 65)
						break nodeCheck;
			}
			else if((dX == 0) && (dZ == 0))
				return;

			// if the node is invalid move on
			if(!newNode.canStandAt())
				return;

			// check scraped blocks on diagonal movement
			if((dX != 0) && (dZ != 0))
			{
				Node scrapedBlock1 = newNode(this.world, node.x + dX, node.y + dY, node.z, this.endNode); // don't
																											// modify
				// z
				if(!scrapedBlock1.canStandAt())
					return;

				Node scrapedBlock2 = newNode(this.world, node.x, node.y + dY, node.z + dZ, this.endNode); // don't
																											// modify
				// x
				if(!scrapedBlock2.canStandAt())
					return;
			}

			// check if head-block is unobstructed when moving up
			if(dY == 1)
			{
				Node headBlock = newNode(this.world, node.x, node.y + 1, node.z, this.endNode);
				if(!headBlock.isUnblocked())
					return;
			}

			// check if head-block is unobstructed when moving down
			if(dY == -1)
			{
				Node headBlock = newNode(this.world, node.x + dX, node.y, node.z + dZ, this.endNode);
				if(!headBlock.isUnblocked())
					return;
			}
		}


		// # node can be reached, update references

		// check if a shorter way to the already known node has been found
		if(!newNode.hasParent())
			newNode.setParent(node);


		// add new node to list of unvisited nodes
		this.unvisitedNodes.add(newNode);
	}


	protected void createPathFromConnectedNodes()
	{
		// this list is reversed since we start from the end and work towards the start
		List<Node> nodeList = new ArrayList<Node>();
		nodeList.add(this.endNode);

		do
		{
			nodeList.add(nodeList.get(nodeList.size() - 1).getParent());
		}
		while(nodeList.get(nodeList.size() - 1).getParent() != null);

		// reverse the list
		Collections.reverse(nodeList);

		this.path = new RawPath(this.world, nodeList);
	}


	// -------
	// UTIL
	// -------
	protected Node newNode(World world, int x, int y, int z, Node target)
	{
		return new Node(world, x, y, z, this, target);
	}

}
