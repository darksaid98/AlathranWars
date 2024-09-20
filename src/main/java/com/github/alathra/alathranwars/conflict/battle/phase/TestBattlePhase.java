package com.github.alathra.alathranwars.conflict.battle.phase;

public enum TestBattlePhase implements IBattlePhase<TestBattlePhase> {
    INITIAL(1),
    FIGHTING(2),
    END(3);

    // Identifiers start at 1, used to traverse between stages
    private final int phaseIdentifier;

    TestBattlePhase(int phaseIdentifier) {
        this.phaseIdentifier = phaseIdentifier;
    }

    public int getPhaseIdentifier() {
        return phaseIdentifier;
    }

    public TestBattlePhase next() throws BattlePhaseSwitchException {
        return next(fromIdentifier(getPhaseIdentifier()));
    }

    public TestBattlePhase previous() throws BattlePhaseSwitchException {
        return previous(fromIdentifier(getPhaseIdentifier()));
    }

    @Override
    public TestBattlePhase next(TestBattlePhase phase) throws BattlePhaseSwitchException {
        return IBattlePhase.super.next(phase);
    }

    @Override
    public TestBattlePhase previous(TestBattlePhase phase) throws BattlePhaseSwitchException {
        return IBattlePhase.super.previous(phase);
    }
}
