package com.github.alathra.alathranwars.conflict.battle.siege;

import com.github.alathra.alathranwars.AlathranWars;
import com.github.alathra.alathranwars.conflict.battle.beam.CrystalLaser;
import com.github.alathra.alathranwars.conflict.battle.beam.Laser;
import com.github.alathra.alathranwars.enums.CaptureProgressDirection;
import com.github.alathra.alathranwars.enums.battle.BattleSide;
import com.github.alathra.alathranwars.enums.battle.BattleVictoryReason;
import com.github.alathra.alathranwars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.time.Instant;

import static com.github.alathra.alathranwars.conflict.battle.siege.Siege.*;
import static com.github.alathra.alathranwars.enums.CaptureProgressDirection.*;

public class SiegeRunnable implements Runnable {
    // Settings
    private static final Duration ANNOUNCEMENT_COOLDOWN = Duration.ofMinutes(5);
    private final static int CAPTURE_RANGE = 10;
    private final @NotNull Siege siege;

    // Variables
    private @NotNull CaptureProgressDirection oldProgressDirection = UNCONTESTED;
    private Instant nextAnnouncement;
    private final ScheduledTask task;
    private @Nullable Laser beam;

    /**
     * Start a siege
     *
     * @param siege the siege
     */
    public SiegeRunnable(@NotNull Siege siege) {
        this.siege = siege;

        siege.setSiegeProgress(0);

        siege.setHomeBlock(siege.getTown().getHomeBlockOrNull());
        siege.setTownSpawn(siege.getTown().getSpawnOrNull());

        nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

        task = AlathranWars.getPaperLib().scheduling().globalRegionalScheduler().runAtFixedRate(this, 0L, 20L);

        siege.updateDisplayBar(CONTESTED);
    }

    /**
     * Resume a siege at tick
     *
     * @param siege         the siege
     * @param siegeProgress the siege ticks
     */
    public SiegeRunnable(@NotNull Siege siege, int siegeProgress) {
        this.siege = siege;

        siege.setSiegeProgress(siegeProgress);

        siege.setHomeBlock(siege.getTown().getHomeBlockOrNull());
        siege.setTownSpawn(siege.getTown().getSpawnOrNull());

        nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

        task = AlathranWars.getPaperLib().scheduling().globalRegionalScheduler().runAtFixedRate(this, 0L, 20L);

        siege.updateDisplayBar(CONTESTED);
    }

    public void cancel() {
        stopBeam();
        if (task.isCancelled()) return;

        task.cancel();
        siege.deleteDisplayBar();
    }

