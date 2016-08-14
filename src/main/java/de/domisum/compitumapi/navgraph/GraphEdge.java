package de.domisum.compitumapi.navgraph;

public class GraphEdge
{

	// REFERENCES
	private final GraphNode node1;
	private final GraphNode node2;

	// PROPERTIES
	private double weight;


	// -------
	// CONSTRUCTOR
	// -------
	public GraphEdge(GraphNode node1, GraphNode node2, double weight)
	{
		this.node1 = node1;
		this.node2 = node2;

		this.weight = weight;
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


	public double getWeight()
	{
		return this.weight;
	}


	// -------
	// SETTERS
	// -------
	public void setWeight(double weight)
	{
		this.weight = weight;
	}

}
