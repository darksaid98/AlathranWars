package me.ShermansWorld.AlathranWars.conflict.battle.siege;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import me.ShermansWorld.AlathranWars.Main;
import me.ShermansWorld.AlathranWars.conflict.War;
import me.ShermansWorld.AlathranWars.conflict.battle.Battle;
import me.ShermansWorld.AlathranWars.conflict.Side;
import me.ShermansWorld.AlathranWars.enums.BattleTeam;
import me.ShermansWorld.AlathranWars.utility.UtilsChat;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Siege extends Battle {
    public static final int MAX_SIEGE_TICKS = 72000; // 60 minute tick constant

    public Set<Player> attackerPlayers = new HashSet<>();
    public Set<Player> defenderPlayers = new HashSet<>();
    public Set<UUID> attackerPlayersIncludingOffline = new HashSet<>();
    public Set<UUID> defenderPlayersIncludingOffline = new HashSet<>();
    // TODO On join during siege, if part of list re-add to battle
    // TODO On leave during siege, if part clean up misc data

    public ArrayList<Location> beaconLocations;

    int[] bukkitId = new int[1];
    private int siegeTicks = 0;

    private War war; // War which siege belongs to
    private Town town; // Town of the siege
    private boolean side1AreAttackers; // bool of if side1 being attacker

    private OfflinePlayer siegeLeader;
    private TownBlock homeBlock;
    private Location townSpawn;
    private BossBar activeBossBar = null;

    public Siege(final War war, final Town town, Player siegeLeader) {
        this.beaconLocations = new ArrayList<>();

        this.war = war;
        this.town = town;

        this.siegeLeader = siegeLeader;

        side1AreAttackers = war.getTownSide(town).getTeam().equals(BattleTeam.SIDE_2);
    }

    /**
     * Starts a siege
     */
    public void start() {
        super.setAttackerPoints(0);
        super.setDefenderPoints(0);
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
        this.war.getSieges().remove(this);
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
                    if (Siege.super.getAttackerPoints() > Siege.super.getDefenderPoints()) {
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
                            "Attacker Points - " + Siege.super.getAttackerPoints());
                        Bukkit.broadcastMessage(
                            "Defender Points - " + Siege.super.getDefenderPoints());
                    }

                    /*if (siegeTicks % 1200 == 0) { // Saves every minute
                        save();
                    }*/
                }

                updateDisplayBar();
            }
        };
    }

    public void attackersWin(final OfflinePlayer siegeLeader) {
        final Resident resident = TownyAPI.getInstance().getResident(siegeLeader.getUniqueId());
        Nation nation = null;
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The attackers have won the siege of " + this.town.getName() + "!");
        try {
            nation = resident.getTown().getNation();
        } catch (Exception ignored) {
        }
        if (nation != null) {
            Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The town of " + this.town.getName()
                + " has been placed under occupation by " + nation.getName() + "!");
        }
//        Main.econ.depositPlayer(siegeLeader, 2500.0);
//        double amt = 0.0;
//        if (this.town.getAccount().getHoldingBalance() > 10000.0) {
//            amt = Math.floor(this.town.getAccount().getHoldingBalance()) / 4.0;
//            this.town.getAccount().withdraw(amt, "war loot");
//        } else {
//            if (this.town.getAccount().getHoldingBalance() < 2500.0) {
//                amt = this.town.getAccount().getHoldingBalance();
//                Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The town of " + this.town.getName()
//                    + " has been destroyed by " + this.getAttackerSide() + "!");
//                TownyUniverse.getInstance().getDataSource().deleteTown(this.town);
//                Main.econ.depositPlayer(siegeLeader, amt);
//                return;
//            }
//            this.town.getAccount().withdraw(2500.0, "war loot");
//            amt = 2500.0;
//        }
        /*Bukkit.broadcastMessage("The town of " + this.town.getName() + " has been sacked by " + this.getAttackerSide()
            + ", valuing $" + amt);*/
        Bukkit.broadcastMessage("The town of " + this.town.getName() + " has been sacked by " + this.getAttackerSide() + "!");
