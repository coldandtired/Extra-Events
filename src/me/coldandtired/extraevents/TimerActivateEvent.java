package me.coldandtired.extraevents;


import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TimerActivateEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private Timer timer;
	
	public TimerActivateEvent(Timer timer)
	{
		this.timer = timer;
	}
	
	public Timer getTimer()
	{
		return timer;
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