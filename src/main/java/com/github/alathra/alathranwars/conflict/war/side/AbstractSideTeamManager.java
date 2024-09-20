package com.github.alathra.alathranwars.conflict.war.side;

import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Provides methods for handling all members of a side
 */
public abstract class AbstractSideTeamManager {
    private final Set<Nation> nations = new HashSet<>(); // Collection of nations in the war (excludes surrendered)
    private final Set<Town> towns = new HashSet<>(); // Collection of towns in the war (excludes surrendered)
    private final Set<OfflinePlayer> players = new HashSet<>(); // Collection of players in the war (excludes surrendered)

    private final Set<Nation> nationsSurrendered = new HashSet<>(); // Collection surrendered of nations in the war (excludes non-surrendered)
    private final Set<Town> townsSurrendered = new HashSet<>(); // Collection of surrendered towns in the war (excludes non-surrendered)
    private final Set<OfflinePlayer> playersSurrendered = new HashSet<>(); // Collection of surrendered players in the war (excludes non-surrendered)

    public AbstractSideTeamManager() {}

    public AbstractSideTeamManager(
        Set<Nation> nations,
        Set<Town> towns,
        Set<OfflinePlayer> players,
        Set<Nation> nationsSurrendered,
        Set<Town> townsSurrendered,
        Set<OfflinePlayer> playersSurrendered
    ) {
        this.nations.addAll(nations);
        this.towns.addAll(towns);
        this.players.addAll(players);
        this.nationsSurrendered.addAll(nationsSurrendered);
        this.townsSurrendered.addAll(townsSurrendered);
        this.playersSurrendered.addAll(playersSurrendered);
    }

    // SECTION Governments management

    /**
     * Add a government to the side (Recursively adds subjects AKA towns, players)
     * @param government a government
     */
    public void add(Government government) {
        if (government instanceof Nation nation) {
            nations.add(nation);
            nation.getTowns().forEach(this::add);
        } else if (government instanceof Town town) {
            towns.add(town);
            town.getResidents().stream()
                .map(Resident::getUUID)
                .map(Bukkit::getOfflinePlayer)
                .forEach(this::add);
        }
    }

    /**
     * Remove a government from the side (Recursively removes subjects AKA towns, players)
     * @param government a government
     */
    public void remove(Government government) {
        if (government instanceof Nation nation) {
            nations.remove(nation);
            nation.getTowns().forEach(this::remove);
        } else if (government instanceof Town town) {
            towns.remove(town);
            town.getResidents().stream()
                .map(Resident::getUUID)
                .map(Bukkit::getOfflinePlayer)
                .forEach(this::remove);
        }
    }

    /**
     * Add a surrendered government to the side (Recursively adds subjects AKA towns, players)
     * @param government a government
     */
    public void addSurrendered(Government government) {
        if (government instanceof Nation nation) {
            nationsSurrendered.add(nation);
            nation.getTowns().forEach(this::addSurrendered);
        } else if (government instanceof Town town) {
            townsSurrendered.add(town);
            town.getResidents().stream()
                .map(Resident::getUUID)
                .map(Bukkit::getOfflinePlayer)
                .forEach(this::addSurrendered);
        }
    }

    /**
     * Remove a surrendered government from the side (Recursively removes subjects AKA towns, players)
     * @param government a government
     */
    public void removeSurrendered(Government government) {
        if (government instanceof Nation nation) {
            nationsSurrendered.remove(nation);
            nation.getTowns().forEach(this::removeSurrendered);
        } else if (government instanceof Town town) {
            townsSurrendered.remove(town);
            town.getResidents().stream()
                .map(Resident::getUUID)
                .map(Bukkit::getOfflinePlayer)
                .forEach(this::removeSurrendered);
        }
    }

    /**
     * Kick a government from the side (Recursively kicks subjects AKA towns, players)
     * @param government a government
     */
    public void kick(Government government) {
        remove(government);
        removeSurrendered(government);
    }

    // SECTION Players management

    /**
     * Add a player to the side
     * @param p a player
     */
    public void add(Player p) {
        add(p.getUniqueId());
    }

