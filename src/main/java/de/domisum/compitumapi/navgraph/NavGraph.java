package de.domisum.compitumapi.navgraph;

import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.keys.Base64Key;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NavGraph
{

	// PROPERTIES
	private String id;
	private Vector3D rangeCenter;
	private double range;

	// REFERENCES
	private World world;
	private Set<GraphNode> nodes = new HashSet<>();


	// -------
	// CONSTRUCTOR
	// -------
	public NavGraph(String id, Vector3D ranceCenter, double range, World world, Collection<GraphNode> nodes)
	{
		this.id = id;
		this.rangeCenter = ranceCenter;
		this.range = range;

		this.world = world;
		for(GraphNode gn : nodes)
			this.nodes.add(gn);
	}


	// -------
	// GETTERS
	// -------
	public String getId()
	{
		return this.id;
	}

	public Vector3D getRangeCenter()
	{
		return this.rangeCenter;
	}

	public double getRange()
	{
		return this.range;
	}

	public boolean isInRange(Location location)
	{
		if(location.getWorld() != this.world)
			return false;

		return new Vector3D(location).distanceToSquared(this.rangeCenter) < this.range*this.range;
	}

	public World getWorld()
	{
		return this.world;
	}


	public Set<GraphNode> getNodes()
	{
		return this.nodes;
	}

	public GraphNode getNode(String id)
	{
		for(GraphNode node : this.nodes)
			if(node.getId().equals(id))
				return node;

		return null;
	}

	public GraphNode getNearestNode(Location location)
	{
		if(!isInRange(location))
			return null;

		double closesDistanceSquared = Double.MAX_VALUE;
		GraphNode closestNode = null;

		for(GraphNode node : nodes)
		{
			double distanceSquared = node.getPositionVector().distanceToSquared(new Vector3D(location));
			if(distanceSquared < closesDistanceSquared)
			{
				closestNode = node;
				closesDistanceSquared = distanceSquared;
			}
		}

		return closestNode;
	}


	private String getUnusedNodeId()
	{
		String id;
		do
			id = Base64Key.generate(GraphNode.KEY_LENGTH);
		while(getNode(id) != null);

		return id;
	}


	// -------
	// CHANGERS
	// -------
	public GraphNode addNode(Vector3D location, GraphNode connectedNode, double edgeWeightModifier)
	{
		return addNode(location.x, location.y, location.z, connectedNode, edgeWeightModifier);
	}

	public GraphNode addNode(double x, double y, double z, GraphNode connectedNode, double edgeWeightModifier)
	{
		GraphNode node = new GraphNode(getUnusedNodeId(), x, y, z);
		if(connectedNode != null)
			node.addEdge(connectedNode, edgeWeightModifier);

		this.nodes.add(node);
		return node;
	}

	public void removeNode(GraphNode node)
	{
		// cloning the HashSet to avoid ConcurrentModification since edges are removed from both nodes
		for(GraphEdge edge : new HashSet<>(node.getEdges()))
			edge.getOther(node).removeEdge(node);

		nodes.remove(node);
	}

}
