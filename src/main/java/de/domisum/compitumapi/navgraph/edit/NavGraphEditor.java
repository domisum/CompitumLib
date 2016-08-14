package de.domisum.compitumapi.navgraph.edit;

import com.darkblade12.particleeffect.ParticleEffect;
import de.domisum.auxiliumapi.data.container.math.Vector3D;
import de.domisum.compitumapi.CompitumAPI;
import de.domisum.compitumapi.navgraph.GraphEdge;
import de.domisum.compitumapi.navgraph.GraphNode;
import de.domisum.compitumapi.navgraph.NavGraph;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

class NavGraphEditor
{

	// CONSTANTS
	private static final double VISIBILITY_RANGE = 24;
	private static final double EDGE_PARTICLE_DISTANCE = 0.5;

	private static final double NODE_SELECTION_MAX_DISTANCE = 2;

	// REFERENCES
	private Player player;

	// STATUS
	private GraphNode connectedNode;


	// -------
	// CONSTRUCTOR
	// -------
	NavGraphEditor(Player player)
	{
		this.player = player;
	}


	// -------
	// GETTERS
	// -------
	Player getPlayer()
	{
		return this.player;
	}


	// -------
	// UPDATE
	// -------
	void update()
	{
		spawnParticles();

		if(this.connectedNode != null)
			if(this.connectedNode.getPositionVector().subtract(new Vector3D(this.player.getLocation())).lengthSquared()
					> VISIBILITY_RANGE*VISIBILITY_RANGE)
				disconnect();
	}


	// -------
	// GRAPH
	// -------
	private NavGraph getGraph()
	{
		return CompitumAPI.getNavGraphManager().getGraphAt(this.player.getLocation());
	}

	private GraphNode getNearbyNode()
	{
		NavGraph graph = getGraph();
		Vector3D playerLocation = new Vector3D(this.player.getLocation());

		double closesDistanceSquared = Double.MAX_VALUE;
		GraphNode closestNode = null;

		for(GraphNode node : graph.getNodes())
		{
			double distanceSquared = node.getPositionVector().distanceToSquared(playerLocation);
			if(distanceSquared < closesDistanceSquared)
			{
				closestNode = node;
				closesDistanceSquared = distanceSquared;
			}
		}

		if(closesDistanceSquared > NODE_SELECTION_MAX_DISTANCE*NODE_SELECTION_MAX_DISTANCE)
			return null;

		return closestNode;
	}


	// -------
	// VISUALIZATION
	// -------
	private void spawnParticles()
	{
		NavGraph graph = getGraph();
		if(graph == null)
			return;

		Vector3D playerLocation = new Vector3D(this.player.getLocation());

		Set<GraphNode> closeNodes = new HashSet<>();
		for(GraphNode n : graph.getNodes())
			if(n.getPositionVector().distanceToSquared(playerLocation) < VISIBILITY_RANGE*VISIBILITY_RANGE)
				closeNodes.add(n);

		Set<GraphEdge> closeEdges = new HashSet<>();
		for(GraphNode n : closeNodes)
			closeEdges.addAll(n.getEdges());

		for(GraphNode n : closeNodes)
			spawnNodeParticles(n);

		for(GraphEdge e : closeEdges)
			spawnEdgeParticles(e);

		if(this.connectedNode != null)
			spawnEdgeParticles(this.connectedNode.getPositionVector(), playerLocation);
	}

	private void spawnNodeParticles(GraphNode node)
	{
		Location location = node.getPositionVector().toLocation(this.player.getWorld());
		ParticleEffect.DAMAGE_INDICATOR.display(0, 0, 0, 0, 1, location, this.player);
	}

	private void spawnEdgeParticles(GraphEdge edge)
	{
		spawnEdgeParticles(edge.getNode1().getPositionVector(), edge.getNode2().getPositionVector());
	}

	private void spawnEdgeParticles(Vector3D start, Vector3D end)
	{
		Vector3D delta = end.subtract(start);
		for(double d = 0; d < delta.length(); d += EDGE_PARTICLE_DISTANCE)
		{
			Vector3D offset = delta.normalize().multiply(d);
			Vector3D vectorLocation = start.add(offset);
			Location location = vectorLocation.toLocation(this.player.getWorld()).add(0, 0.5, 0);

			ParticleEffect.FLAME.display(0, 0, 0, 0, 1, location, this.player);
		}
	}


	// -------
	// EDITING
	// -------
	void connect()
	{
		if(this.connectedNode != null)
		{
			this.player.sendMessage("Connecting failed. You are already connected to a node.");
			return;
		}

		GraphNode nearbyNode = getNearbyNode();
		if(nearbyNode == null)
		{
			this.player.sendMessage("Connecting failed. No node found nearby.");
			return;
		}

		this.connectedNode = nearbyNode;
	}

	void disconnect()
	{
		if(this.connectedNode == null)
		{
			this.player.sendMessage("Disconnecting failed. You are not connected to a node.");
			return;
		}

		this.connectedNode = null;
	}

	void newNode()
	{

	}

}
