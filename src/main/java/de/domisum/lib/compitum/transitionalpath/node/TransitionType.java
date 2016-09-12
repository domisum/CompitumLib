package de.domisum.lib.compitum.transitionalpath.node;

public class TransitionType
{

	public static int NONE = -1;
	public static int WALK = 1;
	/**
	 * This jump is a simple walking jump from a block below to another block above.
	 * For a parcour jump over a hole or something like that use LEAP.
	 */
	public static int JUMP = 2;
	public static int CLIMB = 3;
	public static int FALL = 4;
	public static int LEAP = 5;

}
