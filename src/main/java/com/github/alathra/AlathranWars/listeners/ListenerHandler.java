package com.github.alathra.AlathranWars.listeners;

import com.github.alathra.AlathranWars.Main;
import com.github.alathra.AlathranWars.listeners.items.PlayerInteractListener;
import com.github.alathra.AlathranWars.listeners.siege.BlockBreakPlaceListener;
import com.github.alathra.AlathranWars.listeners.siege.PlayerDeathListener;
import com.github.alathra.AlathranWars.listeners.siege.PlayerQuitListener;
import com.github.alathra.AlathranWars.listeners.siege.PlayerSitListener;
import com.github.alathra.AlathranWars.listeners.war.NationListener;
import com.github.alathra.AlathranWars.listeners.war.PlayerJoinListener;
import com.github.alathra.AlathranWars.listeners.war.TownListener;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler {
    private final Main instance;

    public ListenerHandler(Main instance) {
        this.instance = instance;
    }

    public void onLoad() {

    }

    public void onEnable() {
        // Sieges
        instance.getServer().getPluginManager().registerEvents(new com.github.alathra.AlathranWars.listeners.siege.PlayerJoinListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new BlockBreakPlaceListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerSitListener(), instance);

        // Wars
        instance.getServer().getPluginManager().registerEvents(new NationListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new TownListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new com.github.alathra.AlathranWars.listeners.war.PlayerQuitListener(), instance);

        instance.getServer().getPluginManager().registerEvents(new CommandsListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), instance);
    }

    public void onDisable() {

    }
}