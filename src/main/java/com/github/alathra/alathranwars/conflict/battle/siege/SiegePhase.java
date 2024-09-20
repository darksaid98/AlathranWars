package com.github.alathra.alathranwars.conflict.battle.siege;

import com.github.alathra.alathranwars.conflict.battle.phase.BattlePhaseSwitchException;
import com.github.alathra.alathranwars.conflict.battle.phase.IBattlePhase;

public enum SiegePhase implements IBattlePhase<SiegePhase> {
    SIEGE(1),
    ENDING(2);

    // Identifiers start at 1, used to traverse between stages
    private final int phaseIdentifier;

    SiegePhase(int phaseIdentifier) {
        this.phaseIdentifier = phaseIdentifier;
    }

    @Override
    public int getPhaseIdentifier() {
        return phaseIdentifier;
    }

    @Override
    public SiegePhase next() throws BattlePhaseSwitchException {
        return next(fromIdentifier(getPhaseIdentifier()));
    }

    @Override
    public SiegePhase previous() throws BattlePhaseSwitchException {
        return previous(fromIdentifier(getPhaseIdentifier()));
    }

    @Override
    public SiegePhase next(SiegePhase phase) throws BattlePhaseSwitchException {
        return IBattlePhase.super.next(phase);
    }

    @Override
    public SiegePhase previous(SiegePhase phase) throws BattlePhaseSwitchException {
        return IBattlePhase.super.previous(phase);
    }
}
