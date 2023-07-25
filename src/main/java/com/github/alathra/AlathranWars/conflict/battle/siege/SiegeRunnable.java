package com.github.alathra.AlathranWars.conflict.battle.siege;

import com.github.alathra.AlathranWars.Main;
import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import fr.skytasul.guardianbeam.Laser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.time.Instant;

import static com.github.alathra.AlathranWars.conflict.battle.siege.Siege.*;

public class SiegeRunnable implements Runnable {
    // Settings
    private static final Duration ANNOUNCEMENT_COOLDOWN = Duration.ofMinutes(5);
    private final static int CAPTURE_MAX_ELEVATION = 10;
    private final static int CAPTURE_RANGE = 10;
    private final @NotNull Siege siege;

    // Variables
    private @NotNull CaptureProgressDirection oldProgressDirection = CaptureProgressDirection.NONE;
    private Instant nextAnnouncement;
    private ScheduledTask task;
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

        task = Main.getPaperLib().scheduling().globalRegionalScheduler().runAtFixedRate(this, 0L, 20L);

        siege.updateDisplayBar(CaptureProgressDirection.NONE);
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

        task = Main.getPaperLib().scheduling().globalRegionalScheduler().runAtFixedRate(this, 0L, 20L);

        siege.updateDisplayBar(CaptureProgressDirection.NONE);
    }

    public void cancel() {
        stopBeam();
        if (task.isCancelled()) return;

        try {
            task.cancel();
            siege.deleteDisplayBar();
        } finally {
            task = null;
        }
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
        final @NotNull CaptureProgressDirection progressDirection = getSiegeProgressDirection(townSpawn);

        // Siege is past max time or attackers haven't touched in time, defenders won
        if (
            !progressDirection.equals(CaptureProgressDirection.UP) &&
                (Instant.now().isAfter(siege.getEndTime()) ||
                    Instant.now().isAfter(siege.getLastTouched().plus(ATTACKERS_MUST_TOUCH_END)))
        ) {
            cancel();
            siege.defendersWin();
            return;
        }

        // Attackers captured the town
        if (siege.getSiegeProgress() >= MAX_SIEGE_PROGRESS) {
            cancel();
            siege.attackersWin();
            return;
        }

        switch (progressDirection) {
            case UP -> siege.setSiegeProgress(siege.getSiegeProgress() + 1);
            case DOWN -> siege.setSiegeProgress(siege.getSiegeProgress() - 1);
        }

        if (oldProgressDirection != progressDirection) {
            switch (progressDirection) {
                case UP -> siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                    ColorParser.of("<prefix>The Attackers are capturing the home block.")
                        .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                        .build()
                ));
                case NONE -> {
                    /*if ()
                    siege.getAttackers().forEach(p -> p.sendMessage(
                        ColorParser.of("<prefix>The home block is being contested.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));
                    siege.getDefenders().forEach(p -> p.sendMessage(
                        ColorParser.of("<prefix>The home block is being contested.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));*/
                }
                case DOWN -> siege.getPlayersOnBattlefield().forEach(p -> p.sendMessage(
                    ColorParser.of("<prefix>The Defenders re-secured the home block.")
                        .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                        .build()
                ));
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

    public @NotNull CaptureProgressDirection getSiegeProgressDirection(@NotNull Location townSpawn) {
        int attackersOnPoint = getPeopleOnPoint(townSpawn, BattleSide.ATTACKER);
        int defendersOnPoint = getPeopleOnPoint(townSpawn, BattleSide.DEFENDER);
        final boolean attackersAreOnPoint = attackersOnPoint > 0;
        final boolean defendersAreOnPoint = defendersOnPoint > 0;

        if (attackersAreOnPoint)
            siege.setLastTouched(Instant.now());

        if (attackersAreOnPoint && defendersAreOnPoint) {
            return CaptureProgressDirection.NONE;
        } else if (attackersAreOnPoint && !defendersAreOnPoint) {
            if (siege.getSiegeProgress() == MAX_SIEGE_PROGRESS)
                return CaptureProgressDirection.NONE;

            return CaptureProgressDirection.UP;
        } else {
            if (siege.getSiegeProgress() == 0)
                return CaptureProgressDirection.NONE;

            // If the attackers haven't touched in a while, begin reverting progress
            if (Instant.now().isAfter(siege.getLastTouched().plus(ATTACKERS_MUST_TOUCH_REVERT))) {
                return CaptureProgressDirection.DOWN;
            } else {
                return CaptureProgressDirection.NONE;
            }
        }
    }

    private void startBeam() {
        if (beam != null)
            return;

        try {
            @Nullable Location loc1 = siege.getTownSpawn();
            @NotNull Location loc2 = new Location(loc1.getWorld(), loc1.getX(), loc1.getY() + 350D, loc1.getZ());

            beam = new Laser.CrystalLaser(loc1, loc2, -1, -1);
            beam.start(Main.getInstance());
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private void stopBeam() {
        if (beam != null) {
            beam.stop();
            beam = null;
        }
    }
}
