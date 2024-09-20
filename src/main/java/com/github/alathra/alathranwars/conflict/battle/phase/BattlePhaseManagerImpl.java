package com.github.alathra.alathranwars.conflict.battle.phase;

import java.time.Duration;
import java.time.Instant;

public abstract class BattlePhaseManagerImpl<T extends Enum<T> & IBattlePhase<T>> implements IBattlePhaseManager<T> {
    private T phase;
    private int progress;
    private Instant startTime;

    public BattlePhaseManagerImpl(T phase, int progress, Instant startTime) {
        this.phase = phase;
        this.progress = progress;
        this.startTime = startTime;
    }

    @Override
    public T get() {
        return phase;
    }

    @Override
    public void set(T phase) throws BattlePhaseSwitchException {
        this.phase = phase;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public void setProgress(int val) {
        if (val > 100) { // TODO Use phase progress max here
            val = 100;
        } else if (val < 0) {
            val = 0;
        }

        progress = val;
    }

    @Override
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Instant instant) {
        startTime = instant;
    }

    @Override
    public Duration getElapsedTime() {
        return Duration.between(Instant.now(), getStartTime());
    }
}
