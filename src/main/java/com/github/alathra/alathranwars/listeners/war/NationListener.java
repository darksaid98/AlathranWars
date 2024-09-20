package com.github.alathra.alathranwars.listeners.war;

import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.hook.NameColorHandler;
import com.palmergames.bukkit.towny.event.NationAddTownEvent;
import com.palmergames.bukkit.towny.event.NationPreRenameEvent;
import com.palmergames.bukkit.towny.event.nation.NationPreMergeEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NationListener implements Listener {
    /*@EventHandler
    private void onNationKingChange(NationKingChangeEvent e) {
        e.setCancelMessage("");
        e.setCancelled(true);
    }*/

    @EventHandler
    private void onRename(NationPreRenameEvent e) {
        Nation nation = e.getNation();

        if (WarController.getInstance().isInAnyWars(nation)) {
            e.setCancelMessage("You can't rename a nation while it's in a war.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onTownJoin(NationAddTownEvent e) {
        Nation nation = e.getNation();
        Town town = e.getTown();

        // Add town to all nation wars
        for (War war : WarController.getInstance().getWars(nation)) {
            Side side = war.getSide(nation);
            if (side == null) continue;

            side.add(town);
        }

        // TODO Add nation to towns' wars
        for (War war : WarController.getInstance().getWars(town)) {
            Side side = war.getSide(town);
            if (side == null) continue;

            side.add(nation);
        }

        for (War war : WarController.getInstance().getWars(nation)) {
            Side side = war.getSide(nation);
            if (side == null) continue;

            side.getPlayersOnline().forEach(p -> NameColorHandler.getInstance().calculatePlayerColors(p));
        }
    }

    /*@EventHandler // Do nothing, leaving a nation should not let you escape nation wars
    private void onTownLeave(NationTownLeaveEvent e) {

    }*/

    @EventHandler
    private void onMerge(NationPreMergeEvent e) {
        Nation nation = e.getRemainingNation();
        Nation nation1 = e.getNation();

        if (WarController.getInstance().isInAnyWars(nation) || WarController.getInstance().isInAnyWars(nation1)) {
            e.setCancelMessage("You can't merge nations while they are in a war.");
            e.setCancelled(true);
        }

        // TODO Allow merging nations in war, literally just ensure that towns are added to sieges and sides
    }
}
