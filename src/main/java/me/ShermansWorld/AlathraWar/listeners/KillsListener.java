package me.ShermansWorld.AlathraWar.listeners;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathraWar.deprecated.OldRaid;
import me.ShermansWorld.AlathraWar.deprecated.OldSiege;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.utility.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.UUID;

public final class KillsListener implements Listener {

    public static final HashMap<UUID, OldRaid> respawnQueue = new HashMap<>();

    @EventHandler
    public void onPlayerKilled(final PlayerDeathEvent event) {

        final Player killed = event.getEntity();
        final Player killer = event.getEntity().getKiller();
        Town town = null;
        try {
            town = WorldCoord.parseWorldCoord(killed).getTownBlock().getTown();
        } catch (NotRegisteredException ex2) {
            ex2.printStackTrace();
        }
        boolean playerCloseToHomeBlockSiege = false;
        boolean playerCloseToHomeBlockRaid = false;

        try {
            //OldRaid logic
            for (final OldRaid oldRaid : RaidData.getRaids()) {
                final int homeBlockXCoordRaided = oldRaid.getRaidedTown().getHomeBlock().getCoord().getX() * 16;
                final int homeBlockZCoordRaided = oldRaid.getRaidedTown().getHomeBlock().getCoord().getZ() * 16;
//                final int homeBlockXCoordGather = oldRaid.getGatherTown().getHomeBlock().getCoord().getX() * 16;
//                final int homeBlockZCoordGather = oldRaid.getGatherTown().getHomeBlock().getCoord().getZ() * 16;
                //Carryover from sieges
                if (Math.abs(killed.getLocation().getBlockX() - homeBlockXCoordRaided) <= 200 && Math.abs(killed.getLocation().getBlockZ() - homeBlockZCoordRaided) <= 200) {
                    playerCloseToHomeBlockRaid = true;
                }

                if (oldRaid.getDefenderPlayers().contains(killed.getName()) && ((town != null && town.equals(oldRaid.getRaidedTown())) || playerCloseToHomeBlockRaid)) {
                    //There is seperate behavior if combat hasnt started
                    if (oldRaid.getPhase() == RaidPhase.COMBAT && killer != null) {
                        if (oldRaid.getActiveRaiders().contains(killer.getName())) {
                            this.raidKill(killed, event);
                            oldRaid.defenderKilledInCombat(event);
                            return;
                        }
                    }
                    //teleport back to raided spawn, without damaged gear
                    this.oocKill(killed, event);
                    oldRaid.defenderKilledOutofCombat(event);
                    return;
                }
                if (oldRaid.getActiveRaiders().contains(killed.getName()) && ((town != null && town.equals(oldRaid.getRaidedTown())) || playerCloseToHomeBlockRaid)) {
                    //There is seperate behavior if combat hasnt started
                    if (oldRaid.getPhase() == RaidPhase.COMBAT && killer != null) {
                        if (oldRaid.getDefenderPlayers().contains(killer.getName())) {
                            this.raidKill(killed, event);
                            oldRaid.raiderKilledInCombat(event);
                            respawnQueue.put(killed.getUniqueId(), oldRaid);
                            return;
                        }
                    } else if (oldRaid.getPhase() != RaidPhase.COMBAT && killer != null) {
                        this.oocKill(killed, event);
                        oldRaid.raiderKilledOutofCombat(event);
                        respawnQueue.put(killed.getUniqueId(), oldRaid);
                        return;
                    }
                    //teleport back to gather town spawn, without damaged gear
                    //this is to disincentive people prekilling
                    this.oocKill(killed, event);
                    oldRaid.raiderKilledOutofCombat(event);
                    respawnQueue.put(killed.getUniqueId(), oldRaid);
                    return;
                }
                if ((town != null && town.equals(oldRaid.getRaidedTown()) || playerCloseToHomeBlockRaid)) {
                    this.raidKill(killed, event);
                    return;
                }
            }


            if (killer == null) return;

            for (final OldSiege oldSiege : SiegeData.getSieges()) {
                final int homeBlockXCoord = oldSiege.getTown().getHomeBlock().getCoord().getX() * 16;
                final int homeBlockZCoord = oldSiege.getTown().getHomeBlock().getCoord().getZ() * 16;
                if (Math.abs(killed.getLocation().getBlockX() - homeBlockXCoord) <= 300 && Math.abs(killed.getLocation().getBlockZ() - homeBlockZCoord) <= 300) {
                    playerCloseToHomeBlockSiege = true;
                }
                if (oldSiege.getAttackerPlayers().contains(killer.getName()) && ((town != null && town.equals(oldSiege.getTown())) || playerCloseToHomeBlockSiege)) {
                    if (oldSiege.getDefenderPlayers().contains(killed.getName())) {
                        oldSiege.addPointsToAttackers(20);
                        for (final String playerName : oldSiege.getAttackerPlayers()) {
                            try {
                                Player p = Bukkit.getPlayer(playerName);
                                if (p != null)
                                    p.sendMessage(UtilsChat.getPrefix() + "Defender killed! + 20 Attacker Points");
                            } catch (NullPointerException ignored) {
                                ignored.printStackTrace();
                            }
                        }
                        for (final String playerName : oldSiege.getDefenderPlayers()) {
                            try {
                                Player p = Bukkit.getPlayer(playerName);
                                if (p != null)
                                    p.sendMessage(UtilsChat.getPrefix() + "Defender killed! + 20 Attacker Points");
                            } catch (NullPointerException ignored) {
                                ignored.printStackTrace();
                            }
                        }
                    }
                    this.siegeKill(killed, event);
                    return;
                }
                if (oldSiege.getDefenderPlayers().contains(killer.getName()) && ((town != null && town.equals(oldSiege.getTown())) || playerCloseToHomeBlockSiege)) {
                    if (oldSiege.getAttackerPlayers().contains(killed.getName())) {
                        oldSiege.addPointsToDefenders(20);
                        for (final String playerName : oldSiege.getAttackerPlayers()) {
                            try {
                                Player p = Bukkit.getPlayer(playerName);
                                if (p != null)
                                    p.sendMessage(UtilsChat.getPrefix() + "Attacker killed! + 20 Defender Points");
                            } catch (NullPointerException ex3) {
                                ex3.printStackTrace();
                            }
                        }
                        for (final String playerName : oldSiege.getDefenderPlayers()) {
                            try {
                                Player p = Bukkit.getPlayer(playerName);
                                if (p != null)
                                    p.sendMessage(UtilsChat.getPrefix() + "Attacker killed! + 20 Defender Points");
                            } catch (NullPointerException ex4) {
                                ex4.printStackTrace();
                            }
                        }
                    }
                    this.siegeKill(killed, event);
                    return;
                }
                if ((town != null && town.equals(oldSiege.getTown()) || playerCloseToHomeBlockSiege)) {
                    this.siegeKill(killed, event);
                }
            }

        } catch (NullPointerException | TownyException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Runs last so that it overrides everyone else!
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        if (respawnQueue.containsKey(event.getPlayer().getUniqueId())) {
            OldRaid oldRaid = respawnQueue.get(event.getPlayer().getUniqueId());
            if (oldRaid == null) return;
            if (oldRaid.getActiveRaiders().contains(event.getPlayer().getName())) {
                //do raider respawn
                try {
                    event.setRespawnLocation(oldRaid.getGatherTown().getSpawn());
                    event.getPlayer().sendMessage(UtilsChat.getPrefix() + "You died " + (oldRaid.getPhase().equals(RaidPhase.COMBAT) ? "raiding" : "before combat") + " and have been teleported back to the gather point.");
                } catch (TownyException e) {
                    throw new RuntimeException(e);
                }
            } else if (oldRaid.getDefenderSide().contains(event.getPlayer().getName())) {
                //do defender respawn ?
            } else {
                //invalid code
            }
            respawnQueue.remove(event.getPlayer().getUniqueId());
        }
    }


    /**
     * Damage all gear held by the player (and then send them to spawn?)
     * They don't lose items from death.
     *
     * @param killed killed player
     * @param event  event
     */
    private void siegeKill(final Player killed, final PlayerDeathEvent event) {
        //Helper
        Utils.damageAllGear(killed);

        //OldSiege specific
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
     * @param event  event
     */
    private void raidKill(final Player killed, final PlayerDeathEvent event) {
        //Helper
        Utils.damageAllGear(killed);

        //OldSiege specific
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
     * @param event  event
     */
    private void oocKill(final Player killed, final PlayerDeathEvent event) {
        //OldSiege specific
//        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + killed.getName());
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
    }

}
