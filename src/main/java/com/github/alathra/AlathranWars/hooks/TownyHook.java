package com.github.alathra.AlathranWars.hooks;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.List;

public class TownyHook {
    private static boolean enabled = false;
    private static TownyAPI townyAPI;

    public static void init() {
        if (enabled) return;

        townyAPI = TownyAPI.getInstance();
        enabled = true;
    }

    public static List<Town> getTowns() {
        return townyAPI.getTowns();
    }

    public static List<Nation> getNations() {
        return townyAPI.getNations();
    }
}
