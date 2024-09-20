package com.github.alathra.alathranwars.conflict.war;

import com.github.alathra.alathranwars.conflict.battle.raid.Raid;
import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.conflict.war.side.SideCreationException;
import com.github.alathra.alathranwars.enums.ConflictType;
import com.palmergames.bukkit.towny.object.Government;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WarBuilder {
    private @Nullable UUID uuid;
    private @Nullable String name;
    private @Nullable String label;
    private ConflictType conflictType = ConflictType.WAR;
    private boolean event = false;
    private @Nullable Side side1;
    private @Nullable Side side2;
    private @Nullable Set<Siege> sieges = new HashSet<>();
    private @Nullable Set<Raid> raids = new HashSet<>();

    private @Nullable Government aggressor;
    private @Nullable Government victim;

    public WarBuilder() {
    }

    /**
     * Build a new war from save data
     * @return a new war
     * @throws WarCreationException exception
     */
    public War resume() throws WarCreationException {
        if (uuid == null)
            throw new WarCreationException("Missing state uuid required to create War!");

        if (name == null)
            throw new WarCreationException("Missing state name required to create War!");

        if (label == null)
            throw new WarCreationException("Missing state label required to create War!");

        if (side1 == null)
            throw new WarCreationException("Missing state side1 required to create War!");

        if (side2 == null)
            throw new WarCreationException("Missing state side2 required to create War!");

        if (sieges == null)
            throw new WarCreationException("Missing state sieges required to create War!");

        if (raids == null)
            throw new WarCreationException("Missing state raids required to create War!");

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

    /**
     * Build a new War
     * @return a new war
     * @throws WrapperCommandSyntaxException exception
     * @throws SideCreationException exception
     */
    public War create() throws WrapperCommandSyntaxException, SideCreationException {
        this.setUuid(UUID.randomUUID());

        if (uuid == null)
            throw new WarCreationException("Missing state uuid required to create War!");

        if (label == null)
            throw new WarCreationException("Missing state label required to create War!");

        if (aggressor == null)
            throw new WarCreationException("Missing state aggressor required to create War!");

        if (victim == null)
            throw new WarCreationException("Missing state victim required to create War!");

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

    public WarBuilder setAggressor(Government aggressor) {
        this.aggressor = aggressor;
        return this;
    }

    public WarBuilder setVictim(Government victim) {
        this.victim = victim;
        return this;
    }
}
