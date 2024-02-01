package com.github.alathra.AlathranWars.listeners.war;

import com.github.alathra.AlathranWars.conflict.war.side.Side;
import com.github.alathra.AlathranWars.conflict.war.War;
import com.github.alathra.AlathranWars.conflict.war.WarController;
import com.github.alathra.AlathranWars.hooks.NameColorHandler;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.TownPreRenameEvent;
import com.palmergames.bukkit.towny.event.town.TownKickEvent;
import com.palmergames.bukkit.towny.event.town.TownLeaveEvent;
import com.palmergames.bukkit.towny.event.town.TownPreMergeEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

public class TownListener implements Listener {
    @EventHandler
    private void onRename(TownPreRenameEvent e) {
        Town town = e.getTown();

        if (WarController.getInstance().isTownInAnyWars(town)) {
            e.setCancelMessage("You can't rename a town while it's in a war.");
            e.setCancelled(true);
        }

        // TODO Allow renaming in war, update some vars pretty much
    }

    @EventHandler
    private void onPlayerJoin(TownAddResidentEvent e) {
        Town town = e.getTown();
        Player p = e.getResident().getPlayer();
        if (p == null) return;

        for (War war : WarController.getInstance().getTownWars(town)) {
            Side side = war.getTownSide(town);
            if (side == null) continue;
            side.addPlayer(p);

            final Title warTitle = Title.title(
                ColorParser.of("<gradient:#D72A09:#B01F03><u><b>War")
                    .build(),
                ColorParser.of("<gray><i>You entered the war of <war>!")
                    .parseMinimessagePlaceholder("war", war.getLabel())
                    .build(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
            );
            final Sound warSound = Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.VOICE, 0.5f, 1.0F);

            p.showTitle(warTitle);
            p.playSound(warSound);
        }

        NameColorHandler.getInstance().calculatePlayerColors(p);
    }

    @EventHandler
    private void onPlayerLeave(TownLeaveEvent e) {
        Town town = e.getTown();
        Player p = e.getResident().getPlayer();
        if (p == null) return;

        for (War war : WarController.getInstance().getTownWars(town)) {
            Side side = war.getTownSide(town);
            if (side == null) continue;
            side.removePlayer(p);
        }

        NameColorHandler.getInstance().calculatePlayerColors(p);
    }

    @EventHandler
    private void onPlayerKick(TownKickEvent e) {
        Town town = e.getTown();
        Player p = e.getKickedResident().getPlayer();
        if (p == null) return;

        for (War war : WarController.getInstance().getTownWars(town)) {
            Side side = war.getTownSide(town);
            if (side == null) continue;
            side.removePlayer(p);
        }

        NameColorHandler.getInstance().calculatePlayerColors(p);
    }

    /*@EventHandler
    private void onLeaderChange(TownMayorChangedEvent e) {
        Town town = e.getTown();
        Resident newMayor = e.getNewMayor();
        Resident oldMayor = e.getOldMayor();

        if (e.isKingChange() && e.isNationCapital()) {

        }
    }

    @EventHandler
    private void onLeaderChangeSuccession(TownMayorChosenBySuccessionEvent e) {
        Town town = e.getTown();
        Resident newMayor = e.getNewMayor();
        Resident oldMayor = e.getOldMayor();

        if (e.isKingChange() && e.isNationCapital()) {
        }
    }*/

    @EventHandler
    private void onMerge(TownPreMergeEvent e) {
        Town town = e.getRemainingTown();
        Town town2 = e.getSuccumbingTown();

        if (WarController.getInstance().isTownInAnyWars(town) || WarController.getInstance().isTownInAnyWars(town2)) {
            e.setCancelMessage("You can't merge towns while they are in a war.");
            e.setCancelled(true);
        }

        // TODO Allow merging towns in war, literally just ensure that players are added to sieges and sides
    }

    // TODO On town ruin, bow town out of any active wars, if leader of war surrender
    // TODO On town ruin, leave or cancel sieges where the town is present
    // TODO On town ruin, leave or cancel raids where the town is present
    @EventHandler
    private void onRuin(TownRuinedEvent e) {
        Town town = e.getTown();

        for (War war : WarController.getInstance().getTownWars(town)) {
            Side side = war.getTownSide(town);
            if (side == null) continue;
            war.unsurrenderTown(town);
            side.removeTown(town);
            side.processSurrenders();
        }
    }
}
