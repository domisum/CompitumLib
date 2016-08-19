package de.domisum.compitumapi.navgraph.edit;

import com.darkblade12.particleeffect.ParticleEffect;
import de.domisum.auxiliumapi.data.container.math.Vector3D;
import de.domisum.auxiliumapi.util.bukkit.MessagingUtil;
import de.domisum.auxiliumapi.util.math.MathUtil;
import de.domisum.compitumapi.CompitumAPI;
import de.domisum.compitumapi.navgraph.GraphEdge;
import de.domisum.compitumapi.navgraph.GraphNode;
import de.domisum.compitumapi.navgraph.NavGraph;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

class NavGraphEditor
{

	// CONSTANTS
	private static final double VISIBILITY_RANGE = 24;
	private static final double EDGE_PARTICLE_DISTANCE = 0.2;

	private static final double NODE_SELECTION_MAX_DISTANCE = 1.5;

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
	void update(int updateCount)
	{
		NavGraph graph = getGraph();
		if(graph != null)
			spawnParticles(graph, updateCount);

		sendNearbyGraphName(graph);

		if(this.connectedNode != null)
			if(this.connectedNode.getPositionVector().subtract(new Vector3D(this.player.getLocation())).lengthSquared()
					> VISIBILITY_RANGE*VISIBILITY_RANGE)
			{
				this.player.sendMessage("You were disconnected from the node.");
				disconnect();
			}
	}

	private void sendNearbyGraphName(NavGraph graph)
	{
		String graphName = ChatColor.RED+"No graph in range";
		if(graph != null)
			graphName = "Graph: '"+graph.getId()+"'";

		MessagingUtil.sendActionBarMessage(graphName, this.player);
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
		if(graph == null)
			return null;

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
	private void spawnParticles(NavGraph graph, int updateCount)
	{
		Vector3D playerLocation = new Vector3D(this.player.getLocation());

		Set<GraphNode> closeNodes = new HashSet<>();
		for(GraphNode n : graph.getNodes())
			if(n.getPositionVector().distanceToSquared(playerLocation) < VISIBILITY_RANGE*VISIBILITY_RANGE)
				closeNodes.add(n);

		for(GraphNode n : closeNodes)
			spawnNodeParticles(n);

		int edgeParticleRarity = 1;

		if(updateCount%edgeParticleRarity == 0)
		{
			Set<GraphEdge> closeEdges = new HashSet<>();
			for(GraphNode n : closeNodes)
				closeEdges.addAll(n.getEdges());

			for(GraphEdge e : closeEdges)
				spawnEdgeParticles(e);
		}

		if(this.connectedNode != null && updateCount%edgeParticleRarity == 0)
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
		GraphNode nearbyNode = getNearbyNode();
		if(nearbyNode == null)
		{
			this.player.sendMessage("Connecting failed. No node found nearby.");
			return;
		}

		if(this.connectedNode != null)
		{
			if(nearbyNode == this.connectedNode)
			{
				this.player.sendMessage("Connecting failed. You can't connect a node to itself.");
				return;
			}

			if(this.connectedNode.isConnected(nearbyNode))
			{
				this.player.sendMessage("Connecting failed. The nodes are already connected.");
				return;
			}

			this.connectedNode.addEdge(nearbyNode, 1);
			this.connectedNode = null;
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

	void createNode()
	{
		NavGraph graph = getGraph();
		if(graph == null)
		{
			this.player.sendMessage("Creating node failed. No graph is covering this area.");
			return;
		}

		if(this.connectedNode == null)
			this.player.sendMessage("Warning: The created node is not connected to any other node.");

		this.connectedNode = graph.addNode(new Vector3D(this.player.getLocation()), this.connectedNode, 1);
	}

	void removeNode()
	{
		GraphNode node = getNearbyNode();
		if(node == null)
		{
			this.player.sendMessage("Removing node failed. No node nearby.");
			return;
		}

		// graph can't be null since a node has been found
		NavGraph graph = getGraph();
		graph.removeNode(node);

		if(node == this.connectedNode)
			this.connectedNode = null;
	}

	void info()
	{
		GraphNode node = getNearbyNode();
		if(node == null)
		{
			this.player.sendMessage("Removing node failed. No node nearby.");
			return;
		}
		NavGraph graph = getGraph();

		this.player.sendMessage("Node '"+node.getId()+"' in graph '"+graph.getId()+"':");
		this.player.sendMessage("  x: "+MathUtil.round(node.getX(), 2)+", y: "+MathUtil.round(node.getY(), 2)+", z: "+MathUtil
				.round(node.getZ(), 2));

		this.player.sendMessage("  connected to:");
		if(node.getEdges().size() == 0)
			this.player.sendMessage("    none");
		else
			for(GraphEdge edge : node.getEdges())
			{
				String edgeString = "    ";
				edgeString += "'"+edge.getOther(node).getId()+"'";
				edgeString += ", ";
				edgeString += "mod: "+MathUtil.round(edge.getWeightModifier(), 3);

				this.player.sendMessage(edgeString);
			}
	}

}
