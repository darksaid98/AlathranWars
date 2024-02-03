/*
MIT License

Copyright (c) 2021 SkytAsul

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.github.alathra.AlathranWars.conflict.battle.beam;

import com.github.alathra.AlathranWars.AlathranWars;
import com.github.alathra.AlathranWars.utility.Logger;
import com.github.milkdrinkers.colorparser.ColorParser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A whole class to create Guardian Lasers and Ender Crystal Beams using packets and reflection.<br>
 * Inspired by the API
 * <a href="https://www.spigotmc.org/resources/guardianbeamapi.18329">GuardianBeamAPI</a><br>
 * <b>1.9 -> 1.20</b>
 *
 * @author SkytAsul
 * @version 2.3.3
 * @see <a href="https://github.com/SkytAsul/GuardianBeam">GitHub repository</a>
 */
public abstract class Laser {
    protected final int distanceSquared;
    protected final int duration;
    protected boolean durationInTicks = false;
    protected Location start;
    protected Location end;

    protected Plugin plugin;
    protected ScheduledTask task;

    protected Set<Player> show = ConcurrentHashMap.newKeySet();
    private final Set<Player> seen = new HashSet<>();

    private final List<Runnable> executeEnd = new ArrayList<>(1);

    protected Laser(Location start, Location end, int duration, int distance) {
        if (!Packets.enabled)
            throw new IllegalStateException("The Laser Beam API is disabled. An error has occured during initialization.");
        if (start.getWorld() != end.getWorld())
            throw new IllegalArgumentException("Locations do not belong to the same worlds.");
        this.start = start.clone();
        this.end = end.clone();
        this.duration = duration;
        distanceSquared = distance < 0 ? -1 : distance * distance;
    }

    /**
     * Adds a runnable to execute when the laser reaches its final duration
     *
     * @param runnable action to execute
     * @return this {@link Laser} instance
     */
    public Laser executeEnd(Runnable runnable) {
        executeEnd.add(runnable);
        return this;
    }

    /**
     * Makes the duration provided in the constructor passed as ticks and not seconds
     *
     * @return this {@link Laser} instance
     */
    public Laser durationInTicks() {
        durationInTicks = true;
        return this;
    }

    /**
     * Starts this laser.
     * <p>
     * It will make the laser visible for nearby players and start the countdown to the final duration.
     * <p>
     * Once finished, it will destroy the laser and execute all runnables passed with {@link Laser#executeEnd}.
     *
     * @param plugin plugin used to start the task
     */
    public void start(Plugin plugin) {
        if (task != null) throw new IllegalStateException("Task already started");
        this.plugin = plugin;
        Runnable runnable = new BukkitRunnable() {
            int time = 0;

            @Override
            public void run() {
                try {
                    if (time == duration) {
                        stop();
                        return;
                    }
                    if (!durationInTicks || time % 20 == 0) {
                        for (Player p : start.getWorld().getPlayers()) {
                            if (isCloseEnough(p)) {
                                if (show.add(p)) {
                                    sendStartPackets(p, !seen.add(p));
                                }
                            } else if (show.remove(p)) {
                                sendDestroyPackets(p);
                            }
                        }
                    }
                    time++;
                } catch (ReflectiveOperationException e) {
                    Logger.get().error(ColorParser.of("<red>Laser packer exception: ").build(), e);
                }
            }
        };
        task = AlathranWars.getPaperLib().scheduling().asyncScheduler().runAtFixedRate(runnable, Duration.ZERO, Duration.ofMillis(durationInTicks ? 50L : 1000L));
    }

    /**
     * Stops this laser.
     * <p>
     * This will destroy the laser for every player and start execute all runnables passed with {@link Laser#executeEnd}
     */
    public synchronized void stop() {
        if (task == null || task.isCancelled()) throw new IllegalStateException("Task not started");
        task.cancel();

        try {
            try {
                for (Player p : show) {
                    sendDestroyPackets(p);
                }
                show.clear();
                executeEnd.forEach(Runnable::run);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } catch (IllegalStateException ignored) {
        }


    }

    /**
     * Gets laser status.
     *
     * @return <code>true</code> if the laser is currently running
     * (i.e. {@link #start} has been called and the duration is not over)
     */
    public boolean isStarted() {
        return task != null;
    }

    /**
     * Gets laser type.
     *
     * @return LaserType enum constant of this laser
     */
    public abstract LaserType getLaserType();

    /**
     * Gets the start location of the laser.
     *
     * @return where exactly is the start position of the laser located
     */
    public Location getStart() {
        return start.clone();
    }

    /**
     * Gets the end location of the laser.
     *
     * @return where exactly is the end position of the laser located
     */
    public Location getEnd() {
        return end.clone();
    }

    protected abstract void sendStartPackets(Player p, boolean hasSeen) throws ReflectiveOperationException;

    protected abstract void sendDestroyPackets(Player p) throws ReflectiveOperationException;

    protected boolean isCloseEnough(Player player) {
        if (distanceSquared == -1) return true;
        Location location = player.getLocation();
        return getStart().distanceSquared(location) <= distanceSquared ||
            getEnd().distanceSquared(location) <= distanceSquared;
    }

}