package me.ShermansWorld.AlathraWar.listeners;

import com.palmergames.bukkit.towny.event.nation.NationKingChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NationListener implements Listener {
    /*
     * NationPreTownLeaveEvent, NationPreTownKickEvent, NationPreMergeEvent
     *
     * NationKingChangeEvent
     * NationPreRenameEvent
     *
     *
     *
     * */

    // TODO Prevent nation rename during war, siege, raid

    @EventHandler
    private void onNationKingChange(NationKingChangeEvent e) {
        e.setCancelMessage("");
        e.setCancelled(true);
    }
}
