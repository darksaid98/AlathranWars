package com.github.alathra.AlathranWars.conflict;

import com.github.alathra.AlathranWars.conflict.battle.raid.Raid;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.db.DatabaseQueries;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class WarController {
    private static WarController instance;
    private @NotNull Set<War> wars = new HashSet<>();

    private WarController() {
        if (instance != null)
            Bukkit.getServer().getLogger().warning("Tried to re-initialize singleton");
    }

    @NotNull
    public static WarController getInstance() {
        if (instance == null)
            instance = new WarController();

        return instance;
    }

    public void loadAll() {
        wars = DatabaseQueries.loadAll();
    }

    @NotNull
    public Set<War> getWars() {
        return wars;
    }

    @Nullable
    public War getWar(String warName) {
        for (@NotNull War war : getWars())
            if (war.equals(warName)) return war;

        return null;
    }

    @Nullable
    public War getWar(UUID uuid) {
        for (@NotNull War war : getWars())
            if (war.equals(uuid)) return war;

        return null;
    }

    @Nullable
    public Side getSide(UUID uuid) {
        for (@NotNull War war : getWars())
            if (war.getSide(uuid) != null) return war.getSide(uuid);

        return null;
    }

    @Nullable
    public Siege getSiege(UUID uuid) {
        for (@NotNull War war : getWars())
            if (war.getSiege(uuid) != null) return war.getSiege(uuid);

        return null;
    }

    public void addWar(War war) {
        getWars().add(war);
    }

    public void removeWar(War war) {
        getWars().remove(war);
    }

    public boolean isPlayerInAnyWars(UUID uuid) {
        return getWars().stream()
            .anyMatch(war -> war.isPlayerInWar(uuid));
    }

    public boolean isPlayerInAnyWars(@NotNull Player p) {
        return isPlayerInAnyWars(p.getUniqueId());
    }

    public @NotNull Set<War> getPlayerWars(@NotNull Player p) {
        return getPlayerWars(p.getUniqueId());
    }

    public @NotNull Set<War> getPlayerWars(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.isPlayerInWar(uuid))
            .collect(Collectors.toSet());
    }

    public boolean isTownInAnyWars(Town town) {
        return getWars().stream()
            .anyMatch(war -> war.isTownInWar(town));
    }

    public boolean isTownInAnySieges(Town town) {
        return getWars().stream()
            .anyMatch(war -> war.isTownInWar(town) && war.isTownUnderSiege(town));
    }

    public boolean isTownInAnyRaids(Town town) {
        return getWars().stream()
            .anyMatch(war -> war.isTownInWar(town) && war.isTownUnderRaid(town));
    }

    public boolean isNationInAnySieges(Nation nation) {
        return nation.getTowns().stream()
            .anyMatch(this::isTownInAnySieges);
    }

    public boolean isNationInAnyRaids(Nation nation) {
        return nation.getTowns().stream()
            .anyMatch(this::isTownInAnyRaids);
    }

    public @NotNull Set<Siege> getTownSieges(Town town) {
        return getSieges().stream()
            .filter(siege -> siege.getAttackerSide().isTownOnSide(town) || siege.getDefenderSide().isTownOnSide(town))
            .collect(Collectors.toSet());
    }

    public @NotNull Set<Raid> getTownRaids(Town town) {
        return getRaids().stream()
            .filter(raid -> raid.getAttackerSide().isTownOnSide(town) || raid.getDefenderSide().isTownOnSide(town))
            .collect(Collectors.toSet());
    }

    public @NotNull Set<War> getTownWars(Town town) {
        return getWars().stream()
            .filter(war -> war.isTownInWar(town))
            .collect(Collectors.toSet());
    }

    public boolean isNationInAnyWars(Nation nation) {
        return getWars().stream()
            .anyMatch(war -> war.isNationInWar(nation));
    }

    public @NotNull Set<War> getNationWars(Nation nation) {
        return getWars().stream()
            .filter(war -> war.isNationInWar(nation))
            .collect(Collectors.toSet());
    }

    @NotNull
    public List<String> getWarNames() {
        return getWars().stream()
            .map(War::getName)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    @NotNull
    public List<String> getWarLabels() {
        return getWars().stream()
            .map(War::getLabel)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    @NotNull
    public Set<Siege> getSieges() {
        return getWars().stream()
            .map(War::getSieges)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @NotNull
    public Set<Raid> getRaids() {
        return getWars().stream()
            .map(War::getRaids)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    public boolean isPlayerInAnySiege(@NotNull Player p) {
        return isPlayerInAnySiege(p.getUniqueId());
    }

    public boolean isPlayerInAnySiege(UUID uuid) {
        return getSieges().stream()
            .anyMatch(siege -> siege.isPlayerInSiege(uuid));
    }

    public @NotNull Set<Siege> getPlayerSieges(@NotNull Player p) {
        return getPlayerSieges(p.getUniqueId());
    }

    public @NotNull Set<Siege> getPlayerSieges(UUID uuid) {
        return getSieges().stream()
            .filter(siege -> siege.isPlayerInSiege(uuid))
            .collect(Collectors.toSet());
    }

    public @NotNull Set<Nation> getNationsAtWar() {
        return getWars().stream()
            .map(War::getNations)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    public @NotNull Set<Town> getTownsAtWar() {
        return getWars().stream()
            .map(War::getTowns)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }
}
