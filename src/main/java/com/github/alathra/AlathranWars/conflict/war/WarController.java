package com.github.alathra.AlathranWars.conflict.war;

import com.github.alathra.AlathranWars.conflict.war.side.Side;
import com.github.alathra.AlathranWars.conflict.battle.raid.Raid;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.db.DatabaseQueries;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The singleton War controller.
 */
@Singleton
public class WarController {
    private static WarController instance;
    private @NotNull Set<War> wars = new HashSet<>();

    private WarController() {
        if (instance != null)
            Bukkit.getServer().getLogger().warning("Tried to re-initialize singleton");
    }

    /**
     * Gets or creates an instance of the war controller.
     *
     * @return the instance
     */
    @NotNull
    public static WarController getInstance() {
        if (instance == null)
            instance = new WarController();

        return instance;
    }

    /**
     * Load all wars into memory from database.
     */
    @ApiStatus.Internal
    public void loadAll() {
        wars = DatabaseQueries.loadAll();
    }

    /**
     * Gets all wars.
     *
     * @return the wars
     */
    @NotNull
    public Set<War> getWars() {
        return wars;
    }

    /**
     * Gets war by its name or returns null.
     *
     * @param warName the war name
     * @return the war
     */
    @Nullable
    public War getWar(String warName) {
        return getWars().stream()
            .filter(war -> war.equals(warName))
            .findAny()
            .orElse(null);
    }

