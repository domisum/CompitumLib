package de.domisum.compitumapi.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class MathUtil
{

	public static double round(double number, int digits)
	{
		int factor = 1;
		for(int i = 0; i < digits; i++)
			factor *= 10;

		return (double) Math.round(number * factor) / factor;
	}


	public static int toFP(double d)
	{
		return (int) Math.floor(d * 32) * 128;
	}

	public static byte toPB(float f)
	{
		return (byte) ((int) ((f * 256.0f) / 360.0f));
	}


	public static Location lookAt(Location base, Location target)
	{
		double dX = target.getX() - base.getX();
		double dY = target.getY() - base.getY();
		double dZ = target.getZ() - base.getZ();

		double dXZ = Math.sqrt((dX * dX) + (dZ * dZ));

		Location lookingLocation = base.clone();
		lookingLocation.setYaw((float) -Math.toDegrees(Math.atan2(dX, dZ)));
		lookingLocation.setPitch((float) -Math.toDegrees(Math.atan2(dY, dXZ)));

		return lookingLocation;
	}

	public static Location getCenter(Block block)
	{
		return block.getLocation().add(0.5, 0.5, 0.5);
	}

	public static Location getCenter(Location location)
	{
		return new Location(location.getWorld(), location.getBlockX() + .5, location.getBlockY() + .5, location.getBlockZ() + .5,
				location.getYaw(), location.getPitch());
	}

	public static Location getFloorCenter(Location location)
	{
		return getCenter(location).add(0, -.5, 0);
	}


	public static Location moveLocationTowardsYaw(Location location, double distance)
	{
		double dX = -Math.sin(Math.toRadians(location.getYaw())) * distance;
		double dZ = Math.cos(Math.toRadians(location.getYaw())) * distance;

		Location newLocation = location.clone().add(dX, 0, dZ);

		return newLocation;
	}

}
