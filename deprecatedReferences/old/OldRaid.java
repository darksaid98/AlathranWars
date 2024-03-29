package me.ShermansWorld.AlathranWars.deprecated;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import me.ShermansWorld.AlathranWars.Main;
import me.ShermansWorld.AlathranWars.data.RaidData;
import me.ShermansWorld.AlathranWars.data.RaidPhase;
import me.ShermansWorld.AlathranWars.utility.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
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
done every 24 hours per raided town, every 6 hours in a oldWar per side,
and raided players may run out of the town, forfeiting the cost of the raid.

The raid is split into two phases:
- STAGING PHASE
    v/ Raiders collect at raiding town, joining raid through command
    v/ Defenders are given time to getInstance to the town. (travel is announced way in ahead)
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
 * Implementation of Raids for alathranwars
 *
 * @author AubriTheHuman
 * @author NinjaMandalorian
 * @author ShermansWorld
 */
@Deprecated
public class OldRaid {

    //Ticks between activity, this is lower than siege because more activity is occuring in a raid
    private static final int incremental = 40;
    final int[] bukkitId;
    private final String name;
    public final NamespacedKey bossBarKey = new NamespacedKey(Main.getInstance(), "raidBar." + OldRaid.this.getName());
    private final String defenders;
    public ArrayList<String> activeRaiders = new ArrayList<>();
    public ArrayList<String> raiderPlayers = new ArrayList<>();
    public ArrayList<String> defenderPlayers = new ArrayList<>();
    public Map<WorldCoord, LootBlock> lootedChunks;
    private OldWar oldWar;
    private Town raidedTown;
    private Town gatherTown;
    private String raiders;
    private boolean side1AreRaiders;
    private int raiderScore;
    private int defenderScore;
    private OfflinePlayer owner;
    private int raidTicks;
    private TownBlock homeBlockRaided;
    private TownBlock homeBlockGather;
    private RaidPhase phase;
    private Location townSpawnRaided;
    private Location townSpawnGather;

    /**
     * Constructs raid for staging phase
     *
     * @param oldWar
     * @param raidedTown
     * @param gatherTown
     * @param side1AreRaiders
     * @param raidTicks
     */
    public OldRaid(final OldWar oldWar, final Town raidedTown, final Town gatherTown,
                   final boolean side1AreRaiders, final int raidTicks, OfflinePlayer owner) {

        this.bukkitId = new int[1];
        this.oldWar = oldWar;
        this.raidTicks = raidTicks;
        this.raidedTown = raidedTown;
        this.gatherTown = gatherTown;
        this.owner = owner;
        this.side1AreRaiders = side1AreRaiders;
        this.raiders = side1AreRaiders ? oldWar.getSide1() : oldWar.getSide2();
        this.defenders = !side1AreRaiders ? oldWar.getSide1() : oldWar.getSide2();
        this.raiderScore = 0;
        this.defenderScore = 0;

        //AttackSide-Town
        this.name = oldWar.getName() + "-" + raidedTown.getName().toLowerCase();
        this.lootedChunks = new HashMap<>();

    }

    /**
     * IDK why this constructor exists but it does
     *
     * @param oldWar
     * @param raidedTown
     * @param gatherTown
     * @param side1AreRaiders
     * @param raidTicks
     * @param activeRaiders
     * @param phase
     */
    public OldRaid(final OldWar oldWar, final Town raidedTown, final Town gatherTown, final boolean side1AreRaiders, final int raidTicks, ArrayList<String> activeRaiders, RaidPhase phase, OfflinePlayer owner) {
        this(oldWar, raidedTown, gatherTown, side1AreRaiders, raidTicks, owner);
        this.activeRaiders = activeRaiders;
        this.setRaidPhase(phase);
    }


