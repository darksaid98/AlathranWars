package com.github.alathra.alathranwars.hook;

import com.github.alathra.alathranwars.AlathranWars;
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
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull String getIdentifier() {
        return plugin.getPluginMeta().getName().replace(' ', '_').toLowerCase();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This needs to be true, or PlaceholderAPI will unregister the expansion during a plugin reload.
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player p, @NotNull String params) {
        return switch (params) {
            case "player_tablist" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerTabNameColor(p) + "%essentials_nickname_stripped%";
                yield "%essentials_nickname%";
            }
            case "player_nametag" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerNameColor(p) + "%essentials_nickname_stripped%";
                yield "%essentials_nickname%";
            }
            case "player_tablist_maquillage" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerTabNameColor(p) + "%essentials_nickname_stripped%";
                yield "%maquillage_namecolor_essentialsnick%";
            }
            case "player_nametag_maquillage" -> {
                if (colorHandler.isPlayerUsingModifiedName(p))
                    yield colorHandler.getPlayerNameColor(p) + "%essentials_nickname_stripped%";
                yield "%maquillage_namecolor_essentialsnick%";
            }
            default -> null;
        };
    }
}
