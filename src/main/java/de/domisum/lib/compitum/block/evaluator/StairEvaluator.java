package de.domisum.lib.compitum.block.evaluator;

import de.domisum.lib.compitum.path.node.BlockPathNode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Arrays;

@SuppressWarnings("deprecation")
public class StairEvaluator
{

	// REFERENCES
	/**
	 * directions:
	 * 0: +x
	 * 1: +z
	 * 2: -x
	 * 3: -z
	 */
	private static boolean[][][] isStair; // <id, subId, direction>

	// STATUS
	private static boolean ready = false;


	// INIT
	public static void prepareEvaluation()
	{
		int maxId = -1;
		for(Material mat : Material.values())
			if(mat.getId() > maxId)
				maxId = mat.getId();

		int subIds = 16;
		isStair = new boolean[maxId+1][subIds][4];

		// general
		boolean[] none = new boolean[4];
		boolean[] all4 = new boolean[4];
		Arrays.fill(all4, true);

		boolean[][] noneForEverySubId = new boolean[subIds][4];
		Arrays.fill(noneForEverySubId, none);

		// slabs
		boolean[][] slabDefault = new boolean[subIds][4];
		for(int i = 0; i < 8; i++)
			slabDefault[i] = all4;
		for(int i = 8; i < subIds; i++)
			slabDefault[i] = none;

		// stairs
		boolean[] posX = new boolean[4];
		posX[0] = true;
		boolean[] posZ = new boolean[4];
		posZ[1] = true;
		boolean[] negX = new boolean[4];
		negX[2] = true;
		boolean[] negZ = new boolean[4];
		negZ[3] = true;

		boolean[][] stairDefault = new boolean[subIds][4];
		stairDefault[0] = posX;
		stairDefault[8] = posX;
		stairDefault[2] = posZ;
		stairDefault[2+8] = posZ;
		stairDefault[1] = negX;
		stairDefault[1+8] = negX;
		stairDefault[3] = negZ;
		stairDefault[3+8] = negZ;
		for(int i = 4; i < 8; i++)
		{
			stairDefault[i] = none;
			stairDefault[i+8] = none;
		}

		// looping over all materials
		for(Material mat : Material.values())
		{
			if(mat == Material.STEP || mat == Material.STONE_SLAB2 || mat == Material.WOOD_STEP || mat == Material.PURPUR_SLAB)
				isStair[mat.getId()] = slabDefault;
			else if(mat.name().endsWith("STAIRS"))
				isStair[mat.getId()] = stairDefault;
			else
				isStair[mat.getId()] = noneForEverySubId;
		}

		ready = true;
	}


	// EVALUATION
	public static boolean isStair(BlockPathNode from, BlockPathNode to, World world)
	{
		if(!ready)
			throw new IllegalStateException("CompitumLib has to be enabled before usage!");

		Location stairLocation = to.getLocation(world).add(0, -1, 0);
		Block stairBlock = stairLocation.getBlock();
		int stairBlockTypeId = stairBlock.getType().getId();
		int stairBlockSubId = stairBlock.getData();

		boolean[] directions = isStair[stairBlockTypeId][stairBlockSubId];
		int dX = to.x-from.x;
		int dZ = to.z-from.z;

		if(dX == 1 && directions[0])
			return true;
		if(dZ == 1 && directions[1])
			return true;
		if(dX == -1 && directions[2])
			return true;
		if(dZ == -1 && directions[3])
			return true;

		return false;
	}

}
