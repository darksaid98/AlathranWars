package com.github.alathra.alathranwars.events;

import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.enums.WarDeleteReason;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WarDeleteEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private War war;
    private WarDeleteReason reason;

    public WarDeleteEvent(War war, WarDeleteReason reason) {
        this.war = war;
        this.reason = reason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public War getWar() {
        return war;
    }

    public WarDeleteReason getReason() {
        return reason;
    }
}
