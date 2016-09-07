package de.domisum.compitumapi.transitionalpath;

import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.compitumapi.transitionalpath.node.TransitionalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransitionalBlockPath
{

	// REFERENCES
	private TransitionalNode endNode;

	// PROPERTIES
	private List<TransitionalNode> nodes = new ArrayList<>();


	// -------
	// CONSTRUCTOR
	// -------
	public TransitionalBlockPath(TransitionalNode endNode)
	{
		this.endNode = endNode;

		generatePath();
	}

	private void generatePath()
	{
		TransitionalNode currentNode = this.endNode;
		while(currentNode != null)
		{
			this.nodes.add(currentNode);
			currentNode = currentNode.getParent();
		}

		Collections.reverse(this.nodes);
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public List<TransitionalNode> getNodes()
	{
		return this.nodes;
	}

	@APIUsage
	public double getLength()
	{
		return this.endNode.getWeight();
	}

}
