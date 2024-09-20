package com.github.alathra.alathranwars.conflict.battle.progress;

public abstract class BattleProgressManagerImpl implements IBattleProgressManager {
    private int progress;
    private final int maxProgress;

    public BattleProgressManagerImpl(int progress, int maxProgress) {
        this.progress = progress;
        this.maxProgress = maxProgress;
    }

    @Override
    public int get() {
        return progress;
    }

    @Override
    public void set(int val) {
        if (val > getMax()) {
            val = getMax();
        } else if (val < 0) {
            val = 0;
        }

        progress = val;
    }

    @Override
    public int getMax() {
        return maxProgress;
    }
}
