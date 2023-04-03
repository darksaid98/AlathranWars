package me.ShermansWorld.AlathraWar;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.sun.tools.javac.util.Pair;
import me.ShermansWorld.AlathraWar.commands.RaidCommands;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
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
    v/ Raiders collect at raiding town, joining raid through command
    v/ Defenders are given time to get to the town. (travel is announced way in ahead)
- COMBAT PHASE
    v/ Raiders go to town
    v/ Combat occurs
    - Decides outcome of raid

TODO LIST:
v/ No teleporting by raiders (also defenders)
v/ Points management
    v/ Raider death is negative score
    v/ Defender death is positive score
    - TODO Raiders gain points for
v/ Raiders on death are teleported to their town spawn.
*/

/**
 * This is based on the existing Siege Class
 * @author AubriTheHuman
 * @author NinjaMandalorian
 * @author ShermansWorld
 */
public class Raid {

    private War war;
    private Town raidedTown;
    private Town gatherTown;
    private String raiders;
    private String defenders;
    private boolean side1AreRaiders;
    private boolean side2AreRaiders;
    private int id;
    private int raidScore;
    private Player owner;
    private int raidTicks;
    private TownBlock homeBlockRaided;
    private TownBlock homeBlockGather;
    private RaidPhase phase;
    private Location townSpawnRaided;
    private Location townSpawnGather;
    int[] bukkitId;
    public ArrayList<String> raiderPlayers;
    public ArrayList<String> defenderPlayers;
    public ArrayList<Pair<String,Boolean>> activeRaiders;

    // Constructs raid for staging phase
    public Raid(final int id, final War war, final Town raidedTown, final Town gatherTown, final String raiders, final String defenders,
                 final boolean side1AreRaiders, final boolean side2AreRaiders) {

        this.raidTicks = 0;
        this.bukkitId = new int[1];
        this.raiderPlayers = new ArrayList<String>();
        this.defenderPlayers = new ArrayList<String>();
        this.activeRaiders = new ArrayList<Pair<String,Boolean>>();
        this.war = war;
        this.raidedTown = raidedTown;
        this.gatherTown = gatherTown;
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
            homeBlockRaided = raidedTown.getHomeBlock();
            townSpawnRaided = raidedTown.getSpawn();
            homeBlockGather = gatherTown.getHomeBlock();
            townSpawnGather = gatherTown.getSpawn();
        } catch (TownyException e) {
            e.printStackTrace();
        }

