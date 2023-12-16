package com.github.alathra.AlathranWars.conflict;

import com.github.alathra.AlathranWars.conflict.battle.raid.Raid;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.ConflictType;
import com.palmergames.bukkit.towny.object.Government;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class WarBuilder {
    private UUID uuid;
    private String name;
    private String label;
    private ConflictType conflictType = ConflictType.WAR;
    private boolean event = false;
    private Side side1;
    private Side side2;
    private Set<Siege> sieges = new HashSet<>();
    private Set<Raid> raids = new HashSet<>();

    private Town aggressorTown;
    private Optional<Nation> aggressorNation = Optional.empty();
    private Town victimTown;
    private Optional<Nation> victimNation = Optional.empty();

    private Government aggressor;
    private Government victim;

    public WarBuilder() {}

    public War resume() {
        if (
            uuid == null ||
            name == null ||
            label == null ||
            side1 == null ||
            side2 == null ||
            sieges == null ||
            raids == null
        ) {
            throw new IllegalStateException();
        }

        return new War(
            uuid,
            name,
            label,
            side1,
            side2,
            sieges,
            raids,
            event
        );
    }

    /*public War create() throws WrapperCommandSyntaxException {
        this.setUuid(UUID.randomUUID());

        if (aggressorNation.isEmpty() && aggressorTown != null) {
            // TODO Throw
        }
        if (victimNation.isEmpty() && victimTown != null) {
            // TODO Throw
        }

        return new War(
            uuid,
            label,
            aggressorTown,
            victimTown,
            aggressorNation.isPresent(),
            victimNation.isPresent(),
            event
        );
    }*/

    public War create() throws WrapperCommandSyntaxException, SideCreationException {
        this.setUuid(UUID.randomUUID());

        return new War(
            uuid,
            label,
            aggressor,
            victim,
            event
        );
    }

    public WarBuilder setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public WarBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public WarBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public WarBuilder setConflictType(ConflictType conflictType) {
        this.conflictType = conflictType;
        return this;
    }

    public WarBuilder setSide1(Side side1) {
        this.side1 = side1;
        return this;
    }

    public WarBuilder setSide2(Side side2) {
        this.side2 = side2;
        return this;
    }

    public WarBuilder setEvent(boolean event) {
        this.event = event;
        return this;
    }

    public WarBuilder setSieges(Set<Siege> sieges) {
        this.sieges = sieges;
        return this;
    }

    public WarBuilder setRaids(Set<Raid> raids) {
        this.raids = raids;
        return this;
    }

    public WarBuilder setAggressorTown(Town target) {
        this.aggressorTown = target;
        return this;
    }

    public WarBuilder setAggressorNation(Nation target) {
        this.aggressorTown = target.getCapital();
        this.aggressorNation = Optional.of(target);
        return this;
    }

    public WarBuilder setVictimTown(Town target) {
        this.victimTown = target;
        return this;
    }

    public WarBuilder setVictimNation(Nation target) {
        this.victimTown = target.getCapital();
        this.victimNation = Optional.of(target);
        return this;
    }

    public WarBuilder setAggressor(Government aggressor) {
        this.aggressor = aggressor;
        return this;
    }

    public WarBuilder setVictim(Government victim) {
        this.victim = victim;
        return this;
    }
}
