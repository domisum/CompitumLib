package de.domisum.compitumapi.movementgraph.json;

import de.domisum.auxiliumapi.util.java.annotations.SetByDeserialization;
import de.domisum.compitumapi.movementgraph.MovementGraph;

import java.util.List;

public class SerializationGraph
{

	// REFERENCES
	@SetByDeserialization
	private List<SerializationNode> nodes;

	// PROPERTIES
	@SetByDeserialization
	private String world;


	// -------
	// CONSTRUCTOR
	// -------
	public SerializationGraph()
	{

	}


	// -------
	// GETTERS
	// -------
	public MovementGraph getGraph()
	{
		return null; // TODO
	}

}
