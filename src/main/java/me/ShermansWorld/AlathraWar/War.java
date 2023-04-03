package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;

public class War {
	private String name;
	private final int id;
	private String side1;
	private String side2;
	private ArrayList<String> side1Players;
	private ArrayList<String> side2Players;
	private ArrayList<String> side1Mercs;
	private ArrayList<String> side2Mercs;

	public War(final String name, final int id, final String side1, final String side2) {
		side1Players = new ArrayList<String>();
		side2Players = new ArrayList<String>();
		side1Mercs = new ArrayList<String>();
		side2Mercs = new ArrayList<String>();
		this.name = name;
		this.id = id;
		this.side1 = side1;
		this.side2 = side2;
		side1Players.add("Placeholder1");
		side2Players.add("Placeholder2");
		side1Mercs.add("Placeholder1");
		side2Mercs.add("Placeholder2");
	}

	public void addPlayerSide1(final String playerName) {
		boolean duplicate = false;
		if (!side1Players.isEmpty()) {
			for (int i = 0; i < this.side1Players.size(); ++i) {
				if (playerName.equalsIgnoreCase(this.side1Players.get(i))) {
					duplicate = true;
				}
			}
		}
		if (!duplicate) {
			this.side1Players.add(playerName);
		}
	}

	public void removePlayerSide1(final String playerName) {
		if (!side1Players.isEmpty()) {
			for (int i = 0; i < this.side1Players.size(); ++i) {
				if (playerName.equalsIgnoreCase(this.side1Players.get(i))) {
					this.side1Players.remove(i);
					return;
				}
			}
		}
	}

	public void addPlayerSide2(final String playerName) {
		boolean duplicate = false;
		if (!side2Players.isEmpty()) {
			for (int i = 0; i < this.side2Players.size(); ++i) {
				if (playerName.equalsIgnoreCase(this.side2Players.get(i))) {
					duplicate = true;
				}
			}
		}
		if (!duplicate) {
			this.side2Players.add(playerName);
		}
	}

	public void removePlayerSide2(final String playerName) {
		if (!side2Players.isEmpty()) {
			for (int i = 0; i < this.side2Players.size(); ++i) {
				if (playerName.equalsIgnoreCase(this.side2Players.get(i))) {
					this.side2Players.remove(i);
					return;
				}
			}
		}
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getSide1() {
		return this.side1;
	}

	public void setSide1(final String side1) {
		this.side1 = side1;
	}

	public String getSide2() {
		return this.side2;
	}

	public void setSide2(final String side2) {
		this.side2 = this.side1;
	}

	public ArrayList<String> getSide1Players() {
		return this.side1Players;
	}

	public void setSide1Players(final ArrayList<String> side1Players) {
		this.side1Players = side1Players;
	}

	public ArrayList<String> getSide2Players() {
		return this.side2Players;
	}

	public void setSide2Players(final ArrayList<String> side2Players) {
		this.side2Players = side2Players;
	}
}
