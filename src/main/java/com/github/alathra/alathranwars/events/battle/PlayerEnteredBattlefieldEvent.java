package com.github.alathra.alathranwars.events.battle;

import com.github.alathra.alathranwars.conflict.battle.Battle;
import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.enums.battle.BattleSide;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerEnteredBattlefieldEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player player;
    private final War war;
    private final Battle battle;
    private final BattleSide battleSide;

    public PlayerEnteredBattlefieldEvent(Player player, War war, Battle battle, BattleSide battleSide) {
        this.player = player;
        this.war = war;
        this.battle = battle;
        this.battleSide = battleSide;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Player getPlayer() {
        return player;
    }

    public War getWar() {
        return war;
    }

    public Battle getBattle() {
        return battle;
    }

    public BattleSide getBattleSide() {
        return battleSide;
    }
}
