package com.github.alathra.alathranwars.conflict;

import com.github.alathra.alathranwars.conflict.war.War;

public interface IAssociatedWar {
    /**
     * Gets the associated war with this object from the war uuid
     * @return the associated war
     */
    War getWar();

    /**
     * Check whether a war is associated with this class
     * @param war the war to compare with
     * @return if the war is the one associated with this object
     */
    boolean equals(War war);
}