//        Main.warLogger.log("The town of " + this.town.getName() + " has been sacked by " + this.getAttackerSide()
//            + ", valuing $" + amt);
//        Main.econ.depositPlayer(siegeLeader, amt);

        if (side1AreAttackers) {
            war.getSide1().addScore(50);
        } else {
            war.getSide2().addScore(50);
        }

        stop();
        clearBeacon();
    }

    public void defendersWin() {
//        this.town.getAccount().deposit(2500.0, "War chest");
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The defenders have won the siege of " + this.town.getName() + "!");
//        Bukkit.broadcastMessage(UtilsChat.getPrefix() + this.town.getName()
//            + " has recovered the attackers' war chest, valued at $2,500");
//        Main.warLogger
//            .log(war.getName() + ": The defenders have won the siege of " + this.town.getName() + "!");

        if (side1AreAttackers) {
            war.getSide2().addScore(50);
        } else {
            war.getSide1().addScore(50);
        }

        stop();
        clearBeacon();
    }

    /**
     * No winnder declared
     */
    public void noWinner() {
        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The siege of " + this.town.getName() + " has ended in a draw!");
//        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "No money has been recovered.");
//        Main.warLogger
//            .log(war.getName() + ": No one won the siege of " + this.town.getName() + "!");
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
            this.beaconLocations.add(new Location(world, homeBlockX, homeBlockY, homeBlockZ));
            this.beaconLocations
                .add(new Location(world, homeBlockX, homeBlockY + 1, homeBlockZ));
            this.beaconLocations
                .add(new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ));
            this.beaconLocations.add(
                new Location(world, homeBlockX + 1, homeBlockY - 1, homeBlockZ));
            this.beaconLocations.add(
                new Location(world, homeBlockX - 1, homeBlockY - 1, homeBlockZ));
            this.beaconLocations.add(
                new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ + 1));
            this.beaconLocations.add(
                new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ - 1));
            this.beaconLocations.add(new Location(world, homeBlockX + 1, homeBlockY - 1,
                homeBlockZ + 1));
            this.beaconLocations.add(new Location(world, homeBlockX + 1, homeBlockY - 1,
                homeBlockZ - 1));
            this.beaconLocations.add(new Location(world, homeBlockX - 1, homeBlockY - 1,
                homeBlockZ + 1));
            this.beaconLocations.add(new Location(world, homeBlockX - 1, homeBlockY - 1,
                homeBlockZ - 1));
            for (int i = 2; i < this.beaconLocations.size(); ++i) {
                if (this.beaconLocations.get(i).getBlock().getType() != Material.AIR) {
                    i = 1;
                    homeBlockY++;
                    for (final Location loc : this.beaconLocations) {
                        loc.setY(loc.getY() + 1.0);
                    }
                }
            }
            this.beaconLocations.get(0).getBlock().setType(Material.BEACON);
            this.beaconLocations.get(1).getBlock().setType(Material.RED_STAINED_GLASS);
            for (int i = 2; i < this.beaconLocations.size(); ++i) {
                this.beaconLocations.get(i).getBlock().setType(Material.IRON_BLOCK);
            }
