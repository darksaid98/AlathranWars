package me.ShermansWorld.AlathraWar.data;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.LongDataField;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.War;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RaidData {
    // Static Raid list for all active raids
    private static ArrayList<Raid> raids = new ArrayList<Raid>();
    private final static String dataFolderPath = "plugins" + File.separator + "AlathraWar" + File.separator + "data";

    // Filter for only accessing yml files
    private static FilenameFilter ymlFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".yml");
        }
    };

    public static ArrayList<Raid> getRaids() {
        return raids;
    }

    /**
     * Gets a raid with a specific name
     *
     * @param name - Name to check
     * @return Raid or Null
     */
    public static Raid getRaidOrNull(String name) {
        for (Raid raid : raids) {
            if (raid.getName().equals(name)) return raid;
        }
        return null;
    }

    public static void setRaids(ArrayList<Raid> raids) {
        for (Raid raid : raids) {
            addRaid(raid);
        }
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
        OfflinePlayer p = Bukkit.getOfflinePlayer((String) fileData.get("owner"));

        Raid raid = new Raid(war, raidedTown, gatherTown, attackBoolean, p.getPlayer());

        //Extra properties, if any are missing the raid doesnt exist yet
        if (fileData.get("raidTicks") != null && fileData.get("raidPhase") != null && fileData.get("activeRaiders") != null && fileData.get("lootedChunks") != null) {
            //active properties
            raid.setRaidPhase(RaidPhase.getByName((String) fileData.get("raidPhase")));
            raid.setRaidTicks((Integer) fileData.get("raidTicks"));
            raid.setActiveRaiders((ArrayList<String>) fileData.get("activeRaiders"));

            //looted chunks
            HashMap<String, Object> o = (HashMap<String, Object>) fileData.get("lootedChunks");
            for (String s : o.keySet()) {
                //grab the chunk
                HashMap<String, Object> chunk = (HashMap<String, Object>) o.get(s);

                //grab data for this chunk
                //worldcoord is its own map
                HashMap<String, Object> wc = (HashMap<String, Object>) chunk.get("worldCoord");
                WorldCoord worldCoordc = WorldCoord.parseWorldCoord((String) wc.get("world"), ((Integer) wc.get("x")), ((Integer) wc.get("z")));
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


    /**
     * Saves the war into files.
     *
     * @param raid - War to be saved.
     */
    public static void saveRaid(Raid raid) {
        War war = raid.getWar();
        war.save();
    }

    /**
     * deletes raid, unused
     */
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
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        // Shoves everything into a map.
        returnMap.put("name", raid.getName());
        returnMap.put("raidedTown", raid.getRaidedTown());
        returnMap.put("gatherTown", raid.getRaidedTown());
        returnMap.put("raidTicks", raid.getRaidTicks());
        returnMap.put("raidPhase", raid.getPhase().name());
        returnMap.put("raidScore", raid.getRaidScore());
        returnMap.put("side1AreRaiders", Boolean.toString(raid.getSide1AreRaiders()));
        returnMap.put("activeRaiders", raid.getActiveRaiders());
        //THIS MAY OR MAY NOT WORK
        returnMap.put("owner", raid.getOwner().getUniqueId().toString());

        //create a list from the looted chunks
        List<Object> chunkList = new ArrayList<Object>();
        for (Raid.LootBlock b : raid.getLootedChunks().values()) {
            HashMap<String, Object> blockMap = new HashMap<String, Object>();
            //worldcoord
            HashMap<String, Object> wcMap = new HashMap<String, Object>();
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

        returnMap.put("lootedChunks", chunkList);

        return returnMap;
    }

    /**
     * Creates a map of raid maps
     *
     * @param war - War to map
     * @return Map of Maps
     */
    public static HashMap<String, Object> getRaidMap(War war) {
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        for (Raid raid : war.getRaids()) {
            returnMap.put(raid.getRaidedTown().getName(), raidToMap(raid));
        }
        return returnMap;
    }

    /**
     * @return
     * @Isaac this is for getting when the last raid was on a town
     */
    public static long whenTownLastRaided(Town town) {
        if (town.hasMeta("lastRaided")) {
            CustomDataField field = town.getMetadata("lastRaided");
            if (field != null) {
                if (field instanceof IntegerDataField) {
                    return ((IntegerDataField) field).getValue();
                }
            }
            return -1L;
        } else {
            town.addMetaData(new LongDataField("lastRaided", 0L));
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
        if ((System.currentTimeMillis() / 1000) - (side.equals(war.getSide1()) ? war.getLastRaidTimeSide1() : war.getLastRaidTimeSide2()) <= 21600) {
            return 0;
        }

        //make sure a player is online in the raided town
        boolean onlinePlayer = false;
        for(Resident r : town.getResidents()) {
            if(r.isOnline()) {
                onlinePlayer = true;
            }
        }
        if(!onlinePlayer) return -2;

        //check if being raided
        for (Raid raid : war.getRaids()) {
            if (raid.getRaidedTown().getName().equals(town.getName())) {
                return 1;
            }

        }

        return 2;
    }

}
