package me.ShermansWorld.AlathraWar;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
v/ No teleporting by raiders
v/ Points management
    v/ Raider death is negative score
    v/ Defender death is positive score
    v/ Raiders can loot townblocks for extra score
v/ Raiders on death are teleported to their town spawn.

*/

/**
 * Implementation of Raids for alathrawar
 *
 * @author AubriTheHuman
 * @author NinjaMandalorian
 * @author ShermansWorld
 */
public class Raid {

    //Ticks between activity, this is lower than siege because more activity is occuring in a raid
    private static final int incremental = 40;
    private War war;
    private final String name;
    private Town raidedTown;
    private Town gatherTown;
    private String raiders;
    private String defenders;
    private boolean side1AreRaiders;
    private int raidScore;
    private Player owner;
    private int raidTicks;
    private TownBlock homeBlockRaided;
    private TownBlock homeBlockGather;
    private RaidPhase phase;
    private Location townSpawnRaided;
    private Location townSpawnGather;
    int[] bukkitId;
    public ArrayList<String> activeRaiders;
    public ArrayList<String> raiderPlayers;
    public ArrayList<String> defenderPlayers;
    public Map<WorldCoord, LootBlock> lootedChunks;

    /**
     * Constructs raid for staging phase
     *
     * @param war
     * @param raidedTown
     * @param gatherTown
     * @param side1AreRaiders
     * @param raidTicks
     */
    public Raid(final War war, final Town raidedTown, final Town gatherTown,
                final boolean side1AreRaiders, final int raidTicks, Player owner) {

        this.bukkitId = new int[1];
        this.activeRaiders = activeRaiders == null ? new ArrayList<>() : activeRaiders;
        this.war = war;
        this.raidTicks = raidTicks;
        this.raidedTown = raidedTown;
        this.gatherTown = gatherTown;
        this.owner = owner;

        //AttackSide-Town
        this.name = (side1AreRaiders ? war.getSide1() : war.getSide2()).toLowerCase() + "-" + raidedTown.getName().toLowerCase();
        this.lootedChunks = new HashMap<>();

    }

    /**
     * IDK why this constructor exists but it does
     *
     * @param war
     * @param raidedTown
     * @param gatherTown
     * @param side1AreRaiders
     * @param raidTicks
     * @param activeRaiders
     * @param phase
     */
    public Raid(final War war, final Town raidedTown, final Town gatherTown, final boolean side1AreRaiders, final int raidTicks, ArrayList<String> activeRaiders, RaidPhase phase, Player owner) {
        this(war, raidedTown, gatherTown, side1AreRaiders, raidTicks, owner);
        this.activeRaiders = activeRaiders;
        this.setRaidPhase(phase);
    }


    /**
     * Initial Constructor
     *
     * @param war
     * @param raidedTown
     * @param gatherTown
     * @param side1AreRaiders
     */
    public Raid(final War war, final Town raidedTown, final Town gatherTown,
                final boolean side1AreRaiders, Player owner) {
        this.bukkitId = new int[1];
        this.activeRaiders = activeRaiders == null ? new ArrayList<>() : activeRaiders;
        this.war = war;
        this.raidTicks = 0;
        this.raidedTown = raidedTown;
        this.owner = owner;

        //AttackSide-Town
        this.name = (side1AreRaiders ? war.getSide1() : war.getSide2()).toLowerCase() + "-" + raidedTown.getName().toLowerCase();
        this.lootedChunks = new HashMap<>();
    }

