package de.domisum.compitumapi.navmesh;

import de.domisum.auxiliumapi.util.FileUtil;
import de.domisum.auxiliumapi.util.java.GsonUtil;
import de.domisum.compitumapi.CompitumAPI;
import de.domisum.compitumapi.navmesh.edit.NavMeshEditManager;
import de.domisum.compitumapi.navmesh.json.SerializationNavMesh;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class NavMeshManager
{

	// CONSTANTS
	private static final String NAV_MESHES_DIRECTORY = "navMeshes";
	private static final String NAV_MESH_FILE_EXTENSION = ".navMesh.json";

	// REFERENCES
	private Set<NavMesh> meshes = new HashSet<>();

	private NavMeshEditManager editManager;


	// -------
	// CONSTRUCTOR
	// -------
	public NavMeshManager()
	{

	}

	public void initiialize()
	{
		loadMeshes();

		this.editManager = new NavMeshEditManager();
		this.editManager.initialize();
	}

	public void terminate()
	{
		this.editManager.terminate();

		saveMeshes();
	}


	// -------
	// LOADING
	// -------
	private void loadMeshes()
	{
		CompitumAPI.getLogger().info("Loading NavMeshs...");

		File baseDir = new File(NAV_MESHES_DIRECTORY);
		// noinspection ResultOfMethodCallIgnored
		baseDir.mkdirs();

		for(File file : FileUtil.listFilesRecursively(baseDir))
		{
			if(file.isDirectory())
				continue;

			if(!file.getName().endsWith(NAV_MESH_FILE_EXTENSION))
			{
				CompitumAPI.getLogger().warning(
						"The file '"+file.getAbsolutePath()+" in the NavMesh directory was skipped since it doesn't end with '"
								+NAV_MESH_FILE_EXTENSION+"'.");
				continue;
			}

			String navMeshId = FileUtil.getIdentifier(baseDir, file, NAV_MESH_FILE_EXTENSION);

			String content = FileUtil.readFileToString(file);
			NavMesh navMesh;
			try
			{
				SerializationNavMesh serializationNavMesh = GsonUtil.get().fromJson(content, SerializationNavMesh.class);
				navMesh = serializationNavMesh.convertToNavMesh(navMeshId);
				this.meshes.add(navMesh);

				CompitumAPI.getLogger()
						.info("Loaded NavMesh '"+navMesh.getId()+"' with "+navMesh.getTriangles().size()+" triangles");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		CompitumAPI.getLogger().info("Loading NavMeshes complete: loaded "+this.meshes.size()+" NavMesh(es)");
	}


	// -------
	// SAVING
	// -------
	public void additionalSave()
	{
		saveMeshes();

		for(Player p : Bukkit.getOnlinePlayers())
			p.sendMessage("Saved navMesh data to disk.");
	}

	private void saveMeshes()
	{
		CompitumAPI.getLogger().info("Saving NavMeshs...");

		File baseDir = new File(NAV_MESHES_DIRECTORY);
		// noinspection ResultOfMethodCallIgnored
		baseDir.mkdirs();

		for(NavMesh navMesh : this.meshes)
		{
			File file = new File(baseDir, navMesh.getId()+NAV_MESH_FILE_EXTENSION);

			try
			{
				SerializationNavMesh sNavMesh = new SerializationNavMesh(navMesh);
				String json = GsonUtil.getPretty().toJson(sNavMesh);

				FileUtil.writeStringToFile(file, json);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		CompitumAPI.getLogger().info("Saving NavMeshs complete: saved "+this.meshes.size()+" NavMesh(s)");
	}


	// -------
	// GETTERS
	// -------
	public NavMeshEditManager getEditManager()
	{
		return this.editManager;
	}

	public NavMesh getNavMeshAt(Location location)
	{
		for(NavMesh g : this.meshes)
			if(g.isInRange(location))
				return g;

		return null;
	}


}
