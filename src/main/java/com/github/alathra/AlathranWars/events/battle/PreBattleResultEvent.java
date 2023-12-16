package com.github.alathra.AlathranWars.events.battle;

import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.Battle;
import com.github.alathra.AlathranWars.enums.battle.BattleType;
import com.github.alathra.AlathranWars.enums.battle.BattleVictor;
import com.github.alathra.AlathranWars.enums.battle.BattleVictoryReason;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreBattleResultEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;
    private War war;
    private final Battle battle;
    private final BattleType battleType;
    private final BattleVictor battleVictor;
    private final BattleVictoryReason battleVictoryReason;

    public PreBattleResultEvent(War war, Battle battle, BattleType battleType, BattleVictor battleVictor, BattleVictoryReason battleVictoryReason) {
        this.war = war;
        this.battle = battle;
        this.battleType = battleType;
        this.battleVictor = battleVictor;
        this.battleVictoryReason = battleVictoryReason;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancelled true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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

    public Battle getBattle() {
        return battle;
    }

    public BattleType getBattleType() {
        return battleType;
    }

    public BattleVictor getBattleVictor() {
        return battleVictor;
    }

    public BattleVictoryReason getBattleVictoryReason() {
        return battleVictoryReason;
    }
}
