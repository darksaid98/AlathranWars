package me.ShermansWorld.AlathraWar;

import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import com.palmergames.bukkit.towny.object.Town;

import me.ShermansWorld.AlathraWar.commands.SiegeCommands;
import me.ShermansWorld.AlathraWar.commands.SiegeTabCompletion;
import me.ShermansWorld.AlathraWar.commands.WarCommands;
import me.ShermansWorld.AlathraWar.commands.WarTabCompletion;
import me.ShermansWorld.AlathraWar.data.PrefixData;
import me.ShermansWorld.AlathraWar.data.RolesData;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.hooks.LuckPermsHook;
import me.ShermansWorld.AlathraWar.hooks.TABHook;
import me.ShermansWorld.AlathraWar.listeners.BlockBreakListener;
import me.ShermansWorld.AlathraWar.listeners.JoinListener;
import me.ShermansWorld.AlathraWar.listeners.KillsListener;
import me.ShermansWorld.AlathraWar.roles.AssassinCommand;
import me.ShermansWorld.AlathraWar.roles.MercCommand;

import com.palmergames.bukkit.towny.TownyAPI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	public static WarData warData;
	public static SiegeData siegeData;
	public static PrefixData prefixData;
	public static RolesData rolesData;
	public static Main instance;
	public static Economy econ;
	public static AlathraWarLogger warLogger;

	static {
		instance = null;
		econ = null;
	}

	private static void initData() {
		File userDataFolder = new File("plugins" + File.separator + "AlathraWar" + File.separator + "userdata");
		if (!userDataFolder.exists()) {
			userDataFolder.mkdirs();
		}
		rolesData = new RolesData();
		try {
			Set<String> warsSet = (Set<String>) warData.getConfig().getConfigurationSection("Wars").getKeys(false);
			Iterator<String> it = warsSet.iterator();
			ArrayList<String> warsTempList = new ArrayList<String>();
			while (it.hasNext()) {
				warsTempList.add(it.next().toLowerCase());
			}
			for (int i = 0; i < warsTempList.size(); ++i) {
				final War war = new War(warsTempList.get(i).toLowerCase(),
						warData.getConfig().getString("Wars." + warsTempList.get(i) + ".side1"),
						warData.getConfig().getString("Wars." + warsTempList.get(i) + ".side2"));
				ArrayList<String> side1Players = new ArrayList<String>();
				ArrayList<String> side2Players = new ArrayList<String>();
				side1Players = (ArrayList<String>) warData.getConfig()
						.getList("Wars." + war.getName() + ".side1players");
				side2Players = (ArrayList<String>) warData.getConfig()
						.getList("Wars." + war.getName() + ".side2players");
				war.setSide1Players(side1Players);
				war.setSide2Players(side2Players);
				WarCommands.wars.add(war);
				for (final String playerName : war.getSide1Players()) {
					try {
						Bukkit.getServer().getPlayer(playerName)
								.setPlayerListName(String.valueOf(Helper.color(
										new StringBuilder("&c[").append(war.getSide1()).append("]&r").toString()))
										+ playerName);
					} catch (NullPointerException ex) {
					}
				}
				for (final String playerName : war.getSide2Players()) {
					try {
						Bukkit.getServer().getPlayer(playerName)
								.setPlayerListName(String.valueOf(Helper.color(
										new StringBuilder("&9[").append(war.getSide2()).append("]&r").toString()))
										+ playerName);
					} catch (NullPointerException ex2) {
					}
				}
			}
		} catch (NullPointerException e) {
			Bukkit.getLogger().info("NULL when initializing wars");
		}
		try {
			Set<String> siegeSet = (Set<String>) siegeData.getConfig().getConfigurationSection("Sieges").getKeys(false);
			Iterator<String> it2 = siegeSet.iterator();
			ArrayList<String> siegesTempList = new ArrayList<String>();
			while (it2.hasNext()) {
				siegesTempList.add(it2.next());
			}
			for (int i = 0; i < siegesTempList.size(); ++i) {
				War siegeWar = null;
				Town siegeTown = null;
				for (final War war2 : WarCommands.wars) {
					if (siegeData.getConfig().getString("Sieges." + siegesTempList.get(i) + ".war")
							.equalsIgnoreCase(war2.getName())) {
						siegeWar = war2;
					}
				}
				List<Town> townList = TownyAPI.getInstance().getTowns();
				for (final Town town : townList) {
					if (town.getName().equalsIgnoreCase(
							siegeData.getConfig().getString("Sieges." + siegesTempList.get(i) + ".town"))) {
						siegeTown = town;
					}
				}
				Siege siege = new Siege(Integer.parseInt(siegesTempList.get(i)), siegeWar, siegeTown,
						siegeData.getConfig().getString("Sieges." + siegesTempList.get(i) + ".attackers"),
						siegeData.getConfig().getString("Sieges." + siegesTempList.get(i) + ".defenders"),
						siegeData.getConfig().getBoolean("Sieges." + siegesTempList.get(i) + ".siege1areattackers"),
						siegeData.getConfig().getBoolean("Sieges." + siegesTempList.get(i) + ".siege2areattackers"));
				SiegeCommands.sieges.add(siege);
				siege.start();
				siege.resetBeacon(
						Bukkit.getWorld(siegeData.getConfig().getString("Sieges." + siegesTempList.get(i) + ".world")),
						siegeData.getConfig().getInt("Sieges." + siegesTempList.get(i) + ".homeblockx"),
						siegeData.getConfig().getInt("Sieges." + siegesTempList.get(i) + ".homeblocky"),
						siegeData.getConfig().getInt("Sieges." + siegesTempList.get(i) + ".homeblockz"));
			}
		} catch (NullPointerException e) {
			Bukkit.getLogger().info("NULL when initializing sieges");
		}
		try {
			Set<String> prefixSet = (Set<String>) prefixData.getConfig().getConfigurationSection("Prefixes")
					.getKeys(false);
			Iterator<String> it3 = prefixSet.iterator();
			ArrayList<String> prefixesTempList = new ArrayList<String>();
			while (it3.hasNext()) {
				prefixesTempList.add(it3.next());
			}
			for (int i = 0; i < prefixesTempList.size(); ++i) {
				LuckPermsHook.prefixMap.put(prefixesTempList.get(i),
						prefixData.getConfig().getString("Prefixes." + prefixesTempList.get(i)));
			}
		} catch (NullPointerException e) {
			Bukkit.getLogger().info("NULL when initializing prefixes");
		}
	}

	@SuppressWarnings("rawtypes")
	private boolean setupEconomy() {
		if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>) this.getServer()
				.getServicesManager().getRegistration((Class) Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = (Economy) rsp.getProvider();
		return econ != null;
	}

	public static Main getInstance() {
		return instance;
	}

	public static void initLogs() {
		File logsFolder = new File("plugins" + File.separator + "AlathraWar" + File.separator + "logs");
		if (!logsFolder.exists()) {
			logsFolder.mkdirs();
		}
		File log = new File(
				"plugins" + File.separator + "AlathraWar" + File.separator + "logs" + File.separator + "log.txt");
		if (!log.exists()) {
			try {
				log.createNewFile();
			} catch (IOException e) {
				Bukkit.getLogger().warning("[AlathraWar] Encountered error when creating log file!");
			}
		}
		warLogger = new AlathraWarLogger();
	}

	private static void initAPIs() {
		LuckPermsHook.init();
		TABHook.init();
	}

	public void onEnable() {
		instance = this;
		warData = new WarData(this);
		siegeData = new SiegeData(this);
		prefixData = new PrefixData(this);
		new WarCommands(this);
		new SiegeCommands(this);
		new MercCommand(this);
		new AssassinCommand(this);
		getCommand("war").setTabCompleter(new WarTabCompletion());
		getCommand("siege").setTabCompleter(new SiegeTabCompletion());
		getServer().getPluginManager().registerEvents((Listener) new KillsListener(), (Plugin) this);
		getServer().getPluginManager().registerEvents((Listener) new JoinListener(), (Plugin) this);
		getServer().getPluginManager().registerEvents((Listener) new BlockBreakListener(), (Plugin) this);
		initData();
		initAPIs();
		setupEconomy();
		initLogs();
	}

	public void onDisable() {
	}
}
