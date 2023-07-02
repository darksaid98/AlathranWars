package me.ShermansWorld.AlathraWar.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.deprecated.OldWar;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.hooks.TABHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    public static void checkPlayer(Player p) {
        Resident resident = TownyAPI.getInstance().getResident(p);
        if (resident == null) return;

        Town town = resident.getTownOrNull();
        if (town == null) return;

        String townName = town.getName();
        boolean inWar = false;

        for (final OldWar oldWar : WarData.getWars()) {
            if (oldWar.getSide1Towns().contains(townName.toLowerCase())) {
                TABHook.assignSide1WarSuffix(p, oldWar);
                inWar = true;
            } else if (oldWar.getSide2Towns().contains(townName.toLowerCase())) {
                TABHook.assignSide2WarSuffix(p, oldWar);
                inWar = true;
            }
        }

        if (!inWar) {
            TABHook.resetPrefix(p);
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
    }
}
