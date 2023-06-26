package me.ShermansWorld.AlathraWar;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.commands.CommandHelper;
import me.ShermansWorld.AlathraWar.data.DataManager;
import me.ShermansWorld.AlathraWar.data.WarData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class War {

    // References
    private final ArrayList<Siege> sieges = new ArrayList<>();
    private final ArrayList<Raid> raids = new ArrayList<>();
    // Object Fields
    private final UUID uuid; // TODO Actually convert to using UUID for identifying this war
    private String name;
    private String side1;
    private String side2;
    private ArrayList<String> side1Towns = new ArrayList<>();
    private ArrayList<String> side2Towns = new ArrayList<>();
    private ArrayList<String> surrenderedTowns = new ArrayList<>();
    private int side1Points = 0;
    private int side2Points = 0;
    private int lastRaidTimeSide1 = 0;
    private int lastRaidTimeSide2 = 0;

    /**
     * War Constructor
     *
     * @param name  - Name of war
     * @param side1 - Side 1 name
     * @param side2 - Side 2 name
     */
    public War(final String name, final String side1, final String side2) {
        this.uuid = generateUUID();
        this.name = name;
        this.side1 = side1;
        this.side2 = side2;
    }

    private static ArrayList<String> townListToPlayers(ArrayList<String> townList) {
        ArrayList<String> returnList = new ArrayList<>();
        for (String townString : townList) {
            Town town = TownyAPI.getInstance().getTown(townString);
            if (town != null) {
                for (Resident res : town.getResidents()) {
                    //Minuteman countermeasure! if they are they get skipped
                    if (CommandHelper.isPlayerMinuteman(res.getName()) == 0) {
                        returnList.add(res.getName());
                    }
                }
            }
        }
        return returnList;
    }

    private UUID generateUUID() {
        UUID uuid = UUID.randomUUID();

        while (WarData.getWar(uuid) != null) {
            uuid = UUID.randomUUID();
        }

        return UUID.randomUUID();
    }

    /**
     * Add a nation to a side of the war
     *
     * @param nation - Nation object
     * @param side   - Side name
     */
    public void addNation(final Nation nation, String side) {
        for (Town town : nation.getTowns()) {
            addTown(town.getName(), side);
        }
    }

    /**
     * Add a town to a side of the war
     *
     * @param town - Town object
     * @param side - Side name
     */
    public void addTown(final Town town, String side) {
        addTown(town.getName(), side);
    }

    /**
     * Add a town to a side of the war
     *
     * @param town - Town object
     * @param side - Side name
     */
    public void addTown(final String town, String side) {
        if (side1.equalsIgnoreCase(side)) {
            if (!side1Towns.contains(town.toLowerCase())) {
                side1Towns.add(town.toLowerCase());
                Main.warLogger.log("Town " + town + " joined " + this.name + " on " + side);
                return;
            }
        } else if (side2.equalsIgnoreCase(side)) {
            if (!side2Towns.contains(town.toLowerCase())) {
                side2Towns.add(town.toLowerCase());
                Main.warLogger.log("Town " + town + " joined " + this.name + " on " + side);
                return;
            }
        }
        Main.warLogger.log("Town " + town + " failed to join " + this.name + " on " + side);
    }

    /**
     * Surrenders town
     *
     * @param nation - Nation to surrender
     */
    public void surrenderNation(Nation nation) {
        if (nation != null) {
            for (Town town : nation.getTowns()) {
                surrenderTown(town.getName());
            }
        }
    }

    /**
     * Surrenders town
     *
     * @param town - Town to surrender
     */
    public void surrenderTown(String town) {
        side1Towns.remove(town);
        side2Towns.remove(town);
        surrenderedTowns.add(town);
    }

    /**
     * unsurrender town, perfidy time
     *
     * @param nation - Nation to unsurrender
     */
    public void unSurrenderNation(Nation nation) {
        for (Town town : nation.getTowns()) {
            unSurrenderTown(town.getName());
        }
    }

    /**
     * unsurrender town, perfidy time
     *
     * @param town - Town to unsurrender
     */
    public void unSurrenderTown(String town) {
        surrenderedTowns.remove(town);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Gets side of town
     * -1 - Surrendered
     * 0 - None
     * 1 - Side 1
     * 2 - Side 2
     *
     * @param string town
     * @return result
     */
    public int getSide(String string) {
        for (String str : surrenderedTowns) {
            if (str.equalsIgnoreCase(string)) return -1;
        }

        for (String str : side1Towns) {
            if (str.equalsIgnoreCase(string)) return 1;
        }

        for (String str : side2Towns) {
            if (str.equalsIgnoreCase(string)) return 2;
        }
        return 0;
    }

    public int getSide(Town town) {
        return getSide(town.getName());
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        DataManager.deleteFile("wars" + File.separator + this.getName() + ".yml");
        this.name = name;
        this.save();
    }

    public List<String> getSides() {
        return List.of(getSide1(), getSide2());
    }

    public String getSide1() {
        return this.side1;
    }

    public void setSide1(String side1) {
        this.side1 = side1;
    }

    public String getSide2() {
        return this.side2;
    }

    public void setSide2(String side2) {
        this.side2 = side2;
    }

    public ArrayList<String> getSide1Towns() {
        return this.side1Towns;
    }

    public void setSide1Towns(final ArrayList<String> side1Towns) {
        this.side1Towns = side1Towns;
    }

    public ArrayList<String> getSide2Towns() {
        return this.side2Towns;
    }

    public void setSide2Towns(final ArrayList<String> side2Towns) {
        this.side2Towns = side2Towns;
    }

    public ArrayList<String> getSurrenderedTowns() {
        return surrenderedTowns;
    }

    public void setSurrenderedTowns(ArrayList<String> towns) {
        surrenderedTowns = towns;
    }

    public ArrayList<Siege> getSieges() {
        return sieges;
    }

    public void addSiege(Siege siege) {
        sieges.add(siege);
    }

    public ArrayList<Raid> getRaids() {
        return raids;
    }

    public void addRaid(Raid raid) {
        raids.add(raid);
    }

    /**
     * Saves the war into .yml file folders.
     */
    public void save() {
        WarData.saveWar(this);
    }

    public String toString() {
        return name + "[" + side1 + "." + side2 + "]("
                + side1Towns.size() + "/" + side2Towns.size() + "/" +
                surrenderedTowns.size() + ")";
    }

    public ArrayList<String> getSide1Players() {
        return townListToPlayers(side1Towns);
    }

    public ArrayList<String> getSide2Players() {
        return townListToPlayers(side2Towns);
    }

    public int getLastRaidTimeSide1() {
        return this.lastRaidTimeSide1;
    }

    public void setLastRaidTimeSide1(int lastRaidTime) {
        this.lastRaidTimeSide1 = lastRaidTime;
    }

    public int getLastRaidTimeSide2() {
        return this.lastRaidTimeSide2;
    }

    public void setLastRaidTimeSide2(int lastRaidTime) {
        this.lastRaidTimeSide2 = lastRaidTime;
    }

    public int getSide1Points() {
        return this.side1Points;
    }

    public void setSide1Points(int side1Points) {
        this.side1Points = side1Points;
    }

    public int getSide2Points() {
        return this.side2Points;
    }

    public void setSide2Points(int side2Points) {
        this.side2Points = side2Points;
    }

    public void addSide1Points(int points) {
        side1Points += points;
    }

    public void addSide2Points(int points) {
        side2Points += points;
    }

    public boolean equals(War war) {
        return getUUID().equals(war.getUUID());
    }

    public boolean equals(UUID uuid) {
        return getUUID().equals(uuid);
    }
}
