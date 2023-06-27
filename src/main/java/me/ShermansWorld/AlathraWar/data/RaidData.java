package me.ShermansWorld.AlathraWar.data;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.War;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

public class RaidData {
    // Static Raid list for all active raids
    private static final ArrayList<Raid> raids = new ArrayList<>();


    public static ArrayList<Raid> getRaids() {
        return raids;
    }

    public static void setRaids(ArrayList<Raid> raids) {
        for (Raid raid : raids) {
            addRaid(raid);
        }
    }

    /**
     * Gets a raid with a specific name
     *
     * @param name - Name to check
     * @return Raid or Null
     */
    public static @Nullable Raid getRaid(String name) {
        for (Raid raid : raids) {
            if (raid.getName().equalsIgnoreCase(name)) return raid;
        }
        return null;
    }

    public static void addRaid(Raid raid) {
        if (raid == null) {
            Main.warLogger.log("Attempted to add NULL to raid list.");
            return;
        }
        raids.add(raid);
    }

    public static void removeRaid(Raid raid) {
        raid.getWar().getRaids().remove(raid);
        raids.remove(raid);

        //deleteRaid(raid);
    }

    /**
     * Creates a raid object from a provided HashMap
     *
     * @param fileData
     * @return Raid object
     */
    @SuppressWarnings("unchecked")
    public static Raid fromMap(War war, HashMap<String, Object> fileData) {

        if (fileData.get("raidedTown") == null || fileData.get("gatherTown") == null || fileData.get("side1AreRaiders") == null) {
            return null;
        }

        Town raidedTown = TownyAPI.getInstance().getTown((String) fileData.get("raidedTown"));
        Town gatherTown = TownyAPI.getInstance().getTown((String) fileData.get("gatherTown"));
        boolean attackBoolean = Boolean.parseBoolean((String) fileData.get("side1AreRaiders"));
        //THIS MAY OR MAY NOT WORK
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString((String) fileData.get("owner")));

        if (raidedTown == null || gatherTown == null) {
            return null;
        }
        Raid raid = new Raid(war, raidedTown, gatherTown, attackBoolean, offlinePlayer);

        //Extra properties, if any are missing the raid doesnt exist yet
        if (fileData.get("raiderScore") != null && fileData.get("defenderScore") != null && fileData.get("raidTicks") != null && fileData.get("raidPhase") != null && fileData.get("activeRaiders") != null && fileData.get("lootedChunks") != null && fileData.get("side1AreRaiders") != null) {
            //active properties
            raid.setRaidPhase(RaidPhase.getByName((String) fileData.get("raidPhase")));
            raid.setRaidTicks((int) fileData.get("raidTicks"));
            raid.setActiveRaiders((ArrayList<String>) fileData.get("activeRaiders"));
            raid.setRaiderScore((int) fileData.get("raiderScore"));
            raid.setDefenderScore((int) fileData.get("defenderScore"));
            raid.setSide1AreRaiders(Boolean.parseBoolean((String) fileData.get("side1AreRaiders")));

            //looted chunks
            ArrayList<Object> o = (ArrayList<Object>) fileData.get("lootedChunks");
            for (Object hm : o) {
                //grab the chunk
                HashMap<String, Object> chunk = (HashMap<String, Object>) hm;

                //grab data for this chunk
                //worldcoord is its own map
                HashMap<String, Object> wc = (HashMap<String, Object>) chunk.get("worldCoord");
                WorldCoord worldCoordc = WorldCoord.parseWorldCoord((String) wc.get("world"), ((int) wc.get("x")), ((int) wc.get("z")));
                int ticks = (int) chunk.get("ticks");
                double value = (double) chunk.get("value");
                boolean finished = (boolean) chunk.get("finished");

                //put
                Raid.LootBlock lb = new Raid.LootBlock(worldCoordc, ticks, value, finished);
                raid.getLootedChunks().put(worldCoordc, lb);
            }
        }

        return raid;
    }

    public static ArrayList<Raid> createRaids(War war, Collection<HashMap<String, Object>> raidMaps) {
        ArrayList<Raid> returnList = new ArrayList<>();
        for (HashMap<String, Object> map : raidMaps) {
            Town town = TownyAPI.getInstance().getTown((String) map.get("raidedTown"));
            if (town == null) continue;

            Raid raid = fromMap(war, map);
            returnList.add(raid);
            RaidData.addRaid(raid);
            raid.resume();
        }
        return returnList;
    }


    /**
     * Saves the war into files.
     *
     * @param raid - War to be saved.
     */
    public static void saveRaid(Raid raid) {
        War war = raid.getWar();
        war.save();
    }

