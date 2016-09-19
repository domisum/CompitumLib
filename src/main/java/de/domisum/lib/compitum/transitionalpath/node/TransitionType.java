package de.domisum.lib.compitum.transitionalpath.node;

public class TransitionType
{

	public static final int WALK = 1;
	/**
	 * This JUMP is a simple walking jump from a block below to another block above.
	 * For a parcour jump over a hole LEAP is used.
	 */
	public static final int JUMP = 2;
	public static final int CLIMB = 3;
	public static final int FALL = 4;
	public static final int LEAP = 5;

}
