package me.ShermansWorld.AlathraWar.listeners;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class KillsListener implements Listener
{
    @EventHandler
    public void onPlayerKilled(final PlayerDeathEvent event) {

        final Player killed = event.getEntity();
        final Player killer = event.getEntity().getKiller();
        if (killed == null || killer == null) {
            return;
        }
        Town town = null;
        try {
            town = WorldCoord.parseWorldCoord(killed).getTownBlock().getTown();
        }
        catch (NotRegisteredException ex2) {}
        boolean playerCloseToHomeBlockSiege = false;
        boolean playerCloseToHomeBlockRaid = false;
        try {
            for (final Siege siege : SiegeData.getSieges()) {
                final int homeBlockXCoord = siege.getTown().getHomeBlock().getCoord().getX() * 16;
                final int homeBlockZCoord = siege.getTown().getHomeBlock().getCoord().getZ() * 16;
                if (Math.abs(killed.getLocation().getBlockX() - homeBlockXCoord) <= 300 && Math.abs(killed.getLocation().getBlockZ() - homeBlockZCoord) <= 300) {
                    playerCloseToHomeBlockSiege = true;
                }
                if (siege.getAttackerPlayers().contains(killer.getName()) && ((town != null && town.equals(siege.getTown())) || playerCloseToHomeBlockSiege)) {
                    if (siege.getDefenderPlayers().contains(killed.getName())) {
                        siege.addPointsToAttackers(20);
                        for (final String playerName : siege.getAttackerPlayers()) {
                            try {
                                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! + 20 Attacker Points");
                            }
                            catch (NullPointerException ex5) {}
                        }
                        for (final String playerName : siege.getDefenderPlayers()) {
                            try {
                                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! + 20 Attacker Points");
                            }
                            catch (NullPointerException ex6) {}
                        }
                    }
                    this.siegeKill(killed, event);
                    return;
                }
                if (siege.getDefenderPlayers().contains(killer.getName()) && ((town != null && town.equals(siege.getTown())) || playerCloseToHomeBlockSiege)) {
                    if (siege.getAttackerPlayers().contains(killed.getName())) {
                        siege.addPointsToDefenders(20);
                        for (final String playerName : siege.getAttackerPlayers()) {
                            try {
                                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Attacker killed! + 20 Defender Points");
                            }
                            catch (NullPointerException ex3) {}
                        }
                        for (final String playerName : siege.getDefenderPlayers()) {
                            try {
                                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Attacker killed! + 20 Defender Points");
                            }
                            catch (NullPointerException ex4) {}
                        }
                    }
                    this.siegeKill(killed, event);
                    return;
                }
                if ( (town != null && town.equals(siege.getTown()) || playerCloseToHomeBlockSiege) ) {
                    this.siegeKill(killed, event);
                }
            }


            //Raid logic
            for (final Raid raid : RaidData.getRaids()) {
                final int homeBlockXCoordRaided = raid.getRaidedTown().getHomeBlock().getCoord().getX() * 16;
                final int homeBlockZCoordRaided = raid.getRaidedTown().getHomeBlock().getCoord().getZ() * 16;
//                final int homeBlockXCoordGather = raid.getGatherTown().getHomeBlock().getCoord().getX() * 16;
//                final int homeBlockZCoordGather = raid.getGatherTown().getHomeBlock().getCoord().getZ() * 16;
                //Carryover from sieges
                if (Math.abs(killed.getLocation().getBlockX() - homeBlockXCoordRaided) <= 300 && Math.abs(killed.getLocation().getBlockZ() - homeBlockZCoordRaided) <= 300) {
                    playerCloseToHomeBlockRaid = true;
                }
                if (raid.getActiveRaiders().contains(killer.getName()) && ((town != null && town.equals(raid.getRaidedTown())) || playerCloseToHomeBlockRaid)) {
                    //There is seperate behavior if combat hasnt started
                    if (raid.getPhase() == RaidPhase.COMBAT) {
                        this.raidKill(killed, event);
                        if (raid.getDefenderPlayers().contains(killed.getName())) {
                            raid.defenderKilledInCombat(event);
                        }
                    } else {
                        //teleport back to raided spawn, without damaged gear
                        this.oocKill(killed, event);
                        raid.defenderKilledOutofCombat(event);
                    }
                    return;
                }
                if (raid.getDefenderPlayers().contains(killer.getName()) && ((town != null && town.equals(raid.getRaidedTown())) || playerCloseToHomeBlockRaid)) {
                    //There is seperate behavior if combat hasnt started
                    if (raid.getPhase() == RaidPhase.COMBAT) {
                        this.raidKill(killed, event);
                        if (raid.getActiveRaiders().contains(killed.getName())) {
                            raid.raiderKilledInCombat(event);
                        }
                    } else {
                        //teleport back to gather town spawn, without damaged gear
                        //this is to disincentive people prekilling
                        this.oocKill(killed, event);
                        raid.raiderKilledOutofCombat(event);
                    }
                    return;

                }
                if ( (town != null && town.equals(raid.getRaidedTown()) || playerCloseToHomeBlockRaid) ) {
                    this.siegeKill(killed, event);
                }
            }
        }
        catch (NullPointerException | TownyException ex7) {

        }
    }

    /**
     * Damage all gear held by the player (and then send them to spawn?)
     * They don't lose items from death.
     * @param killed killed player
     * @param event event
     */
    private void siegeKill(final Player killed, final PlayerDeathEvent event) {
        //Helper
        Helper.damageAllGear(killed);

        //Siege specific
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + killed.getName());
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
    }

    /**
     * Damage all gear held by the player (and then send them to spawn?)
     * They don't lose items from death.
     *
     * @param killed killed player
     * @param event event
     */
    private void raidKill(final Player killed, final PlayerDeathEvent event) {
        //Helper
        Helper.damageAllGear(killed);

        //Siege specific
//        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + killed.getName());
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
    }

    /**
     * Dont damage items held by the player
     * They don't lose items from death.
     *
     * @param killed killed player
     * @param event event
     */
    private void oocKill(final Player killed, final PlayerDeathEvent event) {
        //Siege specific
//        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + killed.getName());
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
    }

}
