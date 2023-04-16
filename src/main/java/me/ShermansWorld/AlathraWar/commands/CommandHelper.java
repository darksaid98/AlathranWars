package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.WarData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandHelper {

    /**
     * Grabs list of all war names
     * @return
     */
    protected static List<String> getWarNames() {
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
    protected static List<String> getWarSides(String name) {
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
    protected static List<String> getTownyTowns() {
        List<String> out = new ArrayList<>();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            out.add(t.getName());
        }
        return out;
    }

    /**
     * get list of all towny nations
     *
     * @return
     */
    protected static List<String> getTownyNations() {
        List<String> out = new ArrayList<>();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            try {
                Nation n = t.getNation();
                if(n != null) {
                    if(!out.contains(n.getName())) {
                        out.add(n.getName());
                    }
                }
            } catch (NotRegisteredException e) {
                throw new RuntimeException(e);
            }
        }
        return out;
    }

    protected static List<String> getPlayers() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        List<String> out = new ArrayList<>();
        for (Player p : players) {
            out.add(p.getName());
        }
        return out;
    }

}
