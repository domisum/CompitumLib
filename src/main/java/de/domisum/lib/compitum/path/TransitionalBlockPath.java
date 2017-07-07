package de.domisum.lib.compitum.path;

import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.compitum.path.node.TransitionalBlockNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransitionalBlockPath
{

	// PROPERTIES
	private List<TransitionalBlockNode> nodes = new ArrayList<>();


	// INIT
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


	// GETTERS
	@APIUsage public List<TransitionalBlockNode> getNodes()
	{
		return this.nodes;
	}

}
