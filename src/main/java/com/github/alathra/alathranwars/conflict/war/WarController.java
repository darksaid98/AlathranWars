package com.github.alathra.alathranwars.conflict.war;

import com.github.alathra.alathranwars.conflict.battle.raid.Raid;
import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.database.DatabaseQueries;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The singleton War controller.
 */
@Singleton
public class WarController {
    private static WarController instance;
    private Set<War> wars = new HashSet<>();

    private WarController() {
        if (instance != null)
            Bukkit.getServer().getLogger().warning("Tried to re-initialize singleton");
    }

    /**
     * Gets or creates an instance of the war controller.
     *
     * @return the instance
     */
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
     * Gets all wars.
     *
     * @return the wars
     */
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
     * Is nation or town in any wars. (Includes surrendered nations & towns)
     *
     * @param government the government
     * @return the boolean
     */
    public boolean isInAnyWars(Government government) {
        return getWars().stream()
            .anyMatch(war -> war.isInWar(government));
    }

    /**
     * Is player in any wars. Includes if surrendered.
     *
     * @param p the p
     * @return the boolean
     */
    public boolean isInAnyWars(Player p) {
        return isInAnyWars(p.getUniqueId());
    }

    /**
     * Is player in any wars. Includes if surrendered.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean isInAnyWars(UUID uuid) {
        return getWars().stream()
            .anyMatch(war -> war.isInWar(uuid));
    }

    /**
     * Gets nation or town wars. (Includes surrendered nations & towns)
     *
     * @param government the government
     * @return the nation wars
     */
    public Set<War> getWars(Government government) {
        return getWars().stream()
            .filter(war -> war.isInWar(government))
            .collect(Collectors.toSet());
    }

    /**
     * Gets list of player wars. Includes if surrendered.
     *
     * @param p the p
     * @return the player wars
     */
    public Set<War> getWars(Player p) {
        return getWars(p.getUniqueId());
    }

    /**
     * Gets list of player wars. Includes if surrendered.
     *
     * @param uuid the uuid
     * @return the player wars
     */
    public Set<War> getWars(UUID uuid) {
        return getWars().stream()
            .filter(war -> war.isInWar(uuid))
            .collect(Collectors.toSet());
    }

    // SECTION Battles

    /**
     * Gets siege by the siege UUID.
     *
     * @param uuid the uuid
     * @return the siege
     */
    @Nullable
    public Siege getSiege(UUID uuid) {
        return getWars().stream()
            .map(war -> war.getSiege(uuid))
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
    }

    /**
     * Gets raid by the raid UUID.
     *
     * @param uuid the uuid
     * @return the raid
     */
    @Nullable
    public Raid getRaid(UUID uuid) {
        return getWars().stream()
            .map(war -> war.getRaid(uuid))
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
    }

    /**
     * Is nation or town in any sieges. Includes surrendered nations/towns.
     *
     * @param government the government
     * @return the boolean
     */
    public boolean isInAnySieges(Government government) {
        if (government instanceof Nation nation) {
            return nation.getTowns().stream()
                .anyMatch(this::isInAnySieges);
        } else if (government instanceof Town town) {
            return getWars().stream()
                .anyMatch(war -> war.isInWar(town) && war.isTownUnderSiege(town));
        }
        return false;
    }

    /**
     * Is nation or town in any raids. Includes surrendered nations/towns.
     *
     * @param government the government
     * @return the boolean
     */
    public boolean isInAnyRaids(Government government) {
        if (government instanceof Nation nation) {
            return nation.getTowns().stream()
                .anyMatch(this::isInAnyRaids);
        } else if (government instanceof Town town) {
            return getWars().stream()
                .anyMatch(war -> war.isInWar(town) && war.isTownUnderRaid(town));
        }
        return false;
    }

