package me.ShermansWorld.AlathraWar.data;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.War;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

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
     * @param name - Name to check
     * @return Raid or Null
     */
    public static Raid getRaidOrNull(String name) {
        for (Raid raid : raids) {
            if(raid.getName().equals(name)) return raid;
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
        raids.remove(raid);
        //deleteRaid(raid);
    }

    /**
     * Creates a raid object from a provided HashMap
     * @param fileData
     * @return Raid object
     */
    @SuppressWarnings("unchecked")
    public static Raid fromMap(War war, HashMap<String, Object> fileData) {

        if (fileData.get("raidedTown") == null || fileData.get("gatherTown") == null  || fileData.get("side1AreRaiders") == null) {
            return null;
        }

        Town raidedTown = TownyAPI.getInstance().getTown((String) fileData.get("raidedTown"));
        Town gatherTown = TownyAPI.getInstance().getTown((String) fileData.get("gatherTown"));
        boolean attackBoolean = Boolean.parseBoolean((String) fileData.get("side1AreRaiders"));

        Raid raid = new Raid(war, raidedTown, gatherTown, attackBoolean);

        if(fileData.get("raidTicks") != null && fileData.get("raidPhase") != null && fileData.get("activeRaiders") != null) {
            raid.setRaidPhase(RaidPhase.getByName((String) fileData.get("raidPhase")));
            raid.setRaidTicks((Integer) fileData.get("raidTicks"));
            raid.setActiveRaiders((ArrayList<String>) fileData.get("activeRaiders"));
        }

        return raid;
    }

    /**
     * Saves the war into files.
     * @param raid - War to be saved.
     */
    public static void saveRaid(Raid raid) {
        War war = raid.getWar();
        war.save();
    }

    //TODO Delete raid
    private static void deleteRaid(War war) {
        File[] files = new File(dataFolderPath + File.separator + "wars").listFiles(ymlFilter);

        for (File file : files) {
            if (file.getName().startsWith(war.getName())) {
                file.delete();
            }
        }
    }

    /**
     * Turns a raid into a map
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

        return returnMap;
    }

    /**
     * Creates a map of raid maps
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

    //TODO raid time check
    /**
     * @Isaac this is for getting when the last raid was on a town
     *
     * @return
     */
    public static int whenTownLastRaided() {
        return 0;
    }

    //TODO raid validity check
    /**
     * @Isaac this is for getting if a town can be raided, return (-1, 0, 1, 2) based on status
     * (24 hours town cooldown, 6 hour nation cooldown, valid time to raid, town being raided already)
     *
     * @return
     */
    public static int isValidRaid(War war, Town town) {
        return 0;
    }

}
