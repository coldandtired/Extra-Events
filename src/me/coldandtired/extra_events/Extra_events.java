package me.coldandtired.extra_events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Extra_events extends JavaPlugin implements Listener
{
	private FileConfiguration config;
	private Map<String, Set<Entity>> approached_players = new HashMap<String, Set<Entity>>();
	private Map<String, Set<Entity>> leaving_players = new HashMap<String, Set<Entity>>();
	private Map<String, Set<ProtectedRegion>> players_in_regions = new HashMap<String, Set<ProtectedRegion>>();
	private PluginManager pm;
	private int approach_x;
	private int approach_y;
	private int approach_z;
	private int leave_x;
	private int leave_y;
	private int leave_z;
	private int near_x;
	private int near_y;
	private int near_z;
	private boolean tick = true;
	private WorldGuardPlugin wgp = null;
	
	@Override
	public void onEnable()
	{
		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		approach_x = config.getInt("approach.x");
		approach_y = config.getInt("approach.y");
		approach_z = config.getInt("approach.z");
		near_x = config.getInt("near.x");
		near_y = config.getInt("near.y");
		near_z = config.getInt("near.z");
		leave_x = config.getInt("leave.x");
		leave_y = config.getInt("leave.y");
		leave_z = config.getInt("leave.z");
		pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		
		Plugin p = pm.getPlugin("WorldGuard");
		if (p != null && p instanceof WorldGuardPlugin) wgp = (WorldGuardPlugin)p;
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() 
		{			 
			public void run() {timerTick();}
		}, 10, 10);
	}
	
	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		approached_players = null;
		leaving_players = null;
		pm = null;
	}
	
	private void timerTick()
	{
		checkNearby_Players();
		if (wgp != null) checkRegions();
		checkTime();
		if (tick) pm.callEvent(new SecondTickEvent());
		tick = !tick;
	}
	
	private void checkRegions()
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			Set<ProtectedRegion> temp = players_in_regions.containsKey(p.getName()) ? players_in_regions.get(p.getName()) : new HashSet<ProtectedRegion>();
			Set<ProtectedRegion> sl = new HashSet<ProtectedRegion>();
			Location loc = p.getLocation();
			for (ProtectedRegion r : wgp.getRegionManager(p.getWorld()).getRegions().values())
			{
				if (!r.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) continue;
				if (!temp.contains(r)) pm.callEvent(new PlayerEnterWGRegionEvent(p, r));
				sl.add(r);
			}
			temp.removeAll(sl);
			for (ProtectedRegion r : temp) pm.callEvent(new PlayerLeaveWGRegionEvent(p, r));
			players_in_regions.put(p.getName(), sl);
			
			for (ProtectedRegion r : sl) pm.callEvent(new PlayerInWGRegionEvent(p, r));
		}
	}
	
 	private void checkNearby_Players()
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			Set<Entity> temp = approached_players.containsKey(p.getName()) ? approached_players.get(p.getName()) : new HashSet<Entity>();
			Set<Entity> sl = new HashSet<Entity>();
			for (Entity e : p.getNearbyEntities(approach_x, approach_y, approach_z))
			{
				if (e instanceof LivingEntity && e.isValid())
				{
					if (!temp.contains(e)) pm.callEvent(new PlayerApproachLivingEntityEvent((LivingEntity)e, p));
					sl.add(e);
				}
			}
			approached_players.put(p.getName(), sl);
			
			temp = leaving_players.containsKey(p.getName()) ? leaving_players.get(p.getName()) : new HashSet<Entity>();
			sl = new HashSet<Entity>();
			for (Entity e : p.getNearbyEntities(leave_x, leave_y, leave_z))
			{
				if (e instanceof LivingEntity && e.isValid()) sl.add(e);
			}
			temp.removeAll(sl);
			for (Entity e : temp) if (e.isValid()) pm.callEvent(new PlayerLeaveLivingEntityEvent((LivingEntity)e, p));
			leaving_players.put(p.getName(), sl);
			
			for (Entity e : p.getNearbyEntities(near_x, near_y, near_z))
				if (e instanceof LivingEntity && e.isValid()) pm.callEvent(new PlayerNearLivingEntityEvent((LivingEntity)e, p));
		}
	}
 	
 	private void checkTime()
	{
 		if (!tick) return;
 		long l = Bukkit.getWorlds().get(0).getTime();
 		long i = l % 1000;
 		if (i > 0 && i <= 19) pm.callEvent(new HourChangeEvent((int)(l / 1000)));
		if (l > 0 && l <= 19) pm.callEvent(new DawnEvent());
		else if (l > 6000 && l <= 6019) pm.callEvent(new MiddayEvent());
		else if (l > 12000 && l <= 12019) pm.callEvent(new DuskEvent());
		else if (l > 12500 && l <= 12519) pm.callEvent(new NightEvent());
		else if (l > 18000 && l <= 18019) pm.callEvent(new MidnightEvent());
	}
 	
 	@EventHandler (priority = EventPriority.LOWEST)
	public void hit(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity le = (LivingEntity)event.getEntity();
				
		LivingEntity attacker = null;
		if (event instanceof EntityDamageByEntityEvent)
		{
			Entity e = ((EntityDamageByEntityEvent)event).getDamager();
			if (e instanceof LivingEntity) attacker = (LivingEntity)e;
		}
		
		if (le.getNoDamageTicks() > 10)
		{
			// no damage
			event.setCancelled(true);
			pm.callEvent(new LivingEntityBlockEvent(le, attacker, event.getCause()));
		}	
		else
		{
			// damaged
			LivingEntityDamageEvent lede = new LivingEntityDamageEvent(le, attacker, event.getCause(), event.getDamage());
			pm.callEvent(lede);
			event.setDamage(lede.getDamage());
			event.setCancelled(lede.isCancelled());
		}
	}
}