package de.domisum.compitumapi.navgraph.json;

import de.domisum.auxiliumapi.data.container.Duo;
import de.domisum.auxiliumapi.data.container.math.Vector3D;
import de.domisum.auxiliumapi.util.java.annotations.DeserializationNoArgsConstructor;
import de.domisum.auxiliumapi.util.java.annotations.SetByDeserialization;
import de.domisum.compitumapi.navgraph.GraphNode;
import de.domisum.compitumapi.navgraph.NavGraph;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializationGraph
{

	// PROPERTIES
	@SetByDeserialization
	private String worldName;
	@SetByDeserialization
	private Vector3D rangeCenter;
	@SetByDeserialization
	private double range;

	// REFERENCES
	@SetByDeserialization
	private List<SerializationNode> nodes = new ArrayList<>();


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public SerializationGraph()
	{

	}


	// -------
	// CONVERSION
	// -------
	public SerializationGraph(NavGraph graph)
	{
		this.worldName = graph.getWorld().getName();
		this.rangeCenter = graph.getRangeCenter();
		this.range = graph.getRange();

		for(GraphNode gn : graph.getNodes())
			this.nodes.add(new SerializationNode(gn));
	}

	public NavGraph convertToNavGraph(String id)
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
				if(!node.isConnected(targetNode)) // connections are specified both ways
					node.addEdge(targetNode, edge.b);
			}
		}

		return new NavGraph(id, this.rangeCenter, this.range, Bukkit.getWorld(this.worldName), nodesById.values());
	}

}
