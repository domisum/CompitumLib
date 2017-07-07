package de.domisum.lib.compitum.evaluator;

import org.bukkit.Material;

@SuppressWarnings("deprecation")
public class MaterialEvaluator
{

	// REFERENCES
	private static boolean[] canStandOn;
	private static boolean[] canStandIn;

	// STATUS
	private static boolean ready = false;


	// INIT
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

		ready = true;
	}


	// EVALUATION
	public static boolean canStandOn(int materialID)
	{
		if(!ready)
			throw new IllegalStateException("CompitumLib has to be anabled before usage!");

		return canStandOn[materialID];
	}

	public static boolean canStandIn(int materialID)
	{
		if(!ready)
			throw new IllegalStateException("CompitumLib has to be anabled before usage!");

		return canStandIn[materialID];
	}

}
