package com.github.alathra.AlathranWars.conflict.battle.siege;

import com.github.alathra.AlathranWars.Main;
import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.Battle;
import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.enums.BattleTeam;
import com.github.alathra.AlathranWars.enums.CaptureProgressDirection;
import com.github.alathra.AlathranWars.utility.SQLQueries;
import com.github.alathra.AlathranWars.utility.UUIDUtil;
import com.github.alathra.AlathranWars.utility.UtilsChat;
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

public class Siege extends Battle {
    public static final Duration SIEGE_DURATION = Duration.ofMinutes(60);
    public static final int MAX_SIEGE_PROGRESS_MINUTES = 8; // How many minutes attackers will need to be on point uncontested for to reach 100%
    public static final int MAX_SIEGE_PROGRESS = 60 * 10 * MAX_SIEGE_PROGRESS_MINUTES; // On reaching this, the attackers win. 10 points is added per second
    public static final Duration ATTACKERS_MUST_TOUCH_END = Duration.ofMinutes(40); // If point is not touched in this much time, defenders win
    public static final Duration ATTACKERS_MUST_TOUCH_REVERT = Duration.ofSeconds(60); // If point is not touched in this much time, siege progress begins reverting
    public static final int BATTLEFIELD_RANGE = 500;
    public static final int BATTLEFIELD_START_MAX_RANGE = BATTLEFIELD_RANGE * 2;
    public static final int BATTLEFIELD_START_MIN_RANGE = 75;
    public static final double SIEGE_VICTORY_MONEY = 2500.0;
    private final static Title.Times TITLE_TIMES = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500));
    private final static Sound SOUND_VICTORY = Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.VOICE, 0.5f, 1.0F);
    private final static Sound SOUND_DEFEAT = Sound.sound(Key.key("entity.wither.death"), Sound.Source.VOICE, 0.5f, 1.0F);
    private final @NotNull UUID uuid;
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

    public Siege(final @NotNull War war, final Town town, Player siegeLeader) {
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

    // Construct when loading from DB
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
     * Starts a siege
     */
    public void start() {
        siegeRunnable = new SiegeRunnable(this);
        Main.econ.withdrawPlayer(siegeLeader, SIEGE_VICTORY_MONEY);

        final Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500));
        final Title defTitle = Title.title(
            ColorParser.of("<red><u><b><town>")
                .parseMinimessagePlaceholder("town", town.getName())
                .build(),
            ColorParser.of("<gray><i>Is under siege, defend!")
                .build(),
            times
        );
        final Title attTitle = Title.title(
            ColorParser.of("<red><u><b><town>")
                .parseMinimessagePlaceholder("town", town.getName())
                .build(),
            ColorParser.of("<gray><i>Has been put to siege, attack!")
                .build(),
            times
        );

        final List<Sound> soundList = List.of(
            Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
            Sound.sound(Key.key("item.goat_horn.sound.2"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
            Sound.sound(Key.key("item.goat_horn.sound.3"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F)),
            Sound.sound(Key.key("item.goat_horn.sound.7"), Sound.Source.VOICE, 0.5f, new Random().nextFloat(0.9F, 1.0F))
        );

        getDefenderPlayers().forEach(player -> {
            player.showTitle(defTitle);
            player.playSound(soundList.get(new Random().nextInt(1, soundList.size())));
        });

        getAttackerPlayers().forEach(player -> {
            player.showTitle(attTitle);
            player.playSound(soundList.get(new Random().nextInt(1, soundList.size())));
        });
    }

    /**
     * Resumes a siege (after a server restart e.t.c.)
     */
    public void resume() {
        siegeRunnable = new SiegeRunnable(this, getSiegeProgress());
    }

    /**
     * Stops a siege
     */
    public void stop() {
        if (stopped) return;
        stopped = true;
        siegeRunnable.cancel();
        SQLQueries.deleteSiege(this);
        war.removeSiege(this);
    }

    public void attackersWin() {
        Main.econ.depositPlayer(siegeLeader, SIEGE_VICTORY_MONEY);
        double amt;

        if (town.getAccount().getHoldingBalance() > 10000.0) {
            amt = Math.floor(town.getAccount().getHoldingBalance()) / 4.0;
            town.getAccount().withdraw(amt, "Siege Defeat");
        } else {
            town.getAccount().withdraw(SIEGE_VICTORY_MONEY, "Siege Defeat");
            amt = SIEGE_VICTORY_MONEY;
        }

        Main.econ.depositPlayer(siegeLeader, amt);

        Bukkit.broadcast(ColorParser.of("<prefix>The town of <town> has been sacked and placed under occupation by the armies of <attacker>!")
            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
            .parseMinimessagePlaceholder("town", town.getName())
            .parseMinimessagePlaceholder("attacker", getAttackerSide().getName())
            .build());

        final Title vicAttackTitle = Title.title(
            ColorParser.of("<green><u><b>Victory")
                .build(),
            ColorParser.of("<gray><i><town> has been captured!")
                .parseMinimessagePlaceholder("town", town.getName())
                .build(),
            TITLE_TIMES
        );
        final Title losAttackTitle = Title.title(
            ColorParser.of("<red><u><b>Defeat")
                .build(),
            ColorParser.of("<gray><i><town> has been lost!")
                .parseMinimessagePlaceholder("town", town.getName())
                .build(),
            TITLE_TIMES
        );
        getAttackers().forEach(player -> {
            player.showTitle(vicAttackTitle);
            player.playSound(SOUND_VICTORY);
        });
        getDefenders().forEach(player -> {
            player.showTitle(losAttackTitle);
            player.playSound(SOUND_DEFEAT);
        });

        if (side1AreAttackers) {
            war.getSide1().addScore(50);
            war.getSide2().addScore(5);
        } else {
            war.getSide1().addScore(5);
            war.getSide2().addScore(50);
        }

        stop();

        Side townSide = getWar().getTownSide(town);
        Side attackerSide = getAttackerSide();

        if (attackerSide.equals(townSide)) {
            if (townSide.isTownSurrendered(town))
                war.unsurrenderTown(town);
        } else {
            if (!townSide.isTownSurrendered(town))
                war.surrenderTown(town);
        }
    }

    public void defendersWin() {
        town.getAccount().deposit(SIEGE_VICTORY_MONEY, "Siege Victory");

        Bukkit.broadcast(
            ColorParser.of("<prefix>The siege of <town> has been lifted by the defenders!")
                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                .parseMinimessagePlaceholder("town", town.getName())
                .build()
        );

        final Title vicDefendTitle = Title.title(
            ColorParser.of("<red><u><b>Defeat")
                .build(),
            ColorParser.of("<gray><i>We failed to capture <town>!")
                .parseMinimessagePlaceholder("town", town.getName())
                .build(),
            TITLE_TIMES
        );
        final Title losDefendTitle = Title.title(
            ColorParser.of("<green><u><b>Victory")
                .build(),
            ColorParser.of("<gray><i><town> has been made safe!")
                .parseMinimessagePlaceholder("town", town.getName())
                .build(),
            TITLE_TIMES
        );
        getAttackers().forEach(player -> {
            player.showTitle(vicDefendTitle);
            player.playSound(SOUND_DEFEAT);
        });
        getDefenders().forEach(player -> {
            player.showTitle(losDefendTitle);
            player.playSound(SOUND_VICTORY);
        });

        if (side1AreAttackers) {
            war.getSide2().addScore(10);
        } else {
            war.getSide1().addScore(10);
        }

        stop();
    }

    /**
     * No winner declared
     */
    public void noWinner() {
        Bukkit.broadcast(
            ColorParser.of("<prefix>The siege of <town> has ended in a draw!")
                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                .parseMinimessagePlaceholder("town", town.getName())
                .build()
        );

        final Title drawTitle = Title.title(
            ColorParser.of("<yellow><u><b>Draw")
                .build(),
            ColorParser.of("<gray><i>The siege at <town> has ended!")
                .parseMinimessagePlaceholder("town", town.getName())
                .build(),
            TITLE_TIMES
        );
        getAttackers().forEach(player -> {
            player.showTitle(drawTitle);
            player.playSound(SOUND_DEFEAT);
        });
        getDefenders().forEach(player -> {
            player.showTitle(drawTitle);
            player.playSound(SOUND_DEFEAT);
        });

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
        return side1AreAttackers ? war.getSide1() : war.getSide2();
    }

    /**
     * Gets defender name string
     */
    @NotNull
    public Side getDefenderSide() {
        return side1AreAttackers ? war.getSide2() : war.getSide1();
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
    }
}
