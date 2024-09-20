package com.github.alathra.alathranwars.conflict.war.side;

import com.github.alathra.alathranwars.enums.battle.BattleSide;
import com.github.alathra.alathranwars.enums.battle.BattleTeam;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SideBuilder {
    private @Nullable UUID warUUID;
    private @Nullable UUID uuid;
    private @Nullable BattleSide side;
    private @Nullable BattleTeam team;
    private @Nullable String name;

    private @Nullable Government government;

    private @Nullable Set<Nation> nations;
    private @Nullable Set<Town> towns;
    private @Nullable Set<OfflinePlayer> players;

    private @Nullable Set<Nation> nationsSurrendered;
    private @Nullable Set<Town> townsSurrendered;
    private @Nullable Set<OfflinePlayer> playersSurrendered;

    private @Nullable Instant siegeGrace;
    private @Nullable Instant raidGrace;

    public SideBuilder() {
    }

    /**
     * Build a new side from save data
     * @return a new side
     * @throws SideCreationException exception
     */
    public Side rebuild() throws SideCreationException {
        if (warUUID == null)
            throw new SideCreationException("Missing state warUUID required to create Side!");

        if (uuid == null)
            throw new SideCreationException("Missing state uuid required to create Side!");

        if (government == null)
            throw new SideCreationException("Missing state government required to create Side!");

        if (side == null)
            throw new SideCreationException("Missing state side required to create Side!");

        if (team == null)
            throw new SideCreationException("Missing state team required to create Side!");

        if (name == null)
            throw new SideCreationException("Missing state name required to create Side!");

        if (towns == null)
            throw new SideCreationException("Missing state towns required to create Side!");

        if (nations == null)
            throw new SideCreationException("Missing state nations required to create Side!");

        if (players == null)
            throw new SideCreationException("Missing state players required to create Side!");

        if (townsSurrendered == null)
            throw new SideCreationException("Missing state townsSurrendered required to create Side!");

        if (nationsSurrendered == null)
            throw new SideCreationException("Missing state nationsSurrendered required to create Side!");

        if (playersSurrendered == null)
            throw new SideCreationException("Missing state playersSurrendered required to create Side!");

        if (siegeGrace == null)
            throw new SideCreationException("Missing state siegeGrace required to create Side!");

        if (raidGrace == null)
            throw new SideCreationException("Missing state raidGrace required to create Side!");

        return new Side(
            warUUID,
            uuid,
            government,
            side,
            team,
            name,
            nations,
            towns,
            players,
            nationsSurrendered,
            townsSurrendered,
            playersSurrendered,
            siegeGrace,
            raidGrace
        );
    }

    /**
     * Build a new Side
     * @return a new side
     * @throws SideCreationException exception
     */
    public Side build() throws SideCreationException {
        if (warUUID == null)
            throw new SideCreationException("Missing state warUUID required to create Side!");

        if (uuid == null)
            throw new SideCreationException("Missing state uuid required to create Side!");

        if (government == null)
            throw new SideCreationException("Missing state government required to create Side!");

        if (side == null)
            throw new SideCreationException("Missing state side required to create Side!");

        if (team == null)
            throw new SideCreationException("Missing state team required to create Side!");

        return new Side(
            warUUID,
            uuid,
            government,
            side,
            team
        );
    }

    public SideBuilder setWarUUID(UUID warUUID) {
        this.warUUID = warUUID;
        return this;
    }

    public SideBuilder setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public SideBuilder setSide(BattleSide side) {
        this.side = side;
        return this;
    }

    public SideBuilder setTeam(BattleTeam team) {
        this.team = team;
        return this;
    }

    public SideBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public SideBuilder setLeader(Government target) {
        this.government = target;
        return this;
    }

    public SideBuilder setNations(Set<Nation> nations) {
        this.nations = nations;
        return this;
    }

    public SideBuilder setTowns(Set<Town> towns) {
        this.towns = towns;
        return this;
    }

    public SideBuilder setPlayers(@NotNull Set<UUID> players) {
        this.players = players.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
        return this;
    }

    public SideBuilder setNationsSurrendered(Set<Nation> nationsSurrendered) {
        this.nationsSurrendered = nationsSurrendered;
        return this;
    }

    public SideBuilder setTownsSurrendered(Set<Town> townsSurrendered) {
        this.townsSurrendered = townsSurrendered;
        return this;
    }

    public SideBuilder setPlayersSurrendered(@NotNull Set<UUID> playersSurrendered) {
        this.playersSurrendered = playersSurrendered.stream().map(Bukkit::getOfflinePlayer).collect(Collectors.toSet());
        return this;
    }

    public SideBuilder setSiegeGrace(Instant siegeGrace) {
        this.siegeGrace = siegeGrace;
        return this;
    }

    public SideBuilder setRaidGrace(Instant raidGrace) {
        this.raidGrace = raidGrace;
        return this;
    }
}
