package de.domisum.compitumapi.transitionalpath.path;

import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.compitumapi.transitionalpath.node.TransitionalBlockNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransitionalBlockPath
{

	// PROPERTIES
	private List<TransitionalBlockNode> nodes = new ArrayList<>();


	// -------
	// CONSTRUCTOR
	// -------
	public TransitionalBlockPath(TransitionalBlockNode endNode)
	{
		generatePath(endNode);
	}

	private void generatePath(TransitionalBlockNode endNode)
	{
		TransitionalBlockNode currentNode = endNode;
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

}
