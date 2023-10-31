package com.github.alathra.AlathranWars.conflict.battle;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Battle {
    void start(); // Initiates battle
    void stop(); // Ends battle
    void resume(); // Resume battle after server restart or plugin reload

    void attackersWin(); // A Attacker victory
    void defendersWin(); // A Defender victory
    void equalWin(); // A Draw


    UUID uuid = null; // Battle UUID
    default @NotNull UUID getUUID() {
        return uuid;
    }

    /**
     * Equals boolean.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    default boolean equals(UUID uuid) {
        return getUUID().equals(uuid);
    }

    /**
     * Equals boolean.
     *
     * @param battle the battle
     * @return the boolean
     */
    default boolean equals(@NotNull Battle battle) {
        return getUUID().equals(battle.getUUID());
    }

}
