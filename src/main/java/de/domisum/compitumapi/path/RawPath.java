package de.domisum.compitumapi.path;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

import de.domisum.compitumapi.path.node.Node;

public class RawPath
{

	// STATUS
	private World world;
	private List<Node> nodes;


	// -------
	// CONSTRUCTOR
	// -------
	public RawPath(World world, List<Node> nodes)
	{
		this.world = world;
		this.nodes = nodes;
	}

	@Override
	public String toString()
	{
		String string = "[";

		for(int i = 0; i < this.nodes.size(); i++)
			string += this.nodes.get(i) + (i != (this.nodes.size() - 1) ? "," : "");

		return string + "]";
	}


	// -------
	// GETTERS
	// -------
	public World getWorld()
	{
		return this.world;
	}

	public List<Node> getNodes()
	{
		return new ArrayList<>(this.nodes);
	}


	public int getLength()
	{
		return this.nodes.size();
	}

}
