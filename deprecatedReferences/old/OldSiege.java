package me.ShermansWorld.AlathranWars.deprecated;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import me.ShermansWorld.AlathranWars.Main;
import me.ShermansWorld.AlathranWars.data.SiegeData;
import me.ShermansWorld.AlathranWars.enums.TownWarState;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@Deprecated
public class OldSiege {

    public static final int maxSiegeTicks = 72000; // 60 minute tick constant
    public final NamespacedKey bossBarKey;
    public ArrayList<String> attackerPlayers;
    public ArrayList<String> defenderPlayers;
    public ArrayList<Location> beaconLocs;
    int[] bukkitId;
    private OldWar oldWar; // OldWar which siege belongs to
    private Town town; // Town of the siege
    private boolean side1AreAttackers; // bool of if side1 being attacker
    private int attackerPoints;
    private int defenderPoints;
    private OfflinePlayer siegeLeader;
    private int siegeTicks;
    private TownBlock homeBlock;
    private Location townSpawn;


    public OldSiege(final OldWar oldWar, final Town town, OfflinePlayer siegeLeader) {
        this.siegeTicks = 0;
        this.bukkitId = new int[1];
        this.attackerPlayers = new ArrayList<>();
        this.defenderPlayers = new ArrayList<>();
        this.beaconLocs = new ArrayList<>();
        this.oldWar = oldWar;
        this.town = town;
        this.siegeLeader = siegeLeader;

        bossBarKey = new NamespacedKey(Main.getInstance(), "siegeBar." + this.getName());

        side1AreAttackers = oldWar.getState(town.getName()) == TownWarState.SIDE2;
    }

