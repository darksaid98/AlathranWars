package me.ShermansWorld.AlathraWar.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.War;

import java.io.File;
import java.io.FilenameFilter;

public class WarData
{

    // Static War list for all active wars
    private static ArrayList<War> wars = new ArrayList<War>();
    private final static String dataFolderPath = "plugins" + File.separator + "AlathraWar" + File.separator + "data";

    // Filter for only accessing yml files
    private static FilenameFilter ymlFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".yml");
        }
    };

    // Constructor used to initialise folder.
    public WarData(final Main plugin) {
        File userDataFolder = new File(dataFolderPath + File.separator + "wars");
		if (!userDataFolder.exists()) {
			userDataFolder.mkdirs();
		}
    }
    
    public static ArrayList<War> getWars() {
        return wars;
    }

    /**
     * Gets a war with a specific name
     * @param name - Name to check
     * @return War or Null
     */
    public static War getWar(String name) {
        for (War war : wars) {
            if (war.getName().equalsIgnoreCase(name)) return war;
        }
        return null;
    }

    public static void setWars(ArrayList<War> wars) {
        WarData.wars = wars;
    }

    public static void addWar(War war) {
        if (war == null) {
            Main.warLogger.log("Attempted to add NULL to war list.");
            return;
        }
        wars.add(war);
    }

    public static void removeWar(War war) {
        wars.remove(war);
        deleteWar(war);
    }

    /**
     * Gets the wars currently saved in files as an ArrayList of War Objects
     * @return War Object ArrayList
     */
    public static ArrayList<War> createWars() {
        File[] files = new File(dataFolderPath + File.separator + "wars").listFiles(ymlFilter);

        ArrayList<War> returnList = new ArrayList<War>();
        for (File file : files) {
            try {
                HashMap<String, Object> fileData = DataManager.getData(file);
                War fileWar = fromMap(fileData);
                returnList.add(fileWar); 
            } catch (Exception e) {
                Main.warLogger.log("Failed to load " + file.getName() + " " + e.getMessage());
            }
        }

        return returnList;
    }

    /**
     * Creates a war object from a provided HashMap
     * @param fileData
     * @return War object
     */
    @SuppressWarnings("unchecked")
    public static War fromMap(HashMap<String, Object> fileData) {

        War war = new War((String) fileData.get("name"),
        (String) fileData.get("side1"),
        (String) fileData.get("side2")
        );
        
        war.setSide1Towns((ArrayList<String>) fileData.get("side1Towns"));
        war.setSide2Towns((ArrayList<String>) fileData.get("side2Towns"));

        war.setLastRaidTime((long) ((int) fileData.get("lastRaidTime")));


        // Siege adding from map.
        Collection<HashMap<String, Object>> siegeMaps = ((HashMap<String,HashMap<String, Object>>) fileData.get("sieges")).values();
        ArrayList<Siege> sieges = SiegeData.createSieges(war, siegeMaps);
        for (Siege siege : sieges) {
            war.addSiege(siege);
        }

        return war;
    }

    /**
     * Saves the war into files.
     * @param war - War to be saved.
     */
    public static void saveWar(War war) {
        HashMap<String, Object> sHashMap = new HashMap<String,Object>();

        // Shoves everything into a map.
        sHashMap.put("name", war.getName());
        sHashMap.put("side1", war.getSide1());
        sHashMap.put("side2", war.getSide2());
        sHashMap.put("side1Towns", (List<String>) war.getSide1Towns());
        sHashMap.put("side2Towns", (List<String>) war.getSide2Towns());
        sHashMap.put("surrenderedTowns", (List<String>) war.getSurrenderedTowns());

        sHashMap.put("sieges", SiegeData.getSiegeMap(war));
        sHashMap.put("raids", RaidData.getRaidMap(war));
        sHashMap.put("lastRaidTime", war.getLastRaidTime());


        DataManager.saveData("wars" + File.separator + war.getName() + ".yml", sHashMap);
    }

    private static void deleteWar(War war) {
        File[] files = new File(dataFolderPath + File.separator + "wars").listFiles(ymlFilter);

        for (File file : files) {
            if (file.getName().startsWith(war.getName())) {
                file.delete();
            }
        }
    }
    
}
