package me.coldandtired.extra_events;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Area
{
	private int x_from;
	private int x_to;
	private int y_from;
	private int y_to;
	private int z_from;
	private int z_to;
	private String name;
	private String world_name;
	
	public Area(String world_name, String name, int xf, int xt, int yf, int yt, int zf, int zt)
	{
		this.name = name;
		this.world_name = world_name;
		x_from = xf;
		x_to = xt;
		y_from = yf;
		y_to = yt;
		z_from = zf;
		z_to = zt;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getWorld_name()
	{
		return world_name;
	}
	
	public int getX_from()
	{
		return x_from;
	}
	
	public int getX_to()
	{
		return x_to;
	}
	
	public int getY_from()
	{
		return y_from;
	}
	
	public int getY_to()
	{
		return y_to;
	}
	
	public int getZ_from()
	{
		return z_from;
	}
	
	public int getZ_to()
	{
		return z_to;
	}
	
	public Location getRandom_location()
	{
		Random r = new Random();
		World w = Bukkit.getWorld(world_name);
		int x = r.nextInt((x_to - x_from) + 1) + x_from;
		
		int y = r.nextInt((y_to - y_from) + 1) + y_from;
		if (y > w.getMaxHeight()) y = w.getMaxHeight();
		
		int z = r.nextInt((z_to - z_from) + 1) + z_from; 
		return new Location(w, x, y, z);
	}
	
 	public boolean isIn_area(Location loc)
	{
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		return x >= x_from && x <= x_to && y >= y_from && y <= y_to && z >= z_from && z <= z_to;
	}
}