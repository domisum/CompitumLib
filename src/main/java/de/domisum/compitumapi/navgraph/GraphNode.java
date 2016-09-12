package de.domisum.compitumapi.navgraph;

import de.domisum.lib.auxilium.data.container.math.Vector3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GraphNode
{

	// CONSTANTS
	public static final int KEY_LENGTH = 5;

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
	public String getId()
	{
		return this.id;
	}

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

	public Vector3D getPositionVector()
	{
		return new Vector3D(x, y, z);
	}


	public List<GraphEdge> getEdges()
	{
		return edges;
	}

	public GraphEdge getEdge(GraphNode other)
	{
		for(GraphEdge e : this.edges)
			if(e.getOther(this) == other)
				return e;

		return null;
	}

	public boolean isConnected(GraphNode other)
	{
		return getEdge(other) != null;
	}


	// -------
	// CHANGERS
	// -------
	public void addEdge(GraphNode node, double weightModifier)
	{
		if(isConnected(node))
			throw new IllegalStateException(
					"Creating edge failed. The node '"+this.id+"' is already connected to the node '"+node.id+"'");

		GraphEdge edge = new GraphEdge(this, node, weightModifier);
		this.edges.add(edge);
		node.edges.add(edge);
	}


	public void removeEdge(GraphNode node)
	{
		if(!isConnected(node))
			throw new IllegalStateException(
					"Removing edge failed. The node '"+this.id+"' isn't connected to the node '"+node.id+"'");

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
