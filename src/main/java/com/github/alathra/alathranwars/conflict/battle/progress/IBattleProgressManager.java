package com.github.alathra.alathranwars.conflict.battle.progress;

import org.jetbrains.annotations.ApiStatus;

public interface IBattleProgressManager {
    /**
     * Get the current battle progress
     * @return the current progress
     */
    int get();

    /**
     * Set the current battle progress
     * @param val the new progress (Cannot be higher than max or lower than 0)
     */
    @ApiStatus.Internal
    void set(int val);

    /**
     * Get the current battles maximum progress
     * @return the maximum progress this battle can reach
     */
    int getMax();
}
