package com.github.alathra.alathranwars.hooks;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.Reloadable;
import org.bukkit.Bukkit;

public class PAPIHook implements Reloadable {
    private PAPIExpansion papiExpansion;

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            papiExpansion = new PAPIExpansion(AlathranWars.getInstance(), NameColorHandler.getInstance());
            papiExpansion.register();
        }
    }

    @Override
    public void onDisable() {
        if (papiExpansion != null && papiExpansion.isRegistered())
            papiExpansion.unregister();
    }
}
