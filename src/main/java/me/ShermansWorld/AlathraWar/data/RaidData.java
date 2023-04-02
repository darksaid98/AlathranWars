package me.ShermansWorld.AlathraWar.data;

import me.ShermansWorld.AlathraWar.Main;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class RaidData {
    private Main plugin;
    private FileConfiguration dataConfig;
    private File configFile;

    public RaidData(final Main plugin) {
        this.dataConfig = null;
        this.configFile = null;
        this.plugin = plugin;
        this.saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "raids.yml");
        }
        this.dataConfig = (FileConfiguration) YamlConfiguration.loadConfiguration(this.configFile);
        final InputStream defaultStream = this.plugin.getResource("raids.yml");
        if (defaultStream != null) {
            final YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults((Configuration)defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null) {
            this.reloadConfig();
        }
        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null) {
            return;
        }
        try {
            this.getConfig().save(this.configFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "raids.yml");
        }
        if (!this.configFile.exists()) {
            this.plugin.saveResource("raids.yml", false);
        }
    }
}
