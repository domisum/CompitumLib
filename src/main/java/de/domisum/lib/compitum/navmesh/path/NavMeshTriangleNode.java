package de.domisum.lib.compitum.navmesh.path;

import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.path.node.weighted.WeightedNode;

class NavMeshTriangleNode implements WeightedNode
{

	// REFERENCES
	private NavMeshTriangle triangle;
	private NavMeshTriangleNode parent;

	// STATUS
	private double gValue = -1;
	private double heuristicValue = -1;


	// INIT
	protected NavMeshTriangleNode(NavMeshTriangle triangle, NavMeshTriangleNode parent, double heuristicValue)
	{
		this.triangle = triangle;
		this.parent = parent;

		this.heuristicValue = heuristicValue;
	}

	@Override public int hashCode()
	{
		return this.triangle.hashCode();
	}

	@Override public boolean equals(Object o)
	{
		if(!(o instanceof NavMeshTriangleNode))
			return false;

		NavMeshTriangleNode other = (NavMeshTriangleNode) o;
		return this.triangle.equals(other.triangle);
	}


	// GETTERS
	protected NavMeshTriangle getTriangle()
	{
		return this.triangle;
	}

	protected NavMeshTriangleNode getParent()
	{
		return this.parent;
	}


	@Override public double getGValue()
	{
		if(this.parent == null)
			return 0;

		if(this.gValue == -1)
		{
			double toParent = this.parent.getGValue();
			double fromParent = this.parent.getTriangle().getTransitionTo(this.triangle).getWeight();

			this.gValue = toParent+fromParent;
		}

		return this.gValue;
	}

	@Override public double getHValue()
	{
		return this.heuristicValue;
	}

	@Override public double getFValue()
	{
		return getGValue()+getHValue();
	}

}
