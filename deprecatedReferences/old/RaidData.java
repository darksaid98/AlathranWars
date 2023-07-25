package me.ShermansWorld.AlathranWars.data;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import me.ShermansWorld.AlathranWars.Main;
import me.ShermansWorld.AlathranWars.deprecated.OldRaid;
import me.ShermansWorld.AlathranWars.deprecated.OldWar;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

@Deprecated
public class RaidData {
    // Static OldRaid list for all active OLD_RAIDS
    private static final ArrayList<OldRaid> OLD_RAIDS = new ArrayList<>();


    public static ArrayList<OldRaid> getRaids() {
        return OLD_RAIDS;
    }

    public static void setRaids(ArrayList<OldRaid> oldRaids) {
        for (OldRaid oldRaid : oldRaids) {
            addRaid(oldRaid);
        }
    }

    /**
     * Gets a raid with a specific name
     *
     * @param name - Name to check
     * @return OldRaid or Null
     */
    public static @Nullable OldRaid getRaid(String name) {
        for (OldRaid oldRaid : OLD_RAIDS) {
            if (oldRaid.getName().equalsIgnoreCase(name)) return oldRaid;
        }
        return null;
    }

    public static void addRaid(OldRaid oldRaid) {
        if (oldRaid == null) {
            Main.warLogger.log("Attempted to add NULL to oldRaid list.");
            return;
        }
        OLD_RAIDS.add(oldRaid);
    }

    public static void removeRaid(OldRaid oldRaid) {
        oldRaid.getWar().getRaids().remove(oldRaid);
        OLD_RAIDS.remove(oldRaid);

        //deleteRaid(oldRaid);
    }

    /**
     * Creates a raid object from a provided HashMap
     *
     * @param fileData
     * @return OldRaid object
     */
    @SuppressWarnings("unchecked")
    public static OldRaid fromMap(OldWar oldWar, HashMap<String, Object> fileData) {

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
        OldRaid oldRaid = new OldRaid(oldWar, raidedTown, gatherTown, attackBoolean, offlinePlayer);

        //Extra properties, if any are missing the oldRaid doesnt exist yet
        if (fileData.get("raiderScore") != null && fileData.get("defenderScore") != null && fileData.get("raidTicks") != null && fileData.get("raidPhase") != null && fileData.get("activeRaiders") != null && fileData.get("lootedChunks") != null && fileData.get("side1AreRaiders") != null) {
            //active properties
            oldRaid.setRaidPhase(RaidPhase.getByName((String) fileData.get("raidPhase")));
            oldRaid.setRaidTicks((int) fileData.get("raidTicks"));
            oldRaid.setActiveRaiders((ArrayList<String>) fileData.get("activeRaiders"));
            oldRaid.setRaiderScore((int) fileData.get("raiderScore"));
            oldRaid.setDefenderScore((int) fileData.get("defenderScore"));
            oldRaid.setSide1AreRaiders(Boolean.parseBoolean((String) fileData.get("side1AreRaiders")));

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
                OldRaid.LootBlock lb = new OldRaid.LootBlock(worldCoordc, ticks, value, finished);
                oldRaid.getLootedChunks().put(worldCoordc, lb);
            }
        }

        return oldRaid;
    }

    public static ArrayList<OldRaid> createRaids(OldWar oldWar, Collection<HashMap<String, Object>> raidMaps) {
        ArrayList<OldRaid> returnList = new ArrayList<>();
        for (HashMap<String, Object> map : raidMaps) {
            Town town = TownyAPI.getInstance().getTown((String) map.get("raidedTown"));
            if (town == null) continue;

            OldRaid oldRaid = fromMap(oldWar, map);
            returnList.add(oldRaid);
            RaidData.addRaid(oldRaid);
            oldRaid.resume();
        }
        return returnList;
    }


    /**
     * Saves the war into files.
     *
     * @param oldRaid - OldWar to be saved.
     */
    public static void saveRaid(OldRaid oldRaid) {
        OldWar oldWar = oldRaid.getWar();
        oldWar.save();
    }

