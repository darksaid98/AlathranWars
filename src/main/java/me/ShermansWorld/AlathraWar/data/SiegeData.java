package me.ShermansWorld.AlathraWar.data;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.War;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

public class SiegeData {

    // Static Siege list for all active sieges
    private static final HashSet<Siege> sieges = new HashSet<>();

    public static HashSet<Siege> getSieges() {
        return sieges;
    }

    public static void setSieges(ArrayList<Siege> sieges) {
        for (Siege siege : sieges) {
            addSiege(siege);
        }
    }

    /**
     * Gets a siege with a specific name
     *
     * @param name - Name to check
     * @return Siege or Null
     */
    public static Siege getSiege(String name) {
        for (Siege siege : sieges) {
            if (siege.getTown().getName().equalsIgnoreCase(name)) return siege;
        }
        return null;
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
        //deleteSiege(siege);d
    }

    /**
     * Creates a siege object from a provided HashMap
     *
     * @param fileData data
     * @return Siege object
     */
    private static Siege fromMap(War war, HashMap<String, Object> fileData) {

        if (fileData.get("town") == null || fileData.get("side1AreAttackers") == null) {
            return null;
        }

        Town town = TownyAPI.getInstance().getTown((String) fileData.get("town"));
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString((String) fileData.get("siegeLeader")));

        return new Siege(war, town, offlinePlayer);
    }

    /**
     * Saves the war into files.
     *
     * @param siege - Siege to be saved.
     */
    public static void saveSiege(Siege siege) {
        War war = siege.getWar();
        war.save();
    }

    /**
     * Turns a siege into a map
     *
     * @param siege - Siege
     * @return Map
     */
    public static HashMap<String, Object> siegeToMap(Siege siege) {
        HashMap<String, Object> returnMap = new HashMap<>();
        // Shoves everything into a map.
        returnMap.put("town", siege.getTown().getName());
        returnMap.put("siegeTicks", siege.getSiegeTicks());
        returnMap.put("attackerPoints", siege.getAttackerPoints());
        returnMap.put("defenderPoints", siege.getDefenderPoints());
        returnMap.put("side1AreAttackers", Boolean.toString(siege.getSide1AreAttackers()));
        returnMap.put("siegeLeader", siege.getSiegeLeader().getUniqueId().toString());

        return returnMap;
    }

    /**
     * Creates a map of siege maps
     *
     * @param war - War to map
     * @return Map of Maps
     */
    public static HashMap<String, Object> getSiegeMap(War war) {
        HashMap<String, Object> returnMap = new HashMap<>();
        for (Siege siege : war.getSieges()) {
            returnMap.put(siege.getTown().getName(), siegeToMap(siege));
        }
        return returnMap;
    }

    public static ArrayList<Siege> createSieges(War war, Collection<HashMap<String, Object>> siegeMaps) {
        ArrayList<Siege> returnList = new ArrayList<>();
        for (HashMap<String, Object> map : siegeMaps) {
            Town town = TownyAPI.getInstance().getTown((String) map.get("town"));
            if (town == null) continue;

            Siege siege = fromMap(war, map);
            siege.addPointsToAttackers((int) map.get("attackerPoints"));
            siege.addPointsToDefenders((int) map.get("defenderPoints"));
            siege.resume((int) map.get("siegeTicks"));
            siege.setWar(war);
            returnList.add(siege);

        }
        return returnList;
    }

}
