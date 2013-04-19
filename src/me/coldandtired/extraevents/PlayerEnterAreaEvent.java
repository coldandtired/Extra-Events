package me.coldandtired.extraevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerEnterAreaEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final Player player;
	private final Object area;
	
	public PlayerEnterAreaEvent(Player p, Object area)
	{
		player = p;
		this.area = area;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public Object getArea()
	{
		return area;
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