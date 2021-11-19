// 
// Decompiled by Procyon v0.5.36
// 

package me.ShermansWorld.AlathraWar;

import java.io.IOException;
import java.io.InputStream;
import org.bukkit.configuration.Configuration;
import java.io.Reader;
import java.io.InputStreamReader;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;

public class WarData
{
    private Main plugin;
    private FileConfiguration dataConfig;
    private File configFile;
    
    public WarData(final Main plugin) {
        this.dataConfig = null;
        this.configFile = null;
        this.plugin = plugin;
        this.saveDefaultConfig();
    }
    
    public void reloadConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "wars.yml");
        }
        this.dataConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.configFile);
        final InputStream defaultStream = this.plugin.getResource("wars.yml");
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
            this.configFile = new File(this.plugin.getDataFolder(), "wars.yml");
        }
        if (!this.configFile.exists()) {
            this.plugin.saveResource("wars.yml", false);
        }
    }
}
