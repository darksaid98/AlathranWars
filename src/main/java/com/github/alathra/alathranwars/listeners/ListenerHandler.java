package com.github.alathra.alathranwars.listeners;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.Reloadable;
import com.github.alathra.alathranwars.listeners.items.PlayerInteractListener;
import com.github.alathra.alathranwars.listeners.siege.*;
import com.github.alathra.alathranwars.listeners.war.NationListener;
import com.github.alathra.alathranwars.listeners.war.PlayerJoinListener;
import com.github.alathra.alathranwars.listeners.war.TownListener;
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
        instance.getServer().getPluginManager().registerEvents(new com.github.alathra.alathranwars.listeners.siege.PlayerJoinListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new BlockBreakPlaceListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), instance);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Graves"))
            instance.getServer().getPluginManager().registerEvents(new PlayerGraveListener(), instance);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("HeadsPlus"))
            instance.getServer().getPluginManager().registerEvents(new PlayerHeadsPlusListener(), instance);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("GSit"))
            instance.getServer().getPluginManager().registerEvents(new PlayerSitListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new ItemUseListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerDamageEntityListener(), instance);

        // Battles
        instance.getServer().getPluginManager().registerEvents(new SiegeListener(), instance);

        // Wars
        instance.getServer().getPluginManager().registerEvents(new NationListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new TownListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new com.github.alathra.alathranwars.listeners.war.PlayerQuitListener(), instance);

        instance.getServer().getPluginManager().registerEvents(new CommandsListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), instance);
    }

    @Override
    public void onDisable() {
    }
}