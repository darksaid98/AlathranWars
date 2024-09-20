package com.github.alathra.alathranwars.utility;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.config.ConfigHandler;
import com.github.milkdrinkers.crate.Config;
import org.jetbrains.annotations.NotNull;

/**
 * Convenience class for accessing {@link ConfigHandler#getConfig}
 */
public abstract class Cfg {
    /**
     * Convenience method for {@link ConfigHandler#getConfig} to getConnection {@link Config}
     *
     * @return the config
     */
    @NotNull
    public static Config get() {
        return AlathranWars.getInstance().getConfigHandler().getConfig();
    }
}
