package com.github.alathra.AlathranWars.conflict.battle.siege;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.Battle;
import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.enums.BattleTeam;
import com.github.alathra.AlathranWars.utility.SQLQueries;
import com.github.alathra.AlathranWars.utility.UUIDUtil;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Siege extends Battle {
    public static final int MAX_SIEGE_TICKS = 72000; // 60 minute tick constant
    public static final Duration SIEGE_DURATION = Duration.ofMinutes(60);
    private static final int MAX_SIEGE_PROGRESS_MINUTES = 8;
    public static final int MAX_SIEGE_PROGRESS = 60 * MAX_SIEGE_PROGRESS_MINUTES; // On reaching this, the attackers win. 1 point is added per second
    public static final Duration ATTACKERS_MUST_TOUCH_END = Duration.ofMinutes(40); // If point is not touched in this much time, defenders win
    public static final Duration ATTACKERS_MUST_TOUCH_REVERT = Duration.ofMinutes(2); // If point is not touched in this much time, siege progress reverts
    public static final int BATTLEFIELD_RANGE = 300;

    private final UUID uuid;
    private final Set<Player> attackers = new HashSet<>(); // Players inside battlefield
    private final Set<Player> defenders = new HashSet<>(); // Players inside battlefield
    private final Set<Player> attackerPlayers = new HashSet<>();
    private final Set<Player> defenderPlayers = new HashSet<>();
    private final Set<UUID> attackerPlayersIncludingOffline = new HashSet<>();
    private final Set<UUID> defenderPlayersIncludingOffline = new HashSet<>();

    private Instant endTime;
    private int siegeProgress = 0;
    private Instant lastTouched;

    private SiegeRunnable siegeRunnable;

    private War war; // War which siege belongs to
    private Town town; // Town of the siege
    private boolean side1AreAttackers; // bool of if side1 being attacker

    private OfflinePlayer siegeLeader;
    private @Nullable TownBlock homeBlock = null;
    private @Nullable Location townSpawn = null;
    private @Nullable BossBar activeBossBar = null;

    public Siege(final War war, final Town town, Player siegeLeader) {
        uuid = UUIDUtil.generateSiegeUUID();

        endTime = Instant.now().plus(SIEGE_DURATION);
        lastTouched = Instant.now();

        this.war = war;
        this.town = town;
        this.siegeLeader = siegeLeader;

        side1AreAttackers = war.getTownSide(town).getTeam().equals(BattleTeam.SIDE_2);

        if (getSide1AreAttackers()) {
            attackerPlayersIncludingOffline.addAll(getWar().getSide1().getPlayersIncludingOffline());
            defenderPlayersIncludingOffline.addAll(getWar().getSide2().getPlayersIncludingOffline());
        } else {
            attackerPlayersIncludingOffline.addAll(getWar().getSide2().getPlayersIncludingOffline());
            defenderPlayersIncludingOffline.addAll(getWar().getSide1().getPlayersIncludingOffline());
        }
        calculateOnlinePlayers();
    }

    /**
     * Starts a siege
     */
    public void start() {
        siegeRunnable = new SiegeRunnable(this);
    }

    /**
     * Resumes a siege (after a server restart e.t.c.)
     */
    public void resume(int resumeTick) {
        siegeRunnable = new SiegeRunnable(this, resumeTick);
    }

    /**
     * Stops a siege
     */
    public void stop() {
        siegeRunnable.cancel();
        SQLQueries.deleteSiege(this);
        war.removeSiege(this);
    }

    public void  attackersWin(final OfflinePlayer siegeLeader) {
        final Resident resident = TownyAPI.getInstance().getResident(siegeLeader.getUniqueId());
        Nation nation = null;
        Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The attackers have won the siege of " + town.getName() + "!").build());

        try {
            nation = resident.getTown().getNation();
        } catch (Exception ignored) {
        }

        if (nation != null) {
            Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The town of " + town.getName()
                + " has been placed under occupation by " + nation.getName() + "!").build());
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
        Bukkit.broadcast(new ColorParser("The town of " + this.town.getName() + " has been sacked by " + this.getAttackerSide().getName() + "!").build());
//        Main.warLogger.log("The town of " + this.town.getName() + " has been sacked by " + this.getAttackerSide()
//            + ", valuing $" + amt);
//        Main.econ.depositPlayer(siegeLeader, amt);

        if (side1AreAttackers) {
            war.getSide1().addScore(50);
        } else {
            war.getSide2().addScore(50);
        }

        stop();
        //clearBeacon();
    }

    public void defendersWin() {
//        this.town.getAccount().deposit(2500.0, "War chest");
        Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The defenders have won the siege of " + this.town.getName() + "!").build());
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
        //clearBeacon();
    }

    /**
     * No winner declared
     */
    public void noWinner() {
        Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The siege of " + this.town.getName() + " has ended in a draw!").build());
//        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "No money has been recovered.");
//        Main.warLogger
//            .log(war.getName() + ": No one won the siege of " + this.town.getName() + "!");
        stop();
    }

    @NotNull
    public Instant getLastTouched() {
        return lastTouched;
    }

    public void setLastTouched(Instant lastTouched) {
        this.lastTouched = lastTouched;
    }

    public int getSiegeProgress() {
        return siegeProgress;
    }

    public float getSiegeProgressPercentage() {
        return (getSiegeProgress() * 1.0f) / MAX_SIEGE_PROGRESS;
    }

    public void setSiegeProgress(int siegeProgress) {
        this.siegeProgress = siegeProgress;
    }

    @NotNull
    public War getWar() {
        return war;
    }

    public void setWar(final War war) {
        this.war = war;
    }

    @NotNull
    public Town getTown() {
        return town;
    }

    public void setTown(final Town town) {
        this.town = town;
    }

    /**
     * Gets attacker name string
     */
    @NotNull
    public Side getAttackerSide() {
        return side1AreAttackers ? war.getSide1() : war.getSide2();
    }

    /**
     * Gets defender name string
     */
    @NotNull
    public Side getDefenderSide() {
        return side1AreAttackers ? war.getSide2() : war.getSide1();
    }

    public void updateDisplayBar(CaptureProgressDirection progressDirection) {
        if (activeBossBar == null)
            createNewDisplayBar();

        /*for (BossBarViewer viewer : activeBossBar.viewers()) { // TODO Better perf

        }*/
        for (Player p : Bukkit.getOnlinePlayers()) {
            activeBossBar.removeViewer(p);
        }

        for (Player p : this.getAttackers()) {
            if (p != null)
                p.showBossBar(activeBossBar);
        }

        for (Player p : this.getDefenders()) {
            if (p != null)
                p.showBossBar(activeBossBar);
        }

        String color = "<yellow>";

        switch (progressDirection) {
            case UP -> {
                if (getAttackerSide().getSide().equals(BattleSide.ATTACKER)) {
                    activeBossBar.color(BossBar.Color.RED);
                    color = "<red>";
                } else {
                    activeBossBar.color(BossBar.Color.BLUE);
                    color = "<blue>";
                }
            }
            case NONE -> activeBossBar.color(BossBar.Color.YELLOW);
            case DOWN -> {
                if (getAttackerSide().getSide().equals(BattleSide.ATTACKER)) {
                    activeBossBar.color(BossBar.Color.BLUE);
                    color = "<blue>";
                } else {
                    activeBossBar.color(BossBar.Color.RED);
                    color = "<red>";
                }
            }
        }

        if (Instant.now().isBefore(getEndTime())) {
            activeBossBar.name(
                new ColorParser("<gray>Capture Progress: %s<progress> <gray>Time: %s<time>min".formatted(color, color))
                    .parseMinimessagePlaceholder("progress", "%.0f%%".formatted(getSiegeProgressPercentage() * 100))
                    .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now() , getEndTime()).toMinutes()))
                    .build()
            );
        } else {
            activeBossBar.name(
                new ColorParser("%sOVERTIME".formatted(color)).build()
            );
        }
        activeBossBar.progress(getSiegeProgressPercentage());
    }

    public void createNewDisplayBar() {
        final Component text = new ColorParser("<gray>Capture Progress: <yellow><progress> <gray>Time: <yellow><time>min")
            .parseMinimessagePlaceholder("progress", "%.0f%%".formatted(getSiegeProgressPercentage() * 100))
            .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now() , getEndTime()).toMinutesPart()))
            .build();

        this.activeBossBar = BossBar.bossBar(text, 0, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_10);
    }

    public void deleteDisplayBar() {
        if (activeBossBar != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                activeBossBar.removeViewer(p);
            }

            activeBossBar = null;
        }
    }

    // SECTION UUID

    public UUID getUUID() {
        return uuid;
    }

    /**
     * Equals boolean.
     *
     * @param uuid the uuid
     * @return the boolean
     */
    public boolean equals(UUID uuid) {
        return getUUID().equals(uuid);
    }

    /**
     * Equals boolean.
     *
     * @param siege the siege
     * @return the boolean
     */
    public boolean equals(Siege siege) {
        return getUUID().equals(siege.getUUID());
    }

    // Accessors & Getters

    @Nullable
    public TownBlock getHomeBlock() {
        return homeBlock;
    }

    public void setHomeBlock(TownBlock homeBlock) {
        this.homeBlock = homeBlock;
    }

    @Nullable
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

    @NotNull
    public Set<Player> getAttackers() {
        return this.attackers;
    }

    @NotNull
    public Set<Player> getDefenders() {
        return this.defenders;
    }

    @NotNull
    public Set<Player> getAttackerPlayers() {
        return this.attackerPlayers;
    }

    @NotNull
    public Set<Player> getDefenderPlayers() {
        return this.defenderPlayers;
    }

    @NotNull
    public OfflinePlayer getSiegeLeader() {
        return siegeLeader;
    }

    public void setSiegeLeader(OfflinePlayer siegeLeader) {
        this.siegeLeader = siegeLeader;
    }

    @NotNull
    public String getName() {
        return this.getWar().getName() + "-" + this.getTown();
    }

    // SECTION Time management

    public void setEndTime(Instant time) {
        endTime = time;
    }

    @NotNull
    public Instant getEndTime() {
        return endTime;
    }

    // SECTION Player management

    public boolean isPlayerInSiege(UUID uuid) {
        return attackerPlayersIncludingOffline.contains(uuid) || defenderPlayersIncludingOffline.contains(uuid);
    }

    public boolean isPlayerInSiege(Player p) {
        return isPlayerInSiege(p.getUniqueId());
    }

    public BattleSide getPlayerSideInSiege(UUID uuid) {
        if (attackerPlayersIncludingOffline.contains(uuid))
            return BattleSide.ATTACKER;

        if (defenderPlayersIncludingOffline.contains(uuid))
            return BattleSide.DEFENDER;

        return BattleSide.SPECTATOR;
    }

    public BattleSide getPlayerSideInSiege(Player p) {
        return getPlayerSideInSiege(p.getUniqueId());
    }

    public void addPlayer(Player p, BattleSide side) {
        addPlayer(p.getUniqueId(), side);
    }

    public void addPlayer(OfflinePlayer offlinePlayer, BattleSide side) {
        if (offlinePlayer.hasPlayedBefore())
            addPlayer(offlinePlayer.getUniqueId(), side);
    }

    public void addPlayer(UUID uuid, BattleSide side) {
        if (isPlayerInSiege(uuid)) return;

        switch (side) {
            case ATTACKER -> attackerPlayersIncludingOffline.add(uuid);
            case DEFENDER -> defenderPlayersIncludingOffline.add(uuid);
        }

        if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
            final Player p = Bukkit.getPlayer(uuid);
            addOnlinePlayer(p, side);
        }
    }

    public void addOnlinePlayer(Player p, BattleSide side) {
        switch (side) {
            case ATTACKER -> attackerPlayers.add(p);
            case DEFENDER -> defenderPlayers.add(p);
        }
    }

    public void removePlayer(Player p) {
        removePlayer(p.getUniqueId());
    }

    public void removePlayer(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.hasPlayedBefore())
            removePlayer(offlinePlayer.getUniqueId());
    }

    public void removePlayer(UUID uuid) {
        if (!isPlayerInSiege(uuid)) return;

        final BattleSide side = getPlayerSideInSiege(uuid);

        switch (side) {
            case ATTACKER -> attackerPlayersIncludingOffline.remove(uuid);
            case DEFENDER -> defenderPlayersIncludingOffline.remove(uuid);
        }

        if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
            final Player p = Bukkit.getPlayer(uuid);
            removeOnlinePlayer(p, side);
        }
    }

    public void removeOnlinePlayer(Player p, BattleSide side) {
        switch (side) {
            case ATTACKER -> attackerPlayers.remove(p);
            case DEFENDER -> defenderPlayers.remove(p);
        }
    }

    public void calculateOnlinePlayers() {
        final Set<Player> onlineAttackers = attackerPlayersIncludingOffline.stream()
            .filter(uuid1 -> Bukkit.getOfflinePlayer(uuid1).isOnline())
            .map(Bukkit::getPlayer)
            .collect(Collectors.toSet());

        attackerPlayers.clear();
        attackerPlayers.addAll(onlineAttackers);

        final Set<Player> onlineDefenders = defenderPlayersIncludingOffline.stream()
            .filter(uuid1 -> Bukkit.getOfflinePlayer(uuid1).isOnline())
            .map(Bukkit::getPlayer)
            .collect(Collectors.toSet());

        defenderPlayers.clear();
        defenderPlayers.addAll(onlineDefenders);
    }

    public void calculateBattlefieldPlayers(Location location) {
        final Set<Player> attackersOnBattlefield = attackerPlayers.stream()
            .filter(p -> p.isOnline() && location.distance(p.getLocation()) < BATTLEFIELD_RANGE)
            .collect(Collectors.toSet());

        attackers.clear();
        attackers.addAll(attackersOnBattlefield);

        final Set<Player> defendersOnBattlefield = defenderPlayers.stream()
            .filter(p -> p.isOnline() && location.distance(p.getLocation()) < BATTLEFIELD_RANGE)
            .collect(Collectors.toSet());

        defenders.clear();
        defenders.addAll(defendersOnBattlefield);
    }
}
