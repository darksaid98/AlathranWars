package me.ShermansWorld.AlathraWar;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathraWar.commands.RaidCommands;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

/* RAID EXPLANATION

Raids are run by raiding parties, who gather at a separate town,
begin a raid, then go to the town. They then kill and control the
town over the duration of time, or until the raid is completed by
the raiders.

They then gain plunder in the form of money from the town's bank,
and then leave.

Raids require at least one person from a town to be online, can only be
done every 24 hours per raided town, every 6 hours in a war per side,
and raided players may run out of the town, forfeiting the cost of the raid.

The raid is split into two phases:
- STAGING PHASE
    - Raiders collect at raiding town, joining raid through command
    - Defenders are given time to get to the town.
- COMBAT PHASE
    - Raiders go to town
    - Combat occurs
    - Decides outcome of raid

TODO LIST:
- No teleporting by raiders
- Points management
    - Both sides gain points for kills
    - Raiders gain points for 
- Raiders on death are teleported to their town spawn.
*/

/**
 * This is based on the existing Siege Class
 * @author AubriTheHuman
 * @author NinjaMandalorian
 * @author ShermansWorld
 */
public class Raid {

    private War war;
    private Town town;
    private String raiders;
    private String defenders;
    private boolean side1AreRaiders;
    private boolean side2AreRaiders;
    private int id;
    private int raidScore;
    private int MAXRAIDTICKS;
    private Player owner;
    private int raidTicks;
    private TownBlock homeBlock;
    private RaidPhase phase;
    private Location townSpawn;
    int[] bukkitId;
    public ArrayList<String> raiderPlayers;
    public ArrayList<String> defenderPlayers;

    // Constructs raid for staging phase
    public Raid(final int id, final War war, final Town town, final String raiders, final String defenders,
                 final boolean side1AreRaiders, final boolean side2AreRaiders) {

        this.raidTicks = 0;
        this.bukkitId = new int[1];
        this.raiderPlayers = new ArrayList<String>();
        this.defenderPlayers = new ArrayList<String>();
        this.war = war;
        this.town = town;
        this.raiders = raiders;
        this.id = id;

    }

