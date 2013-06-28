package eu.sylian.extraevents;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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
	private Set<LivingEntity> previous_mobs_in_area = new HashSet<LivingEntity>();	
	
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
	
	public void fillMobs()
	{
		Set<LivingEntity> temp = new HashSet<LivingEntity>();
		for (LivingEntity le : Bukkit.getWorld(world_name).getEntitiesByClass(LivingEntity.class))
		{
			if (isInArea(le.getLocation())) temp.add(le);
		}
		
		for (LivingEntity le : temp)
		{
			if (previous_mobs_in_area.contains(le))
			{
				if (le instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerInAreaEvent((Player)le, this));
				else Bukkit.getPluginManager().callEvent(new LivingEntityInAreaEvent(le, this));
			}
			else
			{
				if (le instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerEnterAreaEvent((Player)le, this));
				else Bukkit.getPluginManager().callEvent(new LivingEntityEnterAreaEvent(le, this));
			}
		}
		
		previous_mobs_in_area.removeAll(temp);
		for (LivingEntity le : previous_mobs_in_area)
		{
			if (le instanceof Player) Bukkit.getPluginManager().callEvent(new PlayerLeaveAreaEvent((Player)le, this));
			else Bukkit.getPluginManager().callEvent(new LivingEntityLeaveAreaEvent(le, this));
		}
		
		previous_mobs_in_area = temp;
	}
	
	public void updateArea(int xf, int xt, int yf, int yt, int zf, int zt)
	{
		x_from = xf;
		x_to = xt;
		y_from = yf;
		y_to = yt;
		z_from = zf;
		z_to = zt;
	}
	
	public Set<LivingEntity> getMobsInArea()
	{
		return previous_mobs_in_area;
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
	
 	public boolean isInArea(Location loc)
	{
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		return x >= x_from && x <= x_to && y >= y_from && y <= y_to && z >= z_from && z <= z_to;
	}
}