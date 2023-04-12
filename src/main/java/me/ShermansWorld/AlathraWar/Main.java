package me.ShermansWorld.AlathraWar;

import me.ShermansWorld.AlathraWar.commands.*;
import me.ShermansWorld.AlathraWar.listeners.CommandsListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.hooks.TABHook;
import me.ShermansWorld.AlathraWar.listeners.BlockBreakListener;
import me.ShermansWorld.AlathraWar.listeners.JoinListener;
import me.ShermansWorld.AlathraWar.listeners.KillsListener;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
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

        WarData.setWars(WarData.createWars());
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
		TABHook.init();
	}

	public void onEnable() {
		instance = this;
        initLogs();

		new WarData(this);
		new WarCommands(this);
		new SiegeCommands(this);
		new RaidCommands(this);
		new AdminCommands(this);
		getCommand("war").setTabCompleter(new WarTabCompletion());
		getCommand("siege").setTabCompleter(new SiegeTabCompletion());
		getCommand("raid").setTabCompleter(new RaidTabCompletion());
		getCommand("alathrawaradmin").setTabCompleter(new AdminTabCompletion());
		getServer().getPluginManager().registerEvents((Listener) new KillsListener(), (Plugin) this);
		getServer().getPluginManager().registerEvents((Listener) new CommandsListener(), (Plugin) this);
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
