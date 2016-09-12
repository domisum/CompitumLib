package de.domisum.lib.compitum.navgraph;

public class GraphEdge
{

	// REFERENCES
	private final GraphNode node1;
	private final GraphNode node2;

	// PROPERTIES
	private double weightModifier;

	// STATUS
	private double rawWeight = -1;


	// -------
	// CONSTRUCTOR
	// -------
	public GraphEdge(GraphNode node1, GraphNode node2, double weightModifier)
	{
		if(node1 == node2)
			throw new IllegalArgumentException("Can't connect a node to itself");

		this.node1 = node1;
		this.node2 = node2;

		this.weightModifier = weightModifier;
	}


	// -------
	// GETTERS
	// -------
	public GraphNode getNode1()
	{
		return this.node1;
	}

	public GraphNode getNode2()
	{
		return this.node2;
	}

	public GraphNode getOther(GraphNode node)
	{
		if(this.node1 == node)
			return this.node2;

		if(this.node2 == node)
			return this.node1;

		throw new IllegalArgumentException("The provided node is not connected through this edge");
	}


	public double getWeightModifier()
	{
		return this.weightModifier;
	}


	// -------
	// SETTERS
	// -------
	public void setWeightModifier(double weightModifier)
	{
		this.weightModifier = weightModifier;
	}


	// -------
	// WEIGHT
	// -------
	public double getWeight()
	{
		if(this.rawWeight == -1)
			calculateRawWeight();

		return this.rawWeight*this.weightModifier;
	}

	private void calculateRawWeight()
	{
		this.rawWeight = this.node1.getPositionVector().subtract(this.node2.getPositionVector()).length();
	}

}
