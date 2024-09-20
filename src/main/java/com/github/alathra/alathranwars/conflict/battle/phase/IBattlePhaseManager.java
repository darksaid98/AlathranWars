package com.github.alathra.alathranwars.conflict.battle.phase;

import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.time.Instant;

public interface IBattlePhaseManager<T extends Enum<T> & IBattlePhase<T>> {
    /**
     * Get the current battle phase
     * @return the phase
     */
    T get();

    /**
     * Set the current battle phase
     * @param phase the phase
     * @throws BattlePhaseSwitchException Thrown if unable to switch phase
     */
    @ApiStatus.Internal
    void set(T phase) throws BattlePhaseSwitchException;

    /**
     * Goes to the next battle phase if possible
     * @throws BattlePhaseSwitchException Thrown if unable to switch phase
     */
    default T next() throws BattlePhaseSwitchException {
        return get().next();
    };

    /**
     * Goes to the previous battle phase if possible
     * @throws BattlePhaseSwitchException Thrown if unable to switch phase
     */
    default T previous() throws BattlePhaseSwitchException {
        return get().previous();
    };

    /**
     * Get the current phase progress
     * @return integer from 0 to 100
     */
    int getProgress();

    /**
     * Set the current phase progress
     * @param val integer from 0 to 100
     */
    @ApiStatus.Internal
    void setProgress(int val);

    /**
     * Get the time at which this phase started
     * @return the instant at which this phase started
     */
    Instant getStartTime();

    /**
     * Set the time of when this phase started
     * @param instant the instant at which this phase started
     */
    @ApiStatus.Internal
    void setStartTime(Instant instant);

    /**
     * Get the duration between now and when phase started
     * @return duration between now and start time
     */
    Duration getElapsedTime();
}
