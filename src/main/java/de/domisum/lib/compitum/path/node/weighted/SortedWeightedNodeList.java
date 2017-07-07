package de.domisum.lib.compitum.path.node.weighted;

import de.domisum.lib.auxilium.util.java.annotations.APIUsage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SortedWeightedNodeList<T extends WeightedNode>
{

	private List<T> nodes;
	private Set<T> nodesContainsTester;


	// INIT
	public SortedWeightedNodeList(int length)
	{
		this.nodes = new ArrayList<>(length);
		this.nodesContainsTester = new HashSet<>(length);
	}


	// GETTERS
	public T getAndRemoveFirst()
	{
		if(this.nodes.isEmpty())
			return null;

		T firstNode = this.nodes.remove(0);
		this.nodesContainsTester.remove(firstNode);

		return firstNode;
	}

	public boolean contains(T node)
	{
		return this.nodesContainsTester.contains(node);
	}

	public int getSize()
	{
		return this.nodes.size();
	}

	private double getValueToCompare(T node)
	{
		return node.getFValue();
	}


	@Deprecated public List<T> getNodes()
	{
		return this.nodes;
	}


	// CHANGERS
	public void addSorted(T node)
	{
		// don't subtract one from the size since the element could be added after the last current entry
		this.nodesContainsTester.add(node);
		insertIntoList(node, 0, this.nodes.size());
	}

	private void insertIntoList(T node, int lowerBound, int upperBound)
	{
		if(lowerBound == upperBound)
		{
			this.nodes.add(lowerBound, node);
			return;
		}

		double nodeValue = getValueToCompare(node);

		int dividingIndex = (lowerBound+upperBound)/2;
		T dividingNode = this.nodes.get(dividingIndex);
		double dividingValue = getValueToCompare(dividingNode);

		if(nodeValue > dividingValue)
			insertIntoList(node, dividingIndex+1, upperBound);
		else
			insertIntoList(node, lowerBound, dividingIndex);
	}

	public void clear()
	{
		this.nodes.clear();
		this.nodesContainsTester.clear();
	}


	@APIUsage public void sort()
	{
		Collections.sort(this.nodes, (n1, n2)->getValueToCompare(n1) > getValueToCompare(n2) ? 1 : -1);
	}

}