    /**
     * Initial Constructor
     *
     * @param oldWar
     * @param raidedTown
     * @param gatherTown
     * @param side1AreRaiders
     */
    public OldRaid(final OldWar oldWar, final Town raidedTown, final Town gatherTown,
                   final boolean side1AreRaiders, OfflinePlayer owner) {
        this.bukkitId = new int[1];
//        this.activeRaiders = activeRaiders == null ? new ArrayList<>() : activeRaiders;
        this.oldWar = oldWar;
        this.raidTicks = 0;
        this.raidedTown = raidedTown;
        this.gatherTown = gatherTown;
        this.owner = owner;
        this.side1AreRaiders = side1AreRaiders;
        this.raiders = side1AreRaiders ? oldWar.getSide1() : oldWar.getSide2();
        this.defenders = !side1AreRaiders ? oldWar.getSide1() : oldWar.getSide2();
        this.raiderScore = 0;
        this.defenderScore = 0;

        //AttackSide-Town
        this.name = oldWar.getName() + "-" + raidedTown.getName().toLowerCase();
        this.lootedChunks = new HashMap<>();
    }

    static /* synthetic */ void access$7(final OldRaid oldRaid, final int raidTicks) {
        oldRaid.raidTicks = raidTicks;
    }

    /**
     * Run when raid starts
     */
    public void start() {

        //start
        this.phase = RaidPhase.START;

        if (this.side1AreRaiders) {
            this.raiderPlayers = this.oldWar.getSide1Players();
            this.defenderPlayers = this.oldWar.getSide2Players();
        } else {
            this.raiderPlayers = this.oldWar.getSide2Players();
            this.defenderPlayers = this.oldWar.getSide1Players();
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
        if (oldWar.getSide1Players().contains(owner.getName())) {
            oldWar.setLastRaidTimeSide1((int) (System.currentTimeMillis() / 1000));
        } else if (oldWar.getSide2Players().contains(owner.getName())) {
            oldWar.setLastRaidTimeSide2((int) (System.currentTimeMillis() / 1000));
        }

        setupDisplayBar();

        // Creates 10 second looping function for OldRaid

        this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(),
            getTickLoop(), 0L, incremental);

        this.getWar().addRaid(this);
        this.save();

    }

    /**
     * Resumes a OldRaid (after a server restart e.t.c.)
     */
    public void resume() {
        if (this.side1AreRaiders) {
            this.raiderPlayers = this.oldWar.getSide1Players();
            this.defenderPlayers = this.oldWar.getSide2Players();
        } else {
            this.raiderPlayers = this.oldWar.getSide2Players();
            this.defenderPlayers = this.oldWar.getSide1Players();
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

        setupDisplayBar();

        // Creates 2 second looping function for OldRaid, restarting this!
        this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(),
            getTickLoop(), 0L, incremental);

    }

    /**
     * End of raid
     */
    public void stop() {
        deleteDisplayBar();
        Bukkit.getScheduler().cancelTask(this.bukkitId[0]);
        this.getWar().getRaids().remove(this);
        RaidData.removeRaid(this);
    }

