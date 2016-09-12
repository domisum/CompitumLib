package de.domisum.compitumapi.navgraph.json;

import de.domisum.compitumapi.navgraph.GraphEdge;
import de.domisum.compitumapi.navgraph.GraphNode;
import de.domisum.lib.auxilium.data.container.Duo;
import de.domisum.lib.auxilium.util.java.annotations.DeserializationNoArgsConstructor;
import de.domisum.lib.auxilium.util.java.annotations.SetByDeserialization;

import java.util.ArrayList;
import java.util.List;

class SerializationNode
{

	// PROPERTIES
	@SetByDeserialization
	private String id;

	@SetByDeserialization
	private double x;
	@SetByDeserialization
	private double y;
	@SetByDeserialization
	private double z;

	// REFERENCES
	@SetByDeserialization
	private List<Duo<String, Double>> edges = new ArrayList<>();


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public SerializationNode()
	{

	}

	SerializationNode(GraphNode graphNode)
	{
		this.id = graphNode.getId();

		this.x = graphNode.getX();
		this.y = graphNode.getY();
		this.z = graphNode.getZ();

		for(GraphEdge e : graphNode.getEdges())
			this.edges.add(new Duo<>(e.getOther(graphNode).getId(), e.getWeightModifier()));
	}


	// -------
	// GETTERS
	// -------
	String getId()
	{
		return this.id;
	}

	GraphNode getUnconnectedNode()
	{
		return new GraphNode(this.id, this.x, this.y, this.z);
	}

	List<Duo<String, Double>> getEdges()
	{
		return this.edges;
	}

}
