package de.domisum.lib.compitum.block;


import de.domisum.lib.auxilium.util.java.annotations.API;
import de.domisum.lib.auxilium.util.math.MathUtil;
import de.domisum.lib.compitum.block.evaluator.MaterialEvaluator;
import de.domisum.lib.compitum.block.evaluator.StairEvaluator;
import de.domisum.lib.compitum.path.node.TransitionType;
import de.domisum.lib.compitum.path.node.BlockPathNode;
import de.domisum.lib.compitum.path.BlockPath;
import de.domisum.lib.compitum.path.node.weighted.SortedWeightedNodeList;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

@API
public class BlockAStar
{

	// CONSTANTS
	private static final double CLIMBING_EXPENSE = 2;

	// PROPERTIES
	private double heuristicImportance = 1.0;
	private int maxNodeVisits = 500;

	private boolean canUseDiagonalMovement = true;
	private boolean canUseLadders = false;

	// INPUT
	private Location startLocation;
	private Location endLocation;

	// STATUS
	private boolean moveDiagonally = true;
	private boolean useLadders = false;

	protected BlockPathNode endNode;

	private SortedWeightedNodeList<BlockPathNode> unvisitedNodes = new SortedWeightedNodeList<>(this.maxNodeVisits*3);
	private Set<BlockPathNode> visitedNodes = new HashSet<>(this.maxNodeVisits);

	private long pathfindingStartNano;
	private long pathfindingEndNano;

	// OUTPUT
	private BlockPath path;
	private String failure;


	// INIT
	@API public BlockAStar(Location startLocation, Location endLocation)
	{
		this.startLocation = startLocation;
		this.endLocation = endLocation;
	}


	// GETTERS
	@API public boolean pathFound()
	{
		return this.path != null;
	}

	@API public BlockPath getPath()
	{
		return this.path;
	}


	@API public String getFailure()
	{
		return this.failure;
	}


	private long getNanoDuration()
	{
		return this.pathfindingEndNano-this.pathfindingStartNano;
	}

	private double getMsDuration()
	{
		return MathUtil.round(getNanoDuration()/1000d/1000, 2);
	}

	@API public String getDiagnose()
	{
		String diagnose = "";

		diagnose += "found="+pathFound()+", ";
		/*if(pathFound())
			diagnose += "length="+getPath().getLength()+", ";*/

		diagnose += "visitedNodes="+this.visitedNodes.size()+", ";
		diagnose += "unvisitedNodes="+this.unvisitedNodes.getSize()+", ";
		diagnose += "durationMs="+getMsDuration()+", ";

		return diagnose;
	}


	// SETTERS
	@API public void setHeuristicImportance(double heuristicImportance)
	{
		this.heuristicImportance = heuristicImportance;
	}

	@API public void setCanUseDiagonalMovement(boolean canUseDiagonalMovement)
	{
		this.canUseDiagonalMovement = canUseDiagonalMovement;
	}

	@API public void setCanUseLadders(boolean canUseLadders)
	{
		this.canUseLadders = canUseLadders;
	}


	// PATHFINDING
	@API public void findPath()
	{
		// don't set the start time if it already exists, this way duration of retries are counted together
		if(this.pathfindingStartNano == 0)
			this.pathfindingStartNano = System.nanoTime();

		// validation
		if(this.startLocation.getWorld() != this.endLocation.getWorld())
			throw new IllegalArgumentException("The start and the end location are not in the same world!");

		// preparation
		BlockPathNode startNode = new BlockPathNode(this.startLocation.getBlockX(),
				this.startLocation.getBlockY(), this.startLocation.getBlockZ());
		// this is needed in case the start and end nodes are the same, so the transition type is set
		startNode.setParent(null, TransitionType.WALK, 0);

		this.endNode = new BlockPathNode(this.endLocation.getBlockX(), this.endLocation.getBlockY(),
				this.endLocation.getBlockZ());

		this.unvisitedNodes.addSorted(startNode);

		// pathfinding
		visitNodes();


		// pathfinding finalization
		// if the end node has a parent, a pathfinding has been found
		if(this.endNode.getParent() != null || startNode.equals(this.endNode))
			this.path = new BlockPath(this.endNode);
		else
			// this looks through the provided options and checks if an ability of the pathfinder is deactivated,
			// if so it activates it and reruns the pathfinding. if there are no other options available, it returns
			retry();

		this.pathfindingEndNano = System.nanoTime();
	}

	protected void visitNodes()
	{
		while(true)
		{
			if(this.unvisitedNodes.getSize() == 0)
			{
				// no unvisited nodes left, nowhere else to go ...
				this.failure = "No unvisted nodes left";
				break;
			}

			if(this.visitedNodes.size() >= this.maxNodeVisits)
			{
				// reached limit of nodes to search
				this.failure = "Number of nodes visited exceeds maximum";
				break;
			}

			BlockPathNode nodeToVisit = this.unvisitedNodes.getAndRemoveFirst();
			this.visitedNodes.add(nodeToVisit);

			// pathing reached end node
			if(isTargetReached(nodeToVisit))
			{
				this.endNode = nodeToVisit;
				break;
			}

			visitNode(nodeToVisit);
		}
	}

	protected boolean isTargetReached(BlockPathNode nodeToVisit)
	{
		return nodeToVisit.equals(this.endNode);
	}


	protected void visitNode(BlockPathNode node)
	{
		lookForWalkableNodes(node);

		if(this.useLadders)
			lookForLadderNodes(node);
	}

	protected void lookForWalkableNodes(BlockPathNode node)
	{
		for(int dX = -1; dX <= 1; dX++)
			for(int dZ = -1; dZ <= 1; dZ++)
				for(int dY = -1; dY <= 1; dY++)
					validateNodeOffset(node, dX, dY, dZ);
	}