        // Creates 10 second looping function for Raid
        this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) Main.getInstance(),
            (Runnable) new Runnable() {

                @Override
                public void run() {
                    if (homeBlockRaided != null) {
                        raidedTown.setHomeBlock(homeBlockRaided);
                        raidedTown.setSpawn(townSpawnRaided);
                    }
                    if (homeBlockGather != null) {
                        gatherTown.setHomeBlock(homeBlockGather);
                        gatherTown.setSpawn(townSpawnGather);
                    }
                    if (Raid.this.side1AreRaiders) {
                        Raid.this.raiderPlayers = Raid.this.war.getSide1Players();
                        Raid.this.defenderPlayers = Raid.this.war.getSide2Players();
                    } else {
                        Raid.this.raiderPlayers = Raid.this.war.getSide2Players();
                        Raid.this.defenderPlayers = Raid.this.war.getSide1Players();
                    }
                    if (Raid.this.raidTicks >= RaidPhase.END.startTick) {
                        Bukkit.getServer().getScheduler().cancelTask(Raid.this.bukkitId[0]);
                        Raid.this.phase = RaidPhase.END;
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


                        //check and start travel phase
                        if (Raid.this.raidTicks >= RaidPhase.TRAVEL.startTick && Raid.this.phase != RaidPhase.TRAVEL) {
                            startTravel();
                        }

                        //check and start combat phase
                        if (Raid.this.raidTicks >= RaidPhase.COMBAT.startTick && Raid.this.phase != RaidPhase.COMBAT) {
                            startCombat();
                        }


                        //Raid start phase behavior
                        if (Raid.this.phase == RaidPhase.GATHER) {
                            //Broadcast
                            if(raidTicks % 6000 == 0) {
                                Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid of "
                                        + Raid.this.getRaidedTown().getName() + " will begin in " + (int) (RaidPhase.TRAVEL.startTick / 20 / 60) + " minutes!");
                                Bukkit.broadcastMessage(
                                        "The Raiders are gathering at " + getGatherTown().getName() + " before making the journey over!");
                            }


                            //Prevent players from leaving gather town prematurely
                            for (String playerName : getActiveRaiders()) {
                                try {
                                    Player p = Bukkit.getPlayer(playerName);
                                    if(!WorldCoord.parseWorldCoord(p).getTownBlock().getTown().equals(Raid.this.getGatherTown())) {
                                        Raid.this.removeActiveRaider(p.getName());
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "By leaving the gathering town you have left the raid on " + Raid.this.getRaidedTown().getName() + "!");
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "To rejoin, do /raid join [war] [town]");

                                    }
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                } catch (NotRegisteredException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                        //Raid Travel phase behavior
                        else if (Raid.this.phase == RaidPhase.TRAVEL) {

                            if(raidTicks % 2400 == 0) {
                                Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The Raiders of "
                                        + (side1AreRaiders ? Raid.this.war.getSide1() : Raid.this.war.getSide2())
                                        + " are on their way to raid "
                                        + Raid.this.getRaidedTown().getName() + "!");
                            }

                                //Check if a player has arrived at the town (in it) and if so start combat
                            for (String player : raiderPlayers) {
                                WorldCoord playercoord = WorldCoord.parseWorldCoord(Bukkit.getPlayer(player));
                                if (Raid.this.getRaidedTown().hasTownBlock(playercoord)) {
                                    startCombat();
                                }
                            }

                        }
                        //Raid combat phase behavior
                        else if (Raid.this.phase == RaidPhase.COMBAT) {

                            //Report
                            if (Raid.this.raidTicks % 6000 == 0) {
                                Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "Report on the raid of "
                                        + Raid.this.getRaidedTown().getName() + ":");
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
        this.phase = RaidPhase.END;
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
                + Raid.this.getRaidedTown().getName() + "!");
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
                + Raid.this.getRaidedTown().getName() + " and the fighting has begun!");
        //in the case were not at that stage already
        Raid.this.raidTicks = RaidPhase.COMBAT.startTick;
    }

    /**
     * Use only during combat phase of raid
     * @param event
     */
    public void raiderKilledInCombat(PlayerDeathEvent event) {

        //award negative score
        this.subtractPointsFromRaidScore(20);
        for (final String playerName : this.getActiveRaiders()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Raider killed! -40 Raid Score");
            }
            catch (NullPointerException ex3) {}
        }
        for (final String playerName : this.getDefenderPlayers()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Raider killed! -20 Raid Score");
            }
            catch (NullPointerException ex4) {}
        }

        //tp to gather town spawn
        final Player killed = event.getEntity();
        if(this.getActiveRaiders().contains(killed.getName())) {
            try {
                killed.teleport(this.getGatherTown().getSpawn());
                killed.sendMessage(String.valueOf(Helper.Chatlabel()) + "You died raiding and have been teleported back to the gather point.");
            } catch (TownyException e) {
                throw new RuntimeException(e);
            }
        }

        //Tag that the player has been killed in the raid
        for(Pair<String,Boolean> p : this.activeRaiders) {
            if(p.fst.equals(killed.getName()) && !p.snd) {
                Pair<String,Boolean> newP = new Pair<String,Boolean>(p.fst, true);
                this.activeRaiders.remove(p);
                this.activeRaiders.add(newP);
            }
        }
    }

    /**
     * Use only during combat phase of raid
     * @param event
     */
    public void defenderKilledInCombat(PlayerDeathEvent event) {
        this.addPointsToRaidScore(20);
        for (final String playerName : this.getActiveRaiders()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! +20 Raid Score");
            }
            catch (NullPointerException ex5) {}
        }
        for (final String playerName : this.getDefenderPlayers()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! +20 Raid Score");
            }
            catch (NullPointerException ex6) {}
        }

        //tp to raid town spawn
        final Player killed = event.getEntity();
        if(this.getActiveRaiders().contains(killed.getName())) {
            try {
                killed.teleport(this.getGatherTown().getSpawn());
                killed.sendMessage(String.valueOf(Helper.Chatlabel()) + "You died raiding and have been teleported back to your town's spawn.");
            } catch (TownyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Use only out of combat phase of raid
     * @param event
     */
    public void raiderKilledOutofCombat(PlayerDeathEvent event) {

        //award negative score
        for (final String playerName : this.getActiveRaiders()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Raider killed before combat, no points awarded and death treated normally.");
            }
            catch (NullPointerException ex3) {}
        }
        for (final String playerName : this.getDefenderPlayers()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Raider killed before combat, no points awarded and death treated normally.");
            }
            catch (NullPointerException ex4) {}
        }

        //tp to gather town spawn
        final Player killed = event.getEntity();
        if(this.getActiveRaiders().contains(killed.getName())) {
            try {
                killed.teleport(this.getGatherTown().getSpawn());
                killed.sendMessage(String.valueOf(Helper.Chatlabel()) + "You died before raiding and have been teleported back to the gather point.");
            } catch (TownyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Use only out of combat phase of raid
     * @param event
     */
    public void defenderKilledOutofCombat(PlayerDeathEvent event) {
        for (final String playerName : this.getActiveRaiders()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed before combat, no points awarded and death treated normally.");
            }
            catch (NullPointerException ex5) {}
        }
        for (final String playerName : this.getDefenderPlayers()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed before combat, no points awarded and death treated normally.");
            }
            catch (NullPointerException ex6) {}
        }

        //tp to raid town spawn
        final Player killed = event.getEntity();
        if(this.getActiveRaiders().contains(killed.getName())) {
            try {
                killed.teleport(this.getGatherTown().getSpawn());
                killed.sendMessage(String.valueOf(Helper.Chatlabel()) + "You died before being raided and have been teleported back to your town's spawn.");
            } catch (TownyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void raidersWin(final Player owner) {
        //TODO
        stop();
    }

    public void defendersWin() {
        //TODO
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

    public Town getRaidedTown() {
        return this.raidedTown;
    }

    public void setRaidedTown(final Town town) {
        this.raidedTown = town;
    }

    public Town getGatherTown() {
        return this.gatherTown;
    }

    public void setGatherTown(final Town town) {
        this.gatherTown = town;
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

    public RaidPhase getPhase() {
        return this.phase;
    }

    public void setRaidPhase(final RaidPhase phase) {
        this.phase = phase;
    }

    public ArrayList<String> getActiveRaiders() {
        ArrayList<String> raidersNames = new ArrayList<>();
        for(Pair<String,Boolean> p : this.activeRaiders) {
            raidersNames.add(p.fst);
        }
        return raidersNames;
    }

    public ArrayList<Pair<String,Boolean>> getActiveRaidersRaw() {
        return this.activeRaiders;
    }

    public void setActiveRaiders(ArrayList<Pair<String,Boolean>> activeRaiders) {
        this.activeRaiders = activeRaiders;
    }

    public void addActiveRaider(String player) {
        this.activeRaiders.add(new Pair<String,Boolean>(player, false));
    }

    /**
     * Finds first!!!! and removes
     * @param name
     */
    public void removeActiveRaider(String name) {
        for(Pair<String,Boolean> p : this.activeRaiders) {
            if(p.fst.equals(name)) this.activeRaiders.remove(p);
            return;
        }
    }
}
