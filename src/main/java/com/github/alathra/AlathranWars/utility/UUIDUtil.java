package com.github.alathra.AlathranWars.utility;

import com.github.alathra.AlathranWars.holder.WarManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UUIDUtil {
    public static @NotNull UUID generateWarUUID() {
        @NotNull UUID uuid = UUID.randomUUID();

        while (WarManager.getInstance().getWar(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    public static @NotNull UUID generateSideUUID(@Nullable UUID previousSideUUID) {
        @NotNull UUID uuid = UUID.randomUUID();

        while (WarManager.getInstance().getSide(uuid) != null || uuid == previousSideUUID) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    public static @NotNull UUID generateSiegeUUID() {
        @NotNull UUID uuid = UUID.randomUUID();

        while (WarManager.getInstance().getSiege(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }
}
