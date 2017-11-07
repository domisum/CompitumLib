package de.domisum.lib.compitum.path;

import de.domisum.lib.auxilium.util.java.annotations.API;
import de.domisum.lib.compitum.path.node.BlockPathNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockPath
{

	// PROPERTIES
	private List<BlockPathNode> nodes = new ArrayList<>();


	// INIT
	public BlockPath(BlockPathNode endNode)
	{
		generatePath(endNode);
	}

	private void generatePath(BlockPathNode endNode)
	{
		BlockPathNode currentNode = endNode;
		while(currentNode != null)
		{
			this.nodes.add(currentNode);
			currentNode = currentNode.getParent();
		}

		Collections.reverse(this.nodes);
	}


	// GETTERS
	@API public List<BlockPathNode> getNodes()
	{
		return this.nodes;
	}

}
