package com.github.alathra.AlathranWars.hooks;

import com.github.alathra.AlathranWars.AlathranWars;
import com.github.alathra.AlathranWars.Reloadable;
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
