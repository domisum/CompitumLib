package de.domisum.compitumapi.navgraph.json;

import de.domisum.auxiliumapi.data.container.Duo;
import de.domisum.auxiliumapi.util.java.annotations.DeserializationNoArgsConstructor;
import de.domisum.auxiliumapi.util.java.annotations.SetByDeserialization;
import de.domisum.compitumapi.navgraph.GraphNode;
import de.domisum.compitumapi.navgraph.NavGraph;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializationGraph
{

	// PROPERTIES
	@SetByDeserialization
	private String worldName;

	// REFERENCES
	@SetByDeserialization
	private List<SerializationNode> nodes;


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public SerializationGraph()
	{

	}


	// -------
	// GETTERS
	// -------
	public NavGraph getGraph()
	{
		Map<String, GraphNode> nodesById = new HashMap<>();

		// create nodes
		for(SerializationNode sn : this.nodes)
			nodesById.put(sn.getId(), sn.getUnconnectedNode());

		// connect nodes
		for(SerializationNode sn : this.nodes)
		{
			GraphNode node = nodesById.get(sn.getId());
			for(Duo<String, Double> edge : sn.getEdges())
			{
				GraphNode targetNode = nodesById.get(edge.a);
				node.addEdge(targetNode, edge.b);
			}
		}

		// add nodes to graph
		NavGraph graph = new NavGraph(Bukkit.getWorld(this.worldName));
		for(GraphNode node : nodesById.values())
			graph.addNode(node);
		return graph;
	}

}
