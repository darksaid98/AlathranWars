package com.github.alathra.AlathranWars.config;

import com.github.alathra.AlathranWars.AlathranWars;
import com.github.alathra.AlathranWars.Reloadable;
import com.github.milkdrinkers.Crate.Config;

import javax.inject.Singleton;

/**
 * A class that generates/loads & provides access to a configuration file.
 */
@Singleton
public class ConfigHandler implements Reloadable {
    private final AlathranWars alathranWars;
    private Config cfg;

    /**
     * Instantiates a new Config handler.
     *
     * @param alathranWars the plugin instance
     */
    public ConfigHandler(AlathranWars alathranWars) {
        this.alathranWars = alathranWars;
    }

    @Override
    public void onLoad() {
        cfg = new Config("config", alathranWars.getDataFolder().getPath(), alathranWars.getResource("config.yml")); // Create a config file from the template in our resources folder
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    /**
     * Gets alathranWars config object.
     *
     * @return the alathranWars config object
     */
    public Config getConfig() {
        return cfg;
    }
}