    @Override
    public void run() {
        final @NotNull Town town = siege.getTown();
        final @Nullable TownBlock homeBlock = siege.getHomeBlock();
        final @Nullable Location townSpawn = siege.getTownSpawn();

        if (homeBlock != null && townSpawn != null) {
            town.setHomeBlock(homeBlock);
            town.setSpawn(townSpawn);
        }

        // Render beam
        startBeam();

        // Calculate battlefield players
        siege.calculateBattlefieldPlayers(townSpawn.toCenterLocation());

        // Progress the siege
        final int attackersOnPoint = getPeopleOnPoint(townSpawn, BattleSide.ATTACKER);
        final int defendersOnPoint = getPeopleOnPoint(townSpawn, BattleSide.DEFENDER);
        final @NotNull CaptureProgressDirection progressDirection = getSiegeProgressDirection(attackersOnPoint, defendersOnPoint);

        // Siege is past max time or attackers haven't touched in time, defenders won
        if (
            (!progressDirection.equals(CONTESTED) && !progressDirection.equals(UP)) &&
                (Instant.now().isAfter(siege.getEndTime()) ||
                    Instant.now().isAfter(siege.getLastTouched().plus(ATTACKERS_MUST_TOUCH_END)))
        ) {
            cancel();
            siege.defendersWin(BattleVictoryReason.OPPONENT_LOST);
            return;
        }

        // Attackers captured the town
        if (siege.getSiegeProgress() >= MAX_SIEGE_PROGRESS) {
            cancel();
            siege.attackersWin(BattleVictoryReason.OPPONENT_LOST);
            return;
        }

        switch (progressDirection) {
            case UP -> {
                final int playerOnPointDiff = attackersOnPoint - defendersOnPoint;

                // If you have less than 5 people contesting you get (4 + excessPlayers) points per second
                if (playerOnPointDiff < 5) {
                    siege.setSiegeProgress(siege.getSiegeProgress() + (4 + playerOnPointDiff));
                } else {
                    siege.setSiegeProgress(siege.getSiegeProgress() + 10);
                }
            }
            case DOWN -> siege.setSiegeProgress(siege.getSiegeProgress() - 10);
        }

        if (oldProgressDirection != progressDirection) {
            switch (progressDirection) {
                case UP -> {
                    siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                        ColorParser.of("<prefix>The Attackers are capturing the home block.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));
                }
                case CONTESTED -> {
                    if (oldProgressDirection.equals(UNCONTESTED))
                        siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                            ColorParser.of("<prefix>The home block is being contested.")
                                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                                .build()
                        ));
                }
                case UNCONTESTED -> {
                    if (oldProgressDirection.equals(UP) || oldProgressDirection.equals(CONTESTED) || oldProgressDirection.equals(DOWN))
                        siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                            ColorParser.of("<prefix>The home block is no longer being contested.")
                                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                                .build()
                        ));
                }
                case DOWN -> {
                    if (oldProgressDirection.equals(UP) || oldProgressDirection.equals(CONTESTED))
                        siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                            ColorParser.of("<prefix>The Defenders re-secured the home block.")
                                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                                .build()
                        ));
                }
            }
        }

        if (Instant.now().isAfter(nextAnnouncement)) {
            nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

            siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                ColorParser.of("<prefix>Siege time remaining: <time> minutes.")
                    .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                    .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now(), siege.getEndTime()).toMinutesPart()))
                    .build()
            ));
        }

        siege.updateDisplayBar(progressDirection);
        oldProgressDirection = progressDirection;
    }

    public int getPeopleOnPoint(@NotNull Location townSpawn, @NotNull BattleSide battleSide) {
        int onPoint = 0;

        for (final @NotNull Player p : (battleSide.equals(BattleSide.ATTACKER) ? siege.getAttackers() : siege.getDefenders())) {
            if (p.isDead())
                continue;

            if (!townSpawn.getWorld().equals(p.getLocation().getWorld()))
                continue;

            if (townSpawn.distance(p.getLocation()) <= CAPTURE_RANGE) {
                onPoint += 1;
            }
        }

        return onPoint;
    }

    public @NotNull CaptureProgressDirection getSiegeProgressDirection(int attackersOnPoint, int defendersOnPoint) {
        final boolean attackersAreOnPoint = attackersOnPoint > 0;
        final boolean defendersAreOnPoint = defendersOnPoint > 0;

        if (attackersAreOnPoint)
            siege.setLastTouched(Instant.now());

        if (attackersAreOnPoint && defendersAreOnPoint) {

            if (attackersOnPoint > defendersOnPoint)
                return UP;

            return CONTESTED;
        } else if (attackersAreOnPoint && !defendersAreOnPoint) {
            if (siege.getSiegeProgress() == MAX_SIEGE_PROGRESS)
                return CONTESTED;

            return UP;
        } else {
            if (siege.getSiegeProgress() == 0)
                return UNCONTESTED;

            // If the attackers haven't touched in a while, begin reverting progress
            if (Instant.now().isAfter(siege.getLastTouched().plus(ATTACKERS_MUST_TOUCH_REVERT))) {
                return DOWN;
            } else {
                return UNCONTESTED;
            }
        }
    }

    private void startBeam() {
        if (beam != null)
            return;

        try {
            try {
                @Nullable Location loc1 = siege.getTownSpawn();
                @NotNull Location loc2 = new Location(loc1.getWorld(), loc1.getX(), loc1.getY() + 350D, loc1.getZ());

                beam = new CrystalLaser(loc1, loc2, -1, 300);
                beam.start(AlathranWars.getInstance());
            } catch (ReflectiveOperationException ignored) {
            }
        } catch (NoClassDefFoundError ignored1) {
        }
    }

    private void stopBeam() {
        if (beam != null) {
            beam.stop();
            beam = null;
        }
    }
}
