package com.github.alathra.AlathranWars.conflict.battle.siege;

import com.github.alathra.AlathranWars.Main;
import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import fr.skytasul.guardianbeam.Laser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;

import static com.github.alathra.AlathranWars.conflict.battle.siege.Siege.*;

public class SiegeRunnable implements Runnable{
    private static final Duration ANNOUNCEMENT_COOLDOWN = Duration.ofMinutes(5);

    private CaptureProgressDirection oldProgressDirection = CaptureProgressDirection.NONE;
    private Instant nextAnnouncement;
    private int taskId = -1;
    private final Siege siege;
    private Laser beam;

    /**
     * Start a siege
     *
     * @param siege the siege
     */
    public SiegeRunnable(Siege siege) {
        this.siege = siege;

        siege.setSiegeProgress(0);

        siege.setHomeBlock(siege.getTown().getHomeBlockOrNull());
        siege.setTownSpawn(siege.getTown().getSpawnOrNull());

        nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

        taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this, 0L, 20L);

        siege.updateDisplayBar(CaptureProgressDirection.NONE);
    }

    /**
     * Resume a siege at tick
     *
     * @param siege      the siege
     * @param siegeProgress the siege ticks
     */
    public SiegeRunnable(Siege siege, int siegeProgress) {
        this.siege = siege;

        siege.setSiegeProgress(siegeProgress);

        siege.setHomeBlock(siege.getTown().getHomeBlockOrNull());
        siege.setTownSpawn(siege.getTown().getSpawnOrNull());

        nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

        taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), this, 0L, 20L);

        siege.updateDisplayBar(CaptureProgressDirection.NONE);
    }

    public void cancel() {
        stopBeam();
        if (taskId == -1) return;

        try {
            Bukkit.getServer().getScheduler().cancelTask(taskId);
            siege.deleteDisplayBar();
        } finally {
            taskId = -1;
        }
    }

    @Override
    public void run() {
        final Town town = siege.getTown();
        final TownBlock homeBlock = siege.getHomeBlock();
        final Location townSpawn = siege.getTownSpawn();

        if (homeBlock != null && townSpawn != null) {
            town.setHomeBlock(homeBlock);
            town.setSpawn(townSpawn);
        }

        // Render beam
        startBeam();

        // Calculate battlefield players
        siege.calculateBattlefieldPlayers(townSpawn.toCenterLocation());

        // Progress the siege
        final CaptureProgressDirection progressDirection = getSiegeProgressDirection(town, townSpawn);

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
            siege.attackersWin(siege.getSiegeLeader());
            return;
        }

        switch (progressDirection) {
            case UP -> siege.setSiegeProgress(siege.getSiegeProgress() + 1);
            case DOWN -> siege.setSiegeProgress(siege.getSiegeProgress() - 1);
        }

        if (oldProgressDirection != progressDirection) {
            switch (progressDirection) {
                case UP -> {
                    siege.getAttackers().forEach(p -> p.sendMessage(
                        new ColorParser("<prefix>The Attackers are capturing the home block.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));
                    siege.getDefenders().forEach(p -> p.sendMessage(
                        new ColorParser("<prefix>The Attackers are capturing the home block.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));
                }
                case NONE -> {
                    /*if ()
                    siege.getAttackers().forEach(p -> p.sendMessage(
                        new ColorParser("<prefix>The home block is being contested.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));
                    siege.getDefenders().forEach(p -> p.sendMessage(
                        new ColorParser("<prefix>The home block is being contested.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));*/
                }
                case DOWN -> {
                    siege.getAttackers().forEach(p -> p.sendMessage(
                        new ColorParser("<prefix>The Defenders re-secured the home block.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));
                    siege.getDefenders().forEach(p -> p.sendMessage(
                        new ColorParser("<prefix>The Defenders re-secured the home block.")
                            .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                            .build()
                    ));
                }
            }
        }

        oldProgressDirection = progressDirection;

        /*Bukkit.broadcast(
            new ColorParser("<prefix>Progress: <progress> <progress2> <test> <score>")
                .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                .parseMinimessagePlaceholder("progress", "%.0f%%".formatted(siege.getSiegeProgressPercentage() * 100))
                .parseMinimessagePlaceholder("progress2", String.valueOf(siege.getSiegeProgressPercentage()))
                .parseMinimessagePlaceholder("test", progressDirection.name())
                .parseMinimessagePlaceholder("score", String.valueOf(siege.getSiegeProgress()))
                .build()
        );*/

        if (Instant.now().isAfter(nextAnnouncement)) {
            nextAnnouncement = Instant.now().plus(ANNOUNCEMENT_COOLDOWN);

            siege.getAttackers().forEach(p -> p.sendMessage(
                new ColorParser("<prefix>Siege time remaining: <time> minutes.")
                    .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                    .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now() , siege.getEndTime()).toMinutesPart()))
                    .build()
            ));
            siege.getDefenders().forEach(p -> p.sendMessage(
                new ColorParser("<prefix>Siege time remaining: <time> minutes.")
                    .parseMinimessagePlaceholder("prefix", UtilsChat.getPrefix())
                    .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now() , siege.getEndTime()).toMinutesPart()))
                    .build()
            ));
        }

        siege.updateDisplayBar(progressDirection);
    }

    private final static int CAPTURE_MAX_ELEVATION = 10;
    private final static int CAPTURE_RANGE = 10;

    public int getPeopleOnPoint(Town town, Location townSpawn, BattleSide battleSide) {
        int onPoint = 0;

        for (final Player p : (battleSide.equals(BattleSide.ATTACKER) ? siege.getAttackers() : siege.getDefenders())) {
//            WorldCoord wc = WorldCoord.parseWorldCoord(p);
//            if (!Objects.equals(wc.getTownOrNull(), town))
//                continue;

//            @Nullable TownBlock homeBlock = town.getHomeBlockOrNull();
//            if (homeBlock == null)
//                continue;

            if (/*!homeBlock.getWorldCoord().equals(wc) || */p.isDead())
                continue;

            if (townSpawn.distance(p.getLocation()) <= CAPTURE_RANGE) {
                onPoint += 1;
            }
//            if ((Math.abs(p.getLocation().getBlockY() - townSpawn.getBlockY())) < CAPTURE_MAX_ELEVATION) {
//                onPoint += 1;
//            }
        }

        return onPoint;
    }

    public CaptureProgressDirection getSiegeProgressDirection(Town town, Location townSpawn) {
        int attackersOnPoint = getPeopleOnPoint(town, townSpawn, BattleSide.ATTACKER);
        int defendersOnPoint = getPeopleOnPoint(town, townSpawn, BattleSide.DEFENDER);
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
        } /*else if (!attackersAreOnPoint && !defendersAreOnPoint) {
            return CaptureProgressDirection.UNCONTESTED;
        }*/ else {
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
            Location loc1 = siege.getTownSpawn();
            Location loc2 = new Location(loc1.getWorld(), loc1.getX(), loc1.getY() + 350D, loc1.getZ());

            beam = new Laser.CrystalLaser(loc1, loc2, -1, BATTLEFIELD_RANGE);

            if (beam != null)
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