    /**
     * Do loot logic for a chunk, synchronized in case multiple players are looting at once and something breaks
     *
     * @param p
     * @param wc
     */
    public synchronized void doLootAt(Player p, WorldCoord wc) {
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
                lb.value = r.nextDouble() * 30;

                //score for looting
                this.addPointsToRaiderScore(10);

                Main.warLogger.log(String.format("A townblock has been looted for $%.2f", lb.value));
                //Broadcast to the whole raid
                for (String s : activeRaiders) {
                    Player pl = Bukkit.getPlayer(s);
                    if (pl != null)
                        pl.sendMessage(UtilsChat.getPrefix() + String.format("A townblock has been looted for $%.2f and +10 points. (Added to pool)", lb.value));
                }
                for (String s : defenderPlayers) {
                    Player pl = Bukkit.getPlayer(s);
                    if (pl != null)
                        pl.sendMessage(UtilsChat.getPrefix() + String.format("A townblock has been looted for $%.2f and +10 points. (Added to pool)", lb.value));
                }

                this.save();
            }
        } else {
            //if not make a new property for it
            this.addLootedChunk(wc);
            for (String s : activeRaiders) {
                Player pl = Bukkit.getPlayer(s);
                if (pl != null)
                    pl.sendMessage(UtilsChat.getPrefix() + "Raiders have started looting a town block!");
            }
            for (String s : defenderPlayers) {
                Player pl = Bukkit.getPlayer(s);
                if (pl != null)
                    pl.sendMessage(UtilsChat.getPrefix() + "Raiders have started looting a town block!");
            }
        }

    }

    /**
     * Begin the Gather phase of the raid
     */
    private void startGather() {
        OldRaid.this.phase = RaidPhase.GATHER;
    }

    /**
     * Begin the Travel phase of the raid
     */
    private void startTravel() {
        OldRaid.this.phase = RaidPhase.TRAVEL;
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The Raiders of "
            + (side1AreRaiders ? OldRaid.this.oldWar.getSide1() : OldRaid.this.oldWar.getSide2())
            + " are coming to raid "
            + OldRaid.this.getRaidedTown().getName() + "!");
        //in the case were not at that stage already, if this happens we have an issue
        OldRaid.this.raidTicks = RaidPhase.TRAVEL.startTick;
    }

    /**
     * Begin the combat phase of the raid
     */
    private void startCombat() {
        OldRaid.this.phase = RaidPhase.COMBAT;
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The Raiders of "
            + (side1AreRaiders ? OldRaid.this.oldWar.getSide1() : OldRaid.this.oldWar.getSide2())
            + " have arrived at "
            + OldRaid.this.getRaidedTown().getName() + " and the fighting has begun!");
        //in the case were not at that stage already
        OldRaid.this.raidTicks = RaidPhase.COMBAT.startTick;
    }

    /**
     * Raider killed in combat
     */
    public void raiderKilledInCombat(PlayerDeathEvent event) {

        //award negative score
        this.addPointsToDefenderScore(40);
        for (final String playerName : this.getActiveRaiders()) {
            Player pl = Bukkit.getPlayer(playerName);
            if (pl != null) pl.sendMessage(UtilsChat.getPrefix() + "Raider killed! -40 OldRaid Score");
        }
        for (final String playerName : this.getDefenderPlayers()) {
            Player pl = Bukkit.getPlayer(playerName);
            if (pl != null) pl.sendMessage(UtilsChat.getPrefix() + "Raider killed! -40 OldRaid Score");
        }
    }

    /**
     * Defender killed in combat
     */
    public void defenderKilledInCombat(PlayerDeathEvent event) {
        this.addPointsToRaiderScore(20);
        for (final String playerName : this.getActiveRaiders()) {
            Player pl = Bukkit.getPlayer(playerName);
            if (pl != null) pl.sendMessage(UtilsChat.getPrefix() + "Defender killed! +20 OldRaid Score");
        }
        for (final String playerName : this.getDefenderPlayers()) {
            Player pl = Bukkit.getPlayer(playerName);
            if (pl != null) pl.sendMessage(UtilsChat.getPrefix() + "Defender killed! +20 OldRaid Score");
        }

//        //tp to raid town spawn
//        final Player killed = event.getEntity();
//        if (this.getActiveRaiders().contains(killed.getName())) {
//            //                killed.teleport(this.getGatherTown().getSpawn());
////                killed.sendMessage(String.valueOf(UtilsChat.getPrefix()) + "You died raiding and have been teleported back to your town's spawn.");
//        }
    }

    /**
     * Raider killed outside of combat
     */
    public void raiderKilledOutofCombat(PlayerDeathEvent event) {

        //award negative score
        for (final String playerName : this.getActiveRaiders()) {
            Player pl = Bukkit.getPlayer(playerName);
            if (pl != null)
                pl.sendMessage(UtilsChat.getPrefix() + "Raider killed before combat, no points awarded and death treated normally.");
        }
        for (final String playerName : this.getDefenderPlayers()) {
            Player pl = Bukkit.getPlayer(playerName);
            if (pl != null)
                pl.sendMessage(UtilsChat.getPrefix() + "Raider killed before combat, no points awarded and death treated normally.");
        }
//
//        //tp to gather town spawn
        final Player killed = event.getEntity();
//        if (this.getActiveRaiders().contains(killed.getName())) {
//            try {
//               } catch (TownyException e) {
//                throw new RuntimeException(e);
//            }
//        }

        //teleport the killer back to the raided towns spawn
        if (killed.getKiller() != null) {
            if (this.getDefenderSide().contains(killed.getKiller().getName())) {
                try {
                    killed.getKiller().teleport(this.getRaidedTown().getSpawn());
                    killed.getKiller().sendMessage(UtilsChat.getPrefix() + "You killed a raider before the raid began and have been teleported back to the defending town.");
                } catch (TownyException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Defender killed out of combat
     */
    public void defenderKilledOutofCombat(PlayerDeathEvent event) {
        for (final String playerName : this.getActiveRaiders()) {
            Player pl = Bukkit.getPlayer(playerName);
            if (pl != null)
                pl.sendMessage(UtilsChat.getPrefix() + "Defender killed before combat, no points awarded and death treated normally.");
        }
        for (final String playerName : this.getDefenderPlayers()) {
            Player pl = Bukkit.getPlayer(playerName);
            if (pl != null)
                pl.sendMessage(UtilsChat.getPrefix() + "Defender killed before combat, no points awarded and death treated normally.");
        }

        //tp to raid town spawn
        final Player killed = event.getEntity();
//        if (this.getDefenders().contains(killed.getName())) {
////             killed.teleport(this.getGatherTown().getSpawn());
////             killed.sendMessage(String.valueOf(UtilsChat.getPrefix()) + "You died before being raided and have been teleported back to the defending town's spawn.");
//        }

        //teleport the killer back to the gather towns spawn
        if (killed.getKiller() != null) {
            if (this.getActiveRaiders().contains(killed.getKiller().getName())) {
                try {
                    killed.getKiller().teleport(this.getGatherTown().getSpawn());
                    killed.getKiller().sendMessage(UtilsChat.getPrefix() + "You killed a defender before the raid began and have been teleported back to the gather town.");
                } catch (TownyException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Raiders win, give payout and finalize raid
     */
    public void raidersWin(final OfflinePlayer owner, int raiderScore, int defenderScore) {
        //TODO finalize payout

        //Calc win factor
        //difference of score minus 800, then divided by 100
        //raid needs 10+ kills over defenders to win
        //factor is 0.5 -> 3
        double factor = ((raiderScore - defenderScore) / 200.0D);
        if (factor > 3.0) factor = 3.0;
        if (factor < 0) factor = 0.0;

        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The raiders from " + this.raiders
            + " have successfully raided " + this.raidedTown.getName() + "!");

        /*
        if town has more than 10k, 1/10th of its valuables times the factor is taken
        if it has less than 10k, 1000 is taken

        the raid chest is also kept
         */
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner.getUniqueId());

        //TODO cap based on the town balance

        double amt = 0.0;

        //calc looted chunks
        double looted = 0.0;
        //calc looted chunks
        for (WorldCoord wc : this.getLootedChunks().keySet()) {
            if (this.getLootedChunks().get(wc).finished) looted += this.getLootedChunks().get(wc).value;
        }

        //multiply by victory factor
        amt += looted * factor;

        //cap based on town bank
        if (this.raidedTown.getAccount().getHoldingBalance() > 10000) {
            //large towns getInstance raided for alot more, cap at 1/10th the town holdings
            double maxVal = Math.floor(this.raidedTown.getAccount().getHoldingBalance()) / 10.0;
            if (amt > maxVal) amt = maxVal;
            this.raidedTown.getAccount().withdraw(amt, "oldWar loot");
        } else if (this.raidedTown.getAccount().getHoldingBalance() < 1000) {
            //generate $1000 if the town doesnt have it
            double maxVal = 1000;
            if (amt > maxVal) amt = maxVal;
            this.raidedTown.getAccount().withdraw(this.raidedTown.getAccount().getHoldingBalance(), "oldWar loot");
        } else {
            //give $1000 for the raid win
            double maxVal = 1000;
            if (amt > maxVal) amt = maxVal;
            this.raidedTown.getAccount().withdraw(amt, "oldWar loot");
        }

        //broadcast victory
        String statement = "raided";
        if (factor <= 1.0) {
            statement = "looted";
        } else if (factor <= 2.0) {
            statement = "ransacked";
        } else if (factor <= 3.0) {
            statement = "emptied";
        }

        Bukkit.broadcastMessage("The town of " + this.raidedTown.getName() + " has been " + statement + " by " + this.getRaiderSide()
            + " in a raid for " + String.format("$%.2f", amt));
        Main.warLogger.log("The town of " + this.raidedTown.getName() + " has been " + statement + " by " + this.getRaiderSide()
            + " in a raid for " + String.format("$%.2f", amt));
        Main.econ.depositPlayer(offlinePlayer, amt);

        if (this.side1AreRaiders) {
            oldWar.addSide1Points(15);
        } else {
            oldWar.addSide2Points(15);
        }

        stop();
    }

    /**
     * Defenders win, payout, and finalize raid
     */
    public void defendersWin(int raiderScore, int defenderScore) {
        //TODO finalize payout
        /*
        If defenders win, they getInstance the 1000 deposit.
         */

        //at a raid score of 699.9999, this results in 0, 700 is considered raider victory
        //at 600 this results in 0.5
        //at 500 this results in 1.0
        //at 0 this results in 3.5, which is capped to 3

        double factor = ((defenderScore - raiderScore) / 200.0D);
        if (factor > 3.0) factor = 3.0;
        if (factor < 0) factor = 0.0;

        if (factor <= 0.5) {
            Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The defenders from " + this.defenders
                + " have barely pushed back the raiders of " + this.raiders + ". More has been lost than gained.");
            Bukkit.broadcastMessage(UtilsChat.getPrefix() + this.raidedTown.getName()
                + " has recovered part of the attackers' raid chest, valued at $500");
            Main.warLogger
                .log("The defenders from " + this.defenders + " have won the raid of " + this.raidedTown.getName() + "!");
            this.raidedTown.getAccount().deposit(500, "OldRaid chest");
        } else if (factor <= 1.5) {
            Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The defenders from " + this.defenders
                + " have fended off the raiders of " + this.raiders + "!");
            Bukkit.broadcastMessage(UtilsChat.getPrefix() + this.raidedTown.getName()
                + " has recovered the attackers' raid chest, valued at $1000");
            Main.warLogger
                .log("The defenders from " + this.defenders + " have won the raid of " + this.raidedTown.getName() + "!");
            this.raidedTown.getAccount().deposit(1000, "OldRaid chest");
        } else if (factor <= 3.0) {
            Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The defenders from " + this.defenders
                + " have wholly defeated the raiders of " + this.raiders + "! They barely broke the walls.");
            Bukkit.broadcastMessage(UtilsChat.getPrefix() + this.raidedTown.getName()
                + " has recovered the attackers' raid chest, valued at $1000.");
            Main.warLogger
                .log("The defenders from " + this.defenders + " have won the raid of " + this.raidedTown.getName() + "!");
            this.raidedTown.getAccount().deposit(1000, "OldRaid chest");
        }

        if (this.side1AreRaiders) {
            oldWar.addSide2Points(15);
        } else {
            oldWar.addSide1Points(15);
        }

        stop();
    }

    /**
     * No winner declared
     */
    public void noWinner() {
        //TODO finalize payout

        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The defenders from " + this.defenders
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
    public void addPointsToRaiderScore(final int points) {
        this.raiderScore += points;
        if (raiderScore < 0) this.raiderScore = 0;
    }

    /**
     * Negatively impact the raid score
     *
     * @param points
     */
    public void addPointsToDefenderScore(final int points) {
        this.defenderScore += points;
        if (defenderScore < 0) this.defenderScore = 0;
    }

    public void subtractPointsFromRaiderScore(final int points) {
        this.raiderScore -= points;
        if (raiderScore < 0) this.raiderScore = 0;
    }

    /**
     * Negatively impact the raid score
     *
     * @param points
     */
    public void subtractPointsFromDefenderScore(final int points) {
        this.defenderScore -= points;
        if (defenderScore < 0) this.defenderScore = 0;
    }

    public String getName() {
        return this.name;
    }

    public OldWar getWar() {
        return this.oldWar;
    }

    public void setWar(final OldWar oldWar) {
        this.oldWar = oldWar;
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

    public OfflinePlayer getOwner() {
        return this.owner;
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
    }

    public String getRaiderSide() {
        return this.raiders;
    }

    public void setRaiders(final String raiders) {
        this.raiders = raiders;
    }

    public String getDefenderSide() {
        return this.defenders;
    }

    public void setDefenders(final String defenders) {
        this.raiders = defenders;
    }

    public int getRaiderScore() {
        return this.raiderScore;
    }

    public void setRaiderScore(final int raiderScore) {
        this.raiderScore = raiderScore;
    }

    public int getDefenderScore() {
        return this.defenderScore;
    }

    public void setDefenderScore(final int defenderScore) {
        this.defenderScore = defenderScore;
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

    public RaidPhase getPhase() {
        return this.phase;
    }

    public void setPhase(RaidPhase phase) {
        this.phase = phase;
        if (phase == RaidPhase.GATHER) {
            startGather();
        } else if (phase == RaidPhase.TRAVEL) {
            startTravel();
        } else if (phase == RaidPhase.COMBAT) {
            startCombat();
        }
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

    private Runnable getTickLoop() {
        return () -> {
            if (homeBlockRaided != null) {
                raidedTown.setHomeBlock(homeBlockRaided);
                raidedTown.setSpawn(townSpawnRaided);
            }
            if (homeBlockGather != null) {
                gatherTown.setHomeBlock(homeBlockGather);
                gatherTown.setSpawn(townSpawnGather);
            }
            if (OldRaid.this.side1AreRaiders) {
                OldRaid.this.raiderPlayers = OldRaid.this.oldWar.getSide1Players();
                OldRaid.this.defenderPlayers = OldRaid.this.oldWar.getSide2Players();
            } else {
                OldRaid.this.raiderPlayers = OldRaid.this.oldWar.getSide2Players();
                OldRaid.this.defenderPlayers = OldRaid.this.oldWar.getSide1Players();
            }
            raidTicks += incremental;

            //check for end
            if (OldRaid.this.raidTicks >= RaidPhase.END.startTick && OldRaid.this.phase == RaidPhase.COMBAT) {
                OldRaid.this.phase = RaidPhase.END;
            }

            //check and start travel phase
            if (OldRaid.this.raidTicks >= RaidPhase.TRAVEL.startTick && OldRaid.this.phase == RaidPhase.GATHER) {
                startTravel();
            }

            //check and start combat phase
            if (OldRaid.this.raidTicks >= RaidPhase.COMBAT.startTick && OldRaid.this.phase == RaidPhase.TRAVEL) {
                startCombat();
            }

            //check and start gather phase
            if (OldRaid.this.raidTicks >= RaidPhase.GATHER.startTick && OldRaid.this.phase == RaidPhase.START) {
                startGather();
            }

            if (OldRaid.this.phase == RaidPhase.END) {
                //TODO: fix raid scoring
                //for raiders to win enough money, they need a significant up from the defenders, tie given to defender
                if (OldRaid.this.raiderScore > OldRaid.this.defenderScore) {
                    OldRaid.this.raidersWin(OldRaid.this.owner, OldRaid.this.raiderScore, OldRaid.this.defenderScore);
                } else {
                    OldRaid.this.defendersWin(OldRaid.this.raiderScore, OldRaid.this.defenderScore);
                }
            }

            //OldRaid gather phase behavior
            if (OldRaid.this.phase == RaidPhase.GATHER) {
                //Broadcast
                if (raidTicks % 6000 == 0) {
                    Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The raid of "
                        + OldRaid.this.getRaidedTown().getName() + " will begin in " + ((RaidPhase.TRAVEL.startTick - raidTicks) / 20 / 60) + " minutes!");
                    Bukkit.broadcastMessage(
                        "The Raiders are gathering at " + getGatherTown().getName() + " before making the journey over!");
                }


                //Prevent players from leaving gather town prematurely, if player is raid owner, dont do that
                for (String playerName : getActiveRaiders()) {
                    try {
                        Player p = Bukkit.getPlayer(playerName);
                        if (p != null) {
                            ArrayList<WorldCoord> cluster = Utils.getCluster(OldRaid.this.getGatherTown().getHomeBlock().getWorldCoord());
                            if (!cluster.contains(WorldCoord.parseWorldCoord(p))) {
                                if (p.equals(owner)) {
                                    p.teleport(gatherTown.getSpawn());
                                    p.sendMessage(UtilsChat.getPrefix() + "You cannot leave the gather town. As raid party leader, you have remained in the party. To stop the raid type /raid abandon");
                                    p.sendMessage(UtilsChat.getPrefix() + "You have been teleported to the gather town's spawn.");
                                } else {
                                    OldRaid.this.removeActiveRaider(p.getName());
                                    p.sendMessage(UtilsChat.getPrefix() + "By leaving the gathering town you have left the raid on " + OldRaid.this.getRaidedTown().getName() + "!");
                                    p.sendMessage(UtilsChat.getPrefix() + "To rejoin, do /raid join [oldWar] [town]");
                                }
                            }
                        }
                    } catch (NullPointerException | TownyException e) {
                        e.printStackTrace();
                    }
                }
            }

            //OldRaid Travel phase behavior
            else if (OldRaid.this.phase == RaidPhase.TRAVEL) {

                //Report
                if (raidTicks % 2400 == 0) {
                    Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The Raiders of "
                        + (side1AreRaiders ? OldRaid.this.oldWar.getSide1() : OldRaid.this.oldWar.getSide2())
                        + " are on their way to raid "
                        + OldRaid.this.getRaidedTown().getName() + "!");
                }

                //Check if a player has arrived at the town (in it, or within 200 blocks) and if so start combat
                for (String player : activeRaiders) {
                    try {
                        boolean playerCloseToHomeBlockRaid = false;
                        final int homeBlockXCoordRaided = OldRaid.this.getRaidedTown().getHomeBlock().getCoord().getX() * 16;
                        final int homeBlockZCoordRaided = OldRaid.this.getRaidedTown().getHomeBlock().getCoord().getZ() * 16;
                        Player p = Bukkit.getPlayer(player);
                        if (p != null) {
                            //Carryover from sieges
                            if (Math.abs(p.getLocation().getBlockX() - homeBlockXCoordRaided) <= 200 && Math.abs(p.getLocation().getBlockZ() - homeBlockZCoordRaided) <= 200) {
                                playerCloseToHomeBlockRaid = true;
                            }

                            WorldCoord playercoord = WorldCoord.parseWorldCoord(Bukkit.getPlayer(player));
                            if (OldRaid.this.getRaidedTown().hasTownBlock(playercoord) || playerCloseToHomeBlockRaid) {
                                startCombat();
                            }
                        }
                    } catch (TownyException e) {
                        e.printStackTrace();
                    }
                }
            }

            //OldRaid combat phase behavior
            else if (OldRaid.this.phase == RaidPhase.COMBAT) {

                //Report
                if (OldRaid.this.raidTicks % 6000 == 0) {
                    Bukkit.broadcastMessage(UtilsChat.getPrefix() + "Report on the raid of "
                        + OldRaid.this.getRaidedTown().getName() + ":");
                    Bukkit.broadcastMessage(
                        "Raider Score - " + OldRaid.this.raiderScore);
                    Bukkit.broadcastMessage(
                        "Defender Score - " + OldRaid.this.defenderScore);
                    Bukkit.broadcastMessage(
                        "Chunks Looted - " + OldRaid.this.lootedChunks.size() + "/" + raidedTown.getTownBlocks().size());
                }

                //Looting chunks
                for (String name : OldRaid.this.getActiveRaiders()) {
                    Player p = Bukkit.getPlayer(name);
                    if (p != null) {
                        WorldCoord wc = WorldCoord.parseWorldCoord(p);
                        if (p.isSneaking()) {
                            //check if our chunk is looted, if so skip this player
                            //Idk if thisll work but if it do then ye
                            if (OldRaid.this.lootedChunks.containsKey(wc)) {
                                if (OldRaid.this.lootedChunks.get(wc).finished) {
                                    p.sendMessage(UtilsChat.getPrefix() + "This chunk is already looted, try another.");
                                } else {
                                    doLootAt(p, wc);
                                }
                            } else {
                                if (OldRaid.this.getRaidedTown().hasTownBlock(wc)) {
                                    doLootAt(p, wc);
                                } else {
                                    p.sendMessage(UtilsChat.getPrefix() + "This space is not part of the raided town, you cannot loot this area.");
                                }
                            }
                        }
                    }
                }

                //refresh the existing display bar
                refreshDisplayBar();
            }
        };
    }

    public void refreshDisplayBar() {
        BossBar bossBar = Bukkit.getBossBar(bossBarKey);
        if (bossBar == null) bossBar = createNewDisplayBar();

        bossBar.setTitle(String.format("%d --  Raiders  -OldRaid Score- Defenders -- %d", OldRaid.this.raiderScore, OldRaid.this.defenderScore));
        bossBar.setProgress((OldRaid.this.raiderScore + 0.5D) / ((OldRaid.this.raiderScore + OldRaid.this.defenderScore) + 1.0D));

        for (String s : OldRaid.this.getActiveRaiders()) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) {
                if (!bossBar.getPlayers().contains(p)) bossBar.addPlayer(p);
            }
        }

        for (String s : OldRaid.this.getDefenderPlayers()) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) {
                if (!bossBar.getPlayers().contains(p)) bossBar.addPlayer(p);
            }
        }
    }

    public void setupDisplayBar() {
        BossBar bossBar = Bukkit.getBossBar(bossBarKey);
        if (bossBar == null) bossBar = createNewDisplayBar();

        for (String s : OldRaid.this.getActiveRaiders()) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) {
                bossBar.addPlayer(p);
            }
        }

        for (String s : OldRaid.this.getDefenderPlayers()) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) bossBar.addPlayer(p);
        }
    }

    public void deleteDisplayBar() {
        BossBar bossBar = Bukkit.getBossBar(bossBarKey);
        if (bossBar != null) {
            bossBar.removeAll();
        }
        Bukkit.removeBossBar(bossBarKey);

    }

    public BossBar createNewDisplayBar() {
        return Bukkit.createBossBar(bossBarKey, String.format("%d --  Raiders  -OldRaid Score- Defenders -- %d", OldRaid.this.raiderScore, OldRaid.this.defenderScore), BarColor.RED, BarStyle.SOLID);
    }

    public void save() {
        RaidData.saveRaid(this);
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

        public final WorldCoord worldCoord;
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