//            save();
        } catch (TownyException ignored) {
        }
    }

    public void resetBeacon(World world, int homeBlockX, int homeBlockY, int homeBlockZ) {
        this.beaconLocations.add(new Location(world, homeBlockX, homeBlockY, homeBlockZ));
        this.beaconLocations.add(new Location(world, homeBlockX, homeBlockY + 1, homeBlockZ));
        this.beaconLocations.add(new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ));
        this.beaconLocations
            .add(new Location(world, homeBlockX + 1, homeBlockY - 1, homeBlockZ));
        this.beaconLocations
            .add(new Location(world, homeBlockX - 1, homeBlockY - 1, homeBlockZ));
        this.beaconLocations
            .add(new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ + 1));
        this.beaconLocations
            .add(new Location(world, homeBlockX, homeBlockY - 1, homeBlockZ - 1));
        this.beaconLocations.add(
            new Location(world, homeBlockX + 1, homeBlockY - 1, homeBlockZ + 1));
        this.beaconLocations.add(
            new Location(world, homeBlockX + 1, homeBlockY - 1, homeBlockZ - 1));
        this.beaconLocations.add(
            new Location(world, homeBlockX - 1, homeBlockY - 1, homeBlockZ + 1));
        this.beaconLocations.add(
            new Location(world, homeBlockX - 1, homeBlockY - 1, homeBlockZ - 1));
    }

    public void clearBeacon() {
        for (final Location loc : this.beaconLocations) {
            loc.getBlock().setType(Material.AIR);
        }
    }

    @NotNull
    public War getWar() {
        return this.war;
    }

    public void setWar(final War war) {
        this.war = war;
    }

    @NotNull
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
    public @NotNull Side getAttackerSide() {
        return side1AreAttackers ? war.getSide1() : war.getSide2();
    }

    /**
     * Gets defender name string
     */
    public @NotNull Side getDefenderSide() {
        return side1AreAttackers ? war.getSide2() : war.getSide1();
    }

    public void updateDisplayBar() {
        if (activeBossBar == null)
            createNewDisplayBar();

        activeBossBar.name(
            new ColorParser("<score1> -- Attackers -Siege Score- Defenders -- <score2>")
                .parseMinimessagePlaceholder("score1", String.valueOf(super.getAttackerPoints()))
                .parseMinimessagePlaceholder("score2", String.valueOf(super.getDefenderPoints()))
                .build()
        );
        activeBossBar.progress((float) ((super.getAttackerPoints() + 0.5D) / ((super.getAttackerPoints() + super.getDefenderPoints()) + 1.0D)));
    }

    public void setupDisplayBar() {
        if (activeBossBar == null)
            createNewDisplayBar();

        for (Player p : this.getAttackerPlayers()) {
            if (p != null) {
                p.showBossBar(activeBossBar);
//                bossBar.addViewer(p);
//                bossBar.addPlayer(p);
            }
        }

        for (Player p : this.getDefenderPlayers()) {
            if (p != null) {
                p.showBossBar(activeBossBar);
//                bossBar.addViewer(p);
//                bossBar.addPlayer(p);
            }
        }
    }

    public void createNewDisplayBar() {
        final Component text = new ColorParser("<score1> -- Attackers -Siege Score- Defenders -- <score2>")
            .parseMinimessagePlaceholder("score1", String.valueOf(super.getAttackerPoints()))
            .parseMinimessagePlaceholder("score2", String.valueOf(super.getDefenderPoints()))
            .build();

        this.activeBossBar = BossBar.bossBar(text, 0, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_6);
    }

    public void deleteDisplayBar() {
        if (activeBossBar != null) {
            for (Player p : this.getAttackerPlayers()) {
                if (p != null) {
                    activeBossBar.removeViewer(p);
                }
            }

            for (Player p : this.getDefenderPlayers()) {
                if (p != null) {
                    activeBossBar.removeViewer(p);
                }
            }

            activeBossBar = null;
        }
    }

    public int getMaxSiegeTicks() {
        return MAX_SIEGE_TICKS;
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

    public Set<Player> getAttackerPlayers() {
        return this.attackerPlayers;
    }

    public void setAttackerPlayer(final Set<Player> attackerPlayers) {
        this.attackerPlayers = attackerPlayers;
    }

    public Set<Player> getDefenderPlayers() {
        return this.defenderPlayers;
    }

    public void setDefenderPlayer(final Set<Player> defenderPlayers) {
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
//        SiegeData.saveSiege(this);
    }

    public String getName() {
        return this.getWar().getName() + "-" + this.getTown();
    }
}
