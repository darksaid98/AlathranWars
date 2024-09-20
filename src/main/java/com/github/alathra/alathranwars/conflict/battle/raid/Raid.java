package com.github.alathra.alathranwars.conflict.battle.raid;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.conflict.battle.Battle;
import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.enums.CaptureProgressDirection;
import com.github.alathra.alathranwars.enums.battle.*;
import com.github.alathra.alathranwars.events.battle.BattleResultEvent;
import com.github.alathra.alathranwars.events.battle.PreBattleResultEvent;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
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
 * Credits to the original authors for their implementation which greatly inspired the new one.
 *
 * @author darksaid98
 *
 * @author AubriTheHuman
 * @author NinjaMandalorian
 * @author ShermansWorld
 */
public class Raid {
//    public static final Duration RAID_DURATION = Duration.ofMinutes(60);
//    public static final int MAX_RAID_PROGRESS_MINUTES = 8; // How many minutes attackers will need to be on point uncontested for to reach 100%
//    public static final int MAX_RAID_PROGRESS = 60 * 10 * MAX_RAID_PROGRESS_MINUTES; // On reaching this, the attackers win. 10 points is added per second
//    public static final Duration ATTACKERS_MUST_TOUCH_END = Duration.ofMinutes(40); // If point is not touched in this much time, defenders win
//    public static final Duration ATTACKERS_MUST_TOUCH_REVERT = Duration.ofSeconds(60); // If point is not touched in this much time, raid progress begins reverting
//    public static final int BATTLEFIELD_RANGE = 500;
//    public static final int BATTLEFIELD_START_MAX_RANGE = BATTLEFIELD_RANGE * 2;
//    public static final int BATTLEFIELD_START_MIN_RANGE = 75;
//    public static final double SIEGE_VICTORY_MONEY = 2500.0;
//
//    private final @NotNull UUID uuid;
//    private final Set<Player> attackers = new HashSet<>(); // Players inside battlefield
//    private final Set<Player> defenders = new HashSet<>(); // Players inside battlefield
//    private final Set<Player> attackerPlayers = new HashSet<>();
//    private final Set<Player> defenderPlayers = new HashSet<>();
//    private final Set<UUID> attackerPlayersIncludingOffline = new HashSet<>();
//    private final Set<UUID> defenderPlayersIncludingOffline = new HashSet<>();
//    private @NotNull War war; // War which raid belongs to
//    private Instant endTime;
//    private int raidProgress = 0;
//    private Instant lastTouched;
//    private RaidRunnable raidRunnable;
//    private Town town; // Town of the raid
//    private boolean side1AreAttackers; // bool of if side1 being attacker
//    private OfflinePlayer raidLeader;
//    private @Nullable TownBlock homeBlock = null;
//    private @Nullable Location townSpawn = null;
//    private @Nullable BossBar activeBossBar = null;
//    private boolean stopped = false; // Used to track if the raid has been already deleted
//
//    public Raid(final @NotNull War war, final Town town, Player raidLeader) {
//        uuid = UUID.randomUUID();
//
//        endTime = Instant.now().plus(RAID_DURATION);
//        lastTouched = Instant.now();
//
//        this.war = war;
//        this.town = town;
//        this.raidLeader = raidLeader;
//
//        side1AreAttackers = war.getSide(town).getTeam().equals(BattleTeam.SIDE_2);
//
//        if (getSide1AreAttackers()) {
//            attackerPlayersIncludingOffline.addAll(getWar().getSide1().getPlayersAll());
//            defenderPlayersIncludingOffline.addAll(getWar().getSide2().getPlayersAll());
//        } else {
//            attackerPlayersIncludingOffline.addAll(getWar().getSide2().getPlayersAll());
//            defenderPlayersIncludingOffline.addAll(getWar().getSide1().getPlayersAll());
//        }
//        calculateOnlinePlayers();
//    }
//
//    // Construct when loading from DB
//    public Raid(War war, @NotNull UUID uuid, Town town, OfflinePlayer raidLeader, Instant endTime, Instant lastTouched, int raidProgress, Set<UUID> attackerPlayersIncludingOffline, Set<UUID> defenderPlayersIncludingOffline) {
//        this.war = war;
//        this.uuid = uuid;
//        this.town = town;
//        this.raidLeader = raidLeader;
//        this.endTime = endTime;
//        this.lastTouched = lastTouched;
//        this.raidProgress = raidProgress;
//        this.attackerPlayersIncludingOffline.addAll(attackerPlayersIncludingOffline);
//        this.defenderPlayersIncludingOffline.addAll(defenderPlayersIncludingOffline);
//
//        side1AreAttackers = war.getSide(town).getTeam().equals(BattleTeam.SIDE_2);
//
//        calculateOnlinePlayers();
//    }
//
//    /**
//     * Starts a raid
//     */
//    public void start() {
//        raidRunnable = new RaidRunnable(this);
//        if (!war.isEventWar())
//            AlathranWars.econ.withdrawPlayer(raidLeader, SIEGE_VICTORY_MONEY);
//
//        final Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500));
//        final Title defTitle = Title.title(
//            ColorParser.of("<red><u><b><town>")
//                .parseMinimessagePlaceholder("town", town.getName())
//                .build(),
//            ColorParser.of("<gray><i>Is under raid, defend!")
//                .build(),
//            times
//        );
//        final Title attTitle = Title.title(
//            ColorParser.of("<red><u><b><town>")
//                .parseMinimessagePlaceholder("town", town.getName())
//                .build(),
//            ColorParser.of("<gray><i>Has been put to raid, attack!")
//                .build(),
//            times
//        );
//
//        final List<Sound> soundList = List.of(
//            Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
//            Sound.sound(Key.key("item.goat_horn.sound.2"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
//            Sound.sound(Key.key("item.goat_horn.sound.3"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
//            Sound.sound(Key.key("item.goat_horn.sound.7"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F))
//        );
//
//        getDefenderPlayers().forEach(player -> {
//            player.showTitle(defTitle);
//            player.playSound(soundList.get(new Random().nextInt(1, soundList.size())));
//        });
//
//        getAttackerPlayers().forEach(player -> {
//            player.showTitle(attTitle);
//            player.playSound(soundList.get(new Random().nextInt(1, soundList.size())));
//        });
//    }
//
//    /**
//     * Resumes a raid (after a server restart e.t.c.)
//     */
//    public void resume() {
//        raidRunnable = new RaidRunnable(this, getRaidProgress());
//    }
//
//    /**
//     * Stops a raid
//     */
//    public void stop() {
//        if (stopped) return;
//        stopped = true;
//        raidRunnable.cancel();
////        DatabaseQueries.deleteRaid(this); // TODO Delete raid
//        war.removeRaid(this);
//    }
//
//    public void attackersWin(BattleVictoryReason reason) {
//        if (!new PreBattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.ATTACKER, reason).callEvent()) return;
//
//        if (!war.isEventWar()) {
//            AlathranWars.econ.depositPlayer(raidLeader, SIEGE_VICTORY_MONEY);
//            double amt;
//
//            if (town.getAccount().getHoldingBalance() > 10000.0) {
//                amt = Math.floor(town.getAccount().getHoldingBalance()) / 4.0;
//                town.getAccount().withdraw(amt, "Raid Defeat");
//            } else {
//                town.getAccount().withdraw(SIEGE_VICTORY_MONEY, "Raid Defeat");
//                amt = SIEGE_VICTORY_MONEY;
//            }
//
//            AlathranWars.econ.depositPlayer(raidLeader, amt);
//        }
//
//        if (side1AreAttackers) {
//            war.getSide1().addScore(50);
//            war.getSide2().addScore(5);
//        } else {
//            war.getSide1().addScore(5);
//            war.getSide2().addScore(50);
//        }
//
//        new BattleResultEvent(war, this, BattleVictor.ATTACKER, reason).callEvent();
//
//        stop();
//
//        if (!war.isEventWar()) {
//            Side townSide = getWar().getSide(town);
//            Side attackerSide = getAttackerSide();
//
//            if (attackerSide.equals(townSide)) {
//                if (townSide.isSurrendered(town))
//                    war.unsurrender(town);
//            } else {
//                if (!townSide.isSurrendered(town))
//                    war.surrender(town);
//            }
//        }
//    }
//
//    public void defendersWin(BattleVictoryReason reason) {
//        if (!new PreBattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.DEFENDER, reason).callEvent()) return;
//
//        if (!war.isEventWar())
//            town.getAccount().deposit(SIEGE_VICTORY_MONEY, "Raid Victory");
//
//        if (side1AreAttackers) {
//            war.getSide2().addScore(10);
//        } else {
//            war.getSide1().addScore(10);
//        }
//
//        new BattleResultEvent(war, this, BattleVictor.DEFENDER, reason).callEvent();
//
//        stop();
//    }
//
//    /**
//     * No winner declared
//     */
//    public void equalWin(BattleVictoryReason reason) {
//        if (!new PreBattleResultEvent(war, this, BattleType.SIEGE, BattleVictor.DRAW, reason).callEvent()) return;
//
//        new BattleResultEvent(war, this, BattleVictor.DRAW, reason).callEvent();
//
//        stop();
//    }
//
//    @NotNull
//    public Instant getLastTouched() {
//        return lastTouched;
//    }
//
//    public void setLastTouched(Instant lastTouched) {
//        this.lastTouched = lastTouched;
//    }
//
//    public int getRaidProgress() {
//        return raidProgress;
//    }
//
//    public void setRaidProgress(int raidProgress) {
//        if (raidProgress < 0) {
//            raidProgress = 0;
//        } else if (raidProgress > MAX_RAID_PROGRESS) {
//            raidProgress = MAX_RAID_PROGRESS;
//        }
//        this.raidProgress = raidProgress;
//    }
//
//    public float getRaidProgressPercentage() {
//        return (getRaidProgress() * 1.0f) / MAX_RAID_PROGRESS;
//    }
//
//    @NotNull
//    public War getWar() {
//        return war;
//    }
//
//    public void setWar(final War war) {
//        this.war = war;
//    }
//
//    @NotNull
//    public Town getTown() {
//        return town;
//    }
//
//    public void setTown(final Town town) {
//        this.town = town;
//    }
//
//    /**
//     * Gets attacker name string
//     */
//    @NotNull
//    public Side getAttackerSide() {
//        return side1AreAttackers ? war.getSide1() : war.getSide2();
//    }
//
//    /**
//     * Gets defender name string
//     */
//    @NotNull
//    public Side getDefenderSide() {
//        return side1AreAttackers ? war.getSide2() : war.getSide1();
//    }
//
//    // SECTION Display Bar
//
//    public void updateDisplayBar(@NotNull CaptureProgressDirection progressDirection) {
//        if (activeBossBar == null)
//            createNewDisplayBar();
//
//        for (@NotNull Player p : Bukkit.getOnlinePlayers()) {
//            p.hideBossBar(activeBossBar);
//        }
//
//        for (@Nullable Player p : this.getPlayersOnBattlefield()) {
//            if (p != null)
//                p.showBossBar(activeBossBar);
//        }
//
//        final String color = switch (progressDirection) {
//            case UP -> {
//                if (getAttackerSide().getSide().equals(BattleSide.ATTACKER)) {
//                    activeBossBar.color(BossBar.Color.RED);
//                    yield "<red>";
//                } else {
//                    activeBossBar.color(BossBar.Color.BLUE);
//                    yield "<blue>";
//                }
//            }
//            case CONTESTED -> {
//                activeBossBar.color(BossBar.Color.YELLOW);
//                yield "<yellow>";
//            }
//            case UNCONTESTED -> {
//                activeBossBar.color(BossBar.Color.WHITE);
//                yield "<white>";
//            }
//            case DOWN -> {
//                if (getAttackerSide().getSide().equals(BattleSide.ATTACKER)) {
//                    activeBossBar.color(BossBar.Color.BLUE);
//                    yield "<blue>";
//                } else {
//                    activeBossBar.color(BossBar.Color.RED);
//                    yield "<red>";
//                }
//            }
//        };
//
//        if (Instant.now().isBefore(getEndTime())) {
//            activeBossBar.name(
//                ColorParser.of("<gray>Capture Progress: %s<progress> <gray>Time: %s<time>min".formatted(color, color))
//                    .parseMinimessagePlaceholder("progress", "%.0f%%".formatted(getRaidProgressPercentage() * 100))
//                    .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now(), getEndTime()).toMinutes()))
//                    .build()
//            );
//        } else {
//            activeBossBar.name(
//                ColorParser.of("%sOVERTIME".formatted(color)).build()
//            );
//        }
//        activeBossBar.progress(getRaidProgressPercentage());
//    }
//
//    public void createNewDisplayBar() {
//        final @NotNull Component text = ColorParser.of("<gray>Capture Progress: <yellow><progress> <gray>Time: <yellow><time>min")
//            .parseMinimessagePlaceholder("progress", "%.0f%%".formatted(getRaidProgressPercentage() * 100))
//            .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now(), getEndTime()).toMinutesPart()))
//            .build();
//
//        this.activeBossBar = BossBar.bossBar(text, 0, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_10);
//    }
//
//    public void deleteDisplayBar() {
//        if (activeBossBar != null) {
//            for (@NotNull Player p : Bukkit.getOnlinePlayers()) {
//                p.hideBossBar(activeBossBar);
//            }
//
//            activeBossBar = null;
//        }
//    }
//
//    // SECTION UUID
//
//    public @NotNull UUID getUUID() {
//        return uuid;
//    }
//
//    /**
//     * Equals boolean.
//     *
//     * @param uuid the uuid
//     * @return the boolean
//     */
//    public boolean equals(UUID uuid) {
//        return getUUID().equals(uuid);
//    }
//
//    /**
//     * Equals boolean.
//     *
//     * @param raid the raid
//     * @return the boolean
//     */
//    public boolean equals(@NotNull Raid raid) {
//        return getUUID().equals(raid.getUUID());
//    }
//
//    // SECTION Accessors & Getters
//
//    @Nullable
//    public TownBlock getHomeBlock() {
//        return homeBlock;
//    }
//
//    public void setHomeBlock(TownBlock homeBlock) {
//        this.homeBlock = homeBlock;
//    }
//
//    @Nullable
//    public Location getTownSpawn() {
//        return townSpawn;
//    }
//
//    public void setTownSpawn(Location townSpawn) {
//        this.townSpawn = townSpawn;
//    }
//
//    public boolean getSide1AreAttackers() {
//        return side1AreAttackers;
//    }
//
//    public void setSide1AreAttackers(final boolean side1AreAttackers) {
//        this.side1AreAttackers = side1AreAttackers;
//    }
//
//    @NotNull
//    public Set<Player> getPlayersOnBattlefield() {
//        return Stream.concat(attackers.stream(), defenders.stream()).collect(Collectors.toSet());
//    }
//
//    @NotNull
//    public Set<Player> getAttackers() {
//        return attackers;
//    }
//
//    @NotNull
//    public Set<Player> getDefenders() {
//        return defenders;
//    }
//
//    @NotNull
//    public Set<Player> getAttackerPlayers() {
//        return attackerPlayers;
//    }
//
//    @NotNull
//    public Set<Player> getDefenderPlayers() {
//        return defenderPlayers;
//    }
//
//    @NotNull
//    public OfflinePlayer getRaidLeader() {
//        return raidLeader;
//    }
//
//    public void setRaidLeader(OfflinePlayer raidLeader) {
//        this.raidLeader = raidLeader;
//    }
//
//    @NotNull
//    public String getName() { // TODO Make better raidnames
//        return getTown().getName();
//    }
//
//    // SECTION Time management
//
//    @NotNull
//    public Instant getEndTime() {
//        return endTime;
//    }
//
//    public void setEndTime(Instant time) {
//        endTime = time;
//    }
//
//    // SECTION Player management
//
//    public boolean isPlayerInRaid(UUID uuid) {
//        return attackerPlayersIncludingOffline.contains(uuid) || defenderPlayersIncludingOffline.contains(uuid);
//    }
//
//    public boolean isPlayerInRaid(@NotNull Player p) {
//        return isPlayerInRaid(p.getUniqueId());
//    }
//
//    public @NotNull BattleSide getPlayerSideInRaid(UUID uuid) {
//        if (attackerPlayersIncludingOffline.contains(uuid))
//            return BattleSide.ATTACKER;
//
//        if (defenderPlayersIncludingOffline.contains(uuid))
//            return BattleSide.DEFENDER;
//
//        return BattleSide.SPECTATOR;
//    }
//
//    public @NotNull BattleSide getPlayerSideInRaid(@NotNull Player p) {
//        return getPlayerSideInRaid(p.getUniqueId());
//    }
//
//    public void addPlayer(@NotNull Player p, @NotNull BattleSide side) {
//        addPlayer(p.getUniqueId(), side);
//    }
//
//    public void addPlayer(@NotNull OfflinePlayer offlinePlayer, @NotNull BattleSide side) {
//        if (offlinePlayer.hasPlayedBefore())
//            addPlayer(offlinePlayer.getUniqueId(), side);
//    }
//
//    public void addPlayer(@NotNull UUID uuid, @NotNull BattleSide side) {
//        if (isPlayerInRaid(uuid)) return;
//
//        switch (side) {
//            case ATTACKER -> attackerPlayersIncludingOffline.add(uuid);
//            case DEFENDER -> defenderPlayersIncludingOffline.add(uuid);
//        }
//
//        if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
//            final @Nullable Player p = Bukkit.getPlayer(uuid);
//            addOnlinePlayer(p, side);
//        }
//    }
//
//    public void addOnlinePlayer(Player p, @NotNull BattleSide side) {
//        switch (side) {
//            case ATTACKER -> attackerPlayers.add(p);
//            case DEFENDER -> defenderPlayers.add(p);
//        }
//    }
//
//    public void removePlayer(@NotNull Player p) {
//        removePlayer(p.getUniqueId());
//    }
//
//    public void removePlayer(@NotNull OfflinePlayer offlinePlayer) {
//        if (offlinePlayer.hasPlayedBefore())
//            removePlayer(offlinePlayer.getUniqueId());
//    }
//
//    public void removePlayer(@NotNull UUID uuid) {
//        if (!isPlayerInRaid(uuid)) return;
//
//        final @NotNull BattleSide side = getPlayerSideInRaid(uuid);
//
//        switch (side) {
//            case ATTACKER -> attackerPlayersIncludingOffline.remove(uuid);
//            case DEFENDER -> defenderPlayersIncludingOffline.remove(uuid);
//        }
//
//        if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
//            final @Nullable Player p = Bukkit.getPlayer(uuid);
//            removeOnlinePlayer(p, side);
//        }
//    }
//
//    public void removeOnlinePlayer(Player p, @NotNull BattleSide side) {
//        switch (side) {
//            case ATTACKER -> attackerPlayers.remove(p);
//            case DEFENDER -> defenderPlayers.remove(p);
//        }
//    }
//
//    public Set<UUID> getAttackerPlayersIncludingOffline() {
//        return attackerPlayersIncludingOffline;
//    }
//
//    public Set<UUID> getDefenderPlayersIncludingOffline() {
//        return defenderPlayersIncludingOffline;
//    }
//
//    public void calculateOnlinePlayers() {
//        final @NotNull Set<Player> onlineAttackers = attackerPlayersIncludingOffline.stream()
//            .filter(uuid1 -> Bukkit.getOfflinePlayer(uuid1).isOnline())
//            .map(Bukkit::getPlayer)
//            .collect(Collectors.toSet());
//
//        attackerPlayers.clear();
//        attackerPlayers.addAll(onlineAttackers);
//
//        final @NotNull Set<Player> onlineDefenders = defenderPlayersIncludingOffline.stream()
//            .filter(uuid1 -> Bukkit.getOfflinePlayer(uuid1).isOnline())
//            .map(Bukkit::getPlayer)
//            .collect(Collectors.toSet());
//
//        defenderPlayers.clear();
//        defenderPlayers.addAll(onlineDefenders);
//    }
//
//    public void calculateBattlefieldPlayers(@NotNull Location location) {
//        final Set<Player> previousAttackersOnBattlefield = new HashSet<>(attackers);
//        final Set<Player> previousDefendersOnBattlefield = new HashSet<>(defenders);
//
//        final @NotNull Set<Player> attackersOnBattlefield = attackerPlayers.stream()
//            .filter(OfflinePlayer::isOnline)
//            .filter(p -> location.getWorld().equals(p.getLocation().getWorld()))
//            .filter(p -> location.distance(p.getLocation()) < BATTLEFIELD_RANGE)
//            .collect(Collectors.toSet());
//
//        attackers.clear();
//        attackers.addAll(attackersOnBattlefield);
//
//        final @NotNull Set<Player> defendersOnBattlefield = defenderPlayers.stream()
//            .filter(OfflinePlayer::isOnline)
//            .filter(p -> location.getWorld().equals(p.getLocation().getWorld()))
//            .filter(p -> location.distance(p.getLocation()) < BATTLEFIELD_RANGE)
//            .collect(Collectors.toSet());
//
//        defenders.clear();
//        defenders.addAll(defendersOnBattlefield);
//
//        // Emit events for players leaving & entering the battlefield
//        for (Player p : previousAttackersOnBattlefield) {
//            if (p.isConnected() && !attackers.contains(p)) {
//                // TODO Player left battlefield
//            }
//        }
//        for (Player p : attackers) {
//            if (p.isConnected() && !previousAttackersOnBattlefield.contains(p)) {
//                // TODO Player entered battlefield
//            }
//        }
//
//        for (Player p : previousDefendersOnBattlefield) {
//            if (p.isConnected() && !defenders.contains(p)) {
//                // TODO Player left battlefield
//            }
//        }
//
//        for (Player p : defenders) {
//            if (p.isConnected() && !previousDefendersOnBattlefield.contains(p)) {
//                // TODO Player entered battlefield
//            }
//        }
//    }
}
