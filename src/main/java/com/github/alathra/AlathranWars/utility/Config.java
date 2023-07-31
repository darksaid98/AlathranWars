package com.github.alathra.AlathranWars.utility;

import com.github.alathra.AlathranWars.Main;
import org.jetbrains.annotations.NotNull;

/**
 * Convenience class for accessing {@link com.github.alathra.AlathranWars.data.ConfigManager}
 */
public abstract class Config {
    public static @NotNull de.leonhard.storage.Config get() {
        return Main.getInstance().getConfigManager().getConfig();
    }
}