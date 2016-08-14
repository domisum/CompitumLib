package de.domisum.compitumapi.navgraph;

import de.domisum.auxiliumapi.data.container.Duo;
import de.domisum.auxiliumapi.util.keys.Base64Key;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NavGraph
{

	// REFERENCES
	private World world;
	private Set<GraphNode> nodes = new HashSet<>();


	// -------
	// CONSTRUCTOR
	// -------
	public NavGraph(World world, Collection<GraphNode> nodes)
	{
		this.world = world;
		for(GraphNode gn : nodes)
			this.nodes.add(gn);
	}

	public NavGraph(World world)
	{
		this(world, new HashSet<>());
	}


	// -------
	// GETTERS
	// -------
	public World getWorld()
	{
		return world;
	}

	public Set<GraphNode> getNodes()
	{
		return nodes;
	}

	public GraphNode getNode(String id)
	{
		for(GraphNode node : this.nodes)
			if(node.getId().equals(id))
				return node;

		return null;
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
	public GraphNode addNode(double x, double y, double z, Duo<String, Double>... connectedNodes)
	{
		GraphNode node = new GraphNode(getUnusedNodeId(), x, y, z);
		for(Duo<String, Double> cn : connectedNodes)
		{
			GraphNode connectedNode = getNode(cn.a);
			node.addEdge(connectedNode, cn.b);
		}

		this.nodes.add(node);
		return node;
	}

}
