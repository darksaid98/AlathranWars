package me.ShermansWorld.AlathranWars.data;

import me.ShermansWorld.AlathranWars.Main;
import me.ShermansWorld.AlathranWars.deprecated.OldRaid;
import me.ShermansWorld.AlathranWars.deprecated.OldSiege;
import me.ShermansWorld.AlathranWars.deprecated.OldWar;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

@Deprecated
public class WarData {

    private final static String dataFolderPath = "plugins" + File.separator + "AlathranWars" + File.separator + "data";
    // Filter for only accessing yml files
    private static final FilenameFilter ymlFilter = (dir, name) -> name.endsWith(".yml");
    // Static OldWar list for all active oldWars
    private static ArrayList<OldWar> oldWars = new ArrayList<>();

    // Constructor used to initialise folder.
    public WarData(final Main plugin) {
        File userDataFolder = new File(dataFolderPath + File.separator + "oldWars");
        if (!userDataFolder.exists()) {
            userDataFolder.mkdirs();
        }
    }

    public static ArrayList<OldWar> getWars() {
        return oldWars;
    }

    public static void setWars(ArrayList<OldWar> oldWars) {
        WarData.oldWars = oldWars;
    }

    public static Collection<String> getWarsNames() {
        List<String> warNames = new ArrayList<>();

        for (OldWar oldWar : getWars()) {
            warNames.add(oldWar.getName());
        }

        Collections.sort(warNames);

        return warNames;
    }

    /**
     * Gets a war with a specific name
     *
     * @param name - Name to check
     * @return OldWar or Null
     */
    public static OldWar getWar(String name) {
        for (OldWar oldWar : oldWars) {
            if (oldWar.getName().equalsIgnoreCase(name)) return oldWar;
        }
        return null;
    }

    public static OldWar getWar(UUID uuid) {
        for (OldWar oldWar : oldWars) {
            if (oldWar.getUUID().equals(uuid)) return oldWar;
        }
        return null;
    }

    public static void addWar(OldWar oldWar) {
        if (oldWar == null) {
            Main.warLogger.log("Attempted to add NULL to oldWar list.");
            return;
        }
        oldWars.add(oldWar);
    }

    public static void removeWar(OldWar oldWar) {
        oldWars.remove(oldWar);
        deleteWar(oldWar);
    }

    /**
     * Gets the oldWars currently saved in files as an ArrayList of OldWar Objects
     *
     * @return OldWar Object ArrayList
     */
    public static ArrayList<OldWar> createWars() {
        File[] files = new File(dataFolderPath + File.separator + "oldWars").listFiles(ymlFilter);

        ArrayList<OldWar> returnList = new ArrayList<>();
        for (File file : files) {
            try {
                HashMap<String, Object> fileData = DataManager.getData(file);
                OldWar fileOldWar = fromMap(fileData);
                returnList.add(fileOldWar);
            } catch (Exception e) {
                Main.warLogger.log("Failed to load " + file.getName());
                e.printStackTrace();
            }
        }

        return returnList;
    }

    /**
     * Creates a war object from a provided HashMap
     *
     * @param fileData
     * @return OldWar object
     */
    @SuppressWarnings("unchecked")
    public static OldWar fromMap(HashMap<String, Object> fileData) {

        OldWar oldWar = new OldWar((String) fileData.get("name"),
            (String) fileData.get("side1"),
            (String) fileData.get("side2")
        );

        oldWar.setSide1Towns((ArrayList<String>) fileData.get("side1Towns"));
        oldWar.setSide2Towns((ArrayList<String>) fileData.get("side2Towns"));
        oldWar.setSurrenderedTowns((ArrayList<String>) fileData.get("surrenderedTowns"));

        oldWar.addSide1Points((int) fileData.get("side1Points"));
        oldWar.addSide2Points((int) fileData.get("side2Points"));

        if (fileData.get("lastRaidTimeSide1") != null && fileData.get("lastRaidTimeSide2") != null) {
            oldWar.setLastRaidTimeSide1((int) fileData.get("lastRaidTimeSide1"));
            oldWar.setLastRaidTimeSide2((int) fileData.get("lastRaidTimeSide2"));
        }


        // OldSiege adding from map.
        Collection<HashMap<String, Object>> siegeMaps = ((HashMap<String, HashMap<String, Object>>) fileData.get("oldSieges")).values();
        ArrayList<OldSiege> oldSieges = SiegeData.createSieges(oldWar, siegeMaps);
        for (OldSiege oldSiege : oldSieges) {
            oldWar.addSiege(oldSiege);
            SiegeData.addSiege(oldSiege);
        }

        // OldRaid adding from map.
        Collection<HashMap<String, Object>> raidMaps = ((HashMap<String, HashMap<String, Object>>) fileData.get("oldRaids")).values();
        ArrayList<OldRaid> oldRaids = RaidData.createRaids(oldWar, raidMaps);
        for (OldRaid oldRaid : oldRaids) {
            oldWar.addRaid(oldRaid);
        }

        return oldWar;
    }

    /**
     * Saves the oldWar into files.
     *
     * @param oldWar - OldWar to be saved.
     */
    public static void saveWar(OldWar oldWar) {
        HashMap<String, Object> sHashMap = new HashMap<>();

        // Shoves everything into a map.
        sHashMap.put("name", oldWar.getName());
        sHashMap.put("side1", oldWar.getSide1());
        sHashMap.put("side2", oldWar.getSide2());
        sHashMap.put("side1Towns", oldWar.getSide1Towns());
        sHashMap.put("side2Towns", oldWar.getSide2Towns());
        sHashMap.put("surrenderedTowns", oldWar.getSurrenderedTowns());
        sHashMap.put("side1Points", oldWar.getSide1Points());
        sHashMap.put("side2Points", oldWar.getSide2Points());

        sHashMap.put("sieges", SiegeData.getSiegeMap(oldWar));
        sHashMap.put("raids", RaidData.getRaidMap(oldWar));
        sHashMap.put("lastRaidTimeSide1", oldWar.getLastRaidTimeSide1());
        sHashMap.put("lastRaidTimeSide2", oldWar.getLastRaidTimeSide2());

        DataManager.saveData("oldWars" + File.separator + oldWar.getName() + ".yml", sHashMap);
    }

    private static void deleteWar(OldWar oldWar) {
        File[] files = new File(dataFolderPath + File.separator + "oldWars").listFiles(ymlFilter);
        for (File file : files) {
            if (file.getName().startsWith(oldWar.getName())) {
                file.delete();
            }
        }
    }

}
