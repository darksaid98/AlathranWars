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

package com.github.alathra.alathranwars.conflict.battle.beam;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public enum LaserType {
    /**
     * Represents a laser from an Ender Crystal entity.
     * <p>
     * Start and end locations are automatically rounded to integers (block locations).
     */
    ENDER_CRYSTAL;

    /**
     * Creates a new Laser instance, {@link CrystalLaser} depending on this enum value.
     *
     * @param start    Location where laser will starts
     * @param end      Location where laser will ends
     * @param duration Duration of laser in seconds (<i>-1 if infinite</i>)
     * @param distance Distance where laser will be visible
     * @throws ReflectiveOperationException if a reflection exception occurred during Laser creation
     * @see Laser#start(Plugin) to start the laser
     * @see Laser#durationInTicks() to make the duration in ticks
     * @see Laser#executeEnd(Runnable) to add Runnable-s to execute when the laser will stop
     */
    public Laser create(Location start, Location end, int duration, int distance) throws ReflectiveOperationException {
        if (this == LaserType.ENDER_CRYSTAL) {
            return new CrystalLaser(start, end, duration, distance);
        }
        throw new IllegalStateException();
    }
}
