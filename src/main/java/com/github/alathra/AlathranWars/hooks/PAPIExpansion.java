package com.github.alathra.AlathranWars.hooks;

import com.github.alathra.AlathranWars.AlathranWars;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPIExpansion extends PlaceholderExpansion {
    private final AlathranWars plugin;
    private final NameColorHandler colorHandler;

    public PAPIExpansion(AlathranWars plugin, NameColorHandler colorHandler) {
        this.plugin = plugin;
        this.colorHandler = colorHandler;
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return "AlathranWars";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "alathranwars";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player p, @NotNull String params) {
        if (params.equals("player_tablist")) {
            if (colorHandler.isPlayerUsingModifiedName(p))
                return colorHandler.getPlayerTabNameColor(p) + "%essentials_nickname_stripped%";
            return "%essentials_nickname%";
        }

        if (params.equals("player_nametag")) {
            if (colorHandler.isPlayerUsingModifiedName(p))
                return colorHandler.getPlayerNameColor(p) + "%essentials_nickname_stripped%";
            return "%essentials_nickname%";
        }

        if (params.equals("player_tablist_maquillage")) {
            if (colorHandler.isPlayerUsingModifiedName(p))
                return colorHandler.getPlayerTabNameColor(p) + "%essentials_nickname_stripped%";
            return "%maquillage_namecolor_essentialsnick%";
        }

        if (params.equals("player_nametag_maquillage")) {
            if (colorHandler.isPlayerUsingModifiedName(p))
                return colorHandler.getPlayerNameColor(p) + "%essentials_nickname_stripped%";
            return "%maquillage_namecolor_essentialsnick%";
        }

        return null;
    }
}
