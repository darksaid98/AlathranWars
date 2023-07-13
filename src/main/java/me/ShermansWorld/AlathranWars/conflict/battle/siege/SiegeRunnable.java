package me.ShermansWorld.AlathranWars.conflict.battle.siege;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathranWars.utility.UtilsChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/*
public class SiegeRunnable implements Runnable{
    */
/**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     *//*

    @Override
    public void run() {
        if (homeBlock != null) {
            town.setHomeBlock(homeBlock);
            town.setSpawn(townSpawn);
        }

        // LMFAO Who cares about performance anyways
        if (Siege.this.side1AreAttackers) {
            Siege.this.attackerPlayers = Siege.this.war.getSide1().getPlayers();
            Siege.this.defenderPlayers = Siege.this.war.getSide2().getPlayers();
        } else {
            Siege.this.attackerPlayers = Siege.this.war.getSide2().getPlayers();
            Siege.this.defenderPlayers = Siege.this.war.getSide1().getPlayers();
        }

        if (Siege.this.siegeTicks >= MAX_SIEGE_TICKS) {

            Bukkit.getServer().getScheduler().cancelTask(Siege.this.bukkitId[0]);

            Siege.this.war.getSieges().remove(Siege.this);
            if (Siege.this.attackerPoints > Siege.this.defenderPoints) {
                Siege.this.attackersWin(Siege.this.siegeLeader);
            } else {
                Siege.this.defendersWin();
            }

        } else {

            boolean attackersAreOnHomeBlock = false;
            boolean defendersAreOnHomeBlock = false;
            siegeTicks = siegeTicks + 200;
            for (final Player pl : Siege.this.attackerPlayers) {
                try {
                    if (pl != null) {
                        WorldCoord wc = WorldCoord.parseWorldCoord(pl);
                        if (wc.getTownBlock().getTown().equals(town)) {
                            if (town.getHomeBlockOrNull() != null) {
                                if (town.getHomeBlockOrNull().getWorldCoord().equals(wc)) {
                                    if (!pl.isDead()) {
                                        if ((Math.abs(pl.getLocation().getBlockY() - townSpawn.getBlockY())) < 10) {
                                            attackersAreOnHomeBlock = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (NotRegisteredException ignored) {
//                            Main.warLogger.log(UtilsChat.getPrefix() + "Attempting to look for town at " + WorldCoord.parseWorldCoord(pl));
                }
            }
            for (final Player pl : Siege.this.defenderPlayers) {
                try {
                    if (pl != null) {
                        WorldCoord wc = WorldCoord.parseWorldCoord(pl);
                        if (wc.getTownBlock().getTown().equals(town)) {
                            if (town.getHomeBlockOrNull() != null) {
                                if (town.getHomeBlockOrNull().getWorldCoord().equals(wc)) {
                                    if (!pl.isDead()) {
                                        if ((Math.abs(pl.getLocation().getBlockY() - townSpawn.getBlockY())) <= 10) {
                                            defendersAreOnHomeBlock = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (NotRegisteredException ignored) {
//                            Main.warLogger.log(UtilsChat.getPrefix() + "Attempting to look for town at " + WorldCoord.parseWorldCoord(pl));
                }
            }
            if (attackersAreOnHomeBlock && defendersAreOnHomeBlock) { // Contested

                if (this.homeBlockControl != 1) {
                    for (final Player pl : Siege.this.attackerPlayers) {
                        try {
                            pl.sendMessage(UtilsChat.getPrefix()
                                + "HomeBlock at " + Siege.this.town.getName() + " contested!");
                        } catch (NullPointerException ignored) {
                        }
                    }
                    for (final Player pl : Siege.this.defenderPlayers) {
                        try {
                            pl.sendMessage(UtilsChat.getPrefix()
                                + "HomeBlock at " + Siege.this.town.getName() + " contested!");
                        } catch (NullPointerException ignored) {
                        }
                    }
                }
                this.homeBlockControl = 1;

            } else if (attackersAreOnHomeBlock && !defendersAreOnHomeBlock) { // Attackers

                if (this.homeBlockControl != 2) {
                    for (final Player pl : Siege.this.attackerPlayers) {
                        try {
                            pl.sendMessage(UtilsChat.getPrefix()
                                + "Attackers have captured the HomeBlock at "
                                + Siege.this.town.getName() + "! +1 Attacker Points per second");
                        } catch (NullPointerException ignored) {
                        }
                    }
                    for (final Player pl : Siege.this.defenderPlayers) {
                        try {
                            pl.sendMessage(UtilsChat.getPrefix()
                                + "Attackers have captured the HomeBlock at "
                                + Siege.this.town.getName() + "! +1 Attacker Points per second");
                        } catch (NullPointerException ignored) {
                        }
                    }
                }

                this.homeBlockControl = 2;
                Siege.this.addPointsToAttackers(10);

            } else { // Defenders / None

                if (this.homeBlockControl != 3) {
                    for (final Player pl : Siege.this.attackerPlayers) {
                        try {
                            pl.sendMessage(UtilsChat.getPrefix()
                                + "Defenders retain control of the HomeBlock at "
                                + Siege.this.town.getName() + "! +1 Defender Points per second");
                        } catch (NullPointerException ignored) {
                        }
                    }
                    for (final Player pl : Siege.this.defenderPlayers) {
                        try {
                            pl.sendMessage(UtilsChat.getPrefix()
                                + "Defenders retain control of the HomeBlock at "
                                + Siege.this.town.getName() + "! +1 Defender Points per second");
                        } catch (NullPointerException ignored) {
                        }
                    }
                }

                Siege.this.addPointsToDefenders(10);
                this.homeBlockControl = 3;

            }

            if (Siege.this.siegeTicks % 6000 == 0) { // Updates every 5 minutes
                Bukkit.broadcastMessage(UtilsChat.getPrefix() + "Report on the siege of "
                    + Siege.this.town.getName() + ":");
                Bukkit.broadcastMessage(
                    "Attacker Points - " + Siege.this.attackerPoints);
                Bukkit.broadcastMessage(
                    "Defender Points - " + Siege.this.defenderPoints);
            }

                    if (siegeTicks % 1200 == 0) { // Saves every minute
                        save();
                    }
        }

        refreshDisplayBar();
    }
}
*/
