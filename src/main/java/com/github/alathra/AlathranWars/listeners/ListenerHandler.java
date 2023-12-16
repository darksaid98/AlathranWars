package com.github.alathra.AlathranWars.listeners;

import com.github.alathra.AlathranWars.AlathranWars;
import com.github.alathra.AlathranWars.Reloadable;
import com.github.alathra.AlathranWars.listeners.items.PlayerInteractListener;
import com.github.alathra.AlathranWars.listeners.siege.*;
import com.github.alathra.AlathranWars.listeners.war.NationListener;
import com.github.alathra.AlathranWars.listeners.war.PlayerJoinListener;
import com.github.alathra.AlathranWars.listeners.war.TownListener;
import org.bukkit.Bukkit;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final AlathranWars instance;

    public ListenerHandler(AlathranWars instance) {
        this.instance = instance;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        // Sieges
        instance.getServer().getPluginManager().registerEvents(new com.github.alathra.AlathranWars.listeners.siege.PlayerJoinListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new BlockBreakPlaceListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), instance);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Graves"))
            instance.getServer().getPluginManager().registerEvents(new PlayerGraveListener(), instance);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("HeadsPlus"))
            instance.getServer().getPluginManager().registerEvents(new PlayerHeadsPlusListener(), instance);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("GSit"))
            instance.getServer().getPluginManager().registerEvents(new PlayerSitListener(), instance);

        // Battles
        instance.getServer().getPluginManager().registerEvents(new SiegeListener(), instance);

        // Wars
        instance.getServer().getPluginManager().registerEvents(new NationListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new TownListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new com.github.alathra.AlathranWars.listeners.war.PlayerQuitListener(), instance);

        instance.getServer().getPluginManager().registerEvents(new CommandsListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), instance);
    }

    @Override
    public void onDisable() {
    }
}