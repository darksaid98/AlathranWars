package com.github.alathra.AlathranWars.conflict;

import com.github.alathra.AlathranWars.enums.battle.BattleSide;
import com.github.alathra.AlathranWars.enums.battle.BattleTeam;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class SideBuilder {
    public SideBuilder() {}

    public Side buildOld() throws SideCreationException {
//        if (warUUID == null || uuid == null || town == null || side == null || team == null || name == null ||
//            towns == null || nations == null || playersIncludingOffline == null ||
//            surrenderedTowns == null || surrenderedNations == null || surrenderedPlayersIncludingOffline == null ||
//            siegeGrace == null || raidGrace == null
//        ) throw new IllegalStateException("Missing state to create Side 1");

        if (warUUID == null || uuid == null
        ) throw new IllegalStateException("Missing state to create Side 1.2");

        if (government == null
        ) throw new IllegalStateException("Missing state to create Side 1.3");
        if (side == null
        ) throw new IllegalStateException("Missing state to create Side 1.1");

        if ( team == null || name == null ||
        towns == null || nations == null
        ) throw new IllegalStateException("Missing state to create Side 1");

        if (playersIncludingOffline == null ||
            surrenderedTowns == null || surrenderedNations == null || surrenderedPlayersIncludingOffline == null ||
            siegeGrace == null || raidGrace == null
        ) throw new IllegalStateException("Missing state to create Side 2");

        return new Side(
            warUUID,
            uuid,
            government,
            side,
            team,
            name,
            towns,
            nations,
            playersIncludingOffline,
            surrenderedTowns,
            surrenderedNations,
            surrenderedPlayersIncludingOffline,
            siegeGrace,
            raidGrace
        );
    }

    public Side buildNew() throws SideCreationException {
        if (warUUID == null || uuid == null || government == null || side == null || team == null)
            throw new IllegalStateException("Missing state to create new Side");

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

    public SideBuilder setTowns(Set<Town> towns) {
        this.towns = towns;
        return this;
    }

    public SideBuilder setNations(Set<Nation> nations) {
        this.nations = nations;
        return this;
    }

    public SideBuilder setPlayersIncludingOffline(Set<UUID> playersIncludingOffline) {
        this.playersIncludingOffline = playersIncludingOffline;
        return this;
    }

    public SideBuilder setSurrenderedTowns(Set<Town> surrenderedTowns) {
        this.surrenderedTowns = surrenderedTowns;
        return this;
    }

    public SideBuilder setSurrenderedNations(Set<Nation> surrenderedNations) {
        this.surrenderedNations = surrenderedNations;
        return this;
    }

    public SideBuilder setSurrenderedPlayersIncludingOffline(Set<UUID> surrenderedPlayersIncludingOffline) {
        this.surrenderedPlayersIncludingOffline = surrenderedPlayersIncludingOffline;
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

    private UUID warUUID;
    private UUID uuid;
    private BattleSide side;
    private BattleTeam team;
    private String name;

    private Government government;
    private Town town;

    private Set<Town> towns;
    private Set<Nation> nations;
    private Set<UUID> playersIncludingOffline;
    private Set<Town> surrenderedTowns;
    private Set<Nation> surrenderedNations;
    private Set<UUID> surrenderedPlayersIncludingOffline;
    private Instant siegeGrace;
    private Instant raidGrace;



}
