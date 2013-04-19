package me.coldandtired.extraevents;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class LivingEntityDamageEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final LivingEntity entity;
	private final LivingEntity attacker;
	private final Projectile projectile;
	private DamageCause cause;
	private int damage;
	
	public LivingEntityDamageEvent(LivingEntity le, LivingEntity a, Projectile p, DamageCause c, int d)
	{
		entity = le;
		attacker = a;
		projectile = p;
		cause = c;
		damage = d;
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
	
	public DamageCause getCause()
	{
		return cause;
	}
	
	public int getDamage()
	{
		return damage;
	}
	
	public void setCause(DamageCause cause)
	{
		this.cause = cause;
	}
	
	public void setDamage(int damage)
	{
		this.damage = damage;
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