package com.github.alathra.alathranwars.utility;


import com.github.alathra.alathranwars.AlathranWars;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

/**
 * A class that provides shorthand access to {@link AlathranWars#getComponentLogger}.
 */
public class Logger {
    /**
     * Get component logger. Shorthand for:
     *
     * @return the component logger {@link AlathranWars#getComponentLogger}.
     */
    @NotNull
    public static ComponentLogger get() {
        return AlathranWars.getInstance().getComponentLogger();
    }
}
