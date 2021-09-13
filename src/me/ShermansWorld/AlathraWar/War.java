package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;

public class War {
	
	private String name;
	private String side1;
	private String side2;
	
	private ArrayList<String> side1Players = new ArrayList<String>();
	private ArrayList<String> side2Players = new ArrayList<String>();
	
	public War (String name, String side1, String side2) {
		this.name = name;
		this.side1 = side1;
		this.side2 = side2;
	}
	
	public void addPlayerSide1(String playerName) {
		boolean duplicate = false;
		for (int i = 0; i < side1Players.size(); i++) {
			if (playerName.equalsIgnoreCase(side1Players.get(i))) {
				duplicate = true;
			}
		}
		if (!duplicate) {
			side1Players.add(playerName);
		}
	}
	
	public void removePlayerSide1(String playerName) {
		for (int i = 0; i < side1Players.size(); i++) {
			if (playerName.equalsIgnoreCase(side1Players.get(i))) {
				side1Players.remove(i);
				return;
			}
		}
	}
	
	public void addPlayerSide2(String playerName) {
		boolean duplicate = false;
		for (int i = 0; i < side2Players.size(); i++) {
			if (playerName.equalsIgnoreCase(side2Players.get(i))) {
				duplicate = true;
			}
		}
		if (!duplicate) {
			side2Players.add(playerName);
		}
	}
	
	public void removePlayerSide2(String playerName) {
		for (int i = 0; i < side2Players.size(); i++) {
			if (playerName.equalsIgnoreCase(side2Players.get(i))) {
				side2Players.remove(i);
				return;
			}
		}
	}
	
	
	// Getters and setters
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSide1() {
		return side1;
	}
	
	public void setSide1(String side1) {
		this.side1 = side1;
	}
	
	public String getSide2() {
		return side2;
	}
	
	public void setSide2(String side2) {
		this.side2 = side1;
	}
	
	public ArrayList<String> getSide1Players() {
		return side1Players;
	}
	
	public void setSide1Players(ArrayList<String> side1Players) {
		this.side1Players = side1Players;
	}
	
	public ArrayList<String> getSide2Players() {
		return side2Players;
	}
	
	public void setSide2Players(ArrayList<String> side2Players) {
		this.side2Players = side2Players;
	}
}