    /**
     * Add a player to the side
     * @param uuid a player uuid
     */
    public void add(UUID uuid) {
        add(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Add a player to the side
     * @param p a player
     */
    public void add(OfflinePlayer p) {
        players.add(p);
    }

    /**
     * Remove a player from the side
     * @param p a player
     */
    public void remove(Player p) {
        remove(p.getUniqueId());
    }

    /**
     * Remove a player from the side
     * @param uuid a player uuid
     */
    public void remove(UUID uuid) {
        remove(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Remove a player from the side
     * @param p a player
     */
    public void remove(OfflinePlayer p) {
        players.remove(p);
    }

    /**
     * Add a surrendered player to the side
     * @param p a player
     */
    public void addSurrendered(Player p) {
        add(p.getUniqueId());
    }

    /**
     * Add a surrendered player to the side
     * @param uuid a player uuid
     */
    public void addSurrendered(UUID uuid) {
        add(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Add a surrendered player to the side
     * @param p a player
     */
    public void addSurrendered(OfflinePlayer p) {
        playersSurrendered.add(p);
    }

    /**
     * Remove a surrendered player from the side
     * @param p a player
     */
    public void removeSurrendered(Player p) {
        remove(p.getUniqueId());
    }

    /**
     * Remove a surrendered player from the side
     * @param uuid a player uuid
     */
    public void removeSurrendered(UUID uuid) {
        remove(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Remove a surrendered player from the side
     * @param p a player
     */
    public void removeSurrendered(OfflinePlayer p) {
        playersSurrendered.remove(p);
    }

    /**
     * Kick a player from the side
     * @param p a player
     */
    public void kick(Player p) {
        remove(p);
        removeSurrendered(p);
    }

    /**
     * Kick a player from the side
     * @param uuid a player uuid
     */
    public void kick(UUID uuid) {
        remove(uuid);
        removeSurrendered(uuid);
    }

    /**
     * Kick a player from the side
     * @param p a player
     */
    public void kick(OfflinePlayer p) {
        remove(p);
        removeSurrendered(p);
    }

    // SECTION Governments Getters

    /**
     * Get nations on this side (excludes surrendered nations)
     * @return the nations
     */
    public Set<Nation> getNations() {
        return nations;
    }

    /**
     * Get nations on this side (excludes non-surrendered nations)
     * @return the nations
     */
    public Set<Nation> getNationsSurrendered() {
        return nationsSurrendered;
    }

    /**
     * Get all nations on this side (includes surrendered nations)
     * @return the nations
     */
    public List<Nation> getNationsAll() {
        return Stream.concat(getNations().stream(), getNationsSurrendered().stream()).toList();
    }

    /**
     * Get towns on this side (excludes surrendered towns)
     * @return the towns
     */
    public Set<Town> getTowns() {
        return towns;
    }

    /**
     * Get towns on this side (excludes non-surrendered towns)
     * @return the towns
     */
    public Set<Town> getTownsSurrendered() {
        return townsSurrendered;
    }

    /**
     * Get all towns on this side (includes surrendered towns)
     * @return the towns
     */
    public List<Town> getTownsAll() {
        return Stream.concat(getTowns().stream(), getTownsSurrendered().stream()).toList();
    }

    // SECTION Players Getters

    /**
     * Get all players on this side (excluding surrendered)
     * @return player list
     */
    public List<OfflinePlayer> getPlayers() {
        return players.stream().toList();
    }

    /**
     * Get all players on this side (excludes non-surrendered players)
     * @return player list
     */
    public List<OfflinePlayer> getPlayersSurrendered() {
        return playersSurrendered.stream().toList();
    }

    /**
     * Get all players on this side (includes surrendered players)
     * @return player list
     */
    public List<OfflinePlayer> getPlayersAll() {
        return Stream.concat(getPlayers().stream(), getPlayersSurrendered().stream()).toList();
    }

    /**
     * Get all online players on this side (excluding surrendered)
     * @return player list
     */
    public List<Player> getPlayersOnline() {
        return players.stream()
            .filter(OfflinePlayer::isOnline)
            .map(OfflinePlayer::getPlayer)
            .toList();
    }

    /**
     * Get all online players on this side (excludes non-surrendered players)
     * @return player list
     */
    public List<Player> getPlayersSurrenderedOnline() {
        return playersSurrendered.stream()
            .filter(OfflinePlayer::isOnline)
            .map(OfflinePlayer::getPlayer)
            .toList();
    }

    /**
     * Get all online players on this side (includes surrendered players)
     * @return player list
     */
    public List<Player> getPlayersOnlineAll() {
        return Stream.concat(getPlayersOnline().stream(), getPlayersSurrenderedOnline().stream()).toList();
    }

    // Checks

    /**
     * Check if a government is on this side (includes surrendered governments)
     * @param government the government
     * @return true if on side
     */
    public boolean isOnSide(Government government) {
        if (government instanceof Nation nation) {
            return nations.contains(nation) || nationsSurrendered.contains(nation);
        } else if (government instanceof Town town) {
            return towns.contains(town) || townsSurrendered.contains(town);
        }
        return false;
    }

    /**
     * Check if a player is on this side (includes surrendered players)
     * @param p the player
     * @return true if on side
     */
    public boolean isOnSide(Player p) {
        return isOnSide(p.getUniqueId());
    }

    /**
     * Check if a player is on this side (includes surrendered players)
     * @param uuid the player uuid
     * @return true if on side
     */
    public boolean isOnSide(UUID uuid) {
        return isOnSide(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Check if a player is on this side (includes surrendered players)
     * @param p the player
     * @return true if on side
     */
    public boolean isOnSide(OfflinePlayer p) {
        return players.contains(p) || playersSurrendered.contains(p);
    }

    /**
     * Check if a government is surrendered on this side
     * @param government the government
     * @return true if on side and surrendered
     */
    public boolean isSurrendered(Government government) {
        if (government instanceof Nation nation) {
            return nationsSurrendered.contains(nation);
        } else if (government instanceof Town town) {
            return townsSurrendered.contains(town);
        }
        return false;
    }

    /**
     * Check if a player is surrendered on this side
     * @param p the player
     * @return true if on side and surrendered
     */
    public boolean isSurrendered(Player p) {
        return isSurrendered(p.getUniqueId());
    }

    /**
     * Check if a player is surrendered on this side
     * @param uuid the player uuid
     * @return true if on side and surrendered
     */
    public boolean isSurrendered(UUID uuid) {
        return isSurrendered(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Check if a player is surrendered on this side
     * @param p the player
     * @return true if on side and surrendered
     */
    public boolean isSurrendered(OfflinePlayer p) {
        return playersSurrendered.contains(p);
    }

    // SECTION Surrendering

    /**
     * Makes a government surrender (Recursively surrenders subjects AKA nations, towns, players)
     * @param government the government
     */
    public void surrender(Government government) {
        if (isSurrendered(government))
            return;

        if (government instanceof Nation nation) {
            remove(nation);
            addSurrendered(nation);
        } else if (government instanceof Town town) {
            remove(town);
            addSurrendered(town);
        }
    }

    /**
     * Makes a player surrender
     * @param p the player
     */
    public void surrender(Player p) { surrender(p.getUniqueId()); }

    /**
     * Makes a player surrender
     * @param uuid the player uuid
     */
    public void surrender(UUID uuid) { surrender(Bukkit.getOfflinePlayer(uuid)); }

    /**
     * Makes a player surrender
     * @param p the player
     */
    public void surrender(OfflinePlayer p) {
        if (!isSurrendered(p))
            return;

        remove(p);
        addSurrendered(p);
    }

    /**
     * Makes a government un-surrender (Recursively un-surrenders subjects AKA nations, towns, players)
     * @param government the government
     */
    public void unsurrender(Government government) {
        if (!isSurrendered(government))
            return;

        if (government instanceof Nation nation) {
            removeSurrendered(nation);
            add(nation);
        } else if (government instanceof Town town) {
            removeSurrendered(town);
            add(town);
        }
    }

    /**
     * Makes a player un-surrender
     * @param p the player
     */
    public void unsurrender(Player p) { unsurrender(p.getUniqueId()); }

    /**
     * Makes a player un-surrender
     * @param uuid the player uuid
     */
    public void unsurrender(UUID uuid) { unsurrender(Bukkit.getOfflinePlayer(uuid)); }

    /**
     * Makes a player un-surrender
     * @param p the player
     */
    public void unsurrender(OfflinePlayer p) {
        if (isSurrendered(p))
            return;

        removeSurrendered(p);
        add(p);
    }

    // SECTION Misc methods

    /**
     * Check if this side is no longer able to continue fighting a war
     * @return true if there are no more nations or towns to participate in the war
     */
    public boolean shouldSurrender() {
        return getNations().isEmpty() && getTowns().isEmpty();
    }

    /**
     * Check if this nation is no longer able to continue fighting a war
     * @param nation the nation
     * @return true if there are no more un-surrendered towns from the nation to participate in the war
     */
    public boolean shouldSurrender(Nation nation) {
        final int townsQty = nation.getNumTowns();
        final int surrenderedTownsQty = nation.getTowns().stream().filter(this::isSurrendered).mapToInt(value -> 1).sum();
        return surrenderedTownsQty == townsQty;

    }
}
