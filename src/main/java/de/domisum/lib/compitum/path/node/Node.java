package de.domisum.lib.compitum.path.node;

import de.domisum.lib.compitum.evaluator.MaterialEvaluator;
import de.domisum.lib.compitum.path.pathfinders.AStar;
import org.bukkit.Location;
import org.bukkit.World;

public class Node implements Comparable<Node>
{

	// STATUS
	public final World world;
	public final int x;
	public final int y;
	public final int z;

	public final AStar aStar;

	public Node target;
	protected Node parent;

	protected boolean expenseCalculated = false;
	protected double expense;

	protected boolean remainingExpenseCalculated = false;
	protected double remainingExpense;

	// CHECKS
	protected int beneathBlockMaterialID;
	protected int bottomBlockMaterialID;
	protected int topBlockMaterialID;

	protected boolean isUnblockedChecked = false;
	protected boolean isUnblocked;

	protected boolean canStandAtChecked = false;
	protected boolean canStandAt;


	// -------
	// CONSTRUCTOR
	// -------
	public Node(World world, int x, int y, int z, AStar aStar, Node target)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;

		this.aStar = aStar;
		this.target = target;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Node))
			return false;

		Node other = (Node) o;

		return (other.x == this.x) && (other.y == this.y) && (other.z == this.z);
	}

	@Override
	public int hashCode()
	{
		return this.x+(513*this.y)+(517*this.z);
	}

	@Override
	public String toString()
	{
		return "node[x="+this.x+",y="+this.y+",z="+this.z+"]";
	}


	// -------
	// SETTERS
	// -------
	public void setTarget(Node target)
	{
		this.target = target;
	}

	public void setParent(Node parent)
	{
		this.parent = parent;
		this.expenseCalculated = false;
	}


	// -------
	// GETTERS
	// -------
	public Node getParent()
	{
		return this.parent;
	}

	public boolean hasParent()
	{
		return this.parent != null;
	}


	public int getBeneathBlockMaterialID()
	{
		if(!this.canStandAtChecked)
			canStandAt();

		return this.beneathBlockMaterialID;
	}

	public int getBottomBlockMaterialID()
	{
		if(!this.isUnblockedChecked)
			isUnblocked();

		return this.bottomBlockMaterialID;
	}

	public int getTopBlockMaterialID()
	{
		if(!this.isUnblockedChecked)
			isUnblocked();

		return this.topBlockMaterialID;
	}


	public Location getCentralLocation(World world)
	{
		return new Location(world, this.x+.5, this.y+.5, this.z+.5);
	}

	public Location getCentralFloorLocation(World world)
	{
		return new Location(world, this.x+.5, this.y, this.z+.5);
	}


	// -------
	// EXPENSE & HEURISTIC
	// -------
	public double getExpense()
	{
		if(this.expenseCalculated)
			return this.expense;

		this.expense = getExpenseVia(this.parent);
		this.expenseCalculated = true;

		return this.expense;
	}

	public double getExpenseVia(Node node)
	{
		double expenseToVia = 0;
		if(node != null)
			expenseToVia = node.getExpense();

		return expenseToVia+getExpenseFromNode(node);
	}

	public double getExpenseFromNode(Node node)
	{
		if(node == null)
			return Integer.MAX_VALUE;

		int dX = Math.abs(this.x-node.x);
		int dY = Math.abs(this.y-node.y);
		int dZ = Math.abs(this.z-node.z);

		int dCombined = dX+dY+dZ;

		if(dCombined == 1)
			return 1; // sqrt(1)
		else if(dCombined == 2)
			return 1.414; // sqrt(2)
		else if(dCombined == 3)
			return 1.732; // sqrt(3)

		return 1;
	}


	public double getRemainingExpense()
	{
		if(this.remainingExpenseCalculated)
			return this.remainingExpense;

		this.remainingExpense = getManhattanDistance(this.target);
		this.remainingExpenseCalculated = true;

		return this.remainingExpense;
	}

	public double getManhattanDistance(Node other)
	{
		double dX = Math.abs(this.x-other.x);
		double dY = Math.abs(this.y-other.y);
		double dZ = Math.abs(this.z-other.z);

		return dX+dY+dZ;
	}

	public double getEuclideanDistanceSquared(Node other)
	{
		double dX = this.x-other.x;
		double dY = this.y-other.y;
		double dZ = this.z-other.z;

		double distanceSquared = (dX*dX)+(dY*dY)+(dZ*dZ);
		return distanceSquared;
	}

	public double getEuclideanDistance(Node other)
	{
		return Math.sqrt(getEuclideanDistanceSquared(other));
	}


	public double getCombinedExpense()
	{
		return getExpense()+(this.aStar.getHeuristicWeight()*getRemainingExpense());
	}

	@Override
	public int compareTo(Node otherNode)
	{
		return Double.compare(getCombinedExpense(), otherNode.getCombinedExpense());
	}


	// -------
	// VALIDITATION
	// -------
	@SuppressWarnings("deprecation")
	public boolean isUnblocked()
	{
		if(this.isUnblockedChecked)
			return this.isUnblocked;

		this.bottomBlockMaterialID = this.world.getBlockAt(this.x, this.y, this.z).getTypeId();
		this.topBlockMaterialID = this.world.getBlockAt(this.x, this.y+1, this.z).getTypeId();

		if(!MaterialEvaluator.canStandIn(this.bottomBlockMaterialID))
			this.isUnblocked = false;
		else if(!MaterialEvaluator.canStandIn(this.topBlockMaterialID))
			this.isUnblocked = false;
		else
			this.isUnblocked = true;

		this.isUnblockedChecked = true;
		return this.isUnblocked;
	}

	@SuppressWarnings("deprecation")
	public boolean canStandAt()
	{
		if(this.canStandAtChecked)
			return this.canStandAt;

		this.beneathBlockMaterialID = this.world.getBlockAt(this.x, this.y-1, this.z).getTypeId();

		if(!isUnblocked())
			this.canStandAt = false;
		else if(!MaterialEvaluator.canStandOn(this.beneathBlockMaterialID))
			this.canStandAt = false;
		else
			this.canStandAt = true;

		this.canStandAtChecked = true;
		return this.canStandAt;
	}

}
