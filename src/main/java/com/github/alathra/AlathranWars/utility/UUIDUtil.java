package com.github.alathra.AlathranWars.utility;

import com.github.alathra.AlathranWars.holder.WarManager;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UUIDUtil {
    public static UUID generateWarUUID() {
        UUID uuid = UUID.randomUUID();

        while (WarManager.getInstance().getWar(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    public static UUID generateSideUUID(@Nullable UUID previousSideUUID) {
        UUID uuid = UUID.randomUUID();

        while (WarManager.getInstance().getSide(uuid) != null || uuid == previousSideUUID) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }

    public static UUID generateSiegeUUID() {
        UUID uuid = UUID.randomUUID();

        while (WarManager.getInstance().getSiege(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return uuid;
    }
}
