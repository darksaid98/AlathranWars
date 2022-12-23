package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;

public class War {
	private String name;
	private String side1;
	private String side2;
	private ArrayList<String> side1Players;
	private ArrayList<String> side2Players;
	private ArrayList<String> side1Mercs;
	private ArrayList<String> side2Mercs;

	public War(final String name, final String side1, final String side2) {
		side1Players = new ArrayList<String>();
		side2Players = new ArrayList<String>();
		side1Mercs = new ArrayList<String>();
		side2Mercs = new ArrayList<String>();
		this.name = name;
		this.side1 = side1;
		this.side2 = side2;
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

	public void addMercSide1(final String playerName) {
		boolean duplicate = false;
		if (!side1Mercs.isEmpty()) {
			for (int i = 0; i < side1Mercs.size(); ++i) {
				if (playerName.equalsIgnoreCase(side1Mercs.get(i))) {
					duplicate = true;
				}
			}
		}
		if (!duplicate) {
			side1Mercs.add(playerName);
		}
	}

	public void removeMercSide1(final String playerName) {
		if (!side1Mercs.isEmpty()) {
			for (int i = 0; i < side1Mercs.size(); ++i) {
				if (playerName.equalsIgnoreCase(side1Mercs.get(i))) {
					side1Mercs.remove(i);
					return;
				}
			}
		}
	}

	public void addMercSide2(final String playerName) {
		boolean duplicate = false;
		if (!side2Mercs.isEmpty()) {
			for (int i = 0; i < side2Mercs.size(); ++i) {
				if (playerName.equalsIgnoreCase(side2Mercs.get(i))) {
					duplicate = true;
				}
			}
		}
		if (!duplicate) {
			side2Mercs.add(playerName);
		}
	}

	public void removeMercSide2(final String playerName) {
		if (!side2Mercs.isEmpty()) {
			for (int i = 0; i < side2Mercs.size(); ++i) {
				if (playerName.equalsIgnoreCase(side2Mercs.get(i))) {
					side2Mercs.remove(i);
					return;
				}
			}
		}
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

	public ArrayList<String> getSide1Mercs() {
		return this.side1Mercs;
	}

	public void setSide1Mercs(final ArrayList<String> side1Mercs) {
		this.side1Mercs = side1Mercs;
	}

	public ArrayList<String> getSide2Mercs() {
		return this.side2Mercs;
	}

	public void setSide2Mercs(final ArrayList<String> side2Mercs) {
		this.side2Mercs = side2Mercs;
	}
}