//    /**
//     * deletes raid, unused
//     */
//    private static void deleteRaid(War war) {
//        File[] files = new File(dataFolderPath + File.separator + "wars").listFiles(ymlFilter);
//
//        for (File file : files) {
//            if (file.getName().startsWith(war.getName())) {
//                file.delete();
//            }
//        }
//    }

    /**
     * Turns a raid into a map
     *
     * @param raid - Raid
     * @return Map
     */
    public static HashMap<String, Object> raidToMap(Raid raid) {
        HashMap<String, Object> returnMap = new HashMap<>();
        // Shoves everything into a map.
        returnMap.put("name", raid.getName());
        returnMap.put("raidedTown", raid.getRaidedTown().getName());
        returnMap.put("gatherTown", raid.getGatherTown().getName());
        returnMap.put("raidTicks", raid.getRaidTicks());
        returnMap.put("raidPhase", raid.getPhase().name());
        returnMap.put("raiderScore", raid.getRaiderScore());
        returnMap.put("defenderScore", raid.getDefenderScore());
        returnMap.put("side1AreRaiders", Boolean.toString(raid.getSide1AreRaiders()));
        returnMap.put("activeRaiders", raid.getActiveRaiders());
        //THIS MAY OR MAY NOT WORK
        returnMap.put("owner", raid.getOwner().getUniqueId().toString());

        //create a list from the looted chunks
        final List<Object> chunkList = getChunkList(raid);

        returnMap.put("lootedChunks", chunkList);

        return returnMap;
    }

    @NotNull
    private static List<Object> getChunkList(Raid raid) {
        List<Object> chunkList = new ArrayList<>();
        for (Raid.LootBlock b : raid.getLootedChunks().values()) {
            HashMap<String, Object> blockMap = new HashMap<>();
            //worldcoord
            HashMap<String, Object> wcMap = new HashMap<>();
            wcMap.put("world", b.worldCoord.getWorldName());
            wcMap.put("x", b.worldCoord.getCoord().getX());
            wcMap.put("z", b.worldCoord.getCoord().getZ());
            blockMap.put("worldCoord", wcMap);
            //properties
            blockMap.put("ticks", b.ticks);
            blockMap.put("value", b.value);
            blockMap.put("finished", b.finished);
            chunkList.add(blockMap);
        }
        return chunkList;
    }

    /**
     * Creates a map of raid maps
     *
     * @param war - War to map
     * @return Map of Maps
     */
    public static HashMap<String, Object> getRaidMap(War war) {
        HashMap<String, Object> returnMap = new HashMap<>();
        for (Raid raid : war.getRaids()) {
            returnMap.put(raid.getRaidedTown().getName(), raidToMap(raid));
        }
        return returnMap;
    }

    /**
     * @return
     * @Isaac this is for getting when the last raid was on a town
     */
    @SuppressWarnings("rawtypes")
    public static long whenTownLastRaided(Town town) {
        if (town.hasMeta("AlathraWar-lastRaided")) {
            CustomDataField field = town.getMetadata("AlathraWar-lastRaided");
            if (field != null) {
                if (field instanceof LongDataField) {
                    return ((LongDataField) field).getValue();
                }
            }
            return -1L;
        } else {
            town.addMetaData(new LongDataField("AlathraWar-lastRaided", 0L));
        }
        return -1L;
    }

    /**
     * @return
     * @Isaac this is for getting if a town can be raided, return (-2, -1, 0, 1, 2) based on status
     * (no players online, 24 hours town cooldown, 6 hour war cooldown, town being raided already, valid time to raid)
     */
    public static int isValidRaid(War war, String side, @Nonnull Town town) {
        long townTime = whenTownLastRaided(town);

        //24 hours town cooldown
        if ((System.currentTimeMillis() / 1000) - townTime <= 86400) {
            return -1;
        }

        //6 hour raid cooldown for war
        if ((System.currentTimeMillis() / 1000) - (side.equalsIgnoreCase(war.getSide1()) ? war.getLastRaidTimeSide1() : war.getLastRaidTimeSide2()) <= 21600) {
            return 0;
        }

        //make sure a player is online in the raided town
        boolean onlinePlayer = false;
        for (Resident r : town.getResidents()) {
            if (r.isOnline()) {
                onlinePlayer = true;
            }
        }
        if (!onlinePlayer) return -2;

        //check if being raided
        for (Raid raid : war.getRaids()) {
            if (raid.getRaidedTown().getName().equalsIgnoreCase(town.getName())) {
                return 1;
            }

        }

        return 2;
    }

}
