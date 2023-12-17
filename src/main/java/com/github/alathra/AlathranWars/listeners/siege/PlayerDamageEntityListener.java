package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.utility.Utils;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamageEntityEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public class PlayerDamageEntityListener implements Listener {
    private final List<EntityType> typeWhitelist = Arrays.asList(
        EntityType.PLAYER,
        EntityType.HORSE,
        EntityType.DONKEY,
        EntityType.PIG,
        EntityType.BOAT,
        EntityType.MINECART,
        EntityType.MINECART_TNT
    )

    /**
     * Always allow damaging certain entities withing the siege zone (mounts and dogs)
     *
     * @param e the e
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(TownyPlayerDamageEntityEvent e) {
        Siege siege = Utils.getClosestSiege(e.getAttackingPlayer(), true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(e.getAttackingPlayer(), siege)) return;

        EntityType type = e.getEntity().getType();
        if (typeWhitelist.contains(type)) {
            e.setCancelled(false); // Force un-cancel event
        }
    }
}
