package com.github.alathra.AlathranWars.holder;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.raid.Raid;
import com.github.alathra.AlathranWars.utility.SQLQueries;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class WarManager {
    private static WarManager instance;
    private Set<War> wars = new HashSet<>();

    private WarManager() {
        if (instance != null)
            Bukkit.getServer().getLogger().warning("Tried to re-initialize singleton");
    }

    @NotNull
    public static WarManager getInstance() {
        if (instance == null)
            instance = new WarManager();

        return instance;
    }

    public void loadAll() {
        wars = SQLQueries.loadAll();
    }

    @NotNull
    public Set<War> getWars() {
        return wars;
    }

    @Nullable
    public War getWar(String warName) {
        for (War war : wars)
            if (war.equals(warName)) return war;

        return null;
    }

    @Nullable
    public War getWar(UUID uuid) {
        for (War war : wars)
            if (war.equals(uuid)) return war;

        return null;
    }

    @Nullable
    public Side getSide(UUID uuid) {
        for (War war : wars)
            if (war.getSide(uuid) != null) return war.getSide(uuid);

        return null;
    }

    @Nullable
    public Siege getSiege(UUID uuid) {
        for (War war : wars)
            if (war.getSiege(uuid) != null) return war.getSiege(uuid);

        return null;
    }

    public void addWar(War war) {
        wars.add(war);
    }

    public void removeWar(War war) {
        wars.remove(war);
    }

    public boolean isPlayerInAnyWars(UUID uuid) {
        for (War war : wars) {
            if (war.isPlayerInWar(uuid)) return true;
        }
        return false;
    }

    public boolean isPlayerInAnyWars(Player p) {
        return isPlayerInAnyWars(p.getUniqueId());
    }

    public boolean isTownInAnyWars(Town town) {
        for (War war : wars) {
            if (war.isTownInWar(town)) return true;
        }
        return false;
    }

    public boolean isNationInAnyWars(Nation nation) {
        for (War war : wars) {
            if (war.isNationInWar(nation)) return true;
        }
        return false;
    }

    @NotNull
    public List<String> getWarNames() {
        return wars.stream()
            .map(War::getName)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    @NotNull
    public List<String> getWarLabels() {
        return wars.stream()
            .map(War::getLabel)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    @NotNull
    public Set<Siege> getSieges() {
        final Set<Siege> sieges = new HashSet<>();

        wars.forEach((War war) -> sieges.addAll(war.getSieges()));

        return sieges;
    }

    @NotNull
    public Set<Raid> getRaids() {
        final Set<Raid> raids = new HashSet<>();

        wars.forEach((War war) -> raids.addAll(war.getRaids()));

        return raids;
    }

    public boolean isPlayerInAnySiege(Player p) {
        return isPlayerInAnySiege(p.getUniqueId());
    }

    public boolean isPlayerInAnySiege(UUID uuid) {
        for (Siege siege : getSieges()) {
            if (siege.isPlayerInSiege(uuid)) return true;
        }

        return false;
    }

    public Set<Siege> getPlayerSieges(Player p) {
        return getPlayerSieges(p.getUniqueId());
    }

    public Set<Siege> getPlayerSieges(UUID uuid) {
        if (!isPlayerInAnySiege(uuid))
            return Collections.emptySet();

        Set<Siege> sieges = new HashSet<>();

        for (Siege siege : getSieges()) {
            if (siege.isPlayerInSiege(uuid))
                sieges.add(siege);
        }

        return sieges;
    }
}
