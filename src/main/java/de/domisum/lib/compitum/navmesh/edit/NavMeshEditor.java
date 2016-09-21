package de.domisum.lib.compitum.navmesh.edit;

import com.darkblade12.particleeffect.ParticleEffect;
import de.domisum.lib.auxilium.data.container.InterchangableDuo;
import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.bukkit.MessagingUtil;
import de.domisum.lib.compitum.CompitumLib;
import de.domisum.lib.compitum.navmesh.NavMesh;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshPoint;
import de.domisum.lib.compitum.navmesh.geometry.NavMeshTriangle;
import de.domisum.lib.compitum.navmesh.transition.NavMeshLadder;
import de.domisum.lib.compitum.navmesh.transition.NavMeshTriangleTransition;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class NavMeshEditor
{

	// CONSTANTS
	private static final double VISIBILITY_RANGE = 24;
	private static final double LINE_PARTICLE_DISTANCE = 0.4;

	private static final double SELECTION_MAX_DISTANCE = 1.5;

	// PROPERTIES
	// editor
	private boolean snapPointsToBlockCenter = true;

	// display
	private boolean showTriangleConnections = false;
	private boolean showTriangleNavigationConnections = false;

	// REFERENCES
	private Player player;
	private List<NavMeshPoint> selectedPoints = new ArrayList<>();
	private NavMeshPoint movingPoint;

	private Vector3D ladderPosition;
	private NavMeshTriangle ladderTriangle;


	// -------
	// CONSTRUCTOR
	// -------
	NavMeshEditor(Player player)
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


	private NavMesh getNavMesh()
	{
		return CompitumLib.getNavMeshManager().getNavMeshAt(this.player.getLocation());
	}

	private NavMeshPoint getNearestPoint()
	{
		NavMesh mesh = getNavMesh();
		if(mesh == null)
			return null;

		Vector3D playerLocation = new Vector3D(this.player.getLocation());

		double minDistanceSquared = Double.MAX_VALUE;
		NavMeshPoint closestPoint = null;

		for(NavMeshPoint point : mesh.getPoints())
		{
			double distanceSquared = point.getPositionVector().distanceToSquared(playerLocation);
			if(distanceSquared < minDistanceSquared)
			{
				closestPoint = point;
				minDistanceSquared = distanceSquared;
			}
		}

		if(minDistanceSquared > SELECTION_MAX_DISTANCE*SELECTION_MAX_DISTANCE)
			return null;

		return closestPoint;
	}

	private NavMeshTriangle getTriangle()
	{
		NavMesh mesh = getNavMesh();
		if(mesh == null)
			return null;

		Location location = this.player.getLocation();
		return mesh.getTriangleAt(location);
	}

	private NavMeshLadder getNearestNavMeshLadder(NavMeshTriangle triangle)
	{
		double minDistanceSquared = Double.MAX_VALUE;
		NavMeshLadder closestLadder = null;

		for(NavMeshTriangleTransition transition : triangle.neighbors.values())
			if(transition instanceof NavMeshLadder)
			{
				NavMeshLadder ladder = (NavMeshLadder) transition;
				Vector3D ladderPosition;

				if(ladder.getTriangleBottom() == triangle)
					ladderPosition = ladder.getPositionBottom();
				else
					ladderPosition = ladder.getPositionTop();

				double distanceSquared = ladderPosition.distanceToSquared(new Vector3D(this.player.getLocation()));
				if(distanceSquared < minDistanceSquared)
				{
					minDistanceSquared = distanceSquared;
					closestLadder = ladder;
				}
			}

		if(minDistanceSquared > SELECTION_MAX_DISTANCE*SELECTION_MAX_DISTANCE)
			return null;

		return closestLadder;
	}


	// -------
	// UPDATE
	// -------
	void update()
	{
		NavMesh mesh = getNavMesh();

		if(mesh != null)
			if(CompitumLib.getNavMeshManager().getEditManager().getUpdateCount()%3 == 0)
				spawnParticles(mesh);

		if(this.movingPoint != null)
		{
			Location location = this.player.getLocation();

			double pX = location.getX();
			double pY = location.getY();
			double pZ = location.getZ();
			if(this.snapPointsToBlockCenter)
			{
				pX = Math.floor(pX)+0.5;
				pY = Math.floor(pY);
				pZ = Math.floor(pZ)+0.5;
			}

			this.movingPoint.setLocation(new Vector3D(pX, pY, pZ));
		}

		sendNearbyNavMeshName(mesh);
	}

	private void sendNearbyNavMeshName(NavMesh mesh)
	{
		String meshName = ChatColor.RED+"No mesh in range";
		if(mesh != null)
			meshName = "Mesh: '"+mesh.getId()+"'";

		MessagingUtil.sendActionBarMessage(meshName, this.player);
	}


	// -------
	// VISUALIZATION
	// -------
	private void spawnParticles(NavMesh mesh)
	{
		Vector3D playerLocation = new Vector3D(this.player.getLocation());

		Set<NavMeshTriangle> triangles = new HashSet<>();
		for(NavMeshPoint point : mesh.getPoints())
			if(point.getPositionVector().distanceToSquared(playerLocation) < VISIBILITY_RANGE*VISIBILITY_RANGE)
			{
				spawnPointParticles(point);
				triangles.addAll(mesh.getTrianglesUsingPoint(point));
			}

		Set<InterchangableDuo<Vector3D, Vector3D>> triangleLines = new HashSet<>();
		Set<InterchangableDuo<Vector3D, Vector3D>> triangleConnections = new HashSet<>();
		Set<InterchangableDuo<Vector3D, Vector3D>> triangleHeuristicConnections = new HashSet<>();
		Set<InterchangableDuo<Vector3D, Vector3D>> ladderLines = new HashSet<>();

		for(NavMeshTriangle triangle : triangles)
		{
			triangleLines.add(new InterchangableDuo<>(triangle.point1.getPositionVector(), triangle.point2.getPositionVector()));
			triangleLines.add(new InterchangableDuo<>(triangle.point2.getPositionVector(), triangle.point3.getPositionVector()));
			triangleLines.add(new InterchangableDuo<>(triangle.point3.getPositionVector(), triangle.point1.getPositionVector()));

			for(Map.Entry<NavMeshTriangle, NavMeshTriangleTransition> entry : triangle.neighbors.entrySet())
			{
				NavMeshTriangle neighbor = entry.getKey();
				NavMeshTriangleTransition transition = entry.getValue();

				if(this.showTriangleConnections)
					triangleConnections.add(new InterchangableDuo<>(triangle.getCenter(), neighbor.getCenter()));
				if(this.showTriangleNavigationConnections)
					triangleHeuristicConnections
							.add(new InterchangableDuo<>(triangle.getHeuristicCenter(), neighbor.getHeuristicCenter()));

				if(transition instanceof NavMeshLadder)
				{
					NavMeshLadder ladder = (NavMeshLadder) transition;
					Vector3D cornerPoint = new Vector3D(ladder.getPositionBottom().x, ladder.getPositionTop().y,
							ladder.getPositionBottom().z);

					ladderLines.add(new InterchangableDuo<>(ladder.getPositionBottom(), cornerPoint));
					ladderLines.add(new InterchangableDuo<>(cornerPoint, ladder.getPositionTop()));
				}
			}
		}

		for(InterchangableDuo<Vector3D, Vector3D> line : triangleLines)
			spawnLineParticles(line.a, line.b, ParticleEffect.FLAME, LINE_PARTICLE_DISTANCE);

		for(InterchangableDuo<Vector3D, Vector3D> line : triangleConnections)
			spawnLineParticles(line.a, line.b, ParticleEffect.DRAGON_BREATH, LINE_PARTICLE_DISTANCE*1.3);

		for(InterchangableDuo<Vector3D, Vector3D> line : triangleHeuristicConnections)
			spawnLineParticles(line.a, line.b, ParticleEffect.VILLAGER_HAPPY, LINE_PARTICLE_DISTANCE*1.5);

		for(InterchangableDuo<Vector3D, Vector3D> line : ladderLines)
			spawnLineParticles(line.a, line.b, ParticleEffect.FIREWORKS_SPARK, LINE_PARTICLE_DISTANCE);
	}

	private void spawnPointParticles(NavMeshPoint point)
	{
		Location location = point.getPositionVector().toLocation(this.player.getWorld());

		ParticleEffect particleEffect = ParticleEffect.DAMAGE_INDICATOR;
		if(this.selectedPoints.contains(point))
		{
			particleEffect = ParticleEffect.HEART;
			location = location.add(0, 0.3, 0);
		}

		particleEffect.display(0, 0, 0, 0, 1, location, this.player);
	}

	private void spawnLineParticles(Vector3D start, Vector3D end, ParticleEffect effect, double distance)
	{
		Vector3D delta = end.subtract(start);
		for(double d = 0; d < delta.length(); d += distance)
		{
			Vector3D offset = delta.normalize().multiply(d);
			Vector3D vectorLocation = start.add(offset);
			Location location = vectorLocation.toLocation(this.player.getWorld()).add(0, 0.5, 0);

			effect.display(0, 0, 0, 0, 1, location, this.player);
		}
	}


	// -------
	// COMMAND
	// -------
	void executeCommand(String[] args)
	{
		if(args.length == 1)
		{
			if(args[0].equalsIgnoreCase("snap"))
			{
				this.snapPointsToBlockCenter = !this.snapPointsToBlockCenter;
				this.player.sendMessage("Snap to block center: "+this.snapPointsToBlockCenter);
				return;
			}
			if(args[0].equalsIgnoreCase("con"))
			{
				this.showTriangleConnections = !this.showTriangleConnections;
				this.player.sendMessage("Show triangle connections: "+this.showTriangleConnections);
				return;
			}
			if(args[0].equalsIgnoreCase("navCon"))
			{
				this.showTriangleNavigationConnections = !this.showTriangleNavigationConnections;
				this.player.sendMessage("Show triangle navigation connections: "+this.showTriangleNavigationConnections);
				return;
			}
		}

		String argsRecombined = "";
		for(String arg : args)
			argsRecombined += arg+" ";
		argsRecombined = argsRecombined.trim();

		this.player.sendMessage("The arguments '"+argsRecombined+"' are invalid.");
	}


	// -------
	// EDITING
	// -------
	NavMeshPoint createPoint()
	{
		NavMesh mesh = getNavMesh();
		if(mesh == null)
		{
			this.player.sendMessage("Creating point failed. No NavMesh is covering this area.");
			return null;
		}

		Location location = this.player.getLocation();

		double pX = location.getX();
		double pY = location.getY();
		double pZ = location.getZ();
		if(this.snapPointsToBlockCenter)
		{
			pX = Math.floor(pX)+0.5;
			pY = Math.floor(pY);
			pZ = Math.floor(pZ)+0.5;
		}

		NavMeshPoint point = mesh.createPoint(pX, pY, pZ);

		if(this.player.isSneaking())
			this.selectedPoints.add(point);

		return point;
	}

	void deletePoint()
	{
		NavMeshPoint point = getNearestPoint();
		if(point == null)
		{
			this.player.sendMessage("Deleting point failed. No point is nearby.");
			return;
		}

		NavMesh mesh = getNavMesh();
		mesh.removePoint(point);

		this.selectedPoints.remove(point);
	}

	void selectPoint()
	{
		NavMeshPoint point = getNearestPoint();
		if(point == null)
		{
			this.player.sendMessage("Selecting point failed. No point is nearby.");
			return;
		}

		if(this.selectedPoints.contains(point))
		{
			this.player.sendMessage("Selecting point failed. The point is already selected.");
			return;
		}

		this.selectedPoints.add(point);
	}

	void deselectPoint()
	{
		if(this.player.isSneaking())
		{
			this.selectedPoints.clear();
			return;
		}

		NavMeshPoint point = getNearestPoint();
		if(point == null)
		{
			this.player.sendMessage("Deselecting point failed. No point is nearby.");
			return;
		}

		if(!this.selectedPoints.contains(point))
		{
			this.player.sendMessage("Deselecting point failed. The point is not selected.");
			return;
		}

		this.selectedPoints.remove(point);
	}


	void createTriangle()
	{
		if(this.selectedPoints.size() > 3)
		{
			this.player.sendMessage("Creating triangle failed. Too many points selected ("+this.selectedPoints.size()+").");
			return;
		}

		if(this.selectedPoints.size() < 2)
		{
			this.player.sendMessage("Creating triangle failed. Not enough points selected ("+this.selectedPoints.size()+").");
			return;
		}

		NavMeshPoint thirdPoint;
		if(this.selectedPoints.size() == 2)
		{
			thirdPoint = createPoint();
			this.selectedPoints.add(thirdPoint);
		}
		else
			thirdPoint = this.selectedPoints.get(2);

		NavMesh mesh = getNavMesh();
		if(mesh == null)
		{
			this.player.sendMessage("Creating triangle failed. No NavMesh is covering this area.");
			return;
		}

		mesh.createTriangle(this.selectedPoints.get(0), this.selectedPoints.get(1), thirdPoint);
		this.selectedPoints.remove(0);
	}

	void deleteTriangle()
	{
		NavMeshTriangle triangle = getTriangle();
		if(triangle == null)
		{
			this.player.sendMessage("Deleting triangle failed. No triangle found at your position.");
			return;
		}

		NavMesh mesh = getNavMesh();
		mesh.deleteTriangle(triangle);

		if(this.ladderTriangle == triangle)
		{
			this.ladderTriangle = null;
			this.ladderPosition = null;
		}
	}


	void movePoint()
	{
		if(this.movingPoint != null)
		{
			this.movingPoint = null;
			return;
		}

		NavMeshPoint point = getNearestPoint();
		if(point == null)
		{
			this.player.sendMessage("Moving point failed. No point is nearby.");
			return;
		}

		this.movingPoint = point;
		// movement itself is then done periodically in update-method
	}


	void info()
	{
		NavMeshTriangle triangle = getTriangle();
		if(triangle == null)
		{
			this.player.sendMessage("Giving info failed. No triangle nearby.");
			return;
		}
		NavMesh mesh = getNavMesh();

		this.player.sendMessage("Triangle '"+triangle.id+"' in graph '"+mesh.getId()+"':");
	}


	void createLadder()
	{
		if(this.player.isSneaking())
		{
			// canceling ladder creation
			if(this.ladderTriangle != null)
			{
				this.ladderTriangle = null;
				this.ladderPosition = null;
				return;
			}

			// deleting triangle
			NavMeshTriangle triangle = getTriangle();
			if(triangle == null)
			{
				this.player.sendMessage("Deleting ladder failed. No triangle nearby.");
				return;
			}

			NavMeshLadder ladder = getNearestNavMeshLadder(triangle);
			if(ladder == null)
			{
				this.player.sendMessage("Deleting ladder failed. No ladder on this triangle.");
				return;
			}

			getNavMesh().removeLadder(ladder);
			return;
		}

		NavMeshTriangle triangle = getTriangle();
		if(triangle == null)
		{
			this.player.sendMessage("Creating ladder failed. The point has to be on a triangle.");
			return;
		}

		if(this.ladderTriangle == null) // first point
		{
			this.ladderTriangle = triangle;
			this.ladderPosition = new Vector3D(this.player.getLocation());

			return;
		}

		// second point
		Vector3D otherLadderPosition = new Vector3D(this.player.getLocation());
		getNavMesh().createLadder(this.ladderTriangle, this.ladderPosition, triangle, otherLadderPosition);
		this.ladderTriangle = null;
		this.ladderPosition = null;
	}

}
