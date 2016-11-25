package de.domisum.lib.compitum.navmesh.path;


import de.domisum.lib.auxilium.util.TextUtil;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxilium.util.java.debug.DebugUtil;
import de.domisum.lib.auxilium.util.java.debug.ProfilerStopWatch;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.navmesh.transition.NavMeshTriangleTransition;
import de.domisum.lib.compitum.universal.SortedWeightedNodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@APIUsage
public class NavMeshTrianglePathfinder
{

	// PROPERTIES
	private int maxNodeVisits = 200;

	private boolean canUseLadders = true;

	// INPUT
	private NavMeshTriangle startTriangle;
	private NavMeshTriangle targetTriangle;

	// STATUS
	private Set<NavMeshTriangleNode> visitedNodes = new HashSet<>(this.maxNodeVisits);
	private SortedWeightedNodeList<NavMeshTriangleNode> unvisitedNodes = new SortedWeightedNodeList<>(this.maxNodeVisits*3);

	private ProfilerStopWatch stopWatch = new ProfilerStopWatch("pathfinding.navMesh.triangleSequence");

	// OUTPUT
	private List<NavMeshTriangle> triangleSequence = null;
	private String failure;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public NavMeshTrianglePathfinder(NavMeshTriangle startTriangle, NavMeshTriangle targetTriangle)
	{
		this.startTriangle = startTriangle;
		this.targetTriangle = targetTriangle;
	}


	// -------
	// GETTERS
	// -------
	public List<NavMeshTriangle> getTriangleSequence()
	{
		return this.triangleSequence;
	}

	public String getFailure()
	{
		return this.failure;
	}

	@APIUsage
	public ProfilerStopWatch getStopWatch()
	{
		return this.stopWatch;
	}


	@APIUsage
	public boolean canUseLadders()
	{
		return this.canUseLadders;
	}


	// -------
	// SETTERS
	// -------
	@APIUsage
	public void setCanUseLadders(boolean canUseLadders)
	{
		this.canUseLadders = canUseLadders;
	}


	// -------
	// PATHFINDING
	// -------
	@APIUsage
	public void findPath()
	{
		this.stopWatch.start();

		NavMeshTriangleNode targetNode = null;

		// pathfinding
		this.unvisitedNodes
				.addSorted(new NavMeshTriangleNode(this.startTriangle, null, calculateHeuristicValue(this.startTriangle)));
		while(true)
		{
			if(this.visitedNodes.size() >= this.maxNodeVisits)
			{
				this.failure = "Too many nodes visited";
				break;
			}

			if(this.unvisitedNodes.getSize() == 0)
			{
				this.failure = "No unvisted nodes left";
				break;
			}

			NavMeshTriangleNode node = this.unvisitedNodes.getAndRemoveFirst();
			if(this.targetTriangle.equals(node.getTriangle()))
			{
				targetNode = node;
				break;
			}

			visitNode(node);
			this.visitedNodes.add(node);
		}

		// converting linked node list into triangle list
		if(targetNode != null)
		{
			this.triangleSequence = new ArrayList<>();

			NavMeshTriangleNode currentNode = targetNode;
			while(currentNode != null)
			{
				this.triangleSequence.add(currentNode.getTriangle());
				currentNode = currentNode.getParent();
			}

			Collections.reverse(this.triangleSequence);
		}

		this.stopWatch.stop();

		DebugUtil.say(TextUtil.getListAsString(triangleSequence));
	}

	private void visitNode(NavMeshTriangleNode node)
	{
		NavMeshTriangle triangle = node.getTriangle();
		for(Map.Entry<NavMeshTriangle, NavMeshTriangleTransition> entry : triangle.neighbors.entrySet())
		{
			NavMeshTriangleNode newNode = new NavMeshTriangleNode(entry.getKey(), node, calculateHeuristicValue(triangle));
			if(this.visitedNodes.contains(newNode))
				continue;

			if(this.unvisitedNodes.contains(newNode))
				continue;

			this.unvisitedNodes.addSorted(newNode);
		}
	}


	private double calculateHeuristicValue(NavMeshTriangle triangle)
	{
		double dX = this.targetTriangle.getHeuristicCenter().x-triangle.getHeuristicCenter().x;
		double dY = this.targetTriangle.getHeuristicCenter().y-triangle.getHeuristicCenter().y;
		double dZ = this.targetTriangle.getHeuristicCenter().z-triangle.getHeuristicCenter().z;

		double dXAbs = Math.abs(dX);
		double dYAbs = Math.abs(dY);
		double dZAbs = Math.abs(dZ);

		// diagonal distance of x and z
		double minD = Math.min(dXAbs, dZAbs);
		double maxD = Math.max(dXAbs, dZAbs);
		double diagonalDistance = (maxD-minD)+minD*1.414;

		// add dY to account for height difference
		return diagonalDistance+dYAbs;
	}

}
