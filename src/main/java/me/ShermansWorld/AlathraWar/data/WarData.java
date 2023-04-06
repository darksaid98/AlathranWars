package me.ShermansWorld.AlathraWar.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.War;

import java.io.File;
import java.io.FilenameFilter;

public class WarData
{
    private static Main plugin;
    private static String dataFolderPath = "plugins" + File.separator + "AlathraWar" + File.separator + "data";
    FilenameFilter ymlFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".yml");
        }
    };


    public WarData(final Main plugin) {
        File userDataFolder = new File(dataFolderPath + File.separator + "wars");
		if (!userDataFolder.exists()) {
			userDataFolder.mkdirs();
		}

        WarData.plugin = plugin;
    }
    
    /**
     * Gets the wars currently saved in files as an ArrayList of War Objects
     * @return War Object ArrayList
     */
    public static ArrayList<War> createWars() {
        File[] files = new File(dataFolderPath + File.separator + "wars").listFiles();

        ArrayList<War> returnList = new ArrayList<War>();
        for (File file : files) {
            HashMap<String, Object> fileData = DataManager.getData(file);
            War fileWar = fromMap(fileData);
            returnList.add(fileWar);
        }

        return returnList;
    }

    /**
     * Creates a war object from a provided HashMap
     * @param fileData
     * @return War object
     */
    public static War fromMap(HashMap<String, Object> fileData) {

        War war = new War((String) fileData.get("name"),
        (String) fileData.get("side1"),
        (String) fileData.get("side2")
        );
        
        war.setSide1Towns(null);
        war.setSide2Towns(null);


        return null;
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


        DataManager.saveData(dataFolderPath + File.separator + "wars" + File.separator + war.getName() + ".yml", sHashMap);
    }
    
}
