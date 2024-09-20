package com.github.alathra.alathranwars.conflict.battle.raid;

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

import static com.github.alathra.alathranwars.enums.CaptureProgressDirection.*;

/*public class RaidRunnable implements Runnable {
    // Settings
    private static final Duration ANNOUNCEMENT_COOLDOWN = Duration.ofMinutes(5);
    private final static int CAPTURE_RANGE = 10;
    private final @NotNull Raid raid;

    // Variables
    private @NotNull CaptureProgressDirection oldProgressDirection = UNCONTESTED;
    private Instant nextAnnouncement;
    private final ScheduledTask task;
    private @Nullable Laser beam;

    *//**
     * Start a raid
     *
     * @param raid the raid
     *//*
    public RaidRunnable(@NotNull Raid raid) {
        this.raid = raid;

        raid.setRaidProgress(0);

        raid.setHomeBlock(raid.getTown().getHomeBlockOrNull());
        raid.setTownSpawn(raid.getTown().getSpawnOrNull());

        nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

        task = AlathranWars.getPaperLib().scheduling().globalRegionalScheduler().runAtFixedRate(this, 0L, 20L);

        raid.updateDisplayBar(CONTESTED);
    }

    *//**
     * Resume a raid at tick
     *
     * @param raid         the raid
     * @param raidProgress the raid ticks
     *//*
    public RaidRunnable(@NotNull Raid raid, int raidProgress) {
        this.raid = raid;

        raid.setRaidProgress(raidProgress);

        raid.setHomeBlock(raid.getTown().getHomeBlockOrNull());
        raid.setTownSpawn(raid.getTown().getSpawnOrNull());

        nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

        task = AlathranWars.getPaperLib().scheduling().globalRegionalScheduler().runAtFixedRate(this, 0L, 20L);

        raid.updateDisplayBar(CONTESTED);
    }

    public void cancel() {
        stopBeam();
        if (task.isCancelled()) return;

        task.cancel();
        raid.deleteDisplayBar();
    }

    @Override
    public void run() {
        final @NotNull Town town = raid.getTown();
        final @Nullable TownBlock homeBlock = raid.getHomeBlock();
        final @Nullable Location townSpawn = raid.getTownSpawn();

        if (homeBlock != null && townSpawn != null) {
            town.setHomeBlock(homeBlock);
            town.setSpawn(townSpawn);
        }

        // Render beam
        startBeam();

        // Calculate battlefield players
        raid.calculateBattlefieldPlayers(townSpawn.toCenterLocation());

        // Progress the raid
        final int attackersOnPoint = getPeopleOnPoint(townSpawn, BattleSide.ATTACKER);
        final int defendersOnPoint = getPeopleOnPoint(townSpawn, BattleSide.DEFENDER);
        final @NotNull CaptureProgressDirection progressDirection = getRaidProgressDirection(attackersOnPoint, defendersOnPoint);

        // Raid is past max time or attackers haven't touched in time, defenders won
        if (
            (!progressDirection.equals(CONTESTED) && !progressDirection.equals(UP)) &&
                (Instant.now().isAfter(raid.getEndTime()) ||
                    Instant.now().isAfter(raid.getLastTouched().plus(ATTACKERS_MUST_TOUCH_END)))
        ) {
            cancel();
            raid.defendersWin(BattleVictoryReason.OPPONENT_LOST);
            return;
        }

        // Attackers captured the town
        if (raid.getRaidProgress() >= MAX_RAID_PROGRESS) {
            cancel();
            raid.attackersWin(BattleVictoryReason.OPPONENT_LOST);
            return;
        }

        switch (progressDirection) {
            case UP -> {
                final int playerOnPointDiff = attackersOnPoint - defendersOnPoint;

                // If you have less than 5 people contesting you get (4 + excessPlayers) points per second
                if (playerOnPointDiff < 5) {
                    raid.setRaidProgress(raid.getRaidProgress() + (4 + playerOnPointDiff));
                } else {
                    raid.setRaidProgress(raid.getRaidProgress() + 10);
                }
            }
            case DOWN -> raid.setRaidProgress(raid.getRaidProgress() - 10);
        }

        if (oldProgressDirection != progressDirection) {
            switch (progressDirection) {
                case UP -> {
                    raid.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                        ColorParser.of("<prefix>The Attackers are capturing the home block.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));
                }
                case CONTESTED -> {
                    if (oldProgressDirection.equals(UNCONTESTED))
                        raid.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                            ColorParser.of("<prefix>The home block is being contested.")
                                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                                .build()
                        ));
                }
                case UNCONTESTED -> {
                    if (oldProgressDirection.equals(UP) || oldProgressDirection.equals(CONTESTED) || oldProgressDirection.equals(DOWN))
                        raid.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                            ColorParser.of("<prefix>The home block is no longer being contested.")
                                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                                .build()
                        ));
                }
                case DOWN -> {
                    if (oldProgressDirection.equals(UP) || oldProgressDirection.equals(CONTESTED))
                        raid.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                            ColorParser.of("<prefix>The Defenders re-secured the home block.")
                                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                                .build()
                        ));
                }
            }
        }

        if (Instant.now().isAfter(nextAnnouncement)) {
            nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

            raid.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                ColorParser.of("<prefix>Raid time remaining: <time> minutes.")
                    .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                    .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now(), raid.getEndTime()).toMinutesPart()))
                    .build()
            ));
        }

        raid.updateDisplayBar(progressDirection);
        oldProgressDirection = progressDirection;
    }

    public int getPeopleOnPoint(@NotNull Location townSpawn, @NotNull BattleSide battleSide) {
        int onPoint = 0;

        for (final @NotNull Player p : (battleSide.equals(BattleSide.ATTACKER) ? raid.getAttackers() : raid.getDefenders())) {
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

    public @NotNull CaptureProgressDirection getRaidProgressDirection(int attackersOnPoint, int defendersOnPoint) {
        final boolean attackersAreOnPoint = attackersOnPoint > 0;
        final boolean defendersAreOnPoint = defendersOnPoint > 0;

        if (attackersAreOnPoint)
            raid.setLastTouched(Instant.now());

        if (attackersAreOnPoint && defendersAreOnPoint) {

            if (attackersOnPoint > defendersOnPoint)
                return UP;

            return CONTESTED;
        } else if (attackersAreOnPoint && !defendersAreOnPoint) {
            if (raid.getRaidProgress() == MAX_RAID_PROGRESS)
                return CONTESTED;

            return UP;
        } else {
            if (raid.getRaidProgress() == 0)
                return UNCONTESTED;

            // If the attackers haven't touched in a while, begin reverting progress
            if (Instant.now().isAfter(raid.getLastTouched().plus(ATTACKERS_MUST_TOUCH_REVERT))) {
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
            @Nullable Location loc1 = raid.getTownSpawn();
            @NotNull Location loc2 = new Location(loc1.getWorld(), loc1.getX(), loc1.getY() + 350D, loc1.getZ());

            beam = new CrystalLaser(loc1, loc2, -1, 300);
            beam.start(AlathranWars.getInstance());
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private void stopBeam() {
        if (beam != null) {
            beam.stop();
            beam = null;
        }
    }
}*/
