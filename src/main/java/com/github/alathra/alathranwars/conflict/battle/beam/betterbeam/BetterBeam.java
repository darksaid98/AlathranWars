package com.github.alathra.alathranwars.conflict.battle.beam.betterbeam;

public class BetterBeam {
    /*BetterBeam() {
        PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        PacketContainer metadataPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

        // Required fields
        // EndCrystal entity
        // Squid entity (Made invisible)
        AlathranWars.getProtocolLibHook().getProtocolManager()

        PacketContainer spawnCrystalPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        PacketContainer spawnCrystalPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        ProtocolManager manager = AlathranWars.getProtocolLibHook().getProtocolManager();

        Entity endcrystal;

        endcrystal.getI
        spawnPacket.getStructures()



    }

    private void spawnEntity(Location location, Vector vector, EntityType type) {
        int entityId = 10000;

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        // Crucial data, do not remove
        packet.getIntegers().write(0, entityId);
        packet.getUUIDs().write(0, UUID.randomUUID());
        packet.getEntityTypeModifier().write(0, type);

        packet.getDoubles()
            .write(0, location.getX())
            .write(1, location.getY())
            .write(2, location.getZ());
        // -- section end --

        // Pitch and yaw, remove this code section if you want to default to 0

        packet.
        packet.getIntegers()
            .write(4, (int) (location.getPitch() * 256.0F / 360.0F))
            .write(5, (int) (location.getYaw() * 256.0F / 360.0F));

        // -- section end --

        // Send the packet to all players
        try {
            for(Player player : Bukkit.getOnlinePlayers()) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void spawnLivingEntity(Location location, Vector vector, EntityType type) {
        int entityId = 10000;

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        // Crucial data, do not remove
        packet.getIntegers().write(0, entityId);
        packet.getUUIDs().write(0, UUID.randomUUID());
        packet.getIntegers().write(1, (int) type.getTypeId()); // getTypeId is deprecated, you can find the actual IDs on wiki.vg/Protocol

        packet.getDoubles()
            .write(0, location.getX())
            .write(1, location.getY())
            .write(2, location.getZ());
        // -- section end --

        // Pitch and yaw, remove this code section if you want to default to 0
        packet.getBytes()
            .write(4, (byte) (location.getYaw() * 256.0F / 360.0F)) // yaw
            .write(5, (byte) (location.getPitch() * 256.0F / 360.0F)) // pitch
            .write(5, (byte) (location.getYaw() * 256.0F / 360.0F)); // head yaw

        // -- section end --

        // Send the packet to all players
        try {
            for(Player player : Bukkit.getOnlinePlayers()) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
}
