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

import com.github.alathra.AlathranWars.utility.Logger;
import com.github.milkdrinkers.colorparser.ColorParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class Packets {
    public static boolean enabled = false;
    static int version;
    private static int versionMinor;
    private static final String npack = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    private static final String cpack = Bukkit.getServer().getClass().getPackage().getName() + ".";

    static final int crystalID = 51; // pre-1.13

    static Object crystalType;

    private static Constructor<?> crystalConstructor;

    private static Object watcherObject4; // crystal target
    private static Object watcherObject5; // crystal base plate

    private static Constructor<?> watcherConstructor;
    private static Method watcherSet;
    private static Method watcherRegister;
    private static Method watcherDirty;
    private static Method watcherPack;

    private static Constructor<?> blockPositionConstructor;

    private static Constructor<?> packetSpawnNormal;
    private static Constructor<?> packetRemove;
    private static Constructor<?> packetMetadata;

    private static Method getHandle;
    private static Field playerConnection;
    private static Method sendPacket;
    private static Method setUUID;
    private static Method setID;

    private static Object fakeSquid;

    private static Object nmsWorld;

    static {
        try {
            // e.g. Bukkit.getServer().getClass().getPackage().getName() -> org.bukkit.craftbukkit.v1_17_R1
            String[] versions = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1).split("_");
            version = Integer.parseInt(versions[1]); // 1.X
            if (version >= 17) {
                // e.g. Bukkit.getBukkitVersion() -> 1.17.1-R0.1-SNAPSHOT
                versions = Bukkit.getBukkitVersion().split("-R")[0].split("\\.");
                versionMinor = versions.length <= 2 ? 0 : Integer.parseInt(versions[2]);
            } else versionMinor = Integer.parseInt(versions[2].substring(1)); // 1.X.Y

            ProtocolMappings mappings = ProtocolMappings.getMappings(version);
            if (mappings == null) {
                mappings = ProtocolMappings.values()[ProtocolMappings.values().length - 1];
            }

            Class<?> entityTypesClass = getNMSClass("world.entity", "EntityTypes");
            Class<Entity> entityClass = Entity.class;
            Class<EndCrystal> crystalClass = EndCrystal.class;
            Class<?> squidClass = Squid.class;
            // invisilibity
            Object watcherObject1 = getField(entityClass, mappings.getWatcherFlags(), null);
            watcherObject4 = getField(crystalClass, mappings.getWatcherTargetLocation(), null);
            watcherObject5 = getField(crystalClass, mappings.getWatcherBasePlate(), null);

            if (version >= 13) {
                crystalType = entityTypesClass.getDeclaredField(mappings.getCrystalTypeName()).get(null);
            }

            Class<?> dataWatcherClass = getNMSClass("network.syncher", "DataWatcher");
            watcherConstructor = dataWatcherClass.getDeclaredConstructor(entityClass);
            if (version >= 18) {
                watcherSet = dataWatcherClass.getDeclaredMethod("b", watcherObject1.getClass(), Object.class);
                watcherRegister = dataWatcherClass.getDeclaredMethod("a", watcherObject1.getClass(), Object.class);
            } else {
                watcherSet = getMethod(dataWatcherClass, "set");
                watcherRegister = getMethod(dataWatcherClass, "register");
            }
            if (version >= 15) watcherDirty = getMethod(dataWatcherClass, "markDirty");
            if (version > 19 || (version == 19 && versionMinor >= 3))
                watcherPack = dataWatcherClass.getDeclaredMethod("b");
            packetSpawnNormal = getNMSClass("network.protocol.game", "PacketPlayOutSpawnEntity").getDeclaredConstructor(version < 17 ? new Class<?>[0] : new Class<?>[]{getNMSClass("world.entity", "Entity")});
            packetRemove = getNMSClass("network.protocol.game", "PacketPlayOutEntityDestroy").getDeclaredConstructor(version == 17 && versionMinor == 0 ? int.class : int[].class);
            packetMetadata = getNMSClass("network.protocol.game", "PacketPlayOutEntityMetadata")
                .getDeclaredConstructor(version < 19 || (version == 19 && versionMinor < 3)
                    ? new Class<?>[]{int.class, dataWatcherClass, boolean.class}
                    : new Class<?>[]{int.class, List.class});

            blockPositionConstructor =
                getNMSClass("core", "BlockPosition").getConstructor(int.class, int.class, int.class);

            nmsWorld = Class.forName(cpack + "CraftWorld").getDeclaredMethod("getHandle").invoke(Bukkit.getWorlds().get(0));

            Constructor<?> squidConstructor = squidClass.getDeclaredConstructors()[0];
            if (version >= 17) {
                crystalConstructor = crystalClass.getDeclaredConstructor(nmsWorld.getClass().getSuperclass(), double.class, double.class, double.class);
            }

            Object[] entityConstructorParams = version < 14 ? new Object[]{nmsWorld} : new Object[]{entityTypesClass.getDeclaredField(mappings.getSquidTypeName()).get(null), nmsWorld};
            fakeSquid = squidConstructor.newInstance(entityConstructorParams);
            Object fakeSquidWatcher = createFakeDataWatcher();
            tryWatcherSet(fakeSquidWatcher, watcherObject1, (byte) 32);

            getHandle = Class.forName(cpack + "entity.CraftPlayer").getDeclaredMethod("getHandle");
            playerConnection = getNMSClass("server.level", "EntityPlayer")
                .getDeclaredField(version < 17 ? "playerConnection" : (version < 20 ? "b" : "c"));
            playerConnection.setAccessible(true);
            sendPacket = getNMSClass("server.network", "PlayerConnection").getMethod(
                version < 18 ? "sendPacket" : (version >= 20 && versionMinor >= 2 ? "b" : "a"),
                getNMSClass("network.protocol", "Packet"));

            if (version >= 17) {
                setUUID = entityClass.getDeclaredMethod("a_", UUID.class);
                setID = entityClass.getDeclaredMethod("e", int.class);
            }

            enabled = true;
        } catch (Exception e) {
            Logger.get().error(
                ColorParser.of("Laser reflection failed to initialize. The utility is disabled. Please ensure your version (<version>) is supported.")
                    .parseMinimessagePlaceholder("version", Bukkit.getServer().getClass().getPackage().getName())
                    .build()
                , e);
        }
    }

    static int generateEID() {
        return Entity.nextEntityId();
    }

    public static void sendPackets(Player p, Object... packets) throws ReflectiveOperationException {
        Object connection = playerConnection.get(getHandle.invoke(p));
        for (Object packet : packets) {
            if (packet == null) continue;
            sendPacket.invoke(connection, packet);
        }
    }

    public static Object createFakeDataWatcher() throws ReflectiveOperationException {
        Object watcher = watcherConstructor.newInstance(fakeSquid);
        if (version > 13) setField(watcher, "registrationLocked", false);
        return watcher;
    }

    public static Object createCrystal(Location location, UUID uuid, int id) throws ReflectiveOperationException {
        Object entity = crystalConstructor.newInstance(nmsWorld, location.getX(), location.getY(), location.getZ());
        setEntityIDs(entity, uuid, id);
        return entity;
    }

    public static Object createPacketEntitySpawnNormal(Location location, int typeID, Object type, int id) throws ReflectiveOperationException {
        Object packet = packetSpawnNormal.newInstance();
        setField(packet, "a", id);
        setField(packet, "b", UUID.randomUUID());
        setField(packet, "c", location.getX());
        setField(packet, "d", location.getY());
        setField(packet, "e", location.getZ());
        setField(packet, "i", (int) (location.getYaw() * 256.0F / 360.0F));
        setField(packet, "j", (int) (location.getPitch() * 256.0F / 360.0F));
        setField(packet, "k", version < 13 ? typeID : type);
        return packet;
    }

    public static Object createPacketEntitySpawnNormal(Object entity) throws ReflectiveOperationException {
        return packetSpawnNormal.newInstance(entity);
    }

    public static void setCrystalWatcher(Object watcher, Location target) throws ReflectiveOperationException {
        Object blockPosition =
            blockPositionConstructor.newInstance(target.getBlockX(), target.getBlockY(), target.getBlockZ());
        tryWatcherSet(watcher, watcherObject4, Optional.of(blockPosition));
        tryWatcherSet(watcher, watcherObject5, Boolean.FALSE);
    }

    public static Object[] createPacketsRemoveEntities(int... entitiesId) throws ReflectiveOperationException {
        Object[] packets;
        if (version == 17 && versionMinor == 0) {
            packets = new Object[entitiesId.length];
            for (int i = 0; i < entitiesId.length; i++) {
                packets[i] = packetRemove.newInstance(entitiesId[i]);
            }
        } else {
            packets = new Object[]{packetRemove.newInstance(entitiesId)};
        }
        return packets;
    }

    public static void setEntityIDs(Object entity, UUID uuid, int id) throws ReflectiveOperationException {
        setUUID.invoke(entity, uuid);
        setID.invoke(entity, id);
    }

    static Object createPacketMetadata(int entityId, Object watcher) throws ReflectiveOperationException {
        if (version < 19 || (version == 19 && versionMinor < 3)) {
            return packetMetadata.newInstance(entityId, watcher, false);
        } else {
            return packetMetadata.newInstance(entityId, watcherPack.invoke(watcher));
        }
    }

    private static void tryWatcherSet(Object watcher, Object watcherObject, Object watcherData) throws ReflectiveOperationException {
        try {
            watcherSet.invoke(watcher, watcherObject, watcherData);
        } catch (InvocationTargetException ex) {
            watcherRegister.invoke(watcher, watcherObject, watcherData);
            if (version >= 15) watcherDirty.invoke(watcher, watcherObject);
        }
    }

    /* Reflection utils */
    private static Method getMethod(Class<?> clazz, String name) throws NoSuchMethodException {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name)) return m;
        }
        throw new NoSuchMethodException(name + " in " + clazz.getName());
    }

    private static void setField(Object instance, String name, Object value) throws ReflectiveOperationException {
        Field field = instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }

    private static Object getField(Class<?> clazz, String name, Object instance) throws ReflectiveOperationException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(instance);
    }

    private static Class<?> getNMSClass(String package17, String className) throws ClassNotFoundException {
        return Class.forName((version < 17 ? npack : "net.minecraft." + package17) + "." + className);
    }

    private enum ProtocolMappings {
        V1_9(9, "Z", "bA", "bB", "b", "c", 94, 68),
        V1_10(10, V1_9),
        V1_11(11, V1_10),
        V1_12(12, V1_11),
        V1_13(13, "ac", "bF", "bG", "b", "c", 70, 28),
        V1_14(14, "W", "b", "bD", "c", "d", 73, 30),
        V1_15(15, "T", "b", "bA", "c", "d", 74, 31),
        V1_16(16, null, "b", "d", "c", "d", -1, 31) {
            @Override
            public String getWatcherFlags() {
                return Packets.versionMinor < 2 ? "T" : "S";
            }
        },
        V1_17(17, "Z", "b", "e", "c", "d", 86, 35, "K", "aJ", "u", "setCollisionRule", "getPlayerNameSet"),
        V1_18(18, null, "b", "e", "c", "d", 86, 35, "K", "aJ", "u", "a", "g") {
            @Override
            public String getWatcherFlags() {
                return Packets.versionMinor < 2 ? "aa" : "Z";
            }
        },
        V1_19(19, null, "b", "e", "c", "d", 89, 38, null, null, "w", "a", "g") {
            @Override
            public String getWatcherFlags() {
                return versionMinor < 4 ? "Z" : "an";
            }

            @Override
            public String getSquidTypeName() {
                if (versionMinor < 3)
                    return "aM";
                else if (versionMinor == 3)
                    return "aN";
                else
                    return "aT";
            }

        },
        V1_20(20, null, "b", "e", "c", "d", 89, 38, "V", "aT", "B", "a", "g") {
            @Override
            public String getWatcherFlags() {
                return versionMinor < 2 ? "an" : "ao";
            }
        },
        ;

        private final int major;
        private final String watcherFlags;
        private final String watcherSpikes;
        private final String watcherTargetEntity;
        private final String watcherTargetLocation;
        private final String watcherBasePlate;
        private final int squidID;
        private final int guardianID;
        private final String guardianTypeName;
        private final String squidTypeName;
        private final String crystalTypeName;
        private final String teamSetCollision;
        private final String teamGetPlayers;

        ProtocolMappings(int major, ProtocolMappings parent) {
            this(major, parent.watcherFlags, parent.watcherSpikes, parent.watcherTargetEntity, parent.watcherTargetLocation, parent.watcherBasePlate, parent.squidID, parent.guardianID, parent.guardianTypeName, parent.squidTypeName, parent.crystalTypeName, parent.teamSetCollision, parent.teamGetPlayers);
        }

        ProtocolMappings(int major,
                         String watcherFlags, String watcherSpikes, String watcherTargetEntity, String watcherTargetLocation, String watcherBasePlate,
                         int squidID, int guardianID) {
            this(major, watcherFlags, watcherSpikes, watcherTargetEntity, watcherTargetLocation, watcherBasePlate, squidID, guardianID, null, "SQUID", "END_CRYSTAL", null, null);
        }

        ProtocolMappings(int major,
                         String watcherFlags, String watcherSpikes, String watcherTargetEntity, String watcherTargetLocation, String watcherBasePlate,
                         int squidID, int guardianID,
                         String guardianTypeName, String squidTypeName, String crystalTypeName, String teamSetCollision, String teamGetPlayers) {
            this.major = major;
            this.watcherFlags = watcherFlags;
            this.watcherSpikes = watcherSpikes;
            this.watcherTargetEntity = watcherTargetEntity;
            this.watcherTargetLocation = watcherTargetLocation;
            this.watcherBasePlate = watcherBasePlate;
            this.squidID = squidID;
            this.guardianID = guardianID;
            this.guardianTypeName = guardianTypeName;
            this.squidTypeName = squidTypeName;
            this.crystalTypeName = crystalTypeName;
            this.teamSetCollision = teamSetCollision;
            this.teamGetPlayers = teamGetPlayers;
        }

        public static ProtocolMappings getMappings(int major) {
            for (ProtocolMappings map : values()) {
                if (major == map.getMajor()) return map;
            }
            return null;
        }

        public int getMajor() {
            return major;
        }

        public String getWatcherFlags() {
            return watcherFlags;
        }

        public String getWatcherTargetLocation() {
            return watcherTargetLocation;
        }

        public String getWatcherBasePlate() {
            return watcherBasePlate;
        }

        public String getSquidTypeName() {
            return squidTypeName;
        }

        public String getCrystalTypeName() {
            return crystalTypeName;
        }

    }
}
