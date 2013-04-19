package me.coldandtired.extraevents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ExtraEvents extends JavaPlugin implements Listener
{
	private Map<String, Set<Entity>> approached_players;
	private Map<String, Set<Entity>> leaving_players;
	private Map<String, Set<ProtectedRegion>> players_in_regions = new HashMap<String, Set<ProtectedRegion>>();
	private Map<String, Set<Area>> players_in_areas = new HashMap<String, Set<Area>>();
	private PluginManager pm;
	private Map<String, Timer> timers;
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
	private Map<String, Area> areas;
	private boolean disabled_timer = false;	
	
	@Override
	public void onEnable()
	{
		loadConfig();	
		pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		
		Plugin p = pm.getPlugin("WorldGuard");
		if (p != null && p instanceof WorldGuardPlugin) wgp = (WorldGuardPlugin)p;
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() 
		{			 
			public void run() {timerTick();}
		}, 10, 10);
	}
	
 	private void loadConfig()
	{
		FileConfiguration config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		
		areas = new HashMap<String, Area>();
		approached_players = new HashMap<String, Set<Entity>>();
		leaving_players = new HashMap<String, Set<Entity>>();
		
		for (String world_name : config.getConfigurationSection("areas").getKeys(false))
		{
			for (String area_name : config.getConfigurationSection("areas." + world_name).getKeys(false))
			{
				String key = world_name + "." + area_name;
				Area area = new Area
						(
							world_name,
							area_name,
							config.getInt("areas." + key + ".x.from"),
							config.getInt("areas." + key + ".x.to"),
							config.getInt("areas." + key + ".y.from"),
							config.getInt("areas." + key + ".y.to"),
							config.getInt("areas." + key + ".z.from"),
							config.getInt("areas." + key + ".z.to")
						);
				areas.put(world_name + ":" + area_name, area);
			}
		}
		
		approach_x = config.getInt("approach.x");
		approach_y = config.getInt("approach.y");
		approach_z = config.getInt("approach.z");
		near_x = config.getInt("near.x");
		near_y = config.getInt("near.y");
		near_z = config.getInt("near.z");
		leave_x = config.getInt("leave.x");
		leave_y = config.getInt("leave.y");
		leave_z = config.getInt("leave.z");
		fillTimers();
	}
	
	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
		approached_players = null;
		leaving_players = null;
		pm = null;
	}
		
	public List<Area> getAreas()
	{
		List<Area> temp = new ArrayList<Area>(areas.values());
		if (wgp != null)
		{
			for (World w : Bukkit.getWorlds())
			{
				for (String s : wgp.getRegionManager(w).getRegions().keySet())
				{
					ProtectedRegion pr = wgp.getRegionManager(w).getRegion(s);
					BlockVector min = pr.getMinimumPoint();
					BlockVector max = pr.getMaximumPoint();
					temp.add(new Area(w.getName(), s,
							min.getBlockX(), max.getBlockX(),
							min.getBlockY(), max.getBlockY(),
							min.getBlockZ(), max.getBlockZ()));
				}
			}
		}
		
		return temp;
	}
	
	private void fillTimers()
	{
		timers = null;
		
		FileConfiguration config = getConfig();
		if (config.contains("timers"))
		{
			timers = new HashMap<String, Timer>();
			for (String s : config.getConfigurationSection("timers").getKeys(false))
			{
				String w = config.getString("timers." + s + ".world");
				if (w == null)
				{
					getLogger().warning("The timer called " + s + " is missing the world value!");
					continue;
				}
				
				if (Bukkit.getWorld(w) == null)
				{
					getLogger().warning("The timer called " + s + " has an unknown world!");
					continue;
				}
				
				int interval = config.getInt("timers." + s + ".interval_in_seconds", 300);
				timers.put(s, new Timer(s, interval, w));
			}
		}
	}
	
	public Area getArea(String name)
	{
		//if (areas == null) return null;
		Area a = areas.get(name);
		if (a != null) return a;
		if (wgp == null) return null;
		
		String[] names = name.split(":");
		ProtectedRegion pr = wgp.getRegionManager(Bukkit.getWorld(names[0])).getRegion(names[1]);
		if (pr == null) return null;

		BlockVector min = pr.getMinimumPoint();
		BlockVector max = pr.getMaximumPoint();
		return new Area(names[0], names[1],
				min.getBlockX(), max.getBlockX(),
				min.getBlockY(), max.getBlockY(),
				min.getBlockZ(), max.getBlockZ());
	}
	
	private void timerTick()
	{
		checkNearbyPlayers();
		if (wgp != null) checkRegions();
		checkAreas();
		checkTime();
		if (tick)
		{
			pm.callEvent(new SecondTickEvent());
			checkTimers();
		}
		tick = !tick;
	}
	
	private void checkAreas()
	{
		for (Player p : Bukkit.getOnlinePlayers())
		{
			Set<Area> temp = players_in_areas.containsKey(p.getName()) ? players_in_areas.get(p.getName()) : new HashSet<Area>();
			Set<Area> sl = new HashSet<Area>();
			Location loc = p.getLocation();
			for (Area a : areas.values())
			{
				if (!a.isIn_area(loc)) continue;
				if (!temp.contains(a)) pm.callEvent(new PlayerEnterAreaEvent(p, a));
				sl.add(a);
			}
			temp.removeAll(sl);
			for (Area a : temp) pm.callEvent(new PlayerLeaveAreaEvent(p, a));
			players_in_areas.put(p.getName(), sl);
			
			for (Area a : sl) pm.callEvent(new PlayerInAreaEvent(p, a));
		}
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
				if (!temp.contains(r)) pm.callEvent(new PlayerEnterAreaEvent(p, r));
				sl.add(r);
			}
			temp.removeAll(sl);
			for (ProtectedRegion r : temp) pm.callEvent(new PlayerLeaveAreaEvent(p, r));
			players_in_regions.put(p.getName(), sl);
			
			for (ProtectedRegion r : sl) pm.callEvent(new PlayerInAreaEvent(p, r));
		}
	}
	
 	private void checkNearbyPlayers()
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
 	
 	private void checkTimers()
 	{
 		if (timers == null || disabled_timer) return;
 	
 		for (Timer t : timers.values()) t.tick();
 	}
 	
 	@EventHandler (priority = EventPriority.HIGHEST)
	public void hit(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity le = (LivingEntity)event.getEntity();			
		
		LivingEntity attacker = null;
		Projectile p = null;
		if (event instanceof EntityDamageByEntityEvent)
		{
			Entity e = ((EntityDamageByEntityEvent)event).getDamager();
			if (e instanceof Projectile)
			{
				p = (Projectile)e;
				attacker = p.getShooter();
				pm.callEvent(new LivingEntityHitByProjectileEvent(le, attacker, p));
			}
			else if (e instanceof LivingEntity) attacker = (LivingEntity)e;
		}
		
		if (le.getNoDamageTicks() > 10)
		{
			// no damage
			//event.setCancelled(true);
			event.setDamage(0);
			pm.callEvent(new LivingEntityBlockEvent(le, attacker, p, event.getCause()));
		}	
		else
		{
			// damaged
			LivingEntityDamageEvent lede = new LivingEntityDamageEvent(le, attacker, p, event.getCause(), event.getDamage());
			pm.callEvent(lede);
			event.setDamage(lede.getDamage());
			event.setCancelled(lede.isCancelled());
		}
	}

 	@EventHandler
	public void targets(EntityTargetLivingEntityEvent event)
	{
 		if (!(event.getEntity() instanceof LivingEntity)) return;
 		
 		if (event.getTarget() instanceof Player) pm.callEvent(new PlayerTargetedEvent((Player)event.getTarget(), (LivingEntity)event.getEntity(), event.getReason()));
	}

 	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
 		if (cmd.getName().equalsIgnoreCase("reload_extraevents"))
		{			
			loadConfig();
			sender.sendMessage("Config reloaded!");
			return true;
		}
 		else if (cmd.getName().equalsIgnoreCase("timers"))
		{
			if (args.length == 0)
			{
				sender.sendMessage("Timers are " + (disabled_timer ? "paused" : "running"));
				return true;
			}
			if (timers == null)
			{
				sender.sendMessage("There are no timers set!");
				sender.sendMessage("Timers are " + (disabled_timer ? "paused" : "running"));
				return true;
			}
			
			if (args[0].equalsIgnoreCase("enable"))
			{
				if (args.length == 1)
				{
					for (Timer t : timers.values())
					{
						t.setEnabled(true);
					}
					sender.sendMessage("Enabled all timers!");
					return true;
				}
				else if (args.length == 2)
				{
					timers.get(args[1]).setEnabled(true);
					sender.sendMessage("Enabled timer " + args[1] + "!");
					return true;
				}
			}
			else if (args[0].equalsIgnoreCase("disable"))
			{
				if (args.length == 1)
				{
					for (Timer t : timers.values())
					{
						t.setEnabled(false);
					}
					sender.sendMessage("Disabled all timers!");
					return true;
				}
				else if (args.length == 2)
				{
					timers.get(args[1]).setEnabled(false);
					sender.sendMessage("Disabled timer " + args[1] + "!");
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("activate"))
			{
				if (args.length == 1)
				{					
					sender.sendMessage("Activating all timers!");
					for (Timer t : timers.values())
					{
						t.activate();
					}
					return true;
				}
				else if (args.length == 2)
				{
					sender.sendMessage("Activating timer " + args[1] + "!");
					timers.get(args[1]).activate();
					return true;
				}
			}
			else if (args[0].equalsIgnoreCase("unpause"))
			{
				if (args.length == 1)
				{
					disabled_timer = false;
					sender.sendMessage("Timers are now running!");
					return true;
				}
				return false;
			}
			else if (args[0].equalsIgnoreCase("pause"))
			{
				if (args.length == 1)
				{
					disabled_timer = true;
					sender.sendMessage("Timers are now paused!");
					return true;
				}
				return false;
			}
			else if (args[0].equalsIgnoreCase("check"))
			{
				if (args.length == 1)
				{
					for (Timer t : timers.values())
					{
						sender.sendMessage(t.check());
					}
					sender.sendMessage("Timers are " + (disabled_timer ? "paused" : "running"));
					return true;
				}
				else if (args.length == 2)
				{
					Timer t = timers.get(args[1]);
					
					sender.sendMessage(t.check());
					sender.sendMessage("Timers are " + (disabled_timer ? "paused" : "running"));
					return true;
				}
			}
			else if (args[0].equalsIgnoreCase("set_interval"))
			{
				if (args.length == 1) return false;
				int i = Integer.parseInt(args[1]);
				if (args.length == 2)
				{
					for (Timer t : timers.values())
					{
						t.setInterval(i);
					}
					sender.sendMessage("All timer intervals set to " + i + "!");
					return true;
				}
				else if (args.length == 3)
				{
					Timer t = timers.get(args[2]);
					t.setInterval(i);
					sender.sendMessage("Timer " + t.getName() + " interval set to " + i + "!");
					return true;
				}
			}
			return false;
		}
 		return false;
	}
}