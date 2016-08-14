package de.domisum.compitumapi.movementgraph.json;

import de.domisum.auxiliumapi.data.container.Duo;
import de.domisum.auxiliumapi.util.java.annotations.SetByDeserialization;
import de.domisum.compitumapi.movementgraph.GraphNode;

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
	public GraphNode getUnconnectedNode()
	{
		return new GraphNode(this.id, this.x, this.y, this.z);
	}

}
