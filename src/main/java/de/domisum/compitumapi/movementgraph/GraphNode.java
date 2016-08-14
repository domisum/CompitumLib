package de.domisum.compitumapi.movementgraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GraphNode
{

	// CONSTANTS
	public static final int KEY_LENGTHS = 4;

	// PROPERTIES
	private final String id;

	private double x;
	private double y;
	private double z;

	// REFERENCES
	private List<GraphEdge> edges = new ArrayList<>();


	// -------
	// CONSTRUCTOR
	// -------
	public GraphNode(String id, double x, double y, double z)
	{
		this.id = id;

		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof GraphNode))
			return false;

		GraphNode other = (GraphNode) o;
		return this.id.equals(other.id);
	}

	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}


	// -------
	// GETTERS
	// -------
	public double getX()
	{
		return this.x;
	}

	public double getY()
	{
		return this.y;
	}

	public double getZ()
	{
		return this.z;
	}


	public int getNumberOfEdges()
	{
		return this.edges.size();
	}

	public boolean isConnected(GraphNode other)
	{
		for(GraphEdge e : this.edges)
			if(e.getOther(this) == other)
				return true;

		return false;
	}


	// -------
	// CHANGERS
	// -------
	public void addEdge(GraphNode node, double weight)
	{
		if(isConnected(node))
			throw new IllegalStateException(
					"Creating edge failed. The node '"+this.id+"' is already connected to the node '"+node.id+"'");

		GraphEdge edge = new GraphEdge(this, node, weight);
		this.edges.add(edge);
		node.edges.remove(edge);
	}


	public void removeEdge(GraphNode node)
	{
		if(!isConnected(node))
			throw new IllegalStateException(
					"Removing edge failed. The node '"+this.id+"' isn't connected to the node '"+this.id+"'");

		Iterator<GraphEdge> iterator = this.edges.iterator();
		while(iterator.hasNext())
		{
			GraphEdge edge = iterator.next();
			if(edge.getOther(this) == node)
			{
				iterator.remove();
				node.edges.remove(edge);
				break;
			}
		}
	}

}
