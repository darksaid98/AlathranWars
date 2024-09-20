package com.github.alathra.alathranwars.conflict.battle.phase;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Used to implement common methods in battle phase enums
 * @param <T> the Enum
 */
public interface IBattlePhase<T extends Enum<T> & IBattlePhase<T>> {
    int getPhaseIdentifier();

    /**
     * Uses {@link #next(Enum)} to get the next phase
     * @return a phase enum value
     * @throws BattlePhaseSwitchException Thrown if no next phase can be found
     */
    T next() throws BattlePhaseSwitchException;

    /**
     * Uses {@link #previous(Enum)} to get the previous phase
     * @return a phase enum value
     * @throws BattlePhaseSwitchException Thrown if no previous phase can be found
     */
    T previous() throws BattlePhaseSwitchException;

    /**
     * Get the next phase after the provided one
     * @param phase a phase enum value
     * @return a phase enum value
     * @throws BattlePhaseSwitchException Thrown if no next phase can be found
     */
    default T next(T phase) throws BattlePhaseSwitchException {
        final int currentPhase = phase.getPhaseIdentifier();

        // Find the next enum constant by incrementing until a valid enum constant is found
        int increment = 0;
        for (int i = 1; i < 6; i++) {
            if (fromIdentifier(currentPhase + i) != null) {
                increment = i;
                break;
            }
        }

        // Failed to find new phase
        if (increment == 0)
            throw new BattlePhaseSwitchException("No later phase could be found.");

        return fromIdentifier(currentPhase + increment);
    }

    /**
     * Get the previous phase to the provided one
     * @param phase a phase enum value
     * @return a phase enum value
     * @throws BattlePhaseSwitchException Thrown if no previous phase can be found
     */
    default T previous(T phase) throws BattlePhaseSwitchException {
        final int currentPhase = phase.getPhaseIdentifier();
        if (currentPhase - 1 < 1)
            throw new BattlePhaseSwitchException("No previous phase could be found.");

        // Find the previous enum constant by decrementing until a valid enum constant is found
        int decrement = 0;
        for (int i = 1; i < 6; i++) {
            if (fromIdentifier(currentPhase - i) != null) {
                decrement = i;
                break;
            }
        }

        // Failed to find new phase
        if (decrement == 0)
            throw new BattlePhaseSwitchException("No previous phase could be found.");

        return fromIdentifier(currentPhase - decrement);
    }

    /**
     * Get a phase from the phase identifier
     * @param identifier the phase identifier
     * @return a phase enum value or null if none was found
     */
    @Nullable
    default T fromIdentifier(final int identifier) {
        if (identifier < 1)
            return null;

        return Arrays.stream(getDeclaringClass().getEnumConstants())
            .filter(phase -> phase.getPhaseIdentifier() == identifier)
            .findFirst()
            .orElse(null);
    }

    /**
     * Don't override this method, it is implemented in all enums by default.
     */
    Class<T> getDeclaringClass();
}
