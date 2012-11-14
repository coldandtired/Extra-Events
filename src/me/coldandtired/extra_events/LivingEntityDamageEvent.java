package me.coldandtired.extra_events;

import org.bukkit.entity.LivingEntity;
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
	private DamageCause cause;
	private int damage;
	
	public LivingEntityDamageEvent(LivingEntity le, LivingEntity a, DamageCause c, int d)
	{
		entity = le;
		attacker = a;
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