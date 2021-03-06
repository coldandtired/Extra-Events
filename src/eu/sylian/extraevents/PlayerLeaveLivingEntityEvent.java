package eu.sylian.extraevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeaveLivingEntityEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final LivingEntity entity;
	private final Player player;
	
	public PlayerLeaveLivingEntityEvent(LivingEntity e, Player p)
	{
		entity = e;
		player = p;
	}
	
	public LivingEntity getEntity()
	{
		return entity;
	}
	
	public Player getPlayer()
	{
		return player;
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