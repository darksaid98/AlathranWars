// 
// Decompiled by Procyon v0.5.36
// 

package me.ShermansWorld.AlathraWar;

import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import com.palmergames.bukkit.towny.object.Town;

import com.palmergames.bukkit.towny.TownyUniverse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    public static WarData data;
    public static SiegeData data2;
    public static Main instance;
    public static Economy econ;
    public static AlathraWarLogger warLogger;
    
    static {
        Main.instance = null;
        Main.econ = null;
    }
    
    private static void initData() {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)getInstance(), (Runnable)new Runnable() {
            @SuppressWarnings("unchecked")
			@Override
            public void run() {
                try {
                    final Set<String> warsSet = (Set<String>)Main.data.getConfig().getConfigurationSection("Wars").getKeys(false);
                    final Iterator<String> it = warsSet.iterator();
                    final ArrayList<String> warsTempList = new ArrayList<String>();
                    while (it.hasNext()) {
                        warsTempList.add(it.next().toLowerCase());
                    }
                    for (int i = 0; i < warsTempList.size(); ++i) {
                        final War war = new War(warsTempList.get(i).toLowerCase(), Main.data.getConfig().getString("Wars." + warsTempList.get(i) + ".side1"), Main.data.getConfig().getString("Wars." + warsTempList.get(i) + ".side2"));
                        ArrayList<String> side1Players = new ArrayList<String>();
                        ArrayList<String> side2Players = new ArrayList<String>();
                        side1Players = (ArrayList<String>)Main.data.getConfig().getList("Wars." + war.getName() + ".side1players");
                        side2Players = (ArrayList<String>)Main.data.getConfig().getList("Wars." + war.getName() + ".side2players");
                        war.setSide1Players(side1Players);
                        war.setSide2Players(side2Players);
                        WarCommands.wars.add(war);
                        for (final String playerName : war.getSide1Players()) {
                            try {
                                Bukkit.getServer().getPlayer(playerName).setPlayerListName(String.valueOf(Helper.color(new StringBuilder("&c[").append(war.getSide1()).append("]&r").toString())) + playerName);
                            }
                            catch (NullPointerException ex) {}
                        }
                        for (final String playerName : war.getSide2Players()) {
                            try {
                                Bukkit.getServer().getPlayer(playerName).setPlayerListName(String.valueOf(Helper.color(new StringBuilder("&9[").append(war.getSide2()).append("]&r").toString())) + playerName);
                            }
                            catch (NullPointerException ex2) {}
                        }
                    }
                }
                catch (NullPointerException e) {
                    Bukkit.getLogger().info("NULL when initializing wars");
                }
                try {
                    final Set<String> siegeSet = (Set<String>)Main.data2.getConfig().getConfigurationSection("Sieges").getKeys(false);
                    final Iterator<String> it2 = siegeSet.iterator();
                    final ArrayList<String> siegesTempList = new ArrayList<String>();
                    while (it2.hasNext()) {
                        siegesTempList.add(it2.next());
                    }
                    for (int i = 0; i < siegesTempList.size(); ++i) {
                        War siegeWar = null;
                        Town siegeTown = null;
                        for (final War war2 : WarCommands.wars) {
                            if (Main.data2.getConfig().getString("Sieges." + siegesTempList.get(i) + ".war").equalsIgnoreCase(war2.getName())) {
                                siegeWar = war2;
                            }
                        }
                        final List<Town> townList = (List<Town>)TownyUniverse.getInstance().getDataSource().getTowns();
                        for (final Town town : townList) {
                            if (town.getName().equalsIgnoreCase(Main.data2.getConfig().getString("Sieges." + siegesTempList.get(i) + ".town"))) {
                                siegeTown = town;
                            }
                        }
                        final Siege siege = new Siege(Integer.parseInt(siegesTempList.get(i)), siegeWar, siegeTown, Main.data2.getConfig().getString("Sieges." + siegesTempList.get(i) + ".attackers"), Main.data2.getConfig().getString("Sieges." + siegesTempList.get(i) + ".defenders"), Main.data2.getConfig().getBoolean("Sieges." + siegesTempList.get(i) + ".siege1areattackers"), Main.data2.getConfig().getBoolean("Sieges." + siegesTempList.get(i) + ".siege2areattackers"));
                        SiegeCommands.sieges.add(siege);
                        siege.start();
                        siege.resetBeacon(Bukkit.getWorld(Main.data2.getConfig().getString("Sieges." + siegesTempList.get(i) + ".world")), Main.data2.getConfig().getInt("Sieges." + siegesTempList.get(i) + ".homeblockx"), Main.data2.getConfig().getInt("Sieges." + siegesTempList.get(i) + ".homeblocky"), Main.data2.getConfig().getInt("Sieges." + siegesTempList.get(i) + ".homeblockz"));
                    }
                }
                catch (NullPointerException e) {
                    Bukkit.getLogger().info("NULL when initializing sieges");
                }
            }
        }, 0L);
    }
    
	@SuppressWarnings("rawtypes")
	private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
		final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>)this.getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (rsp == null) {
            return false;
        }
        Main.econ = (Economy)rsp.getProvider();
        return Main.econ != null;
    }
    
    public static Main getInstance() {
        return Main.instance;
    }
    
    public static void initLogs() {
    	File logsFolder = new File("plugins" + File.separator + "AlathraWar" + File.separator + "logs");
	    if (!logsFolder.exists()) {
	    	logsFolder.mkdirs();
	    }
	    File log = new File("plugins" + File.separator + "AlathraWar" + File.separator + "logs" + File.separator + "log.txt");
	    if (!log.exists()) {
	    	try {
				log.createNewFile();
			} catch (IOException e) {
				Bukkit.getLogger().warning("[AlathraWar] Encountered error when creating log file!");
			}
	    }
	    warLogger = new AlathraWarLogger();
    }
    
    public void onEnable() {
        Main.instance = this;
        Main.data = new WarData(this);
        Main.data2 = new SiegeData(this);
        new WarCommands(this);
        new SiegeCommands(this);
        getCommand("war").setTabCompleter(new WarTabCompletion());
        getCommand("siege").setTabCompleter(new SiegeTabCompletion());
        getServer().getPluginManager().registerEvents((Listener)new KillsListener(), (Plugin)this);
        getServer().getPluginManager().registerEvents((Listener)new JoinListener(), (Plugin)this);
        getServer().getPluginManager().registerEvents((Listener)new BlockBreakListener(), (Plugin)this);
        initData();
        setupEconomy();
        initLogs();
    }
    
    public void onDisable() {
    }
}
