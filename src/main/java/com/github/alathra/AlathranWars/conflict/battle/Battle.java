package com.github.alathra.AlathranWars.conflict.battle;

import com.github.alathra.AlathranWars.enums.battle.BattleType;
import com.github.alathra.AlathranWars.enums.battle.BattleVictoryReason;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Battle {
    void start(); // Initiates battle
    void stop(); // Ends battle
    void resume(); // Resume battle after server restart or plugin reload

    void attackersWin(BattleVictoryReason reason); // A Attacker victory
    void defendersWin(BattleVictoryReason reason); // A Defender victory
    void equalWin(BattleVictoryReason reason); // A Draw


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

    BattleType battleType = null;
    default BattleType getBattleType() {
        return battleType;
    }
}
