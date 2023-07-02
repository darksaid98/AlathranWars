package me.ShermansWorld.AlathraWar.data;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.deprecated.OldSiege;
import me.ShermansWorld.AlathraWar.deprecated.OldWar;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

@Deprecated
public class SiegeData {

    // Static OldSiege list for all active OLD_SIEGES
    private static final HashSet<OldSiege> OLD_SIEGES = new HashSet<>();

    public static HashSet<OldSiege> getSieges() {
        return OLD_SIEGES;
    }

    public static void setSieges(ArrayList<OldSiege> oldSieges) {
        for (OldSiege oldSiege : oldSieges) {
            addSiege(oldSiege);
        }
    }

    /**
     * Gets a siege with a specific name
     *
     * @param name - Name to check
     * @return OldSiege or Null
     */
    public static OldSiege getSiege(String name) {
        for (OldSiege oldSiege : OLD_SIEGES) {
            if (oldSiege.getTown().getName().equalsIgnoreCase(name)) return oldSiege;
        }
        return null;
    }

    public static void addSiege(OldSiege oldSiege) {
        if (oldSiege == null) {
            Main.warLogger.log("Attempted to add NULL to oldSiege list.");
            return;
        }
        OLD_SIEGES.add(oldSiege);
    }

    public static void removeSiege(OldSiege oldSiege) {
        OLD_SIEGES.remove(oldSiege);
        //deleteSiege(oldSiege);d
    }

    /**
     * Creates a siege object from a provided HashMap
     *
     * @param fileData data
     * @return OldSiege object
     */
    private static OldSiege fromMap(OldWar oldWar, HashMap<String, Object> fileData) {

        if (fileData.get("town") == null || fileData.get("side1AreAttackers") == null) {
            return null;
        }

        Town town = TownyAPI.getInstance().getTown((String) fileData.get("town"));
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString((String) fileData.get("siegeLeader")));

        return new OldSiege(oldWar, town, offlinePlayer);
    }

    /**
     * Saves the war into files.
     *
     * @param oldSiege - OldSiege to be saved.
     */
    public static void saveSiege(OldSiege oldSiege) {
        OldWar oldWar = oldSiege.getWar();
        oldWar.save();
    }

    /**
     * Turns a oldSiege into a map
     *
     * @param oldSiege - OldSiege
     * @return Map
     */
    public static HashMap<String, Object> siegeToMap(OldSiege oldSiege) {
        HashMap<String, Object> returnMap = new HashMap<>();
        // Shoves everything into a map.
        returnMap.put("town", oldSiege.getTown().getName());
        returnMap.put("siegeTicks", oldSiege.getSiegeTicks());
        returnMap.put("attackerPoints", oldSiege.getAttackerPoints());
        returnMap.put("defenderPoints", oldSiege.getDefenderPoints());
        returnMap.put("side1AreAttackers", Boolean.toString(oldSiege.getSide1AreAttackers()));
        returnMap.put("siegeLeader", oldSiege.getSiegeLeader().getUniqueId().toString());

        return returnMap;
    }

    /**
     * Creates a map of siege maps
     *
     * @param oldWar - OldWar to map
     * @return Map of Maps
     */
    public static HashMap<String, Object> getSiegeMap(OldWar oldWar) {
        HashMap<String, Object> returnMap = new HashMap<>();
        for (OldSiege oldSiege : oldWar.getSieges()) {
            returnMap.put(oldSiege.getTown().getName(), siegeToMap(oldSiege));
        }
        return returnMap;
    }

    public static ArrayList<OldSiege> createSieges(OldWar oldWar, Collection<HashMap<String, Object>> siegeMaps) {
        ArrayList<OldSiege> returnList = new ArrayList<>();
        for (HashMap<String, Object> map : siegeMaps) {
            Town town = TownyAPI.getInstance().getTown((String) map.get("town"));
            if (town == null) continue;

            OldSiege oldSiege = fromMap(oldWar, map);
            oldSiege.addPointsToAttackers((int) map.get("attackerPoints"));
            oldSiege.addPointsToDefenders((int) map.get("defenderPoints"));
            oldSiege.resume((int) map.get("siegeTicks"));
            oldSiege.setWar(oldWar);
            returnList.add(oldSiege);

        }
        return returnList;
    }

}
