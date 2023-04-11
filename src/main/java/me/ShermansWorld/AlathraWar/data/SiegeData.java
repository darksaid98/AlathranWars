package me.ShermansWorld.AlathraWar.data;

import java.util.ArrayList;
import java.util.HashMap;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.Siege;

import java.io.File;
import java.io.FilenameFilter;

public class SiegeData
{
    
    // Static Siege list for all active sieges
    private static ArrayList<Siege> sieges = new ArrayList<Siege>();
    private final static String dataFolderPath = "plugins" + File.separator + "AlathraWar" + File.separator + "data";

    // Filter for only accessing yml files
    private static FilenameFilter ymlFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".yml");
        }
    };
    
    public static ArrayList<Siege> getSieges() {
        return sieges;
    }

    /**
     * Gets a siege with a specific name
     * @param name - Name to check
     * @return Siege or Null
     */
    public static Siege getSiege(String name) {
        for (Siege siege : sieges) {
            
        }
        return null;
    }

    public static void setSieges(ArrayList<Siege> sieges) {
        for (Siege siege : sieges) {
            addSiege(siege);
        }
    }

    public static void addSiege(Siege siege) {
        if (siege == null) {
            Main.warLogger.log("Attempted to add NULL to siege list.");
            return;
        }
        sieges.add(siege);
    }

    public static void removeSiege(Siege siege) {
        sieges.remove(siege);
        //deleteSiege(siege);
    }

    /**
     * Creates a siege object from a provided HashMap
     * @param fileData
     * @return Siege object
     */
    @SuppressWarnings("unchecked")
    public static Siege fromMap(War war, HashMap<String, Object> fileData) {

        if (fileData.get("town") == null || fileData.get("side1AreAttackers") == null) {
            return null;
        }

        Town town = TownyAPI.getInstance().getTown((String) fileData.get("town"));
        boolean attackBoolean = Boolean.parseBoolean((String) fileData.get("side1AreAttackers"));

        Siege siege = new Siege(war, town, attackBoolean);

        return siege;
    }

    /**
     * Saves the war into files.

     * @param siege - Siege to be saved.
     */
    public static void saveSiege(Siege siege) {
        War war = siege.getWar();
        war.save();
    }

    private static void deleteSiege(War war) {
        File[] files = new File(dataFolderPath + File.separator + "wars").listFiles(ymlFilter);

        for (File file : files) {
            if (file.getName().startsWith(war.getName())) {
                file.delete();
            }
        }
    }

    /**
     * Turns a siege into a map
     * @param siege - Siege 
     * @return Map
     */
    public static HashMap<String, Object> siegeToMap(Siege siege) {
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        // Shoves everything into a map.
        returnMap.put("town", siege.getTown());
        returnMap.put("siegeTicks", siege.getSiegeTicks());
        returnMap.put("attackerPoints", siege.getAttackerPoints());
        returnMap.put("defenderPoints", siege.getDefenderPoints());
        returnMap.put("side1AreAttackers", Boolean.toString(siege.getSide1AreAttackers()));

        return returnMap;
    }

    /**
     * Creates a map of siege maps
     * @param war - War to map
     * @return Map of Maps
     */
    public static HashMap<String, Object> getSiegeMap(War war) {
        HashMap<String, Object> returnMap = new HashMap<String, Object>();
        for (Siege siege : war.getSieges()) {
            returnMap.put(siege.getTown().getName(), siegeToMap(siege));
        }
        return returnMap;
    }
    
}