    /**
     * Gets a list of all active sieges.
     *
     * @return the sieges
     */
    public Set<Siege> getSieges() {
        return getWars().stream()
            .map(War::getSieges)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Gets nation or town sieges. Includes surrendered nation/town.
     *
     * @param government the government
     * @return the town sieges
     */
    public Set<Siege> getSieges(Government government) {
        if (government instanceof Nation nation) {
            return nation.getTowns().stream()
                .flatMap(town -> getSieges(town).stream())
                .collect(Collectors.toSet());
        } else if (government instanceof Town town) {
            return getSieges().stream()
                .filter(siege -> siege.getAttackerSide().isOnSide(town) || siege.getDefenderSide().isOnSide(town))
                .collect(Collectors.toSet());
        }
        return Set.of();
    }

    /**
     * Gets player sieges.
     *
     * @param p the p
     * @return the player sieges
     */
    public Set<Siege> getSieges(Player p) {
        return getSieges(p.getUniqueId());
    }

    /**
     * Gets player sieges. Returns true for offline players.
     *
     * @param uuid the uuid
     * @return the player sieges
     */
    public Set<Siege> getSieges(UUID uuid) {
        return getSieges().stream()
            .filter(siege -> siege.isPlayerParticipating(uuid))
            .collect(Collectors.toSet());
    }

    /**
     * Gets a list of all active raids.
     *
     * @return the raids
     */
    public Set<Raid> getRaids() {
        return getWars().stream()
            .map(War::getRaids)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Gets nation or town raids. Includes surrendered nation/town.
     *
     * @param government the government
     * @return the town raids
     */
    public Set<Raid> getRaids(Government government) {
        // TODO Raids
        /*if (government instanceof Nation nation) {
            return nation.getTowns().stream()
                .flatMap(town -> getRaids(town).stream())
                .collect(Collectors.toSet());
        } else if (government instanceof Town town) {
            return getRaids().stream()
                .filter(raid -> raid.getAttackerSide().isOnSide(town) || raid.getDefenderSide().isOnSide(town))
                .collect(Collectors.toSet());
        }*/
        return Set.of();
    }

    /**
     * Gets player raids.
     *
     * @param p the p
     * @return the player raids
     */
    public Set<Raid> getRaids(Player p) {
        return getRaids(p.getUniqueId());
    }

    /**
     * Gets player raids. Returns true for offline players.
     *
     * @param uuid the uuid
     * @return the player raids
     */
    public Set<Raid> getRaids(UUID uuid) {
        // TODO Raids
        /*return getRaids().stream()
            .filter(raid -> raid.isPlayerInRaid(uuid))
            .collect(Collectors.toSet());*/
        return Collections.emptySet();
    }

    // SECTION Misc

    /**
     * Gets side by the side UUID.
     *
     * @param uuid the uuid
     * @return the side
     */
    @Nullable
    public Side getSide(UUID uuid) {
        return getWars().stream()
            .map(war -> war.getSide(uuid))
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
    }

    // SECTION API

    /**
     * Gets a list of war names.
     *
     * @return the war names
     */
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
    public List<String> getWarLabels() {
        return getWars().stream()
            .map(War::getLabel)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    /**
     * Gets nations at war. Does not include surrendered nations.
     *
     * @return the nations at war
     */
    public Set<Nation> getNationsAtWar() {
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
    public Set<Town> getTownsAtWar() {
        return getWars().stream()
            .map(War::getTowns)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Check if two sides are opposing each-other in a war
     * @param side1 a side
     * @param side2 a side
     * @return true if opposing each-other in a war
     */
    public boolean isAtWar(Side side1, Side side2) {
        // Return early if comparing same side to prevent false positive
        if (side1.equals(side2))
            return false;

        return side1.getWar().equals(side2.getWar());
    }

    /**
     * Check if two governments are at war with each-other
     * @param government1 nation or town
     * @param government2 nation or town
     * @return true if governments are on opposing sides in any war
     */
    public boolean isAtWar(Government government1, Government government2) {
        Set<War> wars1 = getWars(government1);
        Set<War> wars2 = getWars(government2);

        /*final boolean isInSameWar = wars1.stream().anyMatch(wars2::contains);
        if (!isInSameWar)
            return false;*/

        // Get list of common wars
        Set<War> sharedWars = wars1.stream()
            .filter(wars2::contains)
            .collect(Collectors.toSet());

        // Iterate shared wars and check if on opposite sides
        for (War war : sharedWars) {
            Side side1 = war.getSide(government1);
            Side side2 = war.getSide(government2);

            if (side1 == null || side2 == null)
                continue;

            if (!isAtWar(side1, side2)) {
                return true;
            }
        }

        return false;
    }
}
