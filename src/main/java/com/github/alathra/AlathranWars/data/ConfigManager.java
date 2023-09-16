package com.github.alathra.AlathranWars.data;

import com.github.alathra.AlathranWars.Main;
import com.github.milkdrinkers.Crate.Config;

public class ConfigManager {
    private final Main instance;
    private Config cfg;

    public ConfigManager(Main instance) {
        this.instance = instance;
    }

    public void onLoad() {
        cfg = new Config("config", instance.getDataFolder().getPath(), instance.getResource("config.yml"));
    }

    public void onEnable() {
    }

    public void onDisable() {

    }

    public Config getConfig() {
        return cfg;
    }
}
