package de.domisum.compitumapi.navgraph.json;

import de.domisum.auxiliumapi.data.container.Duo;
import de.domisum.auxiliumapi.util.java.annotations.SetByDeserialization;
import de.domisum.compitumapi.navgraph.GraphNode;

import java.util.List;

public class SerializationNode
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
	private List<Duo<String, Double>> edges;


	// -------
	// CONSTRUCTOR
	// -------
	public SerializationNode()
	{

	}


	// -------
	// GETTERS
	// -------
	public String getId()
	{
		return this.id;
	}

	public GraphNode getUnconnectedNode()
	{
		return new GraphNode(this.id, this.x, this.y, this.z);
	}

	public List<Duo<String, Double>> getEdges()
	{
		return edges;
	}

}
