package me.coldandtired.extraevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class LivingEntityBlockEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final LivingEntity entity;
	private final LivingEntity attacker;
	private final Projectile projectile;
	private DamageCause cause;
	
	public LivingEntityBlockEvent(LivingEntity e, LivingEntity a, Projectile p, DamageCause c)
	{
		entity = e;
		attacker = a;
		projectile = p;
		cause = c;
	}
	
	public DamageCause getCause()
	{
		return cause;
	}
	
	public void setCause(DamageCause cause)
	{
		this.cause = cause;
	}
	
	public LivingEntity getEntity()
	{
		return entity;
	}
	
	public LivingEntity getAttacker()
	{
		return attacker;
	}

	public Projectile getProjectile()
	{
		return projectile;
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