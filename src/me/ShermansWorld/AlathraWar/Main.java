package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Town;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	
	public static WarData data;
	public static SiegeData data2;
	public static Main instance = null;
	public static Economy econ = null;
	
	@SuppressWarnings("unchecked")
	private static void initData() {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(getInstance(), new Runnable() {
            public void run() {
            	try {
        			Set<String> warsSet = data.getConfig().getConfigurationSection("Wars").getKeys(false);
        			Iterator<String> it = warsSet.iterator();
        			// converts set to arraylist
        			ArrayList<String> warsTempList = new ArrayList<String>();
        			while (it.hasNext()) {
        				warsTempList.add(it.next());
        			}
        			for (int i = 0; i < warsTempList.size(); i++) {
        				War war = new War(
        						warsTempList.get(i),
        						data.getConfig().getString("Wars." + warsTempList.get(i) + ".side1"),
        						data.getConfig().getString("Wars." + warsTempList.get(i) + ".side2")
        						);
        				ArrayList<String> side1Players = new ArrayList<String>();
        				ArrayList<String> side2Players = new ArrayList<String>();
        				side1Players = (ArrayList<String>) data.getConfig().getList("Wars." + war.getName() + ".side1players");
        				side2Players = (ArrayList<String>) data.getConfig().getList("Wars." + war.getName() + ".side2players");
        				war.setSide1Players(side1Players);
        				war.setSide2Players(side2Players);
        				WarCommands.wars.add(war);
        				for (String playerName : war.getSide1Players()) {
        					try {
        						Bukkit.getServer().getPlayer(playerName).setPlayerListName(Helper.color("&c[" + war.getSide1() + "]&r") + playerName);
        					} catch (NullPointerException e) {
        						
        					}
        				}
        				
        				for (String playerName : war.getSide2Players()) {
        					try {
        						Bukkit.getServer().getPlayer(playerName).setPlayerListName(Helper.color("&9[" + war.getSide2() + "]&r") + playerName);
        					} catch (NullPointerException e) {
        						
        					}
        				}
        			}
        		} catch (NullPointerException e) {
        			Bukkit.getLogger().info("NULL when initializing wars");
        		}
        		try {
        			Set<String> siegeSet = data2.getConfig().getConfigurationSection("Sieges").getKeys(false);
        			Iterator<String> it2 = siegeSet.iterator();
        			ArrayList<String> siegesTempList = new ArrayList<String>();
        			// converts set to arraylist
        			while (it2.hasNext()) {
        				siegesTempList.add(it2.next());
        			}
        			for (int i = 0; i < siegesTempList.size(); i++) {
        				War siegeWar = null;
        				Town siegeTown = null;
        				for (War war : WarCommands.wars) { // parse for the war
        					if (data2.getConfig().getString("Sieges." + siegesTempList.get(i) + ".war").equalsIgnoreCase(war.getName()) ) {
        						siegeWar = war;
        					}
        				}
        				List<Town> townList = TownyUniverse.getInstance().getDataSource().getTowns();
        				for (Town town : townList) {
        					if (town.getName().equalsIgnoreCase(data2.getConfig().getString("Sieges." + siegesTempList.get(i) + ".town"))) {
        						siegeTown = town;
        					}
        				}
        				
        				Siege siege = new Siege (
        						Integer.parseInt(siegesTempList.get(i)), siegeWar, siegeTown, 
        						data2.getConfig().getString("Sieges." + siegesTempList.get(i) + ".attackers"),
        						data2.getConfig().getString("Sieges." + siegesTempList.get(i) + ".defenders"),
        						data2.getConfig().getBoolean("Sieges." + siegesTempList.get(i) + ".siege1areattackers"),
        						data2.getConfig().getBoolean("Sieges." + siegesTempList.get(i) + ".siege2areattackers")
        						);
        				SiegeCommands.sieges.add(siege);
        				siege.start();
        			}
        		} catch (NullPointerException e) {
        			Bukkit.getLogger().info("NULL when initializing sieges");
        		}
            }
        }, 0L);
		
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
	    }
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	    if (rsp == null) {
	    	return false;
	    }
	    econ = rsp.getProvider();
	    return econ != null;
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	
	@Override
	public void onEnable() { //What runs when you start server
		instance = this;
		data = new WarData(this); // Initialize war data file
		data2 = new SiegeData(this);
		
		//initialize commands
		new WarCommands(this);
		new SiegeCommands(this);
		getServer().getPluginManager().registerEvents(new KillsListener(), this);
		getServer().getPluginManager().registerEvents(new JoinListener(), this);
		
		initData();
		setupEconomy();
	}
	
	@Override
	public void onDisable() {
		
	}
	
}
