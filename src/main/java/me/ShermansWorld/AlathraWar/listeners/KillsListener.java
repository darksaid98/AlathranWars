package me.ShermansWorld.AlathraWar.listeners;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.UUIDFetcher;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class KillsListener implements Listener
{

    public static final HashMap<UUID, Raid> respawnqueue = new HashMap<>();

    @EventHandler
    public void onPlayerKilled(final PlayerDeathEvent event) {

        final Player killed = event.getEntity();
        final Player killer = event.getEntity().getKiller();
        Town town = null;
        try {
            town = WorldCoord.parseWorldCoord(killed).getTownBlock().getTown();
        }
        catch (NotRegisteredException ex2) {}
        boolean playerCloseToHomeBlockSiege = false;
        boolean playerCloseToHomeBlockRaid = false;

        try {
            //Raid logic
            for (final Raid raid : RaidData.getRaids()) {
                final int homeBlockXCoordRaided = raid.getRaidedTown().getHomeBlock().getCoord().getX() * 16;
                final int homeBlockZCoordRaided = raid.getRaidedTown().getHomeBlock().getCoord().getZ() * 16;
//                final int homeBlockXCoordGather = raid.getGatherTown().getHomeBlock().getCoord().getX() * 16;
//                final int homeBlockZCoordGather = raid.getGatherTown().getHomeBlock().getCoord().getZ() * 16;
                //Carryover from sieges
                if (Math.abs(killed.getLocation().getBlockX() - homeBlockXCoordRaided) <= 200 && Math.abs(killed.getLocation().getBlockZ() - homeBlockZCoordRaided) <= 200) {
                    playerCloseToHomeBlockRaid = true;
                }

                if (raid.getDefenders().contains(killed.getName()) && ((town != null && town.equals(raid.getRaidedTown())) || playerCloseToHomeBlockRaid)) {
                    //There is seperate behavior if combat hasnt started
                    if (raid.getPhase() == RaidPhase.COMBAT && killer != null) {
                        if (raid.getActiveRaiders().contains(killer.getName())) {
                            this.raidKill(killed, event);
                            raid.defenderKilledInCombat(event);
                        }
                    }
                    //teleport back to raided spawn, without damaged gear
                    this.oocKill(killed, event);
                    raid.defenderKilledOutofCombat(event);
                    return;
                }
                if (raid.getActiveRaiders().contains(killed.getName()) && ((town != null && town.equals(raid.getRaidedTown())) || playerCloseToHomeBlockRaid)) {
                    //There is seperate behavior if combat hasnt started
                    if (raid.getPhase() == RaidPhase.COMBAT && killer != null) {
                        if (raid.getDefenders().contains(killer.getName())) {
                            this.raidKill(killed, event);
                            raid.raiderKilledInCombat(event);
                            respawnqueue.put(killed.getUniqueId(), raid);
                        }
                    }
                    //teleport back to gather town spawn, without damaged gear
                    //this is to disincentive people prekilling
                    this.oocKill(killed, event);
                    raid.raiderKilledOutofCombat(event);
                    respawnqueue.put(killed.getUniqueId(), raid);
                    return;
                }
                if ( (town != null && town.equals(raid.getRaidedTown()) || playerCloseToHomeBlockRaid) ) {
                    this.siegeKill(killed, event);
                }
            }


            if(killer == null) return;

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
                                Player p = Bukkit.getPlayer(playerName);
                                if (p != null) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! + 20 Attacker Points");
                            }
                            catch (NullPointerException ignored) {}
                        }
                        for (final String playerName : siege.getDefenderPlayers()) {
                            try {
                                Player p = Bukkit.getPlayer(playerName);
                                if (p != null) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! + 20 Attacker Points");
                            }
                            catch (NullPointerException ignored) {}
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
                                Player p = Bukkit.getPlayer(playerName);
                                if (p != null) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Attacker killed! + 20 Defender Points");
                            }
                            catch (NullPointerException ex3) {}
                        }
                        for (final String playerName : siege.getDefenderPlayers()) {
                            try {
                                Player p = Bukkit.getPlayer(playerName);
                                if (p != null) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Attacker killed! + 20 Defender Points");
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

        }
        catch (NullPointerException | TownyException ignored) {

        }
    }

    /**
     * Runs last so that it overrides everyone else!
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        if(respawnqueue.containsKey(event.getPlayer().getUniqueId())) {
            Raid raid = respawnqueue.get(event.getPlayer().getUniqueId());
            if (raid == null) return;
            if(raid.getActiveRaiders().contains(event.getPlayer().getName())) {
                //do raider respawn
                try {
                    event.setRespawnLocation(raid.getGatherTown().getSpawn());
                    event.getPlayer().sendMessage(String.valueOf(Helper.Chatlabel()) + "You died " + (raid.getPhase().equals(RaidPhase.COMBAT) ? "raiding" : "before combat") + " and have been teleported back to the gather point.");
                } catch (TownyException e) {
                    throw new RuntimeException(e);
                }
            } else if(raid.getDefenders().contains(event.getPlayer().getName())) {
                //do defender respawn ?
            } else {
                //invalid code
            }
            respawnqueue.remove(event.getPlayer().getUniqueId());
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