    public void start() {

        //start
        this.phase = RaidPhase.START;

        this.raidScore = Main.siegeData.getConfig().getInt("Raids." + String.valueOf(this.id) + ".raidscore");
        this.side1AreRaiders = Main.siegeData.getConfig()
                .getBoolean("Raids." + String.valueOf(this.id) + ".side1areraiders");
        this.side2AreRaiders = Main.siegeData.getConfig()
                .getBoolean("Raids." + String.valueOf(this.id) + ".side2areraiders");
        this.MAXRAIDTICKS = 72000;
        this.raidTicks = Main.siegeData.getConfig().getInt("Raids." + String.valueOf(this.id) + ".raidticks");
        this.owner = Bukkit.getPlayer(Main.siegeData.getConfig().getString("Raids." + String.valueOf(this.id) + ".owner"));

        if (this.side1AreRaiders) {
            this.raiderPlayers = this.war.getSide1Players();
            this.defenderPlayers = this.war.getSide2Players();
        } else {
            this.raiderPlayers = this.war.getSide2Players();
            this.defenderPlayers = this.war.getSide1Players();
        }

        // Sets homeBlock and spawn for town. (So no mid-raid changes.)
        try {
            homeBlock = town.getHomeBlock();
            townSpawn = town.getSpawn();
        } catch (TownyException e) {
            e.printStackTrace();
        }

        // Creates 10 second looping function for Raid
        this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) Main.getInstance(),
                (Runnable) new Runnable() {

                @Override
                public void run() {
                    if (homeBlock != null) {
                        town.setHomeBlock(homeBlock);
                        town.setSpawn(townSpawn);
                    }
                    if (Raid.this.side1AreRaiders) {
                        Raid.this.raiderPlayers = Raid.this.war.getSide1Players();
                        Raid.this.defenderPlayers = Raid.this.war.getSide2Players();
                    } else {
                        Raid.this.raiderPlayers = Raid.this.war.getSide2Players();
                        Raid.this.defenderPlayers = Raid.this.war.getSide1Players();
                    }
                    if (Raid.this.raidTicks >= Raid.this.MAXRAIDTICKS) {
                        Bukkit.getServer().getScheduler().cancelTask(Raid.this.bukkitId[0]);
                        //TODO: fix raid scoring
                        if (Raid.this.raidScore > 750) {
                            Raid.this.raidersWin(Raid.this.owner);
                        } else {
                            Raid.this.defendersWin();
                        }
                    } else {
                        final Raid this$0 = Raid.this;
                        Raid.access$7(this$0, this$0.raidTicks + 200);
                        Main.raidData.getConfig().set("Raids." + String.valueOf(Raid.this.id) + ".raidticks",
                                (Object) Raid.this.raidTicks);
                        Main.raidData.saveConfig();
                    @Override
                    public void run() {
                        if (homeBlock != null) {
                            town.setHomeBlock(homeBlock);
                            town.setSpawn(townSpawn);
                        }

                        // If time runs out, stops scheduled task.
                        if (Raid.this.raidTicks >= Raid.this.MAXRAIDTICKS) {
                            Bukkit.getServer().getScheduler().cancelTask(Raid.this.bukkitId[0]);
                            if (Raid.this.raiderPoints > Raid.this.defenderPoints) {
                                Raid.this.raidersWin(Raid.this.owner);
                            } else {
                                Raid.this.defendersWin();
                            }
                        } else {
                            final Raid this$0 = Raid.this;
                            Raid.access$7(this$0, this$0.raidTicks + 200);
                            Main.raidData.getConfig().set("Raids." + String.valueOf(Raid.this.id) + ".raidticks",
                                    (Object) Raid.this.raidTicks);
                            Main.raidData.saveConfig();

                        //check and start travel phase
                        if (Raid.this.raidTicks >= RaidPhase.TRAVEL.startTick && Raid.this.phase != RaidPhase.TRAVEL) {
                            startTravel();
                        }

                        //check and start combat phase
                        if (Raid.this.raidTicks >= RaidPhase.COMBAT.startTick && Raid.this.phase != RaidPhase.COMBAT) {
                           startCombat();
                        }


                        //Raid start phase behavior
                        if (Raid.this.phase == RaidPhase.START) {
                            //update
                            if (Raid.this.raidTicks % 6000 == 0) {
                                Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid of "
                                        + Raid.this.town.getName() + " will begin in " + (RaidPhase.TRAVEL.startTick / 20 / 60) + " minutes!");
                                Bukkit.broadcastMessage(
                                        "The Raiders are gathering at " + TownyAPI.getInstance().getTownName(Raid.this.owner.getLocation()) + " before making the journey over!");
                            }

                        }
                        //Raid Travel phase behavior
                        else if (Raid.this.phase == RaidPhase.TRAVEL) {
                            //Check if a player has arrived at the town (in it) and if so start combat
                            for(String player : raiderPlayers) {
                                WorldCoord playercoord = WorldCoord.parseWorldCoord(Bukkit.getPlayer(player));
                                if(Raid.this.town.hasTownBlock(playercoord)) {
                                    startCombat();
                                }
                            }

                        }
                        //Raid combat phase behavior
                        else if (Raid.this.phase == RaidPhase.COMBAT) {

                            //Report
                            if (Raid.this.raidTicks % 6000 == 0) {
                                Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "Report on the raid of "
                                        + Raid.this.town.getName() + ":");
                                Bukkit.broadcastMessage(
                                        "Raid Score - " + String.valueOf(Raid.this.raidScore));
                            }
                        }
                    }
                }
            }, 0L, 200L);

    }

    // End of raid
    public void stop() {
        Bukkit.getScheduler().cancelTask(this.bukkitId[0]);
        RaidCommands.raids.remove(this);
        Main.raidData.getConfig().set("Raids." + String.valueOf(this.id), (Object) null);
        Main.raidData.saveConfig();
    }

    /**
     * Begin the Gather phase of the raid
     */
    private void startGather() {
        Raid.this.phase = RaidPhase.GATHER;
    }

    /**
     * Begin the Travel phase of the raid
     */
    private void startTravel() {
        Raid.this.phase = RaidPhase.TRAVEL;
        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The Raiders of "
                + (side1AreRaiders ? Raid.this.war.getSide1() : Raid.this.war.getSide2())
                + " are coming to raid "
                + Raid.this.town.getName() + "!");
        //in the case were not at that stage already, if this happens we have an issue
        Raid.this.raidTicks = RaidPhase.TRAVEL.startTick;
    }

    /**
     * Begin the combat phase of the raid
     */
    private void startCombat() {
        Raid.this.phase = RaidPhase.COMBAT;
        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The Raiders of "
                + (side1AreRaiders ? Raid.this.war.getSide1() : Raid.this.war.getSide2())
                + " have arrived at "
                + Raid.this.town.getName() + " and the fighting has begun!");
        //in the case were not at that stage already
        Raid.this.raidTicks = RaidPhase.COMBAT.startTick;
    }

    /**
     * Need to add call to KillsListener (defined seperate from Siege)
     */
    public void raiderKilled() {

        //award negative score

        //tp to town spawn
    }

    /**
     * Need to add call to KillsListener (defined seperate from Siege)
     */
    public void defenderKilled() {

        //award positive score
    }

    public void raidersWin(final Player owner) {

        stop();
    }

    public void defendersWin() {

        stop();
    }

    public void addPointsToRaidScore(final int points) {
        this.raidScore += points;
    }

    public void subtractPointsFromRaidScore(final int points) {
        this.raidScore -= points;
    }


    public int getID() {
        return this.id;
    }

    public void setID(final int id) {
        this.id = id;
    }

    public War getWar() {
        return this.war;
    }

    public void setWar(final War war) {
        this.war = war;
    }

    public Town getTown() {
        return this.town;
    }

    public void setTown(final Town town) {
        this.town = town;
    }

    public String getRaiders() {
        return this.raiders;
    }

    public void setRaiders(final String raiders) {
        this.raiders = raiders;
    }

    public String getDefenders() {
        return this.defenders;
    }

    public void setDefenders(final String defenders) {
        this.raiders = defenders;
    }

    public int getRaidScore() {
        return this.raidScore;
    }

    public void setRaidScore(final int raidScore) {
        this.raidScore = raidScore;
    }

    public boolean getSide1AreRaiders() {
        return this.side1AreRaiders;
    }

    public void setSide1AreRaiders(final boolean side1AreRaiders) {
        this.side1AreRaiders = side1AreRaiders;
    }

    public boolean getSide2AreRaiders() {
        return this.side2AreRaiders;
    }

    public void setSide2AreRaiders(final boolean side2AreRaiders) {
        this.side2AreRaiders = side2AreRaiders;
    }

    public ArrayList<String> getRaiderPlayers() {
        return this.raiderPlayers;
    }

    public void setRaiderPlayer(final ArrayList<String> raiderPlayers) {
        this.raiderPlayers = raiderPlayers;
    }

    public ArrayList<String> getDefenderPlayers() {
        return this.defenderPlayers;
    }

    public void setDefenderPlayer(final ArrayList<String> defenderPlayers) {
        this.defenderPlayers = defenderPlayers;
    }

    static /* synthetic */ void access$7(final Raid raid, final int raidTicks) {
        raid.raidTicks = raidTicks;
    }
}
