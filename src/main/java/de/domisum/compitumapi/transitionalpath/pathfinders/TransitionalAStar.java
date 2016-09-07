package de.domisum.compitumapi.transitionalpath.pathfinders;

import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.auxiliumapi.util.math.MathUtil;
import de.domisum.compitumapi.path.MaterialEvaluator;
import de.domisum.compitumapi.transitionalpath.SortedTransitionalNodeList;
import de.domisum.compitumapi.transitionalpath.path.TransitionalBlockPath;
import de.domisum.compitumapi.transitionalpath.node.TransitionType;
import de.domisum.compitumapi.transitionalpath.node.TransitionalBlockNode;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@APIUsage
public class TransitionalAStar
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

	private TransitionalBlockNode endNode;

	private long pathfindingStartNano;
	private long pathfindingEndNano;

	private SortedTransitionalNodeList unvisitedNodes = new SortedTransitionalNodeList(this.maxNodeVisits*3);
	private Set<TransitionalBlockNode> visitedNodes = new HashSet<>(this.maxNodeVisits);

	// OUTPUT
	private TransitionalBlockPath path;
	private Exception error;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public TransitionalAStar(Location startLocation, Location endLocation)
	{
		this.startLocation = startLocation;
		this.endLocation = endLocation;
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public boolean pathFound()
	{
		return this.path != null;
	}

	@APIUsage
	public TransitionalBlockPath getPath()
	{
		return this.path;
	}


	@APIUsage
	public Exception getError()
	{
		return this.error;
	}


	private long getNanoDuration()
	{
		return this.pathfindingEndNano-this.pathfindingStartNano;
	}

	private double getMsDuration()
	{
		return MathUtil.round(getNanoDuration()/1000d/1000, 2);
	}

	@APIUsage
	public String getDiagnose()
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


	@Deprecated
	public Set<TransitionalBlockNode> getVisitedNodes()
	{
		return this.visitedNodes;
	}

	@Deprecated
	public Collection<TransitionalBlockNode> getUnvisitedNodes()
	{
		return this.unvisitedNodes.getNodes();
	}


	// -------
	// SETTERS
	// -------
	@APIUsage
	public void setHeuristicImportance(double heuristicImportance)
	{
		this.heuristicImportance = heuristicImportance;
	}

	@APIUsage
	public void setCanUseDiagonalMovement(boolean canUseDiagonalMovement)
	{
		this.canUseDiagonalMovement = canUseDiagonalMovement;
	}

	@APIUsage
	public void setCanUseLadders(boolean canUseLadders)
	{
		this.canUseLadders = canUseLadders;
	}


	// -------
	// PATHFINDING
	// -------
	@APIUsage
	public void findPath()
	{
		// don't set the start time if it is already exists, this way retries are counted together
		if(this.pathfindingStartNano == 0)
			this.pathfindingStartNano = System.nanoTime();

		// validation
		if(this.startLocation.getWorld() != this.endLocation.getWorld())
		{
			this.error = new IllegalArgumentException("The start and the end location are not in the same world!");
			return;
		}

		// preparation
		TransitionalBlockNode startNode = new TransitionalBlockNode(this.startLocation.getBlockX(),
				this.startLocation.getBlockY(), this.startLocation.getBlockZ());
		this.endNode = new TransitionalBlockNode(this.endLocation.getBlockX(), this.endLocation.getBlockY(),
				this.endLocation.getBlockZ());
		this.unvisitedNodes.addSorted(startNode);

		// pathfinding
		while(true)
		{
			if(this.unvisitedNodes.getSize() == 0)
			{
				this.error = new IllegalStateException("No unvisited nodes left to search");
				// no unvisited nodes left, nowhere else to go ...
				break;
			}

			if(this.visitedNodes.size() >= this.maxNodeVisits)
			{
				this.error = new IllegalArgumentException("Reached limit of nodes to search");
				// reached limit of nodes to search
				break;
			}

			TransitionalBlockNode nodeToVisit = this.unvisitedNodes.getAndRemoveFirst();
			this.visitedNodes.add(nodeToVisit);

			// pathing reached end node
			if(nodeToVisit.equals(this.endNode))
			{
				this.endNode = nodeToVisit;
				break;
			}

			visitNode(nodeToVisit);
		}

		// path finalization
		// if the end node has a parent, a path has been found
		if(this.endNode.getParent() != null)
			this.path = new TransitionalBlockPath(this.endNode);
		else
			// this looks through the provided options and checks if an ability of the pathfinder is deactivated,
			// if so it activates it and reruns the pathfinding. if there are no other options available, it returns
			retry();

		this.pathfindingEndNano = System.nanoTime();
	}


	private void visitNode(TransitionalBlockNode node)
	{
		lookForWalkableNodes(node);

		if(this.useLadders)
			lookForLadderNodes(node);
	}

	private void lookForWalkableNodes(TransitionalBlockNode node)
	{
		for(int dX = -1; dX <= 1; dX++)
			for(int dZ = -1; dZ <= 1; dZ++)
				for(int dY = -1; dY <= 1; dY++)
				{
					// prevent movement to the same position
					if(dX == 0 && dY == 0 && dZ == 0)
						continue;

					// prevent diagonal movement if specified
					if(!this.moveDiagonally && dX*dZ != 0)
						continue;

					// prevent diagonal movement at the same time as moving up and down
					if(dX*dZ != 0 && dY != 0)
						continue;


					int sumAbs = Math.abs(dX)+Math.abs(dY)+Math.abs(dZ);
					double weight = 1;
					if(sumAbs == 2)
						weight = 1.41;
					else if(sumAbs == 3)
						weight = 1.73;

					TransitionalBlockNode newNode = new TransitionalBlockNode(node.x+dX, node.y+dY, node.z+dZ);
					newNode.setParent(node, TransitionType.WALK, weight);


					if(doesNodeAlreadyExist(newNode))
						continue;

					// check if player can stand at new node
					if(!isValid(newNode))
						continue;

					// check if the diagonal movement is not prevented by blocks to the side
					if(dX*dZ != 0)
						if(!isDiagonalMovementPossible(node, dX, dZ))
							continue;

					// check if the player hits his head when going up/down
					if(dY == 1)
						if(!isBlockUnobstructed(node.getLocation(this.startLocation.getWorld()).add(0, 2, 0)))
							continue;

					if(dY == -1)
						if(!isBlockUnobstructed(newNode.getLocation(this.startLocation.getWorld()).add(0, 2, 0)))
							continue;


					// actually add the node to the pool
					addNode(newNode);
				}
	}

	private void lookForLadderNodes(TransitionalBlockNode node)
	{
		Location feetLocation = node.getLocation(this.startLocation.getWorld());

		for(int dY = -1; dY <= 1; dY++)
		{
			Location location = feetLocation.clone().add(0, dY, 0);
			if(location.getBlock().getType() != Material.LADDER)
				continue;

			TransitionalBlockNode newNode = new TransitionalBlockNode(node.x, node.y+dY, node.z);
			newNode.setParent(node, TransitionType.CLIMB, CLIMBING_EXPENSE);

			if(doesNodeAlreadyExist(newNode))
				continue;

			addNode(newNode);
		}
	}


	private boolean doesNodeAlreadyExist(TransitionalBlockNode node)
	{
		if(this.visitedNodes.contains(node))
			return true;

		if(this.unvisitedNodes.contains(node))
			return true;

		return false;
	}

	private void addNode(TransitionalBlockNode node)
	{
		node.setHeuristicWeight(getHeuristicWeight(node)*this.heuristicImportance);
		this.unvisitedNodes.addSorted(node);
	}


	// NODE VALIDATION
	private boolean isValid(TransitionalBlockNode node)
	{
		return canStandAt(node.getLocation(this.startLocation.getWorld()));
	}

	private boolean isDiagonalMovementPossible(TransitionalBlockNode node, int dX, int dZ)
	{
		if(isUnobstructed(node.getLocation(this.startLocation.getWorld()).clone().add(dX, 0, 0)))
			return true;

		if(isUnobstructed(node.getLocation(this.startLocation.getWorld()).clone().add(0, 0, dZ)))
			return true;

		return false;
	}

	private boolean canStandAt(Location feetLocation)
	{
		if(!isUnobstructed(feetLocation))
			return false;

		if(!MaterialEvaluator.canStandOn(feetLocation.clone().add(0, -1, 0).getBlock().getTypeId()))
			return false;

		return true;
	}

	private boolean isUnobstructed(Location feetLocation)
	{
		if(!isBlockUnobstructed(feetLocation))
			return false;

		if(!isBlockUnobstructed(feetLocation.clone().add(0, 1, 0)))
			return false;

		return true;
	}


	// LOCATION VALIDATION
	private boolean isBlockUnobstructed(Location location)
	{
		return MaterialEvaluator.canStandIn(location.getBlock().getTypeId());
	}


	// -------
	// HEURISTIC
	// -------
	private double getHeuristicWeight(TransitionalBlockNode node)
	{
		return getEuclideanDistance(node);
	}

	@SuppressWarnings("unused")
	private double getManhattanDistance(TransitionalBlockNode node)
	{
		return Math.abs(node.x-this.endNode.x)+Math.abs(node.y-this.endNode.y)+Math.abs(node.z-this.endNode.z);
	}

	private double getEuclideanDistance(TransitionalBlockNode node)
	{
		int dX = this.endNode.x-node.x;
		int dY = this.endNode.y-node.y;
		int dZ = this.endNode.z-node.z;

		return Math.sqrt(dX*dX+dY*dY+dZ*dZ);
	}


	// -------
	// RETRY
	// -------
	private void retry()
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

	private void reset()
	{
		this.endNode = null;

		this.unvisitedNodes.clear();
		this.visitedNodes.clear();

		this.path = null;
		this.error = null;
	}

}
