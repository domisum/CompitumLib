package de.domisum.lib.compitum.block;

import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.compitum.path.node.BlockPathNode;
import de.domisum.lib.compitum.path.BlockPath;
import de.domisum.lib.compitum.path.Path;
import de.domisum.lib.compitum.path.PathWaypoint;

import java.util.ArrayList;
import java.util.List;

public class BlockPathSmoother
{

	// INPUT
	private BlockPath blockPath;

	// OUTPUT
	private Path smoothPath;


	// INIT
	public BlockPathSmoother(BlockPath blockPath)
	{
		this.blockPath = blockPath;
	}


	// GETTERS
	public Path getSmoothPath()
	{
		return this.smoothPath;
	}


	// CONVERSION
	public void convert()
	{
		List<PathWaypoint> pathWaypoints = new ArrayList<>();
		for(BlockPathNode blockPathNode : this.blockPath.getNodes())
			pathWaypoints.add(new PathWaypoint(new Vector3D(blockPathNode.x+0.5, blockPathNode.y, blockPathNode.z+0.5),
					blockPathNode.getTransitionType()));

		this.smoothPath = new Path(pathWaypoints);
	}

}
