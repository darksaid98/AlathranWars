package me.ShermansWorld.AlathraWar.data;

import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.War;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class TimeoutData {
    // Static War list for all active wars
    private static HashMap<UUID, Integer> timeouts = new HashMap<UUID, Integer>();
    private final static String dataFolderPath = "plugins" + File.separator + "AlathraWar" + File.separator + "data";
    private static File timeoutData = new File(dataFolderPath + File.separator + "timeOutData.yml");

//    // Filter for only accessing yml files
//    private static FilenameFilter ymlFilter = new FilenameFilter() {
//        @Override
//        public boolean accept(File dir, String name) {
//            return name.endsWith(".yml");
//        }
//    };

    // Constructor used to initialise folder.
    public TimeoutData(final Main plugin) {
        if (!timeoutData.exists()) {
            try {
                timeoutData.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static HashMap<UUID, Integer> getWars() {
        return timeouts;
    }

    /**
     * Gets a timeout for the player name, if failed to find return -1
     *
     * @param name - name to check
     * @return timeout or -1
     */
    public static int getTimeout(String name) {
        for (UUID uuid : timeouts.keySet()) {
            if (Bukkit.getPlayer(name).getUniqueId().compareTo(uuid) == 0) return timeouts.get(uuid);
        }
        return -1;
    }

    /**
     * Gets a timeout for the player uuid, if failed to find return -1
     *
     * @param uuidIN - UUID to check
     * @return timeout or -1
     */
    public static int getTimeout(UUID uuidIN) {
        for (UUID uuid : timeouts.keySet()) {
            if (uuidIN.compareTo(uuid) == 0) return timeouts.get(uuid);
        }
        return -1;
    }

    public static void setTimeouts(HashMap<UUID, Integer> timeouts) {
        TimeoutData.timeouts = timeouts;
    }

    /**
     * add player timeout (in seconds)
     * @param player
     * @param time
     */
    public static void addTimeout(Player player, int time) {
        if (player == null) {
            Main.warLogger.log("Attempted to add NULL to timeout list.");
            return;
        }
        timeouts.put(player.getUniqueId(), time);
    }

    public static void removeTimeout(Player player) {
        timeouts.remove(player.getUniqueId());
//        deleteWar(war);
    }

    /**
     * Gets the wars currently saved in files as an ArrayList of War Objects
     *
     * @return War Object ArrayList
     */
    public static HashMap<UUID, Integer> createTimoutMap() {

        HashMap<String, Object> fileData = DataManager.getData(timeoutData);
        HashMap<String, Object> list = (HashMap<String, Object>) fileData.get("timeouts");

        HashMap<UUID, Integer> out = new HashMap<>();
        for (String s : list.keySet()) {
            out.put(UUID.fromString(s), (Integer) list.get(s));
        }

        return out;
    }

    /**
     * Saves the war into files.
     *
     * @param war - War to be saved.
     */
    public static void saveWar(War war) {
        HashMap<String, Object> sHashMap = new HashMap<String, Object>();

        HashMap<String, Object> list = new HashMap<>();

        for(UUID uuid : timeouts.keySet()) {
            list.put(uuid.toString(), timeouts.get(uuid));
        }
        // Shoves everything into a map.
        sHashMap.put("timeouts", list);

        DataManager.saveData("timeOutData.yml", sHashMap);
    }
}