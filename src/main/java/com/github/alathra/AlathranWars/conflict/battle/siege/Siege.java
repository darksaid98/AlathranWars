package com.github.alathra.AlathranWars.conflict.battle.siege;

import com.github.alathra.AlathranWars.AlathranWars;
import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.Battle;
import com.github.alathra.AlathranWars.db.DatabaseQueries;
import com.github.alathra.AlathranWars.enums.CaptureProgressDirection;
import com.github.alathra.AlathranWars.enums.battle.*;
import com.github.alathra.AlathranWars.events.battle.BattleResultEvent;
import com.github.alathra.AlathranWars.events.battle.BattleStartEvent;
import com.github.alathra.AlathranWars.events.battle.PreBattleResultEvent;
import com.github.alathra.AlathranWars.events.battle.PreBattleStartEvent;
import com.github.milkdrinkers.colorparser.ColorParser;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Siege.
 */
public class Siege implements Battle {
    public static final Duration SIEGE_DURATION = Duration.ofMinutes(60);
    public static final int MAX_SIEGE_PROGRESS_MINUTES = 8; // How many minutes attackers will need to be on point uncontested for to reach 100%
    public static final int MAX_SIEGE_PROGRESS = 60 * 10 * MAX_SIEGE_PROGRESS_MINUTES; // On reaching this, the attackers win. 10 points is added per second
    public static final Duration ATTACKERS_MUST_TOUCH_END = Duration.ofMinutes(40); // If point is not touched in this much time, defenders win
    public static final Duration ATTACKERS_MUST_TOUCH_REVERT = Duration.ofSeconds(60); // If point is not touched in this much time, siege progress begins reverting
    public static final int BATTLEFIELD_RANGE = 500;
    public static final int BATTLEFIELD_START_MAX_RANGE = BATTLEFIELD_RANGE * 2;
    public static final int BATTLEFIELD_START_MIN_RANGE = 75;
    public static final double SIEGE_VICTORY_MONEY = 2500.0;

    private final @NotNull UUID uuid;
    private final static BattleType battleType = BattleType.SIEGE;
    private final Set<Player> attackers = new HashSet<>(); // Players inside battlefield
    private final Set<Player> defenders = new HashSet<>(); // Players inside battlefield
    private final Set<Player> attackerPlayers = new HashSet<>();
    private final Set<Player> defenderPlayers = new HashSet<>();
    private final Set<UUID> attackerPlayersIncludingOffline = new HashSet<>();
    private final Set<UUID> defenderPlayersIncludingOffline = new HashSet<>();
    private @NotNull War war; // War which siege belongs to
    private Instant endTime;
    private int siegeProgress = 0;
    private Instant lastTouched;
    private SiegeRunnable siegeRunnable;
    private Town town; // Town of the siege
    private boolean side1AreAttackers; // bool of if side1 being attacker
    private OfflinePlayer siegeLeader;
    private @Nullable TownBlock homeBlock = null;
    private @Nullable Location townSpawn = null;
    private @Nullable BossBar activeBossBar = null;
    private boolean stopped = false; // Used to track if the siege has been already deleted