    /**
     * Gets war by its UUID or returns null.
     *
     * @param uuid the uuid
     * @return the war
     */
    @Nullable
    public War getWar(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.equals(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Gets side by the side UUID.
     *
     * @param uuid the uuid
     * @return the side
     */
    @Nullable
    public Side getSide(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.getSide(uuid) != null)
            .map(war -> war.getSide(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Gets siege by the siege UUID.
     *
     * @param uuid the uuid
     * @return the siege
     */
    @Nullable
    public Siege getSiege(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.getSiege(uuid) != null)
            .map(war -> war.getSiege(uuid))
            .findAny()
            .orElse(null);
    }

    /**
     * Add a war to the war controller.
     *
     * @param war the war
     */
    @ApiStatus.Internal
    public void addWar(War war) {
        getWars().add(war);
    }

    /**
     * Remove a war from the war controller.
     *
     * @param war the war
     */
    @ApiStatus.Internal
    public void removeWar(War war) {
        getWars().remove(war);
    }

    /**
     * Is player in any wars. Includes if surrendered.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean isPlayerInAnyWars(UUID uuid) {
        return getWars().stream()
            .anyMatch(war -> war.isPlayerInWar(uuid));
    }

    /**
     * Is player in any wars. Includes if surrendered.
     *
     * @param p the p
     * @return the boolean
     */
    public boolean isPlayerInAnyWars(Player p) {
        return isPlayerInAnyWars(p.getUniqueId());
    }

    /**
     * Gets list of player wars. Includes if surrendered.
     *
     * @param p the p
     * @return the player wars
     */
    public @NotNull Set<War> getPlayerWars(Player p) {
        return getPlayerWars(p.getUniqueId());
    }

    /**
     * Gets list of player wars. Includes if surrendered.
     *
     * @param uuid the uuid
     * @return the player wars
     */
    public @NotNull Set<War> getPlayerWars(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.isPlayerInWar(uuid))
            .collect(Collectors.toSet());
    }

    /**
     * Is town in any wars. Includes surrendered towns.
     *
     * @param town the town
     * @return the boolean
     */
    public boolean isTownInAnyWars(Town town) {
        return getWars().stream()
            .anyMatch(war -> war.isTownInWar(town));
    }

    /**
     * Is town in any sieges. Includes surrendered towns.
     *
     * @param town the town
     * @return the boolean
     */
    public boolean isTownInAnySieges(Town town) {
        return getWars().stream()
            .anyMatch(war -> war.isTownInWar(town) && war.isTownUnderSiege(town));
    }

    /**
     * Is town in any raids. Includes surrendered towns.
     *
     * @param town the town
     * @return the boolean
     */
    public boolean isTownInAnyRaids(Town town) {
        return getWars().stream()
            .anyMatch(war -> war.isTownInWar(town) && war.isTownUnderRaid(town));
    }

    /**
     * Is nation in any sieges. Includes surrendered nations.
     *
     * @param nation the nation
     * @return the boolean
     */
    public boolean isNationInAnySieges(Nation nation) {
        return nation.getTowns().stream()
            .anyMatch(this::isTownInAnySieges);
    }

    /**
     * Is nation in any raids. Includes surrendered nations.
     *
     * @param nation the nation
     * @return the boolean
     */
    public boolean isNationInAnyRaids(Nation nation) {
        return nation.getTowns().stream()
            .anyMatch(this::isTownInAnyRaids);
    }

    /**
     * Gets town sieges. Includes surrendered towns.
     *
     * @param town the town
     * @return the town sieges
     */
    public @NotNull Set<Siege> getTownSieges(Town town) {
        return getSieges().stream()
            .filter(siege -> siege.getAttackerSide().isTownOnSide(town) || siege.getDefenderSide().isTownOnSide(town))
            .collect(Collectors.toSet());
    }

    /**
     * Gets town raids. Includes surrendered towns.
     *
     * @param town the town
     * @return the town raids
     */
    public @NotNull Set<Raid> getTownRaids(Town town) {
        return getRaids().stream()
            .filter(raid -> raid.getAttackerSide().isTownOnSide(town) || raid.getDefenderSide().isTownOnSide(town))
            .collect(Collectors.toSet());
    }

    /**
     * Gets town wars. Includes surrendered towns.
     *
     * @param town the town
     * @return the town wars
     */
    public @NotNull Set<War> getTownWars(Town town) {
        return getWars().stream()
            .filter(war -> war.isTownInWar(town))
            .collect(Collectors.toSet());
    }

    /**
     * Is nation in any wars. Includes surrendered nations.
     *
     * @param nation the nation
     * @return the boolean
     */
    public boolean isNationInAnyWars(Nation nation) {
        return getWars().stream()
            .anyMatch(war -> war.isNationInWar(nation));
    }

    /**
     * Gets nation wars. Includes surrendered nations.
     *
     * @param nation the nation
     * @return the nation wars
     */
    public @NotNull Set<War> getNationWars(Nation nation) {
        return getWars().stream()
            .filter(war -> war.isNationInWar(nation))
            .collect(Collectors.toSet());
    }

    /**
     * Gets a list of war names.
     *
     * @return the war names
     */
    @NotNull
    public List<String> getWarNames() {
        return getWars().stream()
            .map(War::getName)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    /**
     * Gets a list of war labels.
     *
     * @return the war labels
     */
    @NotNull
    public List<String> getWarLabels() {
        return getWars().stream()
            .map(War::getLabel)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    /**
     * Gets a list of all active sieges.
     *
     * @return the sieges
     */
    @NotNull
    public Set<Siege> getSieges() {
        return getWars().stream()
            .map(War::getSieges)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Gets a list of all active raids.
     *
     * @return the raids
     */
    @NotNull
    public Set<Raid> getRaids() {
        return getWars().stream()
            .map(War::getRaids)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Is player in any siege.
     *
     * @param p the p
     * @return the boolean
     */
    public boolean isPlayerInAnySiege(Player p) {
        return isPlayerInAnySiege(p.getUniqueId());
    }

    /**
     * Is player in any siege. Returns true for offline players.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean isPlayerInAnySiege(UUID uuid) {
        return getSieges().stream()
            .anyMatch(siege -> siege.isPlayerInSiege(uuid));
    }

    /**
     * Gets player sieges.
     *
     * @param p the p
     * @return the player sieges
     */
    public @NotNull Set<Siege> getPlayerSieges(Player p) {
        return getPlayerSieges(p.getUniqueId());
    }

    /**
     * Gets player sieges. Returns true for offline players.
     *
     * @param uuid the uuid
     * @return the player sieges
     */
    public @NotNull Set<Siege> getPlayerSieges(UUID uuid) {
        return getSieges().stream()
            .filter(siege -> siege.isPlayerInSiege(uuid))
            .collect(Collectors.toSet());
    }

    /**
     * Gets nations at war. Does not include surrendered nations.
     *
     * @return the nations at war
     */
    public @NotNull Set<Nation> getNationsAtWar() {
        return getWars().stream()
            .map(War::getNations)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Gets towns at war. Does not include surrendered towns.
     *
     * @return the towns at war
     */
    public @NotNull Set<Town> getTownsAtWar() {
        return getWars().stream()
            .map(War::getTowns)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }
}
