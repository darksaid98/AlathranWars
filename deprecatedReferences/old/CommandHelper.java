package me.ShermansWorld.AlathranWars.deprecated;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathranWars.*;
import me.ShermansWorld.AlathranWars.data.RaidData;
import me.ShermansWorld.AlathranWars.data.RaidPhase;
import me.ShermansWorld.AlathranWars.data.SiegeData;
import me.ShermansWorld.AlathranWars.data.WarData;
import me.ShermansWorld.AlathranWars.deprecated.OldRaid;
import me.ShermansWorld.AlathranWars.deprecated.OldSiege;
import me.ShermansWorld.AlathranWars.deprecated.OldWar;
import me.ShermansWorld.AlathranWars.items.WarItemRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Deprecated
public class CommandHelper {

    /**
     * Grabs list of all war names
     *
     * @return list of wars
     */
    public static List<String> getWarNames() {
        List<String> out = new ArrayList<>();
        for (OldWar w : WarData.getWars()) {
            out.add(w.getName());
        }
        return out;
    }

    /**
     * Grabs list of all sieges
     *
     * @return list of wars
     */
    public static List<String> getSieges() {
        List<String> out = new ArrayList<>();
        for (OldSiege s : SiegeData.getSieges()) {
            out.add(s.getName());
        }
        return out;
    }

    /**
     * Grabs list of all raids
     *
     * @return list of wars
     */
    public static List<String> getRaids() {
        List<String> out = new ArrayList<>();
        for (OldRaid r : RaidData.getRaids()) {
            out.add(r.getName());
        }
        return out;
    }

    /**
     * Grabs first matching war and lists the sides
     *
     * @param name name of war
     * @return side of war
     */
    public static List<String> getWarSides(String name) {
        List<String> out = new ArrayList<>();
        for (OldWar w : WarData.getWars()) {
            if (w.getName().equalsIgnoreCase(name)) {
                out.add(w.getSide1());
                out.add(w.getSide2());
                break;
            }
        }
        return out;
    }

    /**
     * getInstance list of all towny towns
     *
     * @return list of all towns
     */
    public static List<String> getTownyTowns() {
        List<String> out = new ArrayList<>();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            out.add(t.getName());
        }
        return out;
    }

    /**
     * getInstance list of all towny towns in a war
     *
     * @return list of all towns in a war
     */
    public static List<String> getTownyWarTowns(String war) {
        List<String> out = new ArrayList<>();
        OldWar w = WarData.getWar(war);
        if (w == null) return Collections.emptyList();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            if (w.getSide2Towns().contains(t.getName().toLowerCase()) || w.getSide1Towns().contains(t.getName().toLowerCase())) {
                out.add(t.getName());
            }
        }
        return out;
    }

    /**
     * getInstance list of all towny towns in a war
     *
     * @return list of all nations in a war
     */
    public static List<String> getTownyWarNations(String war) {
        List<String> out = new ArrayList<>();
        OldWar w = WarData.getWar(war);
        if (w == null) return Collections.emptyList();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            if (w.getSide2Towns().contains(t.getName().toLowerCase()) || w.getSide1Towns().contains(t.getName().toLowerCase())) {
                if (t.hasNation()) {
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
     * getInstance list of all towny towns on a side in a war
     *
     * @return list of all towns in a side of a war
     */
    public static List<String> getTownyWarTowns(String war, String side) {
        List<String> out = new ArrayList<>();
        OldWar w = WarData.getWar(war);
        if (w == null) return Collections.emptyList();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            if (side.equalsIgnoreCase(w.getSide1())) {
                if (w.getSide1Towns().contains(t.getName().toLowerCase())) {
                    out.add(t.getName());
                }
            } else if (side.equalsIgnoreCase(w.getSide2())) {
                if (w.getSide2Towns().contains(t.getName().toLowerCase())) {
                    out.add(t.getName());
                }
            }
        }
        return out;
    }

    /**
     * return list of all towns in a raid
     *
     * @return list of all towns involved in a raid
     */
    public static List<String> getRaidTowns() {
        List<String> out = new ArrayList<>();
        for (OldRaid r : RaidData.getRaids()) {
            out.add(r.getRaidedTown().getName());
        }
        return out;
    }

    /**
     * return list of all towns in a siege
     *
     * @return list of towns involved in a siege
     */
    public static List<String> getSiegeTowns() {
        List<String> out = new ArrayList<>();
        for (OldSiege r : SiegeData.getSieges()) {
            out.add(r.getTown().getName());
        }
        return out;
    }

    /**
     * getInstance list of all towny nations
     *
     * @return list of all nations
     */
    public static List<String> getTownyNations() {
        List<String> out = new ArrayList<>();
        for (Town t : TownyAPI.getInstance().getTowns()) {
            Nation n = t.getNationOrNull();
            if (n != null) {
                if (!out.contains(n.getName())) {
                    out.add(n.getName());
                }
            }
        }
        return out;
    }

    /**
     * getInstance a list of all online players
     *
     * @return list of all players online
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
     * getInstance a list of all raid phases
     *
     * @return list of all raid phases
     */
    public static List<String> getRaidPhases() {
        List<String> out = new ArrayList<>();
        for (RaidPhase ph : RaidPhase.values()) {
            out.add(ph.name());
        }
        return out;
    }

    /**
     * @param player player to check
     * @return 0, 1, 2 (good to go, insufficient join, insufficient playtime)
     */
    public static int isPlayerMinuteman(String player) {
        Resident res = TownyAPI.getInstance().getResident(player);
        if (res != null) {
            //getInstance registration time
            long regTime = res.getRegistered();
            int playTicks = Bukkit.getPlayer(res.getUUID()).getStatistic(Statistic.PLAY_ONE_MINUTE);
            int out = 0;
            if (Main.getInstance().getConfig().getInt("minimumPlayerAge") > 0) {
                //compare the join date, if they joined less that min age ago it is true
                if ((System.currentTimeMillis() - regTime) < 86400000L * Main.getInstance().getConfig().getInt("minimumPlayerAge"))
                    out = 1;
            }
            if (Main.getInstance().getConfig().getInt("minimumPlayTime") > 0) {
                //check playtime, if its less than min its true
                if (playTicks < (Main.getInstance().getConfig().getInt("minimumPlayTime") * (60 * 60 * 20))) out = 2;
            }

            return out;
        }
        //if the player doesnt exist just return true
        return 0;
    }

    /**
     * log the command
     *
     * @param sender sender
     * @param label  command
     * @param args   command args
     * @return success
     */
    protected static void logCommand(CommandSender sender, String label, String[] args) {
        StringBuilder log = new StringBuilder(label);
        for (String s : args) {
            log.append(" ").append(s);
        }
        Main.warLogger.log(UtilsChat.getPrefix() + sender.getName() + " ran command: " + log);
    }

    /**
     * return list of all alathranwars items with namespace
     *
     * @return list of all alathranwars items namespaced
     */
    public static List<String> getWarItems() {
        return new ArrayList<>(WarItemRegistry.getInstance().getItemRegistry().keySet());
    }
}