    /**
     * Run when raid starts
     */
    public void start() {

        //start
        this.phase = RaidPhase.START;

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

        //set the last raided time to now
        raidedTown.addMetaData(new LongDataField("lastRaided", (System.currentTimeMillis() / 1000)));
        war.setLastRaidTime((System.currentTimeMillis() / 1000));

        // Creates 10 second looping function for Raid

        this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) Main.getInstance(),
                getTickLoop(), 0L, incremental);

    }


    /**
     * Resumes a Raid (after a server restart e.t.c.)
     */
    public void resume() {
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

        // Creates 2 second looping function for Raid, restarting this!
        this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) Main.getInstance(),
                getTickLoop(), 0L, incremental);

    }


    /**
     * End of raid
     */
    public void stop() {
        Bukkit.getScheduler().cancelTask(this.bukkitId[0]);
        RaidData.removeRaid(this);
    }


    /**
     * Do loot logic for a chunk, synchronized in case multiple players are looting at once and something breaks
     *
     * @param p
     * @param wc
     */
    private synchronized void doLootAt(Player p, WorldCoord wc) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0, true, false, true));

        //check if this loot is in progress
        if (this.lootedChunks.containsKey(wc)) {
            LootBlock lb = this.lootedChunks.get(wc);
            lb.ticks += incremental;
            //60 seconds to loot
            if (lb.ticks >= 1200 && !lb.finished) {
                lb.finished = true;
                Random r = new Random();
                //The base value can be abjusted
                lb.value = r.nextDouble() * 100;

                //score for looting
                this.addPointsToRaidScore(10);

                Main.warLogger.log(String.format("A townblock has been looted for $%.2d", lb.value));
                //Broadcast to the whole raid
                for (String s : activeRaiders) {
                    Bukkit.getPlayer(s).sendMessage(Helper.Chatlabel() + String.format("A townblock has been looted for $%.2d and +10 points. (Added to pool)", lb.value));
                }
                for (String s : defenderPlayers) {
                    Bukkit.getPlayer(s).sendMessage(Helper.Chatlabel() + String.format("A townblock has been looted for $%.2d and +10 points. (Added to pool)", lb.value));
                }
            }
        } else {
            //if not make a new property for it
            this.addLootedChunk(wc);
            for (String s : activeRaiders) {
                Bukkit.getPlayer(s).sendMessage(Helper.Chatlabel() + "Raiders have started looting a town block!");
            }
            for (String s : defenderPlayers) {
                Bukkit.getPlayer(s).sendMessage(Helper.Chatlabel() + "Raiders have started looting a town block!");
            }
        }

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
     * Raider killed in combat
     */
    public void raiderKilledInCombat(PlayerDeathEvent event) {

        //award negative score
        this.subtractPointsFromRaidScore(40);
        for (final String playerName : this.getActiveRaiders()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Raider killed! -40 Raid Score");
            } catch (NullPointerException ex3) {
            }
        }
        for (final String playerName : this.getDefenderPlayers()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Raider killed! -40 Raid Score");
            } catch (NullPointerException ex4) {
            }
        }

        //tp to gather town spawn
        final Player killed = event.getEntity();
        if (this.getActiveRaiders().contains(killed.getName())) {
            try {
                killed.teleport(this.getGatherTown().getSpawn());
                killed.sendMessage(String.valueOf(Helper.Chatlabel()) + "You died raiding and have been teleported back to the gather point.");
            } catch (TownyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Defender killed in combat
     */
    public void defenderKilledInCombat(PlayerDeathEvent event) {
        this.addPointsToRaidScore(20);
        for (final String playerName : this.getActiveRaiders()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! +20 Raid Score");
            } catch (NullPointerException ex5) {
            }
        }
        for (final String playerName : this.getDefenderPlayers()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed! +20 Raid Score");
            } catch (NullPointerException ex6) {
            }
        }

        //tp to raid town spawn
        final Player killed = event.getEntity();
        if (this.getActiveRaiders().contains(killed.getName())) {
            //                killed.teleport(this.getGatherTown().getSpawn());
//                killed.sendMessage(String.valueOf(Helper.Chatlabel()) + "You died raiding and have been teleported back to your town's spawn.");
        }
    }

    /**
     * Raider killed outside of combat
     */
    public void raiderKilledOutofCombat(PlayerDeathEvent event) {

        //award negative score
        for (final String playerName : this.getActiveRaiders()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Raider killed before combat, no points awarded and death treated normally.");
            } catch (NullPointerException ex3) {
            }
        }
        for (final String playerName : this.getDefenderPlayers()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Raider killed before combat, no points awarded and death treated normally.");
            } catch (NullPointerException ex4) {
            }
        }

        //tp to gather town spawn
        final Player killed = event.getEntity();
        if (this.getActiveRaiders().contains(killed.getName())) {
            try {
                killed.teleport(this.getGatherTown().getSpawn());
                killed.sendMessage(String.valueOf(Helper.Chatlabel()) + "You died before raiding and have been teleported back to the gather point.");
            } catch (TownyException e) {
                throw new RuntimeException(e);
            }
        }

        //teleport the killer back to the raided towns spawn
        if (this.getDefenders().contains(killed.getKiller().getName())) {
            try {
                killed.getKiller().teleport(this.getRaidedTown().getSpawn());
                killed.getKiller().sendMessage(String.valueOf(Helper.Chatlabel()) + "You killed a raider before the raid began and have been teleported back to the defending town.");
            } catch (TownyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Defender killed out of combat
     */
    public void defenderKilledOutofCombat(PlayerDeathEvent event) {
        for (final String playerName : this.getActiveRaiders()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed before combat, no points awarded and death treated normally.");
            } catch (NullPointerException ex5) {
            }
        }
        for (final String playerName : this.getDefenderPlayers()) {
            try {
                Bukkit.getPlayer(playerName).sendMessage(String.valueOf(Helper.Chatlabel()) + "Defender killed before combat, no points awarded and death treated normally.");
            } catch (NullPointerException ex6) {
            }
        }

        //tp to raid town spawn
        final Player killed = event.getEntity();
        if (this.getDefenders().contains(killed.getName())) {
//             killed.teleport(this.getGatherTown().getSpawn());
//             killed.sendMessage(String.valueOf(Helper.Chatlabel()) + "You died before being raided and have been teleported back to the defending town's spawn.");
        }

        //teleport the killer back to the raided towns spawn
        if (killed.getKiller() instanceof Player) {
            if (this.getActiveRaiders().contains(killed.getKiller().getName())) {
                try {
                    killed.getKiller().teleport(this.getGatherTown().getSpawn());
                    killed.getKiller().sendMessage(String.valueOf(Helper.Chatlabel()) + "You killed a defender before the raid began and have been teleported back to the gather town.");
                } catch (TownyException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Raiders win, give payout and finalize raid
     *
     * @param owner
     * @param raidScore
     */
    public void raidersWin(final Player owner, int raidScore) {
        //TODO finalize payout

        //Calc win factor
        //difference of score minus 800, then divided by 100
        //raid needs 10+ kills over defenders to win
        //factor is 0.5 -> 3
        double factor = ((raidScore - 800.0) / 100) + 0.5;
        if (factor > 3.0) factor = 3.0;

        final Resident resident = TownyAPI.getInstance().getResident(owner);
        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raiders from " + this.raiders
                + " have successfully raided " + this.raidedTown.getName() + "!");

        /*
        if town has more than 10k, 1/10th of its valuables times the factor is taken
        if it has less than 10k, 1000 is taken

        the raid chest is also kept
         */
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner.getUniqueId());

        //raid chest
        Main.econ.depositPlayer(offlinePlayer, 1000);
        double amt = 0.0;
        if (this.raidedTown.getAccount().getHoldingBalance() > 10000) {
            amt = Math.floor(this.raidedTown.getAccount().getHoldingBalance()) / 10.0;
            amt *= factor;
            if (amt > 10000.0) amt = 10000.0;
            this.raidedTown.getAccount().withdraw(amt, "war loot");
        } else if (this.raidedTown.getAccount().getHoldingBalance() < 1000) {
            amt = 1000;
            this.raidedTown.getAccount().withdraw(this.raidedTown.getAccount().getHoldingBalance(), "war loot");
        } else {
            amt = 1000;
            this.raidedTown.getAccount().withdraw(amt, "war loot");
        }

        String statement = "raided";
        if (factor <= 1.0) {
            statement = "looted";
        } else if (factor <= 2.0) {
            statement = "ransacked";
        } else if (factor <= 3.0) {
            statement = "emptied";
        }

        double looted = 0.0;
        //calc looted chunks
        for (WorldCoord wc : this.getLootedChunks().keySet()) {
            if (this.getLootedChunks().get(wc).finished) looted += this.getLootedChunks().get(wc).value;
        }

        amt += looted;

        Bukkit.broadcastMessage("The town of " + this.raidedTown.getName() + " has been " + statement + " by " + this.getRaiders()
                + " in a raid for $" + String.valueOf(amt));
        Main.warLogger.log("The town of " + this.raidedTown.getName() + " has been " + statement + " by " + this.getRaiders()
                + " in a raid for $" + String.valueOf(amt));
        Main.econ.depositPlayer(offlinePlayer, amt);


        stop();
    }

    /**
     * Defenders win, payout, and finalize raid
     *
     * @param raidScore
     */
    public void defendersWin(int raidScore) {
        //TODO finalize payout
        /*
        If defenders win, they get the 1000 deposit.
         */

        //at a raid score of 699.9999, this results in 0, 700 is considered raider victory
        //at 600 this results in 0.5
        //at 500 this results in 1.0
        //at 0 this results in 3.5, which is capped to 3

        double factor = (3 - ((raidScore) / 200.0) - 0.5);
        if (factor > 3.0) factor = 3.0;

        String statement = "raided";
        if (factor <= 0.5) {
            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The defenders from " + this.defenders
                    + " have barely pushed back the raiders of " + this.raiders + ". More has been lost than gained.");
            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + this.raidedTown.getName()
                    + " has recovered part of the attackers' raid chest, valued at $625");
            Main.warLogger
                    .log("The defenders from " + this.defenders + " have won the raid of " + this.raidedTown.getName() + "!");
            this.raidedTown.getAccount().deposit(500, "Raid chest");
        } else if (factor <= 1.5) {
            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The defenders from " + this.defenders
                    + " have fended off the raiders of " + this.raiders + "!");
            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + this.raidedTown.getName()
                    + " has recovered the attackers' raid chest, valued at $1,250");
            Main.warLogger
                    .log("The defenders from " + this.defenders + " have won the raid of " + this.raidedTown.getName() + "!");
            this.raidedTown.getAccount().deposit(1000, "Raid chest");
        } else if (factor <= 3.0) {
            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The defenders from " + this.defenders
                    + " have wholly defeated the raiders of " + this.raiders + "! They barely broke the walls.");
            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + this.raidedTown.getName()
                    + " has recovered the attackers' raid chest, valued at $1,250.");
            Main.warLogger
                    .log("The defenders from " + this.defenders + " have won the raid of " + this.raidedTown.getName() + "!");
            this.raidedTown.getAccount().deposit(1000, "Raid chest");
        }

        stop();
    }

    /**
     * No winner declared
     * @param raidScore
     */
    public void noWinner() {
        //TODO finalize payout

        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The defenders from " + this.defenders
                + " didn't push back the raiders of " + this.raiders + ". Yet no loot was taken, raid considered a draw.");
        Main.warLogger
                .log("No one won the raid of " + this.raidedTown.getName() + "!");

        stop();
    }

    /**
     * Positively impact the raid score
     *
     * @param points
     */
    public void addPointsToRaidScore(final int points) {
        this.raidScore += points;
        //cap
        if (raidScore > 1200) this.raidScore = 1200;
    }

    /**
     * Negatively impact the raid score
     *
     * @param points
     */
    public void subtractPointsFromRaidScore(final int points) {
        this.raidScore -= points;
        //cap
        if (raidScore < 0) this.raidScore = 0;
    }

    public String getName() {
        return this.name;
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

    public Player getOwner() {
        return this.owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
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

    public int getRaidTicks() {
        return this.raidTicks;
    }

    public void setRaidTicks(int ticks) {
        this.raidTicks = ticks;
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
        return this.activeRaiders;
    }

    public void setActiveRaiders(ArrayList<String> activeRaiders) {
        this.activeRaiders = activeRaiders;
    }

    public void addActiveRaider(String player) {
        this.activeRaiders.add(player);
    }

    public void removeActiveRaider(String name) {
        this.activeRaiders.remove(name);
    }

    public Map<WorldCoord, LootBlock> getLootedChunks() {
        if (lootedChunks == null) {
            lootedChunks = new HashMap<>();
        }
        return lootedChunks;
    }

    public void setLootedChunks(Map<WorldCoord, LootBlock> lootedChunks) {
        this.lootedChunks = lootedChunks;
    }

    public void addLootedChunk(WorldCoord c) {
        this.lootedChunks.put(c, new LootBlock(c, 0, 0.0));
    }

    protected Runnable getTickLoop() {
        return (Runnable) new Runnable() {

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
                raidTicks += incremental;

                if (Raid.this.raidTicks >= RaidPhase.END.startTick && Raid.this.phase == RaidPhase.COMBAT) {
                    Raid.this.phase = RaidPhase.END;
                }

                //check and start travel phase
                if (Raid.this.raidTicks >= RaidPhase.TRAVEL.startTick && Raid.this.phase == RaidPhase.GATHER) {
                    startTravel();
                }

                //check and start combat phase
                if (Raid.this.raidTicks >= RaidPhase.COMBAT.startTick && Raid.this.phase == RaidPhase.TRAVEL) {
                    startCombat();
                }

                if (Raid.this.phase == RaidPhase.END) {
                    //TODO: fix raid scoring
                    //for raiders to win, they need a significant up from the defenders
                    if (Raid.this.raidScore > 800) {
                        Raid.this.raidersWin(Raid.this.owner, Raid.this.raidScore);
                    } else {
                        Raid.this.defendersWin(Raid.this.raidScore);
                    }
                }

                //Raid gather phase behavior
                if (Raid.this.phase == RaidPhase.GATHER) {
                    //Broadcast
                    if (raidTicks % 6000 == 0) {
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid of "
                                + Raid.this.getRaidedTown().getName() + " will begin in " + (int) (RaidPhase.TRAVEL.startTick / 20 / 60) + " minutes!");
                        Bukkit.broadcastMessage(
                                "The Raiders are gathering at " + getGatherTown().getName() + " before making the journey over!");
                    }


                    //Prevent players from leaving gather town prematurely, if player is raid owner, dont do that
                    for (String playerName : getActiveRaiders()) {
                        try {
                            Player p = Bukkit.getPlayer(playerName);
                            ArrayList<WorldCoord> cluster = Helper.getCluster(Raid.this.getGatherTown().getHomeBlock().getWorldCoord());
                            if (!cluster.contains(WorldCoord.parseWorldCoord(p))) {
                                if (p.equals(owner)) {
                                    p.teleport(gatherTown.getSpawn());
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot leave the gather town. As raid party leader, you have remained in the party. To stop the raid type /raid abandon");
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You have been teleported to the gather town's spawn.");
                                } else {
                                    Raid.this.removeActiveRaider(p.getName());
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "By leaving the gathering town you have left the raid on " + Raid.this.getRaidedTown().getName() + "!");
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "To rejoin, do /raid join [war] [town]");
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (NotRegisteredException e) {
                            e.printStackTrace();
                        } catch (TownyException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //Raid Travel phase behavior
                else if (Raid.this.phase == RaidPhase.TRAVEL) {

                    //Report
                    if (raidTicks % 2400 == 0) {
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The Raiders of "
                                + (side1AreRaiders ? Raid.this.war.getSide1() : Raid.this.war.getSide2())
                                + " are on their way to raid "
                                + Raid.this.getRaidedTown().getName() + "!");
                    }

                    //Check if a player has arrived at the town (in it, or within 200 blocks) and if so start combat
                    for (String player : activeRaiders) {
                        try {
                            boolean playerCloseToHomeBlockRaid = false;
                            final int homeBlockXCoordRaided = Raid.this.getRaidedTown().getHomeBlock().getCoord().getX() * 16;
                            final int homeBlockZCoordRaided = Raid.this.getRaidedTown().getHomeBlock().getCoord().getZ() * 16;
                            Player p = Bukkit.getPlayer(player);
                            //Carryover from sieges
                            if (Math.abs(p.getLocation().getBlockX() - homeBlockXCoordRaided) <= 200 && Math.abs(p.getLocation().getBlockZ() - homeBlockZCoordRaided) <= 200) {
                                playerCloseToHomeBlockRaid = true;
                            }

                            WorldCoord playercoord = WorldCoord.parseWorldCoord(Bukkit.getPlayer(player));
                            if (Raid.this.getRaidedTown().hasTownBlock(playercoord) || playerCloseToHomeBlockRaid) {
                                startCombat();
                            }
                        } catch (TownyException e) {
                            e.printStackTrace();
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

                    //Looting chunks
                    for (String name : Raid.this.getActiveRaiders()) {
                        Player p = Bukkit.getPlayer(name);
                        WorldCoord wc = WorldCoord.parseWorldCoord(p);
                        if (p.isSneaking()) {
                            //check if our chunk is looted, if so skip this player
                            //Idk if thisll work but if it do then ye
                            if (Raid.this.lootedChunks.containsKey(wc)) {
                                if (Raid.this.lootedChunks.get(wc).finished) {
                                    p.sendMessage(Helper.Chatlabel() + "This chunk is already looted, try another.");
                                } else {
                                    doLootAt(p, wc);
                                }
                            } else {
                                if (Raid.this.getRaidedTown().hasTownBlock(wc)) {
                                    doLootAt(p, wc);
                                } else {
                                    p.sendMessage(Helper.Chatlabel() + "This space is not part of the raided town, you cannot loot this area.");
                                }
                            }
//                                        for (LootBlock c : Raid.this.lootedChunks.values()) {
//                                            if (c.worldCoord.equals(wc)) {
//                                                looted = true;
//                                                p.sendMessage(Helper.Chatlabel() + "This chunk is already looted, try another.");
//                                                continue;
//                                            }
//                                        }

                        }
                    }
                }

            }
        };
    }

    public TownBlock getHomeBlockRaided() {
        return homeBlockRaided;
    }

    public void setHomeBlockRaided(TownBlock homeBlockRaided) {
        this.homeBlockRaided = homeBlockRaided;
    }

    public TownBlock getHomeBlockGather() {
        return homeBlockGather;
    }

    public void setHomeBlockGather(TownBlock homeBlockGather) {
        this.homeBlockGather = homeBlockGather;
    }

    public void setPhase(RaidPhase phase) {
        this.phase = phase;
    }

    public Location getTownSpawnRaided() {
        return townSpawnRaided;
    }

    public void setTownSpawnRaided(Location townSpawnRaided) {
        this.townSpawnRaided = townSpawnRaided;
    }

    public Location getTownSpawnGather() {
        return townSpawnGather;
    }

    public void setTownSpawnGather(Location townSpawnGather) {
        this.townSpawnGather = townSpawnGather;
    }

    public static class LootBlock {

        public WorldCoord worldCoord;
        public int ticks;
        public double value;
        public boolean finished;

        public LootBlock(WorldCoord c, int ticks, double value) {
            this.worldCoord = c;
            this.ticks = ticks;
            this.value = value;
            this.finished = false;
        }

        public LootBlock(WorldCoord c, int ticks, double value, boolean finished) {
            this.worldCoord = c;
            this.ticks = ticks;
            this.value = value;
            this.finished = finished;
        }


    }
}
