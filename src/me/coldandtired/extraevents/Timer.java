package me.coldandtired.extraevents;

import org.bukkit.Bukkit;

public class Timer
{
	private String name;
	private int interval;
	private int remaining;
	private boolean enabled = true;
	private String world;
	
	public Timer(String name, int interval, String world)
	{
		this.name = name;
		this.interval = interval;
		remaining = interval;
		this.world = world;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getInterval()
	{
		return interval;
	}
	
	public void setInterval(int interval)
	{
		this.interval = interval;
	}
	
	public String getWorld()
	{
		return world;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	public void tick()
	{
		if (!enabled) return;
		
		remaining--;
		if (remaining < 1)
		{
			remaining = interval;
			activate();
		}
	}
	
	public void activate()
	{
		Bukkit.getServer().getPluginManager().callEvent(new TimerActivateEvent(this));
	}

	public String check()
	{
		return "Timer " + name + " is " + (enabled ? "enabled, " : "disabled, ")
				+ interval + " second interval (" + remaining + " seconds left)";
	}
}