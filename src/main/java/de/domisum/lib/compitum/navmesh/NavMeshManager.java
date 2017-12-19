package de.domisum.lib.compitum.navmesh;

import de.domisum.lib.auxilium.util.OldFileUtil;
import de.domisum.lib.auxilium.util.json.GsonUtil;
import de.domisum.lib.compitum.CompitumLib;
import de.domisum.lib.compitum.navmesh.json.SerializationNavMesh;
import org.bukkit.Location;

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


	/*
	// INITIALIZATION
	*/
	public void initiialize()
	{
		loadMeshes();
	}

	public void terminate()
	{
		// don't save the meshes by default
	}


	// LOADING
	private void loadMeshes()
	{
		CompitumLib.getLogger().info("Loading NavMeshs...");

		File baseDir = new File(NAV_MESHES_DIRECTORY);
		// noinspection ResultOfMethodCallIgnored
		baseDir.mkdirs();

		for(File file : OldFileUtil.listFilesRecursively(baseDir))
		{
			if(file.isDirectory())
				continue;

			if(!file.getName().endsWith(NAV_MESH_FILE_EXTENSION))
			{
				CompitumLib
						.getLogger()
						.warning("The file '"+file.getAbsolutePath()
								+" in the NavMesh directory was skipped since it doesn't end with '"+NAV_MESH_FILE_EXTENSION
								+"'.");
				continue;
			}

			String navMeshId = OldFileUtil.getIdentifier(baseDir, file, NAV_MESH_FILE_EXTENSION);

			String content = OldFileUtil.readFileToString(file);
			NavMesh navMesh;
			try
			{
				SerializationNavMesh serializationNavMesh = GsonUtil.get().fromJson(content, SerializationNavMesh.class);
				navMesh = serializationNavMesh.convertToNavMesh(navMeshId);
				this.meshes.add(navMesh);

				CompitumLib
						.getLogger()
						.info("Loaded NavMesh '"+navMesh.getId()+"' with "+navMesh.getTriangles().size()+" triangles");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		CompitumLib.getLogger().info("Loading NavMeshes complete: loaded "+this.meshes.size()+" NavMesh(es)");
	}


	// SAVING
	public void saveMeshes()
	{
		CompitumLib.getLogger().info("Saving NavMeshs...");

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

				OldFileUtil.writeStringToFile(file, json);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		CompitumLib.getLogger().info("Saving NavMeshs complete: saved "+this.meshes.size()+" NavMesh(s)");
	}


	// GETTERS
	public NavMesh getNavMeshAt(Location location)
	{
		for(NavMesh g : this.meshes)
			if(g.isInRange(location))
				return g;

		return null;
	}


}
