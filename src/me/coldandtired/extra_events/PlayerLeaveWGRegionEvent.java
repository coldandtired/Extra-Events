package me.coldandtired.extra_events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class PlayerLeaveWGRegionEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final Player player;
	private final ProtectedRegion region;
	
	public PlayerLeaveWGRegionEvent(Player p, ProtectedRegion r)
	{
		player = p;
		region = r;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public ProtectedRegion getRegion()
	{
		return region;
	}
	
	@Override
	public boolean isCancelled() 
	{
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) 
	{
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() 
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList() 
	{
	    return handlers;
	}
}