//    /**
//     * deletes raid, unused
//     */
//    private static void deleteRaid(OldWar war) {
//        File[] files = new File(dataFolderPath + File.separator + "wars").listFiles(ymlFilter);
//
//        for (File file : files) {
//            if (file.getName().startsWith(war.getName())) {
//                file.delete();
//            }
//        }
//    }

    /**
     * Turns a oldRaid into a map
     *
     * @param oldRaid - OldRaid
     * @return Map
     */
    public static HashMap<String, Object> raidToMap(OldRaid oldRaid) {
        HashMap<String, Object> returnMap = new HashMap<>();
        // Shoves everything into a map.
        returnMap.put("name", oldRaid.getName());
        returnMap.put("raidedTown", oldRaid.getRaidedTown().getName());
        returnMap.put("gatherTown", oldRaid.getGatherTown().getName());
        returnMap.put("raidTicks", oldRaid.getRaidTicks());
        returnMap.put("raidPhase", oldRaid.getPhase().name());
        returnMap.put("raiderScore", oldRaid.getRaiderScore());
        returnMap.put("defenderScore", oldRaid.getDefenderScore());
        returnMap.put("side1AreRaiders", Boolean.toString(oldRaid.getSide1AreRaiders()));
        returnMap.put("activeRaiders", oldRaid.getActiveRaiders());
        //THIS MAY OR MAY NOT WORK
        returnMap.put("owner", oldRaid.getOwner().getUniqueId().toString());

        //create a list from the looted chunks
        final List<Object> chunkList = getChunkList(oldRaid);

        returnMap.put("lootedChunks", chunkList);

        return returnMap;
    }

    @NotNull
    private static List<Object> getChunkList(OldRaid oldRaid) {
        List<Object> chunkList = new ArrayList<>();
        for (OldRaid.LootBlock b : oldRaid.getLootedChunks().values()) {
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
     * @param oldWar - OldWar to map
     * @return Map of Maps
     */
    public static HashMap<String, Object> getRaidMap(OldWar oldWar) {
        HashMap<String, Object> returnMap = new HashMap<>();
        for (OldRaid oldRaid : oldWar.getRaids()) {
            returnMap.put(oldRaid.getRaidedTown().getName(), raidToMap(oldRaid));
        }
        return returnMap;
    }

    /**
     * @return
     * @Isaac this is for getting when the last raid was on a town
     */
    @SuppressWarnings("rawtypes")
    public static long whenTownLastRaided(Town town) {
        if (town.hasMeta("AlathranWars-lastRaided")) {
            CustomDataField field = town.getMetadata("AlathranWars-lastRaided");
            if (field != null) {
                if (field instanceof LongDataField) {
                    return ((LongDataField) field).getValue();
                }
            }
            return -1L;
        } else {
            town.addMetaData(new LongDataField("AlathranWars-lastRaided", 0L));
        }
        return -1L;
    }

    /**
     * @return
     * @Isaac this is for getting if a town can be raided, return (-2, -1, 0, 1, 2) based on status
     * (no players online, 24 hours town cooldown, 6 hour oldWar cooldown, town being raided already, valid time to raid)
     */
    public static int isValidRaid(OldWar oldWar, String side, @Nonnull Town town) {
        long townTime = whenTownLastRaided(town);

        //24 hours town cooldown
        if ((System.currentTimeMillis() / 1000) - townTime <= 86400) {
            return -1;
        }

        //6 hour raid cooldown for oldWar
        if ((System.currentTimeMillis() / 1000) - (side.equalsIgnoreCase(oldWar.getSide1()) ? oldWar.getLastRaidTimeSide1() : oldWar.getLastRaidTimeSide2()) <= 21600) {
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
        for (OldRaid oldRaid : oldWar.getRaids()) {
            if (oldRaid.getRaidedTown().getName().equalsIgnoreCase(town.getName())) {
                return 1;
            }

        }

        return 2;
    }

}