	protected void validateNodeOffset(BlockPathNode node, int dX, int dY, int dZ)
	{
		// prevent movement to the same position
		if(dX == 0 && dY == 0 && dZ == 0)
			return;

		// prevent diagonal movement if specified
		if(!this.moveDiagonally && dX*dZ != 0)
			return;

		// prevent diagonal movement at the same time as moving up and down
		if(dX*dZ != 0 && dY != 0)
			return;


		BlockPathNode newNode = new BlockPathNode(node.x+dX, node.y+dY, node.z+dZ);

		if(doesNodeAlreadyExist(newNode))
			return;

		// check if player can stand at new node
		if(!isValid(newNode))
			return;

		// check if the diagonal movement is not prevented by blocks to the side
		if(dX*dZ != 0 && !isDiagonalMovementPossible(node, dX, dZ))
			return;

		// check if the player hits his head when going up/down
		if(dY == 1 && !isBlockUnobstructed(node.getLocation(this.startLocation.getWorld()).add(0, 2, 0)))
			return;

		if(dY == -1 && !isBlockUnobstructed(newNode.getLocation(this.startLocation.getWorld()).add(0, 2, 0)))
			return;


		// get transition type (walk up stairs, jump up blocks)
		int transitionType = TransitionType.WALK;
		if(dY == 1)
		{
			boolean isStair = StairEvaluator.isStair(node, newNode, this.startLocation.getWorld());
			if(!isStair)
				transitionType = TransitionType.JUMP;
		}


		// calculate weight
		// TODO punish 90° turns
		int sumAbs = Math.abs(dX)+Math.abs(dY)+Math.abs(dZ);
		double weight = 1;
		if(sumAbs == 2)
			weight = 1.41;
		else if(sumAbs == 3)
			weight = 1.73;

		// punish jumps to favor stair climbing
		if(transitionType == TransitionType.JUMP)
			weight += 0.5;


		// actually add the node to the pool
		newNode.setParent(node, transitionType, weight);
		addNode(newNode);
	}

	protected void lookForLadderNodes(BlockPathNode node)
	{
		Location feetLocation = node.getLocation(this.startLocation.getWorld());

		for(int dY = -1; dY <= 1; dY++)
		{
			Location location = feetLocation.clone().add(0, dY, 0);
			if(location.getBlock().getType() != Material.LADDER)
				continue;

			BlockPathNode newNode = new BlockPathNode(node.x, node.y+dY, node.z);
			newNode.setParent(node, TransitionType.CLIMB, CLIMBING_EXPENSE);

			if(doesNodeAlreadyExist(newNode))
				continue;

			addNode(newNode);
		}
	}


	protected boolean doesNodeAlreadyExist(BlockPathNode node)
	{
		if(this.visitedNodes.contains(node))
			return true;

		return this.unvisitedNodes.contains(node);

	}

	protected void addNode(BlockPathNode node)
	{
		node.setHeuristicWeight(getHeuristicWeight(node)*this.heuristicImportance);
		this.unvisitedNodes.addSorted(node);
	}


	// NODE VALIDATION
	protected boolean isValid(BlockPathNode node)
	{
		return canStandAt(node.getLocation(this.startLocation.getWorld()));
	}

	protected boolean isDiagonalMovementPossible(BlockPathNode node, int dX, int dZ)
	{
		if(!isUnobstructed(node.getLocation(this.startLocation.getWorld()).clone().add(dX, 0, 0)))
			return false;

		return isUnobstructed(node.getLocation(this.startLocation.getWorld()).clone().add(0, 0, dZ));

	}

	@SuppressWarnings("deprecation") protected boolean canStandAt(Location feetLocation)
	{
		if(!isUnobstructed(feetLocation))
			return false;

		return MaterialEvaluator.canStandOn(feetLocation.clone().add(0, -1, 0).getBlock().getTypeId());

	}

	protected boolean isUnobstructed(Location feetLocation)
	{
		if(!isBlockUnobstructed(feetLocation))
			return false;

		return isBlockUnobstructed(feetLocation.clone().add(0, 1, 0));

	}


	// LOCATION VALIDATION
	@SuppressWarnings("deprecation") protected boolean isBlockUnobstructed(Location location)
	{
		return MaterialEvaluator.canStandIn(location.getBlock().getTypeId());
	}


	// HEURISTIC
	protected double getHeuristicWeight(BlockPathNode node)
	{
		return getEuclideanDistance(node);
	}

	@SuppressWarnings("unused") protected double getManhattanDistance(BlockPathNode node)
	{
		return Math.abs(node.x-this.endNode.x)+Math.abs(node.y-this.endNode.y)+Math.abs(node.z-this.endNode.z);
	}

	protected double getEuclideanDistance(BlockPathNode node)
	{
		int dX = this.endNode.x-node.x;
		int dY = this.endNode.y-node.y;
		int dZ = this.endNode.z-node.z;

		return Math.sqrt(dX*dX+dY*dY+dZ*dZ);
	}


	// RETRY
	protected void retry()
	{
		lookForReason:
		{
			// the special abilities are sorted by likelyhood of appearance to further speed up the pathfinding

			if(this.canUseDiagonalMovement && !this.moveDiagonally)
			{
				this.moveDiagonally = true;
				break lookForReason;
			}

			if(this.canUseLadders && !this.useLadders)
			{
				this.useLadders = true;
				break lookForReason;
			}

			return;
		}

		reset();
		findPath();
	}

	protected void reset()
	{
		this.endNode = null;

		this.unvisitedNodes.clear();
		this.visitedNodes.clear();

		this.path = null;
		this.failure = null;
	}

}