    /**
     * Instantiates a new Siege. Used when creating a new siege.
     *
     * @param war         the war
     * @param town        the town
     * @param siegeLeader the siege leader
     */
    public Siege(final @NotNull War war, final Town town, Player siegeLeader) {
        uuid = UUID.randomUUID();

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
     * Instantiates a new Siege. Used when loading existing Siege from Database.
     *
     * @param war                             the war
     * @param uuid                            the uuid
     * @param town                            the town
     * @param siegeLeader                     the siege leader
     * @param endTime                         the end time
     * @param lastTouched                     the last touched
     * @param siegeProgress                   the siege progress
     * @param attackerPlayersIncludingOffline the attacker players including offline
     * @param defenderPlayersIncludingOffline the defender players including offline
     */
    public Siege(War war, @NotNull UUID uuid, Town town, OfflinePlayer siegeLeader, Instant endTime, Instant lastTouched, int siegeProgress, Set<UUID> attackerPlayersIncludingOffline, Set<UUID> defenderPlayersIncludingOffline) {
        this.war = war;
        this.uuid = uuid;
        this.town = town;
        this.siegeLeader = siegeLeader;
        this.endTime = endTime;
        this.lastTouched = lastTouched;
        this.siegeProgress = siegeProgress;
        this.attackerPlayersIncludingOffline.addAll(attackerPlayersIncludingOffline);
        this.defenderPlayersIncludingOffline.addAll(defenderPlayersIncludingOffline);

        side1AreAttackers = war.getTownSide(town).getTeam().equals(BattleTeam.SIDE_2);

        calculateOnlinePlayers();
    }

    /**
     * Starts the battle
     */
    public void start() {
        if (!new PreBattleStartEvent(war, this, BattleType.SIEGE).callEvent()) return;

        siegeRunnable = new SiegeRunnable(this);
        stopped = false;

        if (!war.isEvent())
            AlathranWars.econ.withdrawPlayer(siegeLeader, SIEGE_VICTORY_MONEY);

        new BattleStartEvent(war, this).callEvent();
    }

    /**
     * Resumes the battle (after a server restart e.t.c.)
     */
    public void resume() {
        if (!new PreBattleStartEvent(war, this, BattleType.SIEGE).callEvent()) return;

        siegeRunnable = new SiegeRunnable(this, getSiegeProgress());
        stopped = false;

        new BattleStartEvent(war, this).callEvent();
    }

    /**
     * Stops the battle
     * </p>
     * Internal stop method for battles which triggers cleanup methods
     */
    public void stop() {
        if (stopped) return;
        stopped = true;
        siegeRunnable.cancel();
        DatabaseQueries.deleteSiege(this); // TODO Run as latent event?
        war.removeSiege(this); // TODO Run as latent event?
    }

    /**
     * Stop a battle in favor of the attackers
     * @param reason what triggered the end
     */
    public void attackersWin(BattleVictoryReason reason) {
        if (!new PreBattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.ATTACKER, reason).callEvent()) return;

        if (!war.isEvent()) {
            AlathranWars.econ.depositPlayer(siegeLeader, SIEGE_VICTORY_MONEY);
            double amt;

            if (town.getAccount().getHoldingBalance() > 10000.0) {
                amt = Math.floor(town.getAccount().getHoldingBalance()) / 4.0;
                town.getAccount().withdraw(amt, "Siege Defeat");
            } else {
                town.getAccount().withdraw(SIEGE_VICTORY_MONEY, "Siege Defeat");
                amt = SIEGE_VICTORY_MONEY;
            }

            AlathranWars.econ.depositPlayer(siegeLeader, amt);
        }

        new BattleResultEvent(war, this, BattleVictor.ATTACKER, reason).callEvent();

        stop();
    }

    /**
     * Stop a battle in favor of the defenders
     * @param reason what triggered the end
     */
    public void defendersWin(BattleVictoryReason reason) {
        if (!new PreBattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.DEFENDER, reason).callEvent()) return;

        if (!war.isEvent())
            town.getAccount().deposit(SIEGE_VICTORY_MONEY, "Siege Victory");

        new BattleResultEvent(war, this, BattleVictor.DEFENDER, reason).callEvent();

