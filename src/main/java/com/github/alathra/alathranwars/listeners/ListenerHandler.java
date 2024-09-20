package com.github.alathra.alathranwars.listeners;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.Reloadable;
import com.github.alathra.alathranwars.listeners.siege.*;
import com.github.alathra.alathranwars.listeners.war.NationListener;
import com.github.alathra.alathranwars.listeners.war.PlayerJoinListener;
import com.github.alathra.alathranwars.listeners.war.PlayerQuitListener;
import com.github.alathra.alathranwars.listeners.war.TownListener;
import org.bukkit.Bukkit;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final AlathranWars plugin;

    /**
     * Instantiates a the Listener handler.
     *
     * @param plugin the plugin instance
     */
    public ListenerHandler(AlathranWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        // Sieges
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakPlaceListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), plugin);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Graves"))
            plugin.getServer().getPluginManager().registerEvents(new PlayerGraveListener(), plugin);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("HeadsPlus"))
            plugin.getServer().getPluginManager().registerEvents(new PlayerHeadsPlusListener(), plugin);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("GSit"))
            plugin.getServer().getPluginManager().registerEvents(new PlayerSitListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemUseListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageEntityListener(), plugin);

        // Battles
        plugin.getServer().getPluginManager().registerEvents(new SiegeListener(), plugin);

        // Wars
        plugin.getServer().getPluginManager().registerEvents(new NationListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new TownListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), plugin);

        plugin.getServer().getPluginManager().registerEvents(new CommandsListener(), plugin);

        // Misc
        plugin.getServer().getPluginManager().registerEvents(new UpdateCheckListener(), plugin);
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) // TODO Check if hook enabled
            plugin.getServer().getPluginManager().registerEvents(new VaultListener(), plugin);

    }

    @Override
    public void onDisable() {
    }
}