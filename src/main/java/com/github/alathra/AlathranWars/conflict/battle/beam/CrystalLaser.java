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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class CrystalLaser extends Laser {
    private final Object crystal;
    private final int crystalID = Packets.generateEID();
    private Object createCrystalPacket;
    private Object metadataPacketCrystal;
    private final Object[] destroyPackets;
    private final Object fakeCrystalDataWatcher;

    /**
     * Creates a new Ender Crystal Laser instance
     *
     * @param start    Location where laser will starts. The Crystal laser do not handle decimal number, it will be rounded to blocks.
     * @param end      Location where laser will ends. The Crystal laser do not handle decimal number, it will be rounded to blocks.
     * @param duration Duration of laser in seconds (<i>-1 if infinite</i>)
     * @param distance Distance where laser will be visible (<i>-1 if infinite</i>)
     * @throws ReflectiveOperationException if a reflection exception occurred during Laser creation
     * @see #start(Plugin) to start the laser
     * @see #durationInTicks() to make the duration in ticks
     * @see #executeEnd(Runnable) to add Runnable-s to execute when the laser will stop
     */
    public CrystalLaser(Location start, Location end, int duration, int distance) throws ReflectiveOperationException {
        super(start, end, duration, distance);

        fakeCrystalDataWatcher = Packets.createFakeDataWatcher();
        Packets.setCrystalWatcher(fakeCrystalDataWatcher, end);
        if (Packets.version < 17) {
            crystal = null;
        } else {
            crystal = Packets.createCrystal(start, UUID.randomUUID(), crystalID);
        }
        metadataPacketCrystal = Packets.createPacketMetadata(crystalID, fakeCrystalDataWatcher);

        destroyPackets = Packets.createPacketsRemoveEntities(crystalID);
    }

    private Object getCrystalSpawnPacket() throws ReflectiveOperationException {
        if (createCrystalPacket == null) {
            if (Packets.version < 17) {
                createCrystalPacket = Packets.createPacketEntitySpawnNormal(start, Packets.crystalID, Packets.crystalType, crystalID);
            } else {
                createCrystalPacket = Packets.createPacketEntitySpawnNormal(crystal);
            }
        }
        return createCrystalPacket;
    }

    @Override
    public LaserType getLaserType() {
        return LaserType.ENDER_CRYSTAL;
    }

    @Override
    protected void sendStartPackets(Player p, boolean hasSeen) throws ReflectiveOperationException {
        Packets.sendPackets(p, getCrystalSpawnPacket());
        Packets.sendPackets(p, metadataPacketCrystal);
    }

    @Override
    protected void sendDestroyPackets(Player p) throws ReflectiveOperationException {
        Packets.sendPackets(p, destroyPackets);
    }
}
