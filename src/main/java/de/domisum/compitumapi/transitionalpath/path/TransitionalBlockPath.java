package de.domisum.compitumapi.transitionalpath.path;

import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.compitumapi.transitionalpath.node.TransitionalBlockNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransitionalBlockPath
{

	// REFERENCES
	private TransitionalBlockNode endNode;

	// PROPERTIES
	private List<TransitionalBlockNode> nodes = new ArrayList<>();


	// -------
	// CONSTRUCTOR
	// -------
	public TransitionalBlockPath(TransitionalBlockNode endNode)
	{
		this.endNode = endNode;

		generatePath();
	}

	private void generatePath()
	{
		TransitionalBlockNode currentNode = this.endNode;
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
	public List<TransitionalBlockNode> getNodes()
	{
		return this.nodes;
	}

	@APIUsage
	public double getLength()
	{
		return this.endNode.getWeight();
	}

}
