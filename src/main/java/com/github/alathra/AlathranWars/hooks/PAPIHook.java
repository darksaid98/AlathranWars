package com.github.alathra.AlathranWars.hooks;

import com.github.alathra.AlathranWars.AlathranWars;
import com.github.alathra.AlathranWars.Reloadable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PAPIHook extends PlaceholderExpansion implements Reloadable {
    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        this.register();
    }

    @Override
    public void onDisable() {
        this.unregister();
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
        return "darksaid98";
    }

    @Override
    public @NotNull String getVersion() {
        return AlathranWars.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player p, @NotNull String params) {
        NameColorHandler colorHandler = NameColorHandler.getInstance();

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

        return "Alathran Wars Error";
    }
}
