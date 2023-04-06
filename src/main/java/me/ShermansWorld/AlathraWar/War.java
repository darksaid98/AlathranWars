package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import me.ShermansWorld.AlathraWar.data.WarData;

public class War {

    // Object Fields
	private String name;
    private String side1;
    private String side2;
	private ArrayList<String> side1Towns = new ArrayList<String>();
	private ArrayList<String> side2Towns = new ArrayList<String>();
    private ArrayList<String> surrenderedTowns = new ArrayList<String>();

    /**
     * War Constructor
     * @param name - Name of war
     * @param side1 - Side 1 name
     * @param side2 - Side 2 name
     */
	public War(final String name, final String side1, final String side2) {
		this.name = name;
		this.side1 = side1;
		this.side2 = side2;
	}

    /**
     * Add a nation to a side of the war
     * @param nation - Nation object
     * @param side - Side name
     */
    public void addNation(final Nation nation, String side) {
        for (Town town : nation.getTowns()) {
            addTown(town.getName(), side);
        }
    }

    /**
     * Add a town to a side of the war
     * @param town - Town object
     * @param side - Side name
     */
    public void addTown(final Town town, String side) {
        addTown(town.getName(), side);
    }

    /**
     * Add a town to a side of the war
     * @param town - Town object
     * @param side - Side name
     */
	public void addTown(final String town, String side) {
        if (side1.equalsIgnoreCase(side)) {
            if (!side1Towns.contains(town)) {
                side1Towns.add(town);
                Main.warLogger.log("Town "+ town + " joined " + this.name + " on " + side);
            }
        } else if(side2.equalsIgnoreCase(side)) {
            if (!side2Towns.contains(town)) {
                side2Towns.add(town);
                Main.warLogger.log("Town "+ town + " joined " + this.name + " on " + side);
            }
        }
        Main.warLogger.log("Town "+ town + " failed to join " + this.name + " on " + side);
	}

    /**
     * Surrenders town
     * @param town - Town to surrender
     */
    public void surrenderTown(String town) {
        side1Towns.remove(town);
        side2Towns.remove(town);
        surrenderedTowns.add(town);
    }
	
	public String getName() {
		return this.name;
	}

	public String getSide1() {
		return this.side1;
	}

	public String getSide2() {
		return this.side2;
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

}
