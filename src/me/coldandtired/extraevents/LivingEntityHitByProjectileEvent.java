package me.coldandtired.extraevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LivingEntityHitByProjectileEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final LivingEntity entity;
	private final LivingEntity attacker;
	private final Projectile projectile;
	
	public LivingEntityHitByProjectileEvent(LivingEntity e, LivingEntity a, Projectile p)
	{
		entity = e;
		attacker = a;
		projectile = p;
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