package de.domisum.compitumapi.path;

import org.bukkit.Material;

@SuppressWarnings("deprecation")
public class MaterialEvaluator
{

	private static boolean[] canStandOn;
	private static boolean[] canStandIn;

	// -------
	// CONSTRUCTOR
	// -------
	public static void prepareEvaluation()
	{
		int maxId = -1;
		for(Material mat : Material.values())
			if(mat.getId() > maxId)
				maxId = mat.getId();

		canStandOn = new boolean[maxId+1]; // value shift
		canStandIn = new boolean[maxId+1]; // value shift

		for(Material mat : Material.values())
		{
			int id = mat.getId();

			// general values
			canStandOn[id] = mat.isSolid();
			canStandIn[id] = !mat.isSolid();

			// you cannot stand on fences -> walking over fences not possible
			if(mat.name().contains("FENCE"))
				canStandOn[id] = false;

			// standing in signs is possible
			if(mat == Material.SIGN_POST)
			{
				canStandIn[id] = true;
				canStandOn[id] = false;
			}
		}
	}


	// -------
	// EVALUATION
	// -------
	public static boolean canStandOn(int materialID)
	{
		return canStandOn[materialID];
	}

	public static boolean canStandIn(int materialID)
	{
		return canStandIn[materialID];
	}

}
