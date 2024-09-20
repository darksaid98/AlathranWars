package com.github.alathra.alathranwars.conflict;

import java.util.UUID;

public interface IUnique<T extends IUnique<T>> {
    UUID getUUID();

    /**
     * Equals boolean.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    default boolean equals(UUID uuid) {
        return getUUID().equals(uuid);
    }

    /**
     * Equals boolean.
     *
     * @param obj the object implementing IUnique
     * @return the boolean
     */
    default boolean equals(T obj) {
        return getUUID().equals(obj.getUUID());
    }
}