        stop();
    }

    /**
     * End a battle in favor of no one
     * @param reason what triggered the end
     */
    public void equalWin(BattleVictoryReason reason) {
        if (!new PreBattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.DRAW, reason).callEvent()) return;

        new BattleResultEvent(war, this, BattleVictor.DRAW, reason).callEvent();

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

    public void setSiegeProgress(int siegeProgress) {
        if (siegeProgress < 0) {
            siegeProgress = 0;
        } else if (siegeProgress > MAX_SIEGE_PROGRESS) {
            siegeProgress = MAX_SIEGE_PROGRESS;
        }
        this.siegeProgress = siegeProgress;
    }

    public float getSiegeProgressPercentage() {
        return (getSiegeProgress() * 1.0f) / MAX_SIEGE_PROGRESS;
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
        return getSide1AreAttackers() ? war.getSide1() : war.getSide2();
    }

    /**
     * Gets defender name string
     */
    @NotNull
    public Side getDefenderSide() {
        return getSide1AreAttackers() ? war.getSide2() : war.getSide1();
    }

    // SECTION Display Bar

    public void updateDisplayBar(@NotNull CaptureProgressDirection progressDirection) {
        if (activeBossBar == null)
            createNewDisplayBar();

        for (@NotNull Player p : Bukkit.getOnlinePlayers()) {
            p.hideBossBar(activeBossBar);
        }

        for (@Nullable Player p : this.getPlayersOnBattlefield()) {
            if (p != null)
                p.showBossBar(activeBossBar);
        }

        final String color = switch (progressDirection) {
            case UP -> {
                if (getAttackerSide().getSide().equals(BattleSide.ATTACKER)) {
                    activeBossBar.color(BossBar.Color.RED);
                    yield "<red>";
                } else {
                    activeBossBar.color(BossBar.Color.BLUE);
                    yield "<blue>";
                }
            }
            case CONTESTED -> {
                activeBossBar.color(BossBar.Color.YELLOW);
                yield "<yellow>";
            }
            case UNCONTESTED -> {
                activeBossBar.color(BossBar.Color.WHITE);
                yield "<white>";
            }
            case DOWN -> {
                if (getAttackerSide().getSide().equals(BattleSide.ATTACKER)) {
                    activeBossBar.color(BossBar.Color.BLUE);
                    yield "<blue>";
                } else {
                    activeBossBar.color(BossBar.Color.RED);
                    yield "<red>";
                }
            }
        };

        if (Instant.now().isBefore(getEndTime())) {
            activeBossBar.name(
                ColorParser.of("<gray>Capture Progress: %s<progress> <gray>Time: %s<time>min".formatted(color, color))
                    .parseMinimessagePlaceholder("progress", "%.0f%%".formatted(getSiegeProgressPercentage() * 100))
                    .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now(), getEndTime()).toMinutes()))
                    .build()
            );
        } else {
            activeBossBar.name(
                ColorParser.of("%sOVERTIME".formatted(color)).build()
            );
        }
        activeBossBar.progress(getSiegeProgressPercentage());
    }

    public void createNewDisplayBar() {
        final @NotNull Component text = ColorParser.of("<gray>Capture Progress: <yellow><progress> <gray>Time: <yellow><time>min")
            .parseMinimessagePlaceholder("progress", "%.0f%%".formatted(getSiegeProgressPercentage() * 100))
            .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now(), getEndTime()).toMinutesPart()))
            .build();

        this.activeBossBar = BossBar.bossBar(text, 0, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_10);
    }

    public void deleteDisplayBar() {
        if (activeBossBar != null) {
            for (@NotNull Player p : Bukkit.getOnlinePlayers()) {
                p.hideBossBar(activeBossBar);
            }

            activeBossBar = null;
        }
    }

    // SECTION UUID

    public @NotNull UUID getUUID() {
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
    public boolean equals(@NotNull Siege siege) {
        return getUUID().equals(siege.getUUID());
    }

    // SECTION BattleType

    @Override
    public BattleType getBattleType() {
        return battleType;
    }

    // SECTION Accessors & Getters

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
        return side1AreAttackers;
    }

    public void setSide1AreAttackers(final boolean side1AreAttackers) {
        this.side1AreAttackers = side1AreAttackers;
    }

    @NotNull
    public Set<Player> getPlayersOnBattlefield() {
        return Stream.concat(attackers.stream(), defenders.stream()).collect(Collectors.toSet());
    }

    @NotNull
    public Set<Player> getAttackers() {
        return attackers;
    }

    @NotNull
    public Set<Player> getDefenders() {
        return defenders;
    }

    @NotNull
    public Set<Player> getAttackerPlayers() {
        return attackerPlayers;
    }

    @NotNull
    public Set<Player> getDefenderPlayers() {
        return defenderPlayers;
    }

    @NotNull
    public OfflinePlayer getSiegeLeader() {
        return siegeLeader;
    }

    public void setSiegeLeader(OfflinePlayer siegeLeader) {
        this.siegeLeader = siegeLeader;
    }

    @NotNull
    public String getName() { // TODO Make better siegenames
        return getTown().getName();
    }

    // SECTION Time management

    @NotNull
    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant time) {
        endTime = time;
    }

    // SECTION Player management

    public boolean isPlayerInSiege(UUID uuid) {
        return attackerPlayersIncludingOffline.contains(uuid) || defenderPlayersIncludingOffline.contains(uuid);
    }

    public boolean isPlayerInSiege(@NotNull Player p) {
        return isPlayerInSiege(p.getUniqueId());
    }

    public @NotNull BattleSide getPlayerSideInSiege(UUID uuid) {
        if (attackerPlayersIncludingOffline.contains(uuid))
            return BattleSide.ATTACKER;

        if (defenderPlayersIncludingOffline.contains(uuid))
            return BattleSide.DEFENDER;

        return BattleSide.SPECTATOR;
    }

    public @NotNull BattleSide getPlayerSideInSiege(@NotNull Player p) {
        return getPlayerSideInSiege(p.getUniqueId());
    }

    public void addPlayer(@NotNull Player p, @NotNull BattleSide side) {
        addPlayer(p.getUniqueId(), side);
    }

    public void addPlayer(@NotNull OfflinePlayer offlinePlayer, @NotNull BattleSide side) {
        if (offlinePlayer.hasPlayedBefore())
            addPlayer(offlinePlayer.getUniqueId(), side);
    }

    public void addPlayer(@NotNull UUID uuid, @NotNull BattleSide side) {
        if (isPlayerInSiege(uuid)) return;

        switch (side) {
            case ATTACKER -> attackerPlayersIncludingOffline.add(uuid);
            case DEFENDER -> defenderPlayersIncludingOffline.add(uuid);
        }

        if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
            final @Nullable Player p = Bukkit.getPlayer(uuid);
            addOnlinePlayer(p, side);
        }
    }

    public void addOnlinePlayer(Player p, @NotNull BattleSide side) {
        switch (side) {
            case ATTACKER -> attackerPlayers.add(p);
            case DEFENDER -> defenderPlayers.add(p);
        }
    }

    public void removePlayer(@NotNull Player p) {
        removePlayer(p.getUniqueId());
    }

    public void removePlayer(@NotNull OfflinePlayer offlinePlayer) {
        if (offlinePlayer.hasPlayedBefore())
            removePlayer(offlinePlayer.getUniqueId());
    }

    public void removePlayer(@NotNull UUID uuid) {
        if (!isPlayerInSiege(uuid)) return;

        final @NotNull BattleSide side = getPlayerSideInSiege(uuid);

        switch (side) {
            case ATTACKER -> attackerPlayersIncludingOffline.remove(uuid);
            case DEFENDER -> defenderPlayersIncludingOffline.remove(uuid);
        }

        if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
            final @Nullable Player p = Bukkit.getPlayer(uuid);
            removeOnlinePlayer(p, side);
        }
    }

    public void removeOnlinePlayer(Player p, @NotNull BattleSide side) {
        switch (side) {
            case ATTACKER -> attackerPlayers.remove(p);
            case DEFENDER -> defenderPlayers.remove(p);
        }
    }

    public Set<UUID> getAttackerPlayersIncludingOffline() {
        return attackerPlayersIncludingOffline;
    }

    public Set<UUID> getDefenderPlayersIncludingOffline() {
        return defenderPlayersIncludingOffline;
    }

    public void calculateOnlinePlayers() {
        final @NotNull Set<Player> onlineAttackers = attackerPlayersIncludingOffline.stream()
            .filter(uuid1 -> Bukkit.getOfflinePlayer(uuid1).isOnline())
            .map(Bukkit::getPlayer)
            .collect(Collectors.toSet());

        attackerPlayers.clear();
        attackerPlayers.addAll(onlineAttackers);

        final @NotNull Set<Player> onlineDefenders = defenderPlayersIncludingOffline.stream()
            .filter(uuid1 -> Bukkit.getOfflinePlayer(uuid1).isOnline())
            .map(Bukkit::getPlayer)
            .collect(Collectors.toSet());

        defenderPlayers.clear();
        defenderPlayers.addAll(onlineDefenders);
    }

    public void calculateBattlefieldPlayers(@NotNull Location location) {
        final Set<Player> previousAttackersOnBattlefield = new HashSet<>(attackers);
        final Set<Player> previousDefendersOnBattlefield = new HashSet<>(defenders);

        final @NotNull Set<Player> attackersOnBattlefield = attackerPlayers.stream()
            .filter(OfflinePlayer::isOnline)
            .filter(p -> location.getWorld().equals(p.getLocation().getWorld()))
            .filter(p -> location.distance(p.getLocation()) < BATTLEFIELD_RANGE)
            .collect(Collectors.toSet());

        attackers.clear();
        attackers.addAll(attackersOnBattlefield);

        final @NotNull Set<Player> defendersOnBattlefield = defenderPlayers.stream()
            .filter(OfflinePlayer::isOnline)
            .filter(p -> location.getWorld().equals(p.getLocation().getWorld()))
            .filter(p -> location.distance(p.getLocation()) < BATTLEFIELD_RANGE)
            .collect(Collectors.toSet());

        defenders.clear();
        defenders.addAll(defendersOnBattlefield);

        // TODO Emit events for players leaving & entering the battlefield

        // Leaving attackers
        previousAttackersOnBattlefield.stream()
            .filter(p -> p.isConnected() && !attackers.contains(p))
            .collect(Collectors.toSet())
            .forEach(p -> {
                // TODO Player left battlefield

            }
        );

        // Entering attackers
        attackers.stream()
            .filter(p -> p.isConnected() && !previousAttackersOnBattlefield.contains(p))
            .collect(Collectors.toSet())
            .forEach(p -> {
                // TODO Player entered battlefield

            }
        );

        // Leaving defenders
        previousDefendersOnBattlefield.stream()
            .filter(p -> p.isConnected() && !defenders.contains(p))
            .collect(Collectors.toSet())
            .forEach(p -> {
                // TODO Player left battlefield

            }
        );

        // Entering defenders
        defenders.stream()
            .filter(p -> p.isConnected() && !previousDefendersOnBattlefield.contains(p))
            .collect(Collectors.toSet())
            .forEach(p -> {
                // TODO Player entered battlefield
            }
        );
    }
}
