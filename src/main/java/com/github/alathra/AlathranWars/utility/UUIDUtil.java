package com.github.alathra.AlathranWars.utility;

import com.github.alathra.AlathranWars.conflict.WarController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Convenience class for generating UUID for different things in the plugin
 */
public abstract class UUIDUtil {
    public static @NotNull UUID generateWarUUID() {
        @NotNull UUID uuid = UUID.randomUUID();

        while (WarController.getInstance().getWar(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    public static @NotNull UUID generateSideUUID(@Nullable UUID previousSideUUID) {
        @NotNull UUID uuid = UUID.randomUUID();

        while (WarController.getInstance().getSide(uuid) != null || uuid == previousSideUUID) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    public static @NotNull UUID generateSiegeUUID() {
        @NotNull UUID uuid = UUID.randomUUID();

        while (WarController.getInstance().getSiege(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }
}
