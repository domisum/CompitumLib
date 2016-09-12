package de.domisum.lib.compitum.transitionalpath.path;

import de.domisum.lib.compitum.transitionalpath.node.TransitionalBlockNode;
import de.domisum.lib.auxilium.data.container.Duo;
import de.domisum.lib.auxilium.data.container.math.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class TransitionalPathSmoother
{

	// INPUT
	private TransitionalBlockPath blockPath;

	// OUTPUT
	private TransitionalPath smoothPath;


	// -------
	// CONSTRUCTOR
	// -------
	public TransitionalPathSmoother(TransitionalBlockPath blockPath)
	{
		this.blockPath = blockPath;
	}


	// -------
	// GETTERS
	// -------
	public TransitionalPath getSmoothPath()
	{
		return this.smoothPath;
	}


	// -------
	// CONVERSION
	// -------
	public void convert()
	{
		List<Duo<Vector3D, Integer>> waypoints = new ArrayList<>();
		for(TransitionalBlockNode blockNode : this.blockPath.getNodes())
			waypoints.add(new Duo<>(new Vector3D(blockNode.x+0.5, blockNode.y, blockNode.z+0.5), blockNode.getTransitionType()));

		this.smoothPath = new TransitionalPath(waypoints);
	}

}