    /**
     * Starts a siege
     */
    public void start() {
        attackerPoints = 0;
        defenderPoints = 0;
        this.siegeTicks = 0;
        homeBlock = town.getHomeBlockOrNull();
        townSpawn = town.getSpawnOrNull();

        this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), getTickLoop(), 0L, 200L);

        setupDisplayBar();
    }

    /**
     * Resumes a siege (after a server restart e.t.c.)
     */
    public void resume(int resumeTick) {

        siegeTicks = resumeTick;

        homeBlock = town.getHomeBlockOrNull();
        townSpawn = town.getSpawnOrNull();

        this.bukkitId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), getTickLoop(), 0L, 200L);

        setupDisplayBar();
    }

    /**
     * Stops a siege
     */
    public void stop() {
        deleteDisplayBar();
        Bukkit.getScheduler().cancelTask(this.bukkitId[0]);
        this.oldWar.getSieges().remove(this);
        SiegeData.removeSiege(this);

    }

    private Runnable getTickLoop() {
        return new Runnable() {
            int homeBlockControl = 0;

            @Override
            public void run() {
                if (homeBlock != null) {
                    town.setHomeBlock(homeBlock);
                    town.setSpawn(townSpawn);
                }
                if (OldSiege.this.side1AreAttackers) {
                    OldSiege.this.attackerPlayers = OldSiege.this.oldWar.getSide1Players();
                    OldSiege.this.defenderPlayers = OldSiege.this.oldWar.getSide2Players();
                } else {
                    OldSiege.this.attackerPlayers = OldSiege.this.oldWar.getSide2Players();
                    OldSiege.this.defenderPlayers = OldSiege.this.oldWar.getSide1Players();
                }
                if (OldSiege.this.siegeTicks >= maxSiegeTicks) {
                    Bukkit.getServer().getScheduler().cancelTask(OldSiege.this.bukkitId[0]);
                    SiegeData.removeSiege(OldSiege.this);
                    if (OldSiege.this.attackerPoints > OldSiege.this.defenderPoints) {
                        OldSiege.this.attackersWin(OldSiege.this.siegeLeader);
                    } else {
                        OldSiege.this.defendersWin();
                    }
                } else {
                    boolean attackersAreOnHomeBlock = false;
                    boolean defendersAreOnHomeBlock = false;
                    siegeTicks = siegeTicks + 200;
                    for (final String playerName : OldSiege.this.attackerPlayers) {
                        Player pl = Main.getInstance().getServer().getPlayer(playerName);
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
                            Main.warLogger.log(UtilsChat.getPrefix() + "Attempting to look for town at " + WorldCoord.parseWorldCoord(pl));
                        }
                    }
                    for (final String playerName : OldSiege.this.defenderPlayers) {
                        Player pl = Main.getInstance().getServer().getPlayer(playerName);
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
                            Main.warLogger.log(UtilsChat.getPrefix() + "Attempting to look for town at " + WorldCoord.parseWorldCoord(pl));
                        }
                    }
                    if (attackersAreOnHomeBlock && defendersAreOnHomeBlock) { // Contested
                        if (this.homeBlockControl != 1) {
                            for (final String playerName : OldSiege.this.attackerPlayers) {
                                try {
                                    Bukkit.getPlayer(playerName).sendMessage(UtilsChat.getPrefix()
                                        + "HomeBlock at " + OldSiege.this.town.getName() + " contested!");
                                } catch (NullPointerException ex5) {
                                }
                            }
                            for (final String playerName : OldSiege.this.defenderPlayers) {
                                try {
                                    Bukkit.getPlayer(playerName).sendMessage(UtilsChat.getPrefix()
                                        + "HomeBlock at " + OldSiege.this.town.getName() + " contested!");
                                } catch (NullPointerException ex6) {
                                }
                            }
                        }
                        this.homeBlockControl = 1;
                    } else if (attackersAreOnHomeBlock && !defendersAreOnHomeBlock) { // Attackers
                        if (this.homeBlockControl != 2) {
                            for (final String playerName : OldSiege.this.attackerPlayers) {
                                try {
                                    Bukkit.getPlayer(playerName).sendMessage(UtilsChat.getPrefix()
                                        + "Attackers have captured the HomeBlock at "
                                        + OldSiege.this.town.getName() + "! +1 Attacker Points per second");
                                } catch (NullPointerException ex7) {
                                }
                            }
                            for (final String playerName : OldSiege.this.defenderPlayers) {
                                try {
                                    Bukkit.getPlayer(playerName).sendMessage(UtilsChat.getPrefix()
                                        + "Attackers have captured the HomeBlock at "
                                        + OldSiege.this.town.getName() + "! +1 Attacker Points per second");
                                } catch (NullPointerException ex8) {
                                }
                            }
                        }
                        this.homeBlockControl = 2;
                        OldSiege.this.addPointsToAttackers(10);
                    } else { // Defenders / None
                        if (this.homeBlockControl != 3) {
                            for (final String playerName : OldSiege.this.attackerPlayers) {
                                try {
                                    Bukkit.getPlayer(playerName).sendMessage(UtilsChat.getPrefix()
                                        + "Defenders retain control of the HomeBlock at "
                                        + OldSiege.this.town.getName() + "! +1 Defender Points per second");
                                } catch (NullPointerException ex9) {
                                }
                            }
                            for (final String playerName : OldSiege.this.defenderPlayers) {
                                try {
                                    Bukkit.getPlayer(playerName).sendMessage(UtilsChat.getPrefix()
                                        + "Defenders retain control of the HomeBlock at "
                                        + OldSiege.this.town.getName() + "! +1 Defender Points per second");
                                } catch (NullPointerException ex10) {
                                }
                            }
                        }
                        OldSiege.this.addPointsToDefenders(10);
                        this.homeBlockControl = 3;
                    }
                    if (OldSiege.this.siegeTicks % 6000 == 0) { // Updates every 5 minutes
                        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "Report on the siege of "
                            + OldSiege.this.town.getName() + ":");
                        Bukkit.broadcastMessage(
                            "Attacker Points - " + OldSiege.this.attackerPoints);
                        Bukkit.broadcastMessage(
                            "Defender Points - " + OldSiege.this.defenderPoints);
                    }
                    if (siegeTicks % 1200 == 0) { // Saves every minute
                        save();
                    }
                }

                refreshDisplayBar();
            }


        };
    }

    public void attackersWin(final OfflinePlayer siegeLeader) {
        final Resident resident = TownyAPI.getInstance().getResident(siegeLeader.getUniqueId());
        Nation nation = null;
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The attackers have won the siege of " + this.town.getName() + "!");
        try {
            nation = resident.getTown().getNation();
        } catch (Exception e) {
        }
        if (nation != null) {
            Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The town of " + this.town.getName()
                + " has been placed under occupation by " + nation.getName() + "!");
        }
        Main.econ.depositPlayer(siegeLeader, 2500.0);
        double amt = 0.0;
        if (this.town.getAccount().getHoldingBalance() > 10000.0) {
            amt = Math.floor(this.town.getAccount().getHoldingBalance()) / 4.0;
            this.town.getAccount().withdraw(amt, "oldWar loot");
        } else {
            if (this.town.getAccount().getHoldingBalance() < 2500.0) {
                amt = this.town.getAccount().getHoldingBalance();
                Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The town of " + this.town.getName()
                    + " has been destroyed by " + this.getAttackerSide() + "!");
                TownyUniverse.getInstance().getDataSource().deleteTown(this.town);
                Main.econ.depositPlayer(siegeLeader, amt);
                return;
            }
            this.town.getAccount().withdraw(2500.0, "oldWar loot");
            amt = 2500.0;
        }
        Bukkit.broadcastMessage("The town of " + this.town.getName() + " has been sacked by " + this.getAttackerSide()
            + ", valuing $" + amt);
        Main.warLogger.log("The town of " + this.town.getName() + " has been sacked by " + this.getAttackerSide()
            + ", valuing $" + amt);
        Main.econ.depositPlayer(siegeLeader, amt);

        if (side1AreAttackers) {
            oldWar.addSide1Points(50);
        } else {
            oldWar.addSide2Points(50);
        }

        stop();
        clearBeacon();
    }

    public void defendersWin() {
        this.town.getAccount().deposit(2500.0, "OldWar chest");
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The defenders have won the siege of " + this.town.getName() + "!");
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + this.town.getName()
            + " has recovered the attackers' oldWar chest, valued at $2,500");
        Main.warLogger
            .log(oldWar.getName() + ": The defenders have won the siege of " + this.town.getName() + "!");

        if (side1AreAttackers) {
            oldWar.addSide2Points(50);
        } else {
            oldWar.addSide1Points(50);
        }

        stop();
        clearBeacon();
    }

    /**
     * No winnder declared
     */
    public void noWinner() {
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The siege of " + this.town.getName() + " was a draw!");
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "No money has been recovered.");
        Main.warLogger
            .log(oldWar.getName() + ": No one won the siege of " + this.town.getName() + "!");
        stop();
        clearBeacon();
    }

    public void createBeacon() {
        try {
            World world = town.getWorld();
            int homeBlockX = this.town.getHomeBlock().getX() * 16;
            int homeBlockZ = (this.town.getHomeBlock().getZ() + 1) * 16;
            if (homeBlockX > 0) {
                homeBlockX -= 8;
            }
            if (homeBlockZ > 0) {
                homeBlockZ -= 8;
            }
            if (homeBlockX < 0) {
                homeBlockX += 8;
            }
            if (homeBlockZ < 0) {
                homeBlockZ += 8;
            }
            int homeBlockY = 253;
            final Location beaconLoc = new Location(this.town.getWorld(), homeBlockX,
                homeBlockY - 2, homeBlockZ);
            while (beaconLoc.getBlock().getType().equals(Material.AIR)) {
                --homeBlockY;
                beaconLoc.setY(homeBlockY);
            }
            homeBlockY += 2;
            this.beaconLocs.add(new Location(world, homeBlockX, homeBlockY, homeBlockZ));
            this.beaconLocs
                .add(new Location(world, homeBlockX, homeBlockY + 1, homeBlockZ));
            this.beaconLocs
                .add(new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ));
            this.beaconLocs.add(
                new Location(world, homeBlockX + 1, homeBlockY - 1, homeBlockZ));
            this.beaconLocs.add(
                new Location(world, homeBlockX - 1, homeBlockY - 1, homeBlockZ));
            this.beaconLocs.add(
                new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ + 1));
            this.beaconLocs.add(
                new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ - 1));
            this.beaconLocs.add(new Location(world, homeBlockX + 1, homeBlockY - 1,
                homeBlockZ + 1));
            this.beaconLocs.add(new Location(world, homeBlockX + 1, homeBlockY - 1,
                homeBlockZ - 1));
            this.beaconLocs.add(new Location(world, homeBlockX - 1, homeBlockY - 1,
                homeBlockZ + 1));
            this.beaconLocs.add(new Location(world, homeBlockX - 1, homeBlockY - 1,
                homeBlockZ - 1));
            for (int i = 2; i < this.beaconLocs.size(); ++i) {
                if (this.beaconLocs.get(i).getBlock().getType() != Material.AIR) {
                    i = 1;
                    homeBlockY++;
                    for (final Location loc : this.beaconLocs) {
                        loc.setY(loc.getY() + 1.0);
                    }
                }
            }
            this.beaconLocs.get(0).getBlock().setType(Material.BEACON);
            this.beaconLocs.get(1).getBlock().setType(Material.RED_STAINED_GLASS);
            for (int i = 2; i < this.beaconLocs.size(); ++i) {
                this.beaconLocs.get(i).getBlock().setType(Material.IRON_BLOCK);
            }
            save();
        } catch (TownyException ex) {
        }
    }

    public void resetBeacon(World world, int homeBlockX, int homeBlockY, int homeBlockZ) {
        this.beaconLocs.add(new Location(world, homeBlockX, homeBlockY, homeBlockZ));
        this.beaconLocs.add(new Location(world, homeBlockX, homeBlockY + 1, homeBlockZ));
        this.beaconLocs.add(new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ));
        this.beaconLocs
            .add(new Location(world, homeBlockX + 1, homeBlockY - 1, homeBlockZ));
        this.beaconLocs
            .add(new Location(world, homeBlockX - 1, homeBlockY - 1, homeBlockZ));
        this.beaconLocs
            .add(new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ + 1));
        this.beaconLocs
            .add(new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ - 1));
        this.beaconLocs.add(
            new Location(world, homeBlockX + 1, homeBlockY - 1, homeBlockZ + 1));
        this.beaconLocs.add(
            new Location(world, homeBlockX + 1, homeBlockY - 1, homeBlockZ - 1));
        this.beaconLocs.add(
            new Location(world, homeBlockX - 1, homeBlockY - 1, homeBlockZ + 1));
        this.beaconLocs.add(
            new Location(world, homeBlockX - 1, homeBlockY - 1, homeBlockZ - 1));
    }

    public void clearBeacon() {
        for (final Location loc : this.beaconLocs) {
            loc.getBlock().setType(Material.AIR);
        }
    }

    public void addPointsToAttackers(final int points) {
        this.attackerPoints += points;
    }

    public void addPointsToDefenders(final int points) {
        this.defenderPoints += points;
    }

    public OldWar getWar() {
        return this.oldWar;
    }

    public void setWar(final OldWar oldWar) {
        this.oldWar = oldWar;
    }

    public Town getTown() {
        return this.town;
    }

    public void setTown(final Town town) {
        this.town = town;
    }

    public OfflinePlayer getSiegeOwner() {
        return siegeLeader;
    }

    public void setSiegeOwner(OfflinePlayer siegeOwner) {
        this.siegeLeader = siegeOwner;
    }

    /**
     * Gets attacker name string
     */
    public String getAttackerSide() {
        if (side1AreAttackers) {
            return oldWar.getSide1();
        } else {
            return oldWar.getSide2();
        }
    }

    /**
     * Gets defender name string
     */
    public String getDefenderSide() {
        if (side1AreAttackers) {
            return oldWar.getSide2();
        } else {
            return oldWar.getSide1();
        }
    }

    public void refreshDisplayBar() {
        BossBar bossBar = Bukkit.getBossBar(bossBarKey);
        if (bossBar == null) bossBar = createNewDisplayBar();

        bossBar.setTitle(String.format("%d -- Attackers -OldSiege Score- Defenders -- %d", this.attackerPoints, this.defenderPoints));
        bossBar.setProgress((this.attackerPoints + 0.5D) / ((this.attackerPoints + this.defenderPoints) + 1.0D));

        for (String s : this.getAttackerPlayers()) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) {
                if (!bossBar.getPlayers().contains(p)) bossBar.addPlayer(p);
            }
        }

        for (String s : this.getDefenderPlayers()) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) {
                if (!bossBar.getPlayers().contains(p)) bossBar.addPlayer(p);
            }
        }
    }

    public void setupDisplayBar() {
        BossBar bossBar = Bukkit.getBossBar(bossBarKey);
        if (bossBar == null) bossBar = createNewDisplayBar();

        for (String s : this.getAttackerPlayers()) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) {
                bossBar.addPlayer(p);
            }
        }

        for (String s : this.getDefenderPlayers()) {
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
        return Bukkit.createBossBar(bossBarKey, String.format("%d -- Attackers -OldSiege Score- Defenders -- %d", this.attackerPoints, this.defenderPoints), BarColor.YELLOW, BarStyle.SOLID);
    }

    public int getAttackerPoints() {
        return this.attackerPoints;
    }

    public void setAttackerPoints(int points) {
        this.attackerPoints = points;
    }

    public int getDefenderPoints() {
        return this.defenderPoints;
    }

    public void setDefenderPoints(int points) {
        this.defenderPoints = points;
    }

    public int getMaxSiegeTicks() {
        return maxSiegeTicks;
    }

    public TownBlock getHomeBlock() {
        return homeBlock;
    }

    public void setHomeBlock(TownBlock homeBlock) {
        this.homeBlock = homeBlock;
    }

    public Location getTownSpawn() {
        return townSpawn;
    }

    public void setTownSpawn(Location townSpawn) {
        this.townSpawn = townSpawn;
    }

    public boolean getSide1AreAttackers() {
        return this.side1AreAttackers;
    }

    public void setSide1AreAttackers(final boolean side1AreAttackers) {
        this.side1AreAttackers = side1AreAttackers;
    }

    public ArrayList<String> getAttackerPlayers() {
        return this.attackerPlayers;
    }

    public void setAttackerPlayer(final ArrayList<String> attackerPlayers) {
        this.attackerPlayers = attackerPlayers;
    }

    public ArrayList<String> getDefenderPlayers() {
        return this.defenderPlayers;
    }

    public void setDefenderPlayer(final ArrayList<String> defenderPlayers) {
        this.defenderPlayers = this.attackerPlayers;
    }

    public int getSiegeTicks() {
        return siegeTicks;
    }

    public void setSiegeTicks(int siegeTicks) {
        this.siegeTicks = siegeTicks;
    }

    public OfflinePlayer getSiegeLeader() {
        return siegeLeader;
    }

    public void save() {
        SiegeData.saveSiege(this);
    }

    public String getName() {
        return this.getWar().getName() + "-" + this.getTown();
    }
}
