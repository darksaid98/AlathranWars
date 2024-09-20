package com.github.alathra.alathranwars.conflict.battle;

import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.enums.battle.BattleSide;
import com.github.alathra.alathranwars.events.battle.PlayerEnteredBattlefieldEvent;
import com.github.alathra.alathranwars.events.battle.PlayerLeftBattlefieldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides methods for handling all members of a Battle
 */
public abstract class AbstractBattleTeamManagement {
    // SECTION Player management

    private final List<OfflinePlayer> attackers = new ArrayList<>(); // A list of all players who are in the attackers team
    private final List<OfflinePlayer> defenders = new ArrayList<>(); // A list of all players who are in the attackers team

    /**
     * Get all online players eligible to fight in the battle
     * @param side battle side
     * @return list of players
     */
    public List<Player> getPlayersOnline(BattleSide side) {
        return switch (side) {
            case ATTACKER -> attackers.stream()
                .filter(OfflinePlayer::isOnline)
                .map(OfflinePlayer::getPlayer)
                .toList();
            case DEFENDER -> defenders.stream()
                .filter(OfflinePlayer::isOnline)
                .map(OfflinePlayer::getPlayer)
                .toList();
            case SPECTATOR -> Bukkit.getOnlinePlayers().stream()
                .map(OfflinePlayer::getPlayer)
                .filter(p -> !attackers.contains(p) && !defenders.contains(p))
                .toList();
        };
    }

    /**
     * Get all players eligible to fight in the battle
     * @param side battle side
     * @return list of players
     */
    public List<OfflinePlayer> getPlayers(BattleSide side) {
        return switch (side) {
            case ATTACKER -> attackers;
            case DEFENDER -> defenders;
            default -> List.of();
        };
    }

    public void addPlayer(OfflinePlayer p, BattleSide side) {
        switch (side) {
            case ATTACKER -> attackers.add(p);
            case DEFENDER -> defenders.add(p);
        }
    }

