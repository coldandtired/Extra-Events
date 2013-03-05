package me.coldandtired.extra_events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class PlayerTargetedEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final Player player;
	private final LivingEntity le;
	private final TargetReason reason;
	
	public PlayerTargetedEvent(Player player, LivingEntity le, TargetReason reason)
	{
		this.player = player;
		this.le = le;
		this.reason = reason;
	}
	
	public LivingEntity getEntity()
	{
		return le;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public TargetReason getReason()
	{
		return reason;
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