package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CommandHelper {

    /**
     * Grabs list of all war names
     * @return
     */
    public static List<String> getWarNames() {
        List<String> out = new ArrayList<>();
        for (War w : WarData.getWars()) {
            out.add(w.getName());
        }
        return out;
    }

    /**
     * Grabs first matching war and lists the sides
     * @param name
     * @return
     */
    public static List<String> getWarSides(String name) {
        List<String> out = new ArrayList<>();
        for (War w : WarData.getWars()) {
            if(w.getName().equals(name)) {
                out.add(w.getSide1());
                out.add(w.getSide2());
                break;
            }
        }
        return out;
    }

    /**
     * get list of all towny towns
     *
     * @return
     */
    public static List<String> getTownyTowns() {
        List<String> out = new ArrayList<>();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            out.add(t.getName());
        }
        return out;
    }

    /**
     * get list of all towny towns in a war
     *
     * @return
     */
    public static List<String> getTownyWarTowns(String war) {
        List<String> out = new ArrayList<>();
        War w = WarData.getWar(war);
        if(w == null) return Collections.emptyList();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            if(w.getSide2Towns().contains(t.getName()) || w.getSide1Towns().contains(t.getName())) {
                out.add(t.getName());
            }
        }
        return out;
    }

    /**
     * get list of all towny towns in a war
     *
     * @return
     */
    public static List<String> getTownyWarNations(String war) {
        List<String> out = new ArrayList<>();
        War w = WarData.getWar(war);
        if(w == null) return Collections.emptyList();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            if(w.getSide2Towns().contains(t.getName()) || w.getSide1Towns().contains(t.getName())) {
                if(t.hasNation()) {
                    try {
                        out.add(t.getNation().getName());
                    } catch (NotRegisteredException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return out;
    }

    /**
     * get list of all towny towns on a side in a war
     *
     * @return
     */
    public static List<String> getTownyWarTowns(String war, String side) {
        List<String> out = new ArrayList<>();
        War w = WarData.getWar(war);
        if(w == null) return Collections.emptyList();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            if(side.equals(w.getSide1())) {
                if(w.getSide1Towns().contains(t.getName())) {
                    out.add(t.getName());
                }
            } else if(side.equals(w.getSide2())) {
                if(w.getSide2Towns().contains(t.getName())) {
                    out.add(t.getName());
                }
            }
        }
        return out;
    }

    /**
     * return list of all towns in a raid
     * @return
     */
    public static List<String> getRaidTowns() {
        List<String> out = new ArrayList<>();
        for (Raid r : RaidData.getRaids()) {
            out.add(r.getRaidedTown().getName());
        }
        return out;
    }

    /**
     * return list of all towns in a raid
     * @return
     */
    public static List<String> getSiegeTowns() {
        List<String> out = new ArrayList<>();
        for (Siege r : SiegeData.getSieges()) {
            out.add(r.getTown().getName());
        }
        return out;
    }

    /**
     * get list of all towny nations
     *
     * @return
     */
    public static List<String> getTownyNations() {
        List<String> out = new ArrayList<>();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            Nation n = t.getNationOrNull();
            if(n != null) {
                if(!out.contains(n.getName())) {
                    out.add(n.getName());
                }
            }
        }
        return out;
    }

    /**
     * get a list of all online players
     * @return
     */
    public static List<String> getPlayers() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        List<String> out = new ArrayList<>();
        for (Player p : players) {
            out.add(p.getName());
        }
        return out;
    }

    /**
     * get a list of all raid phases
     * @return
     */
    public static List<String> getRaidPhases() {
        List<String> out = new ArrayList<>();
        for (RaidPhase ph : RaidPhase.values()) {
            out.add(ph.name());
        }
        return out;
    }
}
