package me.ShermansWorld.AlathraWar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SiegeData {
	
	private Main plugin;
	private FileConfiguration dataConfig = null;
	private File configFile = null;

	public SiegeData(Main plugin) {
		this.plugin = plugin;
		saveDefaultConfig();
	}

	public void reloadConfig() {
		if (configFile == null) {
			this.configFile = new File(plugin.getDataFolder(), "sieges.yml");
		}
		dataConfig = YamlConfiguration.loadConfiguration(configFile);

		InputStream defaultStream = plugin.getResource("sieges.yml");

		if (defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
			dataConfig.setDefaults(defaultConfig);
		}

	}
	
	public FileConfiguration getConfig() {
		if (dataConfig == null) {
			reloadConfig();
		}
		return dataConfig;
	}
	
	public void saveConfig() {
		if (dataConfig == null || configFile == null) {
			return;
		}
		
		try {
			getConfig().save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveDefaultConfig() {
		if (configFile == null) {
			configFile = new File(plugin.getDataFolder(), "sieges.yml");
		}
		
		if (!configFile.exists()) {
			plugin.saveResource("sieges.yml", false);
		}
	}

}

