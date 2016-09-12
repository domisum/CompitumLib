package de.domisum.compitumapi.navgraph.pathfinding;

import de.domisum.compitumapi.navgraph.GraphEdge;
import de.domisum.compitumapi.navgraph.GraphNode;
import de.domisum.lib.auxilium.util.DebugUtil;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxilium.util.math.MathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NavGraphAStar
{

	// INPUT
	private GraphNode startNode;
	private GraphNode endNode;

	// STATUS
	private Set<GraphNode> unvisitedNodes = new HashSet<>();
	private Set<GraphNode> visitedNodes = new HashSet<>();

	private Map<GraphNode, GraphNode> parentage = new HashMap<>(); // <child, parent>
	private Map<GraphNode, Double> costToReach = new HashMap<>();

	private long startNano;
	private long endNano;

	// OUTPUT
	private List<GraphNode> path;


	// -------
	// CONSTRUCTOR
	// -------
	public NavGraphAStar(GraphNode startNode, GraphNode endNode)
	{
		this.startNode = startNode;
		this.endNode = endNode;
	}


	// -------
	// GETTERS
	// -------
	public List<GraphNode> getPath()
	{
		return this.path;
	}

	@APIUsage
	public boolean isPathFound()
	{
		return this.path != null;
	}


	private long getDurationNano()
	{
		return this.endNano-this.startNano;
	}

	public double getDurationMicro()
	{
		return MathUtil.round(getDurationNano()/1000d, 0);
	}


	private double getGValue(GraphNode node)
	{
		if(!this.costToReach.containsKey(node))
			return 0;

		return this.costToReach.get(node);
	}

	private GraphNode getNextUnvisitedNode()
	{
		// TODO improve perfomance of this
		double minimumF = Double.MAX_VALUE;
		GraphNode mostPromisingNode = null;

		for(GraphNode gn : this.unvisitedNodes)
		{
			double g = getGValue(gn);
			double h = this.endNode.getPositionVector().subtract(gn.getPositionVector()).length();
			double f = g+h;

			if(f < minimumF)
			{
				minimumF = f;
				mostPromisingNode = gn;
			}
		}

		return mostPromisingNode;
	}


	// -------
	// PATHFINDING
	// -------
	public void findPath()
	{
		this.startNano = System.nanoTime();

		this.unvisitedNodes.add(this.startNode);
		while(true)
		{
			if(this.unvisitedNodes.size() == 0)
			{
				DebugUtil.say("no unvisited nodes left");
				break;
			}

			GraphNode nodeToVisit = getNextUnvisitedNode();
			if(nodeToVisit.equals(this.endNode))
				break;

			visitNode(nodeToVisit);
		}

		if(this.parentage.get(this.endNode) != null)
			createPathFromParentage();

		this.endNano = System.nanoTime();
	}

	private void visitNode(GraphNode node)
	{
		this.unvisitedNodes.remove(node);
		this.visitedNodes.add(node);

		for(GraphEdge edge : node.getEdges())
		{
			GraphNode otherNode = edge.getOther(node);

			if(this.visitedNodes.contains(otherNode))
				continue;

			// not sure if this is the right way, maybe needs updating of parent? idk
			if(this.unvisitedNodes.contains(otherNode))
			{
				double currentGValue = getGValue(otherNode);
				double gValueFromHere = getGValue(node)+edge.getWeight();

				// only continue if the node is not reachable by this one faster
				if(gValueFromHere > currentGValue)
					continue;
			}

			addNode(node, otherNode);
		}
	}

	private void addNode(GraphNode parent, GraphNode newNode)
	{
		this.parentage.put(newNode, parent);
		double additionalCost = newNode.getEdge(parent).getWeight();
		this.costToReach.put(newNode, getGValue(parent)+additionalCost);

		this.unvisitedNodes.add(newNode);
	}


	private void createPathFromParentage()
	{
		this.path = new ArrayList<>();

		GraphNode currentNode = this.endNode;
		while(currentNode != null)
		{
			this.path.add(currentNode);
			currentNode = this.parentage.get(currentNode);
		}

		Collections.reverse(this.path);
	}

}