    public void addPlayer(Player p, BattleSide side) { addPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()), side); }

    public void addPlayer(UUID uuid, BattleSide side) { addPlayer(Bukkit.getOfflinePlayer(uuid), side); }

    public void removePlayer(OfflinePlayer p, BattleSide side) {
        switch (side) {
            case ATTACKER -> attackers.remove(p);
            case DEFENDER -> defenders.remove(p);
        }
    }

    public void removePlayer(Player p, BattleSide side) { removePlayer(p.getUniqueId(), side); }

    public void removePlayer(UUID uuid, BattleSide side) { removePlayer(Bukkit.getOfflinePlayer(uuid), side); }

    // Player info methods

    public BattleSide getPlayerBattleSide(OfflinePlayer p) {
        if (getPlayers(BattleSide.ATTACKER).contains(p))
            return BattleSide.ATTACKER;

        if (getPlayers(BattleSide.DEFENDER).contains(p))
            return BattleSide.DEFENDER;

        return BattleSide.SPECTATOR;
    }

    public BattleSide getPlayerBattleSide(Player p) {
        return getPlayerBattleSide(p.getUniqueId());
    }

    public BattleSide getPlayerBattleSide(UUID uuid) {
        return getPlayerBattleSide(Bukkit.getOfflinePlayer(uuid));
    }

    // Misc

    /**
     * Checks if the player is considered part of this battle
     * @param p the player
     * @return yes if they have joined the battle
     */
    public boolean isPlayerParticipating(OfflinePlayer p) {
        return getPlayers(BattleSide.ATTACKER).contains(p) || getPlayers(BattleSide.DEFENDER).contains(p);
    }

    /**
     * Checks if the player is considered part of this battle
     * @param p the player
     * @return yes if they have joined the battle
     */
    public boolean isPlayerParticipating(Player p) {
        return isPlayerParticipating(p.getUniqueId());
    }

    /**
     * Checks if the player is considered part of this battle
     * @param uuid the player uuid
     * @return yes if they have joined the battle
     */
    public boolean isPlayerParticipating(UUID uuid) {
        return isPlayerParticipating(Bukkit.getOfflinePlayer(uuid));
    }

    // SECTION Active player management

    private final List<Player> activeAttackers = new ArrayList<>(); // Players in the battle zone
    private final List<Player> activeDefenders = new ArrayList<>(); // Players in the battle zone
    private final List<Player> activeSpectators = new ArrayList<>(); // Players in the battle zone

    /**
     * Get all online players within the battlefield.
     * @param side battle side
     * @return list of players
     */
    public List<Player> getActivePlayers(BattleSide side) {
        return switch (side) {
            case ATTACKER -> activeAttackers;
            case DEFENDER -> activeDefenders;
            case SPECTATOR -> activeSpectators;
        };
    }

    private void addActivePlayer(Player p, BattleSide side) {
        switch (side) {
            case ATTACKER -> activeAttackers.add(p);
            case DEFENDER -> activeDefenders.add(p);
            case SPECTATOR -> activeSpectators.add(p);
        };
    }

    private void removeActivePlayer(Player p, BattleSide side) {
        switch (side) {
            case ATTACKER -> activeAttackers.remove(p);
            case DEFENDER -> activeDefenders.remove(p);
            case SPECTATOR -> activeSpectators.remove(p);
        };
    }

    public void calculateBattlefieldPlayers(Location location, int range, War war, Battle battle) {
        final List<Player> previousAttackersOnBattlefield = new ArrayList<>(getActivePlayers(BattleSide.ATTACKER));
        final List<Player> previousDefendersOnBattlefield = new ArrayList<>(getActivePlayers(BattleSide.DEFENDER));
        final List<Player> previousSpectatorsOnBattlefield = new ArrayList<>(getActivePlayers(BattleSide.SPECTATOR));

        activeAttackers.clear();
        activeSpectators.addAll(getPlayersWithinRange(BattleSide.ATTACKER, location, range));

        activeDefenders.clear();
        activeSpectators.addAll(getPlayersWithinRange(BattleSide.DEFENDER, location, range));

        activeSpectators.clear();
        activeSpectators.addAll(getPlayersWithinRange(BattleSide.SPECTATOR, location, range));

        // Attackers
        emitLeaving(BattleSide.ATTACKER, previousAttackersOnBattlefield, war, battle); // Emit Leaving attackers events
        emitEntering(BattleSide.ATTACKER, previousAttackersOnBattlefield, war, battle); // Emit Entering attackers events

        // Defenders
        emitLeaving(BattleSide.DEFENDER, previousDefendersOnBattlefield, war, battle); // Emit Leaving defenders events
        emitEntering(BattleSide.DEFENDER, previousDefendersOnBattlefield, war, battle); // Emit Entering defenders events

        // Spectators
        emitLeaving(BattleSide.SPECTATOR, previousSpectatorsOnBattlefield, war, battle); // Emit Leaving spectators events
        emitEntering(BattleSide.SPECTATOR, previousSpectatorsOnBattlefield, war, battle); // Emit Entering spectators events
    }

    private List<Player> getPlayersWithinRange(BattleSide battleSide, Location location, int range) {
        return getPlayersOnline(battleSide).stream()
            .filter(OfflinePlayer::isOnline)
            .filter(p -> location.getWorld().equals(p.getLocation().getWorld()))
            .filter(p -> location.distance(p.getLocation()) < range)
            .toList();
    }

    private void emitEntering(BattleSide battleSide, List<Player> previousPlayers, War war, Battle battle) {
        getActivePlayers(battleSide).stream()
            .filter(p -> p.isConnected() && !previousPlayers.contains(p))
            .collect(Collectors.toSet())
            .forEach(p -> new PlayerEnteredBattlefieldEvent(p, war, battle, battleSide).callEvent());
    }

    private void emitLeaving(BattleSide battleSide, List<Player> previousPlayers, War war, Battle battle) {
        previousPlayers.stream()
            .filter(p -> p.isConnected() && !getActivePlayers(battleSide).contains(p))
            .collect(Collectors.toSet())
            .forEach(p -> new PlayerLeftBattlefieldEvent(p, war, battle, battleSide).callEvent());
    }
}
