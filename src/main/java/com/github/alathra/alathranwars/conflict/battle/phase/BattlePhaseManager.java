package com.github.alathra.alathranwars.conflict.battle.phase;

import java.time.Instant;

public class BattlePhaseManager<T extends Enum<T> & IBattlePhase<T>> extends BattlePhaseManagerImpl<T> {
    public BattlePhaseManager(T phase, int progress, Instant startTime) {
        super(phase, progress, startTime);
    }
}
