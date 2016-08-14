package de.domisum.compitumapi.navgraph;

import org.bukkit.World;

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
	public NavGraph(World world)
	{
		this.world = world;
	}


	// -------
	// GETTERS
	// -------
	public GraphNode getNode(String id)
	{
		for(GraphNode node : nodes)
			if(node.getId().equals(id))
				return node;

		return null;
	}


	// -------
	// CHANGERS
	// -------
	public void addNode(GraphNode node)
	{
		nodes.add(node);
	}

}